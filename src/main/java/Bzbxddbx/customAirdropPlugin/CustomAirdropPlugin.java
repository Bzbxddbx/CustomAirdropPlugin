package Bzbxddbx.customAirdropPlugin;

import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.command.AirdropCommand;
import Bzbxddbx.customAirdropPlugin.command.AirdropTabCompleter;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import Bzbxddbx.customAirdropPlugin.config.LootConfig;
import Bzbxddbx.customAirdropPlugin.core.AsyncLocationSearcher;
import Bzbxddbx.customAirdropPlugin.listener.BlockBreakListener;
import Bzbxddbx.customAirdropPlugin.listener.PlayerInteractListener;
import Bzbxddbx.customAirdropPlugin.manager.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class CustomAirdropPlugin extends JavaPlugin {

    private AirdropManager airdropManager;
    private LootConfig lootConfig;

    @Override
    public void onEnable() {
        ConfigSettings settings = ConfigSettings.fromConfig(this);
        this.lootConfig = new LootConfig(this);
        this.lootConfig.load();
        this.airdropManager = new EventManager(
                this, new AsyncLocationSearcher(this, settings), this.lootConfig, settings);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(this.airdropManager), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this.airdropManager), this);
        AirdropCommand command = new AirdropCommand(this.airdropManager, this.lootConfig);
        Objects.requireNonNull(this.getCommand("airdropstart")).setExecutor(command);
        Objects.requireNonNull(this.getCommand("airdrop")).setExecutor(command);
        Objects.requireNonNull(this.getCommand("airdrop")).setTabCompleter(new AirdropTabCompleter());
    }

    @Override
    public void onDisable() {
        if (this.airdropManager != null) {
            this.airdropManager.stopEvent();
        }
    }

    public LootConfig getLootConfig() {
        return this.lootConfig;
    }
}