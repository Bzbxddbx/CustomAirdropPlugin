package Bzbxddbx.customAirdropPlugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public record ConfigSettings(
        int searchRadius,
        long cooldownTicks,
        long despawnTicks,
        String eventStartMessage
) {

    public static ConfigSettings fromConfig(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        int searchRadius = config.getInt("settings.search-radius", 1000);
        long cooldownTicks = secondsToTicks(config.getLong("settings.cooldown-seconds", 60));
        long despawnTicks = minutesToTicks(config.getLong("settings.despawn-minutes", 10));
        String message = config.getString(
                "settings.messages.event-start",
                "<gold><b>[Аирдроп]</b> Мистический аирдроп начал падать! Координаты: X=<x>, Y=<y>, Z=<z></gold>"
        );
        return new ConfigSettings(
                Math.max(1, searchRadius),
                Math.max(0, cooldownTicks),
                Math.max(1, despawnTicks),
                message
        );
    }

    private static long secondsToTicks(long seconds) {
        return seconds * 20L;
    }

    private static long minutesToTicks(long minutes) {
        return minutes * 60L * 20L;
    }
}