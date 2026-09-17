package Bzbxddbx.customAirdropPlugin;

import Bzbxddbx.customAirdropPlugin.manager.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomAirdropPlugin extends JavaPlugin {

    private EventManager eventManager;

    @Override
    public void onEnable() {
        this.eventManager = new EventManager();
    }

    @Override
    public void onDisable() {
        if (this.eventManager != null) {
            this.eventManager.stopEvent();
        }
    }
}