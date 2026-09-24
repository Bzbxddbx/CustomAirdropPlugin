package Bzbxddbx.customAirdropPlugin.config;

/**
 * Контракт для источников, которые можно перезагрузить на лету (без рестарта
 * сервера). Позволяет команде {@code reload} не зависеть от конкретного конфига.
 */
public interface Reloadable {

    void reload();
}