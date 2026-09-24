package Bzbxddbx.customAirdropPlugin.core.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Чистая раскладка наград по слотам инвентаря без перезаписи занятых слотов.
 * Не содержит серверных зависимостей и полностью тестируется без MockBukkit.
 */
public final class SlotPlacer {

    private SlotPlacer() {
    }

    /**
     * Возвращает слоты для {@code stackCount} предметов. На каждый предмет даётся
     * до {@code attempts} попыток найти свободный слот; если место не нашлось,
     * предмет пропускается.
     */
    public static List<Integer> occupy(int inventorySize, int attempts, Random random, int stackCount) {
        List<Integer> occupied = new ArrayList<>(Math.min(stackCount, inventorySize));
        for (int i = 0; i < stackCount; i++) {
            int slot = findFreeSlot(inventorySize, attempts, random, occupied);
            if (slot < 0) {
                break;
            }
            occupied.add(slot);
        }
        return occupied;
    }

    private static int findFreeSlot(int inventorySize, int attempts, Random random, List<Integer> occupied) {
        for (int attempt = 0; attempt < attempts; attempt++) {
            int slot = random.nextInt(inventorySize);
            if (!occupied.contains(slot)) {
                return slot;
            }
        }
        return -1;
    }
}