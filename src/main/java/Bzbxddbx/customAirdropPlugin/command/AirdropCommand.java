package Bzbxddbx.customAirdropPlugin.command;

import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.config.LootConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public final class AirdropCommand implements CommandExecutor {

    private final AirdropManager airdropManager;
    private final LootConfig lootConfig;

    public AirdropCommand(AirdropManager airdropManager, LootConfig lootConfig) {
        this.airdropManager = airdropManager;
        this.lootConfig = lootConfig;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("airdropstart")) {
            if (this.airdropManager.startEvent()) {
                sender.sendMessage(Component.text("Аирдроп запущен!"));
            } else {
                sender.sendMessage(Component.text("Аирдроп пока недоступен, подождите немного!"));
            }
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            this.lootConfig.load();
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<green><b>[Аирдроп]</b> Конфигурация лута успешно перезагружена на лету!</green>"));
            return true;
        }
        sender.sendMessage(Component.text("Использование: /airdrop reload"));
        return true;
    }
}