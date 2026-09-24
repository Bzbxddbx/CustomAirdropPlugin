package Bzbxddbx.customAirdropPlugin.core.animation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FallPathTest {

    @Test
    void spawnsDistanceAboveTarget() {
        assertEquals(104, FallPath.spawnY(320, 64, 40));
    }

    @Test
    void spawnClampedToWorldCeiling() {
        assertEquals(312, FallPath.spawnY(320, 300, 40));
    }

    @Test
    void respectsMinimumGap() {
        assertEquals(69, FallPath.spawnY(320, 64, 5));
    }

    @Test
    void shortWorldKeepsGap() {
        assertEquals(60, FallPath.spawnY(65, 55, 10));
    }
}