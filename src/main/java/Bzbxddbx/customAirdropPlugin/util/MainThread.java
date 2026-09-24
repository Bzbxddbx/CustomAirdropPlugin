package Bzbxddbx.customAirdropPlugin.util;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Выполняет действие на главном потоке. На Paper команды и менеджер работают на
 * главном потоке, но переходы из async-chain делают проверку обязательной.
 */
public final class MainThread {

    private MainThread() {
    }

    public static void run(JavaPlugin plugin, Runnable action) {
        if (plugin.getServer().isPrimaryThread()) {
            action.run();
        } else {
            plugin.getServer().getGlobalRegionScheduler().run(plugin, task -> action.run());
        }
    }
}