package Bzbxddbx.customAirdropPlugin.config;

import net.kyori.adventure.text.Component;

public record ConfigSettings(
        long cooldownTicks,
        int searchRadius,
        int dropAltitude,
        Component eventStartMessage,
        Component airdropOpenedMessage
) {

    public static ConfigSettings defaults() {
        return new ConfigSettings(
                1200L,
                1000,
                100,
                Component.text("Мистический аирдроп начал падать!"),
                Component.text("Мистический аирдроп был открыт!")
        );
    }
}