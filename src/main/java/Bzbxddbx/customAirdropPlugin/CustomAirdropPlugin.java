package Bzbxddbx.customAirdropPlugin;

import Bzbxddbx.customAirdropPlugin.config.LootConfig;
import Bzbxddbx.customAirdropPlugin.core.AsyncLocationSearcher;
import Bzbxddbx.customAirdropPlugin.listener.PlayerInteractListener;
import Bzbxddbx.customAirdropPlugin.manager.EventManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomAirdropPlugin extends JavaPlugin {

    private EventManager eventManager;
    private LootConfig lootConfig;

    @Override
    public void onEnable() {
        this.lootConfig = new LootConfig(this);
        this.lootConfig.load();
        this.eventManager = new EventManager(this, new AsyncLocationSearcher(), this.lootConfig);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this.eventManager), this);
        Objects.requireNonNull(this.getCommand("airdropstart")).setExecutor((sender, command, label, args) -> {
            this.eventManager.startEvent();
            sender.sendMessage(Component.text("Аирдроп запущен!"));
            return true;
        });
        Objects.requireNonNull(this.getCommand("airdrop")).setExecutor((sender, command, label, args) -> {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                this.lootConfig.load();
                sender.sendMessage(MiniMessage.miniMessage().deserialize(
                        "<green><b>[Аирдроп]</b> Конфигурация лута успешно перезагружена на лету!</green>"));
                return true;
            }
            sender.sendMessage(Component.text("Использование: /airdrop reload"));
            return true;
        });
    }

    @Override
    public void onDisable() {
        if (this.eventManager != null) {
            this.eventManager.stopEvent();
        }
    }

    public LootConfig getLootConfig() {
        return this.lootConfig;
    }
}