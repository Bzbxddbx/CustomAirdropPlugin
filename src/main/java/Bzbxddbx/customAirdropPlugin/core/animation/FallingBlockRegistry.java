package Bzbxddbx.customAirdropPlugin.core.animation;

import org.bukkit.Location;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реестр активных падающих блоков. Используется слушателем, чтобы перехватить
 * приземление FallingBlock и передать управление соответствующему аниматору.
 */
public final class FallingBlockRegistry {

    private final Map<UUID, FallingAirdropAnimator> active = new ConcurrentHashMap<>();

    public void register(FallingAirdropAnimator animator) {
        this.active.put(animator.getFallingBlockId(), animator);
    }

    public void unregister(FallingAirdropAnimator animator) {
        this.active.remove(animator.getFallingBlockId());
    }

    /**
     * @return {@code true}, если сущность принадлежала активному аниматору и
     * приземление было обработано им.
     */
    public boolean intercept(UUID entityId, Location landingLocation) {
        FallingAirdropAnimator animator = this.active.get(entityId);
        if (animator == null) {
            return false;
        }
        animator.land(landingLocation);
        return true;
    }
}