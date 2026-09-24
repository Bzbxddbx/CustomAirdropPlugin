package Bzbxddbx.customAirdropPlugin.config;

import Bzbxddbx.customAirdropPlugin.api.loot.LootProvider;
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

public final class LootConfig implements LootProvider, Reloadable {

    record ParsedLoot(String material, double chance, int minAmount, int maxAmount) {
    }

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
        List<ParsedLoot> parsed = parseLoot(config.getConfigurationSection("loot"));
        this.container = buildContainer(parsed);
        this.plugin.getLogger().info("Loot loaded: " + this.container.items().size() + " entries, slots "
                + this.minSlots + "-" + this.maxSlots + ".");
    }

    private LootContainer buildContainer(List<ParsedLoot> parsed) {
        if (parsed == null) {
            this.plugin.getLogger().warning("loot.yml: no valid loot entries found, using fallback loot.");
            return fallback();
        }
        List<LootItem> items = new ArrayList<>();
        for (ParsedLoot entry : parsed) {
            Material material = Material.matchMaterial(entry.material());
            if (material == null) {
                this.plugin.getLogger().warning("Unknown material in loot.yml: " + entry.material());
                continue;
            }
            items.add(new LootItem(new ItemStack(material, entry.minAmount()),
                    entry.chance(), entry.minAmount(), entry.maxAmount()));
        }
        if (items.isEmpty()) {
            this.plugin.getLogger().warning("loot.yml: no valid loot entries found, using fallback loot.");
            return fallback();
        }
        return new LootContainer(items);
    }

    private static LootContainer fallback() {
        return new LootContainer(List.of(
                new LootItem(new ItemStack(Material.DIAMOND), 1.0, 1, 4),
                new LootItem(new ItemStack(Material.EMERALD), 1.0, 1, 3)
        ));
    }

    static List<ParsedLoot> parseLoot(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        List<ParsedLoot> entries = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            String materialName = entry.getString("material", "");
            if (materialName.isBlank()) {
                continue;
            }
            double chance = entry.getDouble("chance", 0.0);
            int minAmount = entry.getInt("min-amount", 1);
            int maxAmount = entry.getInt("max-amount", minAmount);
            entries.add(new ParsedLoot(materialName, chance, minAmount, maxAmount));
        }
        return entries.isEmpty() ? null : entries;
    }

    @Override
    public void reload() {
        this.load();
    }

    @Override
    public List<ItemStack> provideLoot() {
        if (this.container == null || this.container.items().isEmpty()) {
            return List.of();
        }
        return this.container.generateRandomLoot(this.minSlots, this.maxSlots);
    }
}