package Bzbxddbx.customAirdropPlugin.core.loot;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int target = random.nextInt(minSlots, maxSlots + 1);
        List<ItemStack> result = new ArrayList<>(target);
        if (this.items.isEmpty()) {
            return result;
        }
        for (int i = 0; i < target; i++) {
            result.add(this.items.get(random.nextInt(this.items.size())).generateStack());
        }
        return result;
    }
}