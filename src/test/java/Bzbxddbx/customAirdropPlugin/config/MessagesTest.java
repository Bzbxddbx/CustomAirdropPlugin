package Bzbxddbx.customAirdropPlugin.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesTest {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Test
    void rendersTemplateWithPlaceholders() {
        Messages messages = Messages.fromConfig(new YamlConfiguration());
        Component expected = MINI_MESSAGE.deserialize(
                "<gold><b>[Аирдроп]</b> Мистический аирдроп начал падать! Координаты: X=10, Y=20, Z=30</gold>");
        assertEquals(expected, messages.render("chat.event-start",
                Placeholder.unparsed("x", "10"),
                Placeholder.unparsed("y", "20"),
                Placeholder.unparsed("z", "30")));
    }

    @Test
    void fileOverridesDefault() {
        YamlConfiguration file = new YamlConfiguration();
        file.set("hologram.opened", "<red>opened!</red>");
        Messages messages = Messages.fromConfig(file);
        assertEquals(MINI_MESSAGE.deserialize("<red>opened!</red>"), messages.render("hologram.opened"));
    }

    @Test
    void missingKeyFallsBackToUnknownMessage() {
        Messages messages = Messages.fromConfig(new YamlConfiguration());
        assertEquals(MINI_MESSAGE.deserialize("<gray>[unknown message: nope]</gray>"), messages.render("nope"));
    }
}