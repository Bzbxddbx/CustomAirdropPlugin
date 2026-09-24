package Bzbxddbx.customAirdropPlugin.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Хранилище пользовательских текстов. Шаблоны задаются в {@code messages.yml} в
 * формате MiniMessage; в коде не остаётся ни одной литеральной строки для игрока.
 * Все ключи имеют дефолты на случай повреждённого или отсутствующего файла.
 */
public final class Messages {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final Map<String, String> templates;

    public Messages(Map<String, String> templates) {
        this.templates = Map.copyOf(templates);
    }

    public static Messages load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        return fromConfig(YamlConfiguration.loadConfiguration(file));
    }

    static Messages fromConfig(FileConfiguration config) {
        Map<String, String> templates = new HashMap<>(defaults());
        config.getValues(true).forEach((key, value) -> {
            if (value instanceof String text) {
                templates.put(key, text);
            }
        });
        return new Messages(templates);
    }

    public Component render(String key, TagResolver... resolvers) {
        String template = this.templates.getOrDefault(key,
                defaults().getOrDefault(key, "<gray>[unknown message: " + key + "]</gray>"));
        return MINI_MESSAGE.deserialize(template, resolvers);
    }

    private static Map<String, String> defaults() {
        Map<String, String> map = new HashMap<>();
        map.put("hologram.active",
                "<gold><b>[Мистический сундук]</b></gold>\n<gray>Кликни, чтобы открыть</gray>");
        map.put("hologram.opened", "<red><b>[ОТКРЫТ]</b></red>");
        map.put("chat.event-start",
                "<gold><b>[Аирдроп]</b> Мистический аирдроп начал падать! Координаты: X=<x>, Y=<y>, Z=<z></gold>");
        map.put("chat.event-stopped", "<yellow>[Аирдроп] Событие аирдропа завершено</yellow>");
        map.put("block.cannot-break", "<red>Вы не можете сломать мистический сундук!</red>");
        map.put("command.no-permission", "<red>У вас нет прав</red>");
        map.put("command.airdrop-started", "<green>[Аирдроп] Событие запущено!</green>");
        map.put("command.airdrop-cooldown", "<red>[Аирдроп] Подождите немного перед следующим запуском!</red>");
        map.put("command.airdrop-reloaded", "<green>[Аирдроп] Конфигурация лута успешно перезагружена!</green>");
        map.put("command.airdrop-usage", "<gray>Использование: /airdrop reload|stop</gray>");
        return Map.copyOf(map);
    }
}