package Bzbxddbx.customAirdropPlugin.listener;

import Bzbxddbx.customAirdropPlugin.core.animation.FallingBlockRegistry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * Перехватывает приземление падающих блоков анимации: блок не ставится физикой
 * как обычный блок (control переходит к аниматору), а обработка передаётся
 * соответствующему аниматору через {@link FallingBlockRegistry}.
 */
public final class FallingBlockListener implements Listener {

    private final FallingBlockRegistry registry;

    public FallingBlockListener(FallingBlockRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (this.registry.intercept(event.getEntity().getUniqueId(), event.getEntity().getLocation())) {
            event.setCancelled(true);
        }
    }
}