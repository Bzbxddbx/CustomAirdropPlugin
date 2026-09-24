package Bzbxddbx.customAirdropPlugin.manager;

/**
 * Поколение события аирдропа. Каждый запрос на запуск получает свой номер;
 * при остановке или новом запуске поколение инвалидируется, и устаревший
 * асинхронный поиск или анимация падения отбрасываются.
 */
public final class EventEpoch {

    private int value;

    public int next() {
        return ++this.value;
    }

    public void invalidate() {
        this.value++;
    }

    public boolean isCurrent(int generation) {
        return generation == this.value;
    }
}