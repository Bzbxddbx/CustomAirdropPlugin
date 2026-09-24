package Bzbxddbx.customAirdropPlugin.core.loot;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SlotPlacerTest {

    @Test
    void occupiesDistinctSlotsWithinBounds() {
        List<Integer> slots = SlotPlacer.occupy(27, 10, new Random(7), 5);
        assertEquals(5, slots.size());
        assertEquals(5, slots.stream().distinct().count());
        assertTrue(slots.stream().allMatch(slot -> slot >= 0 && slot < 27));
    }

    @Test
    void neverReturnsOutOfRangeSlot() {
        List<Integer> slots = SlotPlacer.occupy(9, 3, new Random(1), 100);
        assertTrue(slots.size() <= 9);
        assertTrue(slots.stream().allMatch(slot -> slot >= 0 && slot < 9));
    }

    @Test
    void exhaustedAttemptsProduceFewerSlots() {
        List<Integer> slots = SlotPlacer.occupy(9, 1, new Random(1), 100);
        assertTrue(slots.size() < 100);
    }
}