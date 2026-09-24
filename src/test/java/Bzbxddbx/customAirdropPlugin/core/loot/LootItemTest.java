package Bzbxddbx.customAirdropPlugin.core.loot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LootItemTest {

    @Test
    void acceptsValidBounds() {
        assertDoesNotThrow(() -> new LootItem(null, 0.0, 1, 1));
        assertDoesNotThrow(() -> new LootItem(null, 1.0, 3, 5));
    }

    @Test
    void rejectsChanceOutsideRange() {
        assertThrows(IllegalArgumentException.class, () -> new LootItem(null, -0.01, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new LootItem(null, 1.01, 1, 1));
    }

    @Test
    void rejectsInvalidAmountRange() {
        assertThrows(IllegalArgumentException.class, () -> new LootItem(null, 0.5, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new LootItem(null, 0.5, 5, 2));
    }
}