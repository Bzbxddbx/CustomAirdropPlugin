package Bzbxddbx.customAirdropPlugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public record ConfigSettings(
        int searchRadius,
        long cooldownTicks,
        long despawnTicks,
        SpawnMode spawnMode,
        int fallDistance,
        long fallTimeoutTicks
) {

    public static ConfigSettings fromConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        return fromConfig(plugin.getConfig());
    }

    public static ConfigSettings fromConfig(FileConfiguration config) {
        int searchRadius = Math.max(1, config.getInt("settings.search-radius", 1000));
        long cooldownTicks = Math.max(0, secondsToTicks(config.getLong("settings.cooldown-seconds", 60)));
        long despawnTicks = Math.max(1, minutesToTicks(config.getLong("settings.despawn-minutes", 10)));
        SpawnMode spawnMode = SpawnMode.parse(config.getString("settings.spawn-mode"));
        int fallDistance = Math.max(5, config.getInt("settings.fall-distance", 40));
        long fallTimeoutTicks = Math.max(1, secondsToTicks(config.getLong("settings.fall-timeout-seconds", 30)));
        return new ConfigSettings(searchRadius, cooldownTicks, despawnTicks, spawnMode, fallDistance, fallTimeoutTicks);
    }

    private static long secondsToTicks(long seconds) {
        return seconds * 20L;
    }

    private static long minutesToTicks(long minutes) {
        return minutes * 60L * 20L;
    }
}