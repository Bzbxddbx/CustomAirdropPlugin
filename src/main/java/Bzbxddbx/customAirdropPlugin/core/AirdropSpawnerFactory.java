package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.loot.AirdropSpawner;
import Bzbxddbx.customAirdropPlugin.api.loot.LootProvider;
import Bzbxddbx.customAirdropPlugin.config.ConfigSettings;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.config.SpawnMode;
import Bzbxddbx.customAirdropPlugin.core.animation.FallingBlockRegistry;
import Bzbxddbx.customAirdropPlugin.core.fx.AirdropEffects;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Фабрика спавн-стратегий. Система открыта для расширения (OCP): новый режим
 * появления — это новая реализация {@link AirdropSpawner}, менеджер и слушатели
 * не изменяются.
 */
public final class AirdropSpawnerFactory {

    private final JavaPlugin plugin;
    private final GlobalRegionScheduler scheduler;
    private final LootProvider lootProvider;
    private final AirdropEffects effects;
    private final Messages messages;
    private final ConfigSettings settings;
    private final FallingBlockRegistry registry;

    public AirdropSpawnerFactory(JavaPlugin plugin, GlobalRegionScheduler scheduler, LootProvider lootProvider,
                                 AirdropEffects effects, Messages messages, ConfigSettings settings,
                                 FallingBlockRegistry registry) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.lootProvider = lootProvider;
        this.effects = effects;
        this.messages = messages;
        this.settings = settings;
        this.registry = registry;
    }

    public AirdropSpawner create() {
        return this.settings.spawnMode() == SpawnMode.FALLING
                ? new FallingAirdropSpawner(this.plugin, this.scheduler, this.registry, this.lootProvider,
                        this.effects, this.messages, this.settings)
                : new InstantAirdropSpawner(this.lootProvider, this.effects, this.messages);
    }
}