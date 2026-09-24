package Bzbxddbx.customAirdropPlugin.manager;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.api.AirdropState;
import Bzbxddbx.customAirdropPlugin.api.location.LocationSearcher;
import Bzbxddbx.customAirdropPlugin.api.loot.AirdropSpawner;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.core.ActiveAirdrop;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.concurrent.CancellationException;

/**
 * Оркестратор события: cooldown, защита от повторного запуска, асинхронный
 * поиск точки, активация аирдропа (мгновенно или после падения), автоудаление
 * неоткрытого сундука. Зависит только от интерфейсов и настроек (DIP).
 */
public final class EventManager implements AirdropManager {

    private static final long UNSTARTED = Long.MIN_VALUE;

    private final JavaPlugin plugin;
    private final GlobalRegionScheduler scheduler;
    private final LocationSearcher locationSearcher;
    private final AirdropSpawner spawner;
    private final Messages messages;
    private final ConfigSettings settings;
    private final EventEpoch epoch = new EventEpoch();

    private Airdrop activeAirdrop;
    private ScheduledTask despawnTask;
    private ScheduledTask hologramTask;
    private long lastStartTick = UNSTARTED;

    public EventManager(JavaPlugin plugin, GlobalRegionScheduler scheduler, LocationSearcher locationSearcher,
                        AirdropSpawner spawner, Messages messages, ConfigSettings settings) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.locationSearcher = locationSearcher;
        this.spawner = spawner;
        this.messages = messages;
        this.settings = settings;
    }

    @Override
    public boolean startEvent() {
        long now = this.plugin.getServer().getCurrentTick();
        if (this.lastStartTick != UNSTARTED && now - this.lastStartTick < this.settings.cooldownTicks()) {
            this.plugin.getLogger().info("Airdrop start skipped: cooldown is still active.");
            return false;
        }
        this.lastStartTick = now;
        if (this.activeAirdrop != null) {
            this.stopEvent();
        }
        int generation = this.epoch.next();
        World world = this.plugin.getServer().getWorlds().getFirst();
        this.locationSearcher.findSafeLocation(world).thenAccept(location ->
                this.scheduler.run(this.plugin, task -> this.handleLocation(generation, location)));
        return true;
    }

    private void handleLocation(int generation, Location location) {
        if (!this.epoch.isCurrent(generation) || !this.plugin.isEnabled()) {
            return;
        }
        this.spawner.spawn(location).whenComplete((airdrop, error) -> {
            if (error != null) {
                if (!(error instanceof CancellationException)) {
                    this.plugin.getLogger().severe("Airdrop spawn failed: " + error.getMessage());
                }
                return;
            }
            if (airdrop == null) {
                return;
            }
            this.scheduler.run(this.plugin, task -> this.activate(generation, airdrop));
        });
    }

    private void activate(int generation, Airdrop airdrop) {
        if (!this.epoch.isCurrent(generation) || !this.plugin.isEnabled()) {
            airdrop.remove();
            return;
        }
        this.activeAirdrop = airdrop;
        this.broadcastStart(airdrop.getLocation());
        this.scheduleDespawn();
        this.scheduleHologramRefresh();
    }

    private void broadcastStart(Location location) {
        this.plugin.getServer().broadcast(this.messages.render("chat.event-start",
                Placeholder.unparsed("x", String.valueOf(location.getBlockX())),
                Placeholder.unparsed("y", String.valueOf(location.getBlockY())),
                Placeholder.unparsed("z", String.valueOf(location.getBlockZ()))));
    }

    private void scheduleDespawn() {
        if (this.despawnTask != null) {
            this.despawnTask.cancel();
        }
        this.despawnTask = this.scheduler.runDelayed(this.plugin, task -> {
            if (this.activeAirdrop != null && this.activeAirdrop.getState() == AirdropState.ACTIVE) {
                this.stopEvent();
            }
        }, this.settings.despawnTicks());
    }

    private void scheduleHologramRefresh() {
        if (this.hologramTask != null) {
            this.hologramTask.cancel();
        }
        this.hologramTask = this.scheduler.runAtFixedRate(this.plugin, task -> {
            if (this.plugin.isEnabled() && this.activeAirdrop instanceof ActiveAirdrop active
                    && active.refreshHologramIfMissing()) {
                this.plugin.getLogger().warning("Airdrop hologram was missing, respawned.");
            }
        }, 20L, 20L);
    }

    @Override
    public void stopEvent() {
        this.epoch.invalidate();
        if (this.despawnTask != null) {
            this.despawnTask.cancel();
            this.despawnTask = null;
        }
        if (this.hologramTask != null) {
            this.hologramTask.cancel();
            this.hologramTask = null;
        }
        this.spawner.dispose();
        if (this.activeAirdrop != null) {
            this.activeAirdrop.remove();
            this.activeAirdrop = null;
            this.plugin.getServer().broadcast(this.messages.render("chat.event-stopped"));
        }
    }

    @Override
    public Optional<Airdrop> getActiveAirdrop() {
        return Optional.ofNullable(this.activeAirdrop);
    }
}