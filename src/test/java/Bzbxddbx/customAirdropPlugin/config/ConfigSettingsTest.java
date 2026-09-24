package Bzbxddbx.customAirdropPlugin.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigSettingsTest {

    @Test
    void defaultsUsedWhenConfigEmpty() {
        ConfigSettings settings = ConfigSettings.fromConfig(new YamlConfiguration());
        assertEquals(1000, settings.searchRadius());
        assertEquals(60L * 20L, settings.cooldownTicks());
        assertEquals(10L * 60L * 20L, settings.despawnTicks());
        assertEquals(SpawnMode.FALLING, settings.spawnMode());
        assertEquals(40, settings.fallDistance());
        assertEquals(30L * 20L, settings.fallTimeoutTicks());
    }

    @Test
    void explicitValuesAreApplied() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("settings.search-radius", 500);
        config.set("settings.cooldown-seconds", 30);
        config.set("settings.despawn-minutes", 5);
        config.set("settings.spawn-mode", "instant");
        config.set("settings.fall-distance", 20);
        config.set("settings.fall-timeout-seconds", 15);
        ConfigSettings settings = ConfigSettings.fromConfig(config);
        assertEquals(500, settings.searchRadius());
        assertEquals(30L * 20L, settings.cooldownTicks());
        assertEquals(5L * 60L * 20L, settings.despawnTicks());
        assertEquals(SpawnMode.INSTANT, settings.spawnMode());
        assertEquals(20, settings.fallDistance());
        assertEquals(15L * 20L, settings.fallTimeoutTicks());
    }

    @Test
    void invalidRadiusClampedToMinimum() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("settings.search-radius", -10);
        assertEquals(1, ConfigSettings.fromConfig(config).searchRadius());
    }

    @Test
    void fallSettingsClamped() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("settings.fall-distance", 1);
        config.set("settings.fall-timeout-seconds", -5);
        ConfigSettings settings = ConfigSettings.fromConfig(config);
        assertEquals(5, settings.fallDistance());
        assertEquals(1L, settings.fallTimeoutTicks());
    }
}