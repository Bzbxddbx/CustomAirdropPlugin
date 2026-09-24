package Bzbxddbx.customAirdropPlugin.core.loot;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class LootContainer {

    private final List<LootItem> items;

    public LootContainer(List<LootItem> items) {
        this.items = List.copyOf(items);
    }

    public List<LootItem> items() {
        return this.items;
    }

    public List<ItemStack> generateRandomLoot(int minSlots, int maxSlots) {
        return generateRandomLoot(minSlots, maxSlots, ThreadLocalRandom.current());
    }

    public List<ItemStack> generateRandomLoot(int minSlots, int maxSlots, Random random) {
        int target = random.nextInt(minSlots, maxSlots + 1);
        List<ItemStack> result = new ArrayList<>(target);
        if (this.items.isEmpty()) {
            return result;
        }
        for (int i = 0; i < target; i++) {
            result.add(pickWeighted(random).generateStack(random));
        }
        return result;
    }

    LootItem pickWeighted(Random random) {
        double total = 0.0;
        for (LootItem item : this.items) {
            total += item.chance();
        }
        if (total <= 0.0) {
            return this.items.get(random.nextInt(this.items.size()));
        }
        double cursor = random.nextDouble() * total;
        for (LootItem item : this.items) {
            cursor -= item.chance();
            if (cursor < 0.0) {
                return item;
            }
        }
        return this.items.get(this.items.size() - 1);
    }
}