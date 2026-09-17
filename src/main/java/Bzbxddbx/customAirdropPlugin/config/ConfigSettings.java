package Bzbxddbx.customAirdropPlugin.config;

import net.kyori.adventure.text.Component;

public record ConfigSettings(
        long cooldownTicks,
        int dropAltitude,
        Component eventStartMessage,
        Component airdropOpenedMessage
) {

    public static ConfigSettings defaults() {
        return new ConfigSettings(
                1200L,
                100,
                Component.text("The airdrop event has started"),
                Component.text("The airdrop has been opened")
        );
    }
}