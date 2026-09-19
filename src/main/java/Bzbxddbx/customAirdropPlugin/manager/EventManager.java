package Bzbxddbx.customAirdropPlugin.manager;

import Bzbxddbx.customAirdropPlugin.CustomAirdropPlugin;
import Bzbxddbx.customAirdropPlugin.api.Airdrop;
import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.api.location.LocationSearcher;
import Bzbxddbx.customAirdropPlugin.config.LootConfig;
import Bzbxddbx.customAirdropPlugin.core.ActiveAirdrop;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Optional;

public final class EventManager implements AirdropManager {

    private final CustomAirdropPlugin plugin;
    private final LocationSearcher locationSearcher;
    private final LootConfig lootConfig;
    private Airdrop activeAirdrop;

    public EventManager(CustomAirdropPlugin plugin, LocationSearcher locationSearcher, LootConfig lootConfig) {
        this.plugin = plugin;
        this.locationSearcher = locationSearcher;
        this.lootConfig = lootConfig;
    }

    @Override
    public void startEvent() {
        World world = Bukkit.getWorlds().getFirst();
        this.locationSearcher.findSafeLocation(world)
            .thenAccept(location -> Bukkit.getScheduler().runTask(this.plugin, () -> {
                var airdrop = new ActiveAirdrop(location,
                        this.lootConfig.getContainer(),
                        this.lootConfig.getMinSlots(),
                        this.lootConfig.getMaxSlots());
                this.activeAirdrop = airdrop;
                airdrop.spawn();
                Bukkit.getServer().broadcast(Component.text("Мистический аирдроп начал падать! Координаты: X="
                        + location.getBlockX() + " Y=" + location.getBlockY() + " Z=" + location.getBlockZ()));
            }));
    }

    @Override
    public void stopEvent() {
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