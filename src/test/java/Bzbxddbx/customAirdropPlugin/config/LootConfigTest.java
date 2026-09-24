package Bzbxddbx.customAirdropPlugin.config;

import Bzbxddbx.customAirdropPlugin.config.LootConfig.ParsedLoot;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LootConfigTest {

    private static ConfigurationSection lootSection() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("loot.diamond.material", "DIAMOND");
        config.set("loot.diamond.chance", 0.8);
        config.set("loot.diamond.min-amount", 1);
        config.set("loot.diamond.max-amount", 4);
        config.set("loot.emerald.material", "EMERALD");
        return config.getConfigurationSection("loot");
    }

    @Test
    void parsesValidEntries() {
        List<ParsedLoot> entries = LootConfig.parseLoot(lootSection());
        assertEquals(2, entries.size());
        ParsedLoot diamond = entries.get(0);
        assertEquals("DIAMOND", diamond.material());
        assertEquals(0.8, diamond.chance());
        assertEquals(1, diamond.minAmount());
        assertEquals(4, diamond.maxAmount());
    }

    @Test
    void skipsBlankMaterial() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("loot.valid.material", "DIAMOND");
        config.set("loot.valid.chance", 0.5);
        config.set("loot.junk.material", "");
        List<ParsedLoot> entries = LootConfig.parseLoot(config.getConfigurationSection("loot"));
        assertEquals(1, entries.size());
        assertEquals("DIAMOND", entries.get(0).material());
    }

    @Test
    void nullOrDefaultsForMissingFields() {
        YamlConfiguration config = new YamlConfiguration();
        config.set("loot.sparse.material", "STONE");
        ParsedLoot stone = LootConfig.parseLoot(config.getConfigurationSection("loot")).get(0);
        assertEquals(0.0, stone.chance());
        assertEquals(1, stone.minAmount());
        assertEquals(1, stone.maxAmount());
    }

    @Test
    void nullWhenSectionEmptyOrMissing() {
        assertNull(LootConfig.parseLoot(null));
        assertNull(LootConfig.parseLoot(new YamlConfiguration().getConfigurationSection("missing")));
        YamlConfiguration config = new YamlConfiguration();
        config.set("loot.empty.material", "");
        assertNull(LootConfig.parseLoot(config.getConfigurationSection("loot")));
    }
}