package Bzbxddbx.customAirdropPlugin.manager;

import Bzbxddbx.customAirdropPlugin.CustomAirdropPlugin;
import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.api.AirdropState;
import Bzbxddbx.customAirdropPlugin.api.location.LocationSearcher;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import Bzbxddbx.customAirdropPlugin.config.LootConfig;
import Bzbxddbx.customAirdropPlugin.core.ActiveAirdrop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;

public final class EventManager implements AirdropManager {

    private static final long UNSTARTED = Long.MIN_VALUE;

    private final CustomAirdropPlugin plugin;
    private final LocationSearcher locationSearcher;
    private final LootConfig lootConfig;
    private final ConfigSettings settings;
    private Airdrop activeAirdrop;
    private BukkitTask despawnTask;
    private long lastStartTick = UNSTARTED;

    public EventManager(CustomAirdropPlugin plugin, LocationSearcher locationSearcher,
                        LootConfig lootConfig, ConfigSettings settings) {
        this.plugin = plugin;
        this.locationSearcher = locationSearcher;
        this.lootConfig = lootConfig;
        this.settings = settings;
        this.plugin.getLogger().info("Event-start message: " + this.settings.eventStartMessage());
    }

    @Override
    public boolean startEvent() {
        long now = Bukkit.getCurrentTick();
        if (this.lastStartTick != UNSTARTED
                && now - this.lastStartTick < this.settings.cooldownTicks()) {
            this.plugin.getLogger().info("Airdrop start skipped: cooldown is still active.");
            return false;
        }
        this.lastStartTick = now;
        if (this.activeAirdrop != null) {
            this.stopEvent();
        }
        World world = Bukkit.getWorlds().getFirst();
        this.locationSearcher.findSafeLocation(world).thenAccept(location -> {
            if (!this.plugin.isEnabled()) {
                return;
            }
            Bukkit.getScheduler().runTask(this.plugin, () -> this.spawnAirdrop(location));
        });
        return true;
    }

    private void spawnAirdrop(Location location) {
        try {
            this.activeAirdrop = new ActiveAirdrop(location, this.lootConfig);
            this.activeAirdrop.spawn();
            Bukkit.getServer().broadcast(MiniMessage.miniMessage().deserialize(
                    this.settings.eventStartMessage(),
                    Placeholder.unparsed("x", String.valueOf(location.getBlockX())),
                    Placeholder.unparsed("y", String.valueOf(location.getBlockY())),
                    Placeholder.unparsed("z", String.valueOf(location.getBlockZ()))));
        } catch (Exception e) {
            this.plugin.getLogger().severe("Failed to spawn airdrop: " + e.getMessage());
        } finally {
            this.scheduleDespawn();
        }
    }

    private void scheduleDespawn() {
        if (this.despawnTask != null) {
            this.despawnTask.cancel();
        }
        this.despawnTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (this.activeAirdrop != null && this.activeAirdrop.getState() == AirdropState.ACTIVE) {
                this.stopEvent();
            }
        }, this.settings.despawnTicks());
    }

    @Override
    public void stopEvent() {
        if (this.despawnTask != null) {
            this.despawnTask.cancel();
            this.despawnTask = null;
        }
        if (this.activeAirdrop != null) {
            this.activeAirdrop.remove();
            this.activeAirdrop = null;
            Bukkit.getServer().broadcast(Component.text("Аирдроп был принудительно удален!"));
        }
    }

    @Override
    public Optional<Airdrop> getActiveAirdrop() {
        return Optional.ofNullable(this.activeAirdrop);
    }
}