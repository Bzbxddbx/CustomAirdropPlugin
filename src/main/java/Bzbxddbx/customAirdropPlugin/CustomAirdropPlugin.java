package Bzbxddbx.customAirdropPlugin;

import Bzbxddbx.customAirdropPlugin.core.AsyncLocationSearcher;
import Bzbxddbx.customAirdropPlugin.listener.PlayerInteractListener;
import Bzbxddbx.customAirdropPlugin.manager.EventManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomAirdropPlugin extends JavaPlugin {

    private EventManager eventManager;

    @Override
    public void onEnable() {
        this.eventManager = new EventManager(this, new AsyncLocationSearcher());
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this.eventManager), this);
        Objects.requireNonNull(this.getCommand("airdropstart")).setExecutor((sender, command, label, args) -> {
            this.eventManager.startEvent();
            sender.sendMessage(Component.text("Аирдроп запущен!"));
            return true;
        });
    }

    @Override
    public void onDisable() {
        if (this.eventManager != null) {
            this.eventManager.stopEvent();
        }
    }
}