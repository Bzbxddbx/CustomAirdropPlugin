package Bzbxddbx.customAirdropPlugin.core.loot;

import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public record LootItem(ItemStack itemStack, double chance, int minAmount, int maxAmount) {

    public LootItem {
        if (chance < 0.0 || chance > 1.0) {
            throw new IllegalArgumentException("chance must be in [0.0, 1.0]");
        }
        if (minAmount < 1 || maxAmount < minAmount) {
            throw new IllegalArgumentException("invalid amount range");
        }
    }

    public ItemStack generateStack() {
        int amount = ThreadLocalRandom.current().nextInt(this.minAmount, this.maxAmount + 1);
        ItemStack stack = this.itemStack.clone();
        stack.setAmount(amount);
        return stack;
    }
}