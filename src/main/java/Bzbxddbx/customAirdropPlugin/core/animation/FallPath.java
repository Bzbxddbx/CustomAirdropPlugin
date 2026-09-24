package Bzbxddbx.customAirdropPlugin.core.animation;

/**
 * Чистая математика анимации падения: расчёт стартовой высоты FallingBlock.
 * Без серверных зависимостей, полностью тестируется.
 */
public final class FallPath {

    private static final int MIN_GAP_ABOVE_GROUND = 5;
    private static final int SAFETY_CEILING = 8;

    private FallPath() {
    }

    /**
     * @param maxHeight    высота мира ({@link org.bukkit.World#getMaxHeight()})
     * @param targetY      блок приземления
     * @param fallDistance желаемое расстояние падения в блоках
     * @return Y стартовой позиции падающего блока
     */
    public static int spawnY(int maxHeight, int targetY, int fallDistance) {
        int min = targetY + MIN_GAP_ABOVE_GROUND;
        int desired = targetY + fallDistance;
        int cap = maxHeight - SAFETY_CEILING;
        return Math.max(min, Math.min(desired, cap));
    }
}