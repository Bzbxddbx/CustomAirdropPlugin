package Bzbxddbx.customAirdropPlugin.command;

import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.util.MainThread;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * {@code /airdropstart} — запуск события аирдропа (право {@code airdrop.command.start}).
 */
public final class StartAirdropCommand implements BasicCommand {

    public static final String PERMISSION = "airdrop.command.start";

    private final JavaPlugin plugin;
    private final AirdropManager airdropManager;
    private final Messages messages;

    public StartAirdropCommand(JavaPlugin plugin, AirdropManager airdropManager, Messages messages) {
        this.plugin = plugin;
        this.airdropManager = airdropManager;
        this.messages = messages;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        CommandSender sender = stack.getSender();
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(this.messages.render("command.no-permission"));
            return;
        }
        MainThread.run(this.plugin, () -> {
            if (this.airdropManager.startEvent()) {
                sender.sendMessage(this.messages.render("command.airdrop-started"));
            } else {
                sender.sendMessage(this.messages.render("command.airdrop-cooldown"));
            }
        });
    }
}