package Bzbxddbx.customAirdropPlugin;

import Bzbxddbx.customAirdropPlugin.command.AirdropCommand;
import Bzbxddbx.customAirdropPlugin.command.StartAirdropCommand;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import Bzbxddbx.customAirdropPlugin.config.LootConfig;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.core.AsyncLocationSearcher;
import Bzbxddbx.customAirdropPlugin.core.AirdropSpawnerFactory;
import Bzbxddbx.customAirdropPlugin.core.animation.FallingBlockRegistry;
import Bzbxddbx.customAirdropPlugin.core.fx.AirdropEffects;
import Bzbxddbx.customAirdropPlugin.listener.BlockBreakListener;
import Bzbxddbx.customAirdropPlugin.listener.FallingBlockListener;
import Bzbxddbx.customAirdropPlugin.listener.PlayerInteractListener;
import Bzbxddbx.customAirdropPlugin.manager.EventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Composition root: собирает граф зависимостей, регистрирует слушателей и
 * Brigadier-команды. Не содержит бизнес-логики.
 */
public final class CustomAirdropPlugin extends JavaPlugin {

    private EventManager eventManager;

    @Override
    public void onEnable() {
        ConfigSettings settings = ConfigSettings.fromConfig(this);
        Messages messages = Messages.load(this);
        LootConfig lootConfig = new LootConfig(this);
        lootConfig.load();

        AirdropEffects effects = new AirdropEffects();
        FallingBlockRegistry fallingBlockRegistry = new FallingBlockRegistry();
        this.eventManager = new EventManager(
                this,
                this.getServer().getGlobalRegionScheduler(),
                new AsyncLocationSearcher(this, settings),
                new AirdropSpawnerFactory(this, this.getServer().getGlobalRegionScheduler(), lootConfig, effects,
                        messages, settings, fallingBlockRegistry).create(),
                messages,
                settings);

        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(this.eventManager), this);
        this.getServer().getPluginManager().registerEvents(new BlockBreakListener(this.eventManager, messages), this);
        this.getServer().getPluginManager().registerEvents(new FallingBlockListener(fallingBlockRegistry), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            event.registrar().register("airdropstart", new StartAirdropCommand(this, this.eventManager, messages));
            event.registrar().register(new AirdropCommand(this, lootConfig, this.eventManager, messages).build());
        });
    }

    @Override
    public void onDisable() {
        if (this.eventManager != null) {
            this.eventManager.stopEvent();
        }
    }
}