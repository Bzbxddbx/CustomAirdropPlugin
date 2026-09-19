package Bzbxddbx.customAirdropPlugin.config;

import Bzbxddbx.customAirdropPlugin.core.loot.LootContainer;
import Bzbxddbx.customAirdropPlugin.core.loot.LootItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class LootConfig {

    private static final List<LootItem> FALLBACK = List.of(
            new LootItem(new ItemStack(Material.DIAMOND), 1.0, 1, 4),
            new LootItem(new ItemStack(Material.EMERALD), 1.0, 1, 3)
    );

    private final JavaPlugin plugin;
    private LootContainer container;
    private int minSlots;
    private int maxSlots;

    public LootConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(this.plugin.getDataFolder(), "loot.yml");
        if (!file.exists()) {
            this.plugin.saveResource("loot.yml", false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        this.minSlots = config.getInt("settings.min-slots", 3);
        this.maxSlots = config.getInt("settings.max-slots", 6);
        if (this.maxSlots < this.minSlots) {
            this.maxSlots = this.minSlots;
        }
        this.container = this.parseLoot(config.getConfigurationSection("loot"));
        this.plugin.getLogger().info("Loot loaded: " + this.container.items().size() + " entries, slots "
                + this.minSlots + "-" + this.maxSlots + ".");
    }

    private LootContainer parseLoot(ConfigurationSection section) {
        List<LootItem> items = new ArrayList<>();
        if (section != null) {
            for (String key : section.getKeys(false)) {
                ConfigurationSection entry = section.getConfigurationSection(key);
                if (entry == null) {
                    continue;
                }
                String materialName = entry.getString("material", "");
                Material material = Material.matchMaterial(materialName);
                if (material == null) {
                    this.plugin.getLogger().warning("Unknown material in loot.yml: " + materialName);
                    continue;
                }
                double chance = entry.getDouble("chance", 0.0);
                int minAmount = entry.getInt("min-amount", 1);
                int maxAmount = entry.getInt("max-amount", minAmount);
                ItemStack stack = new ItemStack(material, minAmount);
                items.add(new LootItem(stack, chance, minAmount, maxAmount));
            }
        }
        if (items.isEmpty()) {
            this.plugin.getLogger().warning("loot.yml: no valid loot entries found, using fallback loot.");
            return new LootContainer(FALLBACK);
        }
        return new LootContainer(items);
    }

    public LootContainer getContainer() {
        return this.container;
    }

    public int getMinSlots() {
        return this.minSlots;
    }

    public int getMaxSlots() {
        return this.maxSlots;
    }
}