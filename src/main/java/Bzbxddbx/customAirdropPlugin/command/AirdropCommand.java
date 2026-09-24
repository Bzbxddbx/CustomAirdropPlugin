package Bzbxddbx.customAirdropPlugin.command;

import Bzbxddbx.customAirdropPlugin.api.AirdropManager;
import Bzbxddbx.customAirdropPlugin.config.Messages;
import Bzbxddbx.customAirdropPlugin.config.Reloadable;
import Bzbxddbx.customAirdropPlugin.util.MainThread;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * {@code /airdrop reload} — перезагрузка лута на лету ({@code airdrop.command.reload}),
 * {@code /airdrop stop} — остановка активного события ({@code airdrop.command.stop}).
 * Строится нативным Brigadier-деревом Paper.
 */
public final class AirdropCommand {

    public static final String RELOAD_PERMISSION = "airdrop.command.reload";
    public static final String STOP_PERMISSION = "airdrop.command.stop";

    private final JavaPlugin plugin;
    private final Reloadable reloadable;
    private final AirdropManager airdropManager;
    private final Messages messages;

    public AirdropCommand(JavaPlugin plugin, Reloadable reloadable, AirdropManager airdropManager,
                          Messages messages) {
        this.plugin = plugin;
        this.reloadable = reloadable;
        this.airdropManager = airdropManager;
        this.messages = messages;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal("airdrop")
                .then(Commands.literal("reload").executes(context -> this.reload(context.getSource())))
                .then(Commands.literal("stop").executes(context -> this.stop(context.getSource())))
                .executes(context -> this.usage(context.getSource()))
                .build();
    }

    private int reload(CommandSourceStack stack) {
        CommandSender sender = stack.getSender();
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(this.messages.render("command.no-permission"));
            return 0;
        }
        MainThread.run(this.plugin, () -> {
            this.reloadable.reload();
            sender.sendMessage(this.messages.render("command.airdrop-reloaded"));
        });
        return 1;
    }

    private int stop(CommandSourceStack stack) {
        CommandSender sender = stack.getSender();
        if (!sender.hasPermission(STOP_PERMISSION)) {
            sender.sendMessage(this.messages.render("command.no-permission"));
            return 0;
        }
        MainThread.run(this.plugin, () -> this.airdropManager.stopEvent());
        return 1;
    }

    private int usage(CommandSourceStack stack) {
        stack.getSender().sendMessage(this.messages.render("command.airdrop-usage"));
        return 1;
    }
}