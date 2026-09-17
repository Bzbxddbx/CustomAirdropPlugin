package Bzbxddbx.customAirdropPlugin.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;

public record ConfigSettings(
        long cooldownTicks,
        int searchRadius,
        int dropAltitude,
        List<Material> loot,
        Component eventStartMessage,
        Component airdropOpenedMessage
) {

    public static ConfigSettings defaults() {
        return new ConfigSettings(
                1200L,
                1000,
                100,
                List.of(
                        Material.DIAMOND,
                        Material.NETHERITE_INGOT,
                        Material.GOLDEN_APPLE,
                        Material.EMERALD,
                        Material.TOTEM_OF_UNDYING
                ),
                Component.text("Мистический аирдроп начал падать!"),
                Component.text("Мистический аирдроп был открыт!")
        );
    }
}