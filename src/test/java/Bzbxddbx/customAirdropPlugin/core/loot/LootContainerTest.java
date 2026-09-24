package Bzbxddbx.customAirdropPlugin.core.loot;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootContainerTest {

    private static LootItem item(double chance) {
        return new LootItem(null, chance, 1, 1);
    }

    @Test
    void weightedSelectionFollowsChanceRatio() {
        LootContainer container = new LootContainer(List.of(
                item(0.75),
                item(0.25),
                item(0.0)
        ));
        int first = 0;
        int second = 0;
        Random random = new Random(42);
        int total = 2000;
        for (int i = 0; i < total; i++) {
            double chance = container.pickWeighted(random).chance();
            if (chance == 0.75) {
                first++;
            } else if (chance == 0.25) {
                second++;
            }
        }
        assertEquals(total, first + second, "chance 0.0 must never be picked");
        assertTrue(first > second, "higher weight must be picked more often");
        assertTrue(first > total * 0.6);
        assertTrue(first < total * 0.9);
    }

    @Test
    void zeroTotalWeightFallsBackToUniform() {
        LootContainer container = new LootContainer(List.of(
                new LootItem(null, 0.0, 1, 1),
                new LootItem(null, 0.0, 2, 2),
                new LootItem(null, 0.0, 3, 3)
        ));
        Random random = new Random(7);
        boolean sawFirst = false;
        boolean sawSecond = false;
        boolean sawThird = false;
        for (int i = 0; i < 2000; i++) {
            int minAmount = container.pickWeighted(random).minAmount();
            sawFirst |= minAmount == 1;
            sawSecond |= minAmount == 2;
            sawThird |= minAmount == 3;
        }
        assertTrue(sawFirst && sawSecond && sawThird);
    }

    @Test
    void emptyContainerProducesNoLoot() {
        LootContainer container = new LootContainer(List.of());
        assertTrue(container.generateRandomLoot(1, 5, new Random(0)).isEmpty());
    }
}