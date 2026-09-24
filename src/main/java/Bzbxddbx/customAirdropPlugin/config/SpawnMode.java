package Bzbxddbx.customAirdropPlugin.config;

/**
 * Режим появления сундука: мгновенно или через анимацию падения с неба.
 */
public enum SpawnMode {
    INSTANT,
    FALLING;

    public static SpawnMode parse(String value) {
        return value != null && value.trim().equalsIgnoreCase("instant") ? INSTANT : FALLING;
    }
}