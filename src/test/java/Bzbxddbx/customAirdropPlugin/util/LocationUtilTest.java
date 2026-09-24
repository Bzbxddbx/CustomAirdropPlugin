package Bzbxddbx.customAirdropPlugin.util;

import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocationUtilTest {

    @Test
    void sameBlockInSameWorld() {
        Location first = new Location(null, 10.0, 64.0, 20.0);
        Location second = new Location(null, 10.9, 64.4, 20.7);
        assertTrue(LocationUtil.isSameBlock(first, second));
    }

    @Test
    void differentCoordinatesAreNotSameBlock() {
        Location first = new Location(null, 10.0, 64.0, 20.0);
        Location second = new Location(null, 11.0, 64.0, 20.0);
        assertFalse(LocationUtil.isSameBlock(first, second));
    }

    @Test
    void floorOfFractionalCoordinatesMatters() {
        Location first = new Location(null, 0.0, 0.0, 0.0);
        Location second = new Location(null, 0.5, 0.0, 0.0);
        assertTrue(LocationUtil.isSameBlock(first, second));
        Location third = new Location(null, 1.0, 0.0, 0.0);
        assertFalse(LocationUtil.isSameBlock(first, third));
    }

    @Test
    void nullArgumentsAreNotSameBlock() {
        assertFalse(LocationUtil.isSameBlock(null, new Location(null, 0, 0, 0)));
        assertFalse(LocationUtil.isSameBlock(new Location(null, 0, 0, 0), null));
        assertFalse(LocationUtil.isSameBlock(null, null));
    }
}