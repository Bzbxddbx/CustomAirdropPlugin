package Bzbxddbx.customAirdropPlugin.core;

import Bzbxddbx.customAirdropPlugin.api.AirdropState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActiveAirdropTest {

    @Test
    void activeStateUsesActiveHologramKey() {
        assertEquals("hologram.active", ActiveAirdrop.hologramKeyFor(AirdropState.ACTIVE));
    }

    @Test
    void openedStateUsesOpenedHologramKey() {
        assertEquals("hologram.opened", ActiveAirdrop.hologramKeyFor(AirdropState.OPENED));
    }

    @Test
    void nonOpenedStatesUseActiveHologramKey() {
        assertEquals("hologram.active", ActiveAirdrop.hologramKeyFor(AirdropState.WAITING));
        assertEquals("hologram.active", ActiveAirdrop.hologramKeyFor(AirdropState.SPAWNING));
    }
}