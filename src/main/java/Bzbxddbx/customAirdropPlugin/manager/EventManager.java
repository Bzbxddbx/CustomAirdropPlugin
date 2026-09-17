package Bzbxddbx.customAirdropPlugin.manager;

import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.api.location.LocationSearcher;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import Bzbxddbx.customAirdropPlugin.core.ActiveAirdrop;
import Bzbxddbx.customAirdropPlugin.core.AirdropState;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

public final class EventManager implements AirdropManager, LocationSearcher {

    private final JavaPlugin plugin;
    private final ConfigSettings config = ConfigSettings.defaults();
    private Airdrop activeAirdrop;

    public EventManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void startEvent() {
        World world = Bukkit.getWorlds().getFirst();
        this.findSafeLocation(world)
            .thenAcceptAsync(location -> {
                var airdrop = new ActiveAirdrop(UUID.randomUUID(), location, AirdropState.ACTIVE);
                this.activeAirdrop = airdrop;
                airdrop.spawn();
                Bukkit.getServer().broadcast(this.config.eventStartMessage());
                Bukkit.getServer().broadcast(Component.text("Координаты аирдропа: X="
                        + location.getBlockX() + " Y=" + location.getBlockY() + " Z=" + location.getBlockZ()));
            }, Bukkit.getScheduler().getMainThreadExecutor(this.plugin));
    }

    @Override
    public void stopEvent() {
        if (this.activeAirdrop != null) {
            this.activeAirdrop.remove();
            this.activeAirdrop = null;
        }
    }

    @Override
    public Optional<Airdrop> getActiveAirdrop() {
        return Optional.ofNullable(this.activeAirdrop);
    }
}