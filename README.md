# CustomAirdropPlugin

Плагин для Paper (1.20.4–1.21+) с системой кастомных аирдропов: случайная безопасная точка на карте, появление «Мистического сундука», голограмма-табличка над ним. Лут полностью настраивается через `loot.yml` и перезагружается на лету.

## Возможности

- **Как работает**: `/airdropstart` запускает событие — сервер асинхронно находит безопасную точку и спавнит сундук с голограммой. Игрок кликает ПКМ по сундуку в статусе `ACTIVE` — тот открывается с частицами и звуком, голограмма меняется на «ОТКРЫТ», а сундук заполняется лотом из конфига.
- **Динамический лут (`loot.yml`)**: предметы, число слотов наград (`min-slots`/`max-slots`) и диапазоны стаков задаются в YAML. Файл читается один раз при старте — без I/O лагов в момент открытия. `/airdrop reload` перечитывает его без перезапуска сервера.
- **Отказоустойчивость**: повреждённый/пустой файл даёт предупреждение в консоль и аварийный лут (1 алмаз); пустая секция `loot` — fallback DIAMOND/EMERALD.
- **LootProvider**: `ActiveAirdrop` зависит только от интерфейса поставщика лута (`api/loot`), а не от конфига или плагина.
- **Тексты** — только Adventure `Component`, для голограмм и сообщений команд — MiniMessage.
- **Голограмма** — нативный `TextDisplay` из Paper API, без сторонних библиотек.
- **Поиск локации** — строго асинхронный (`CompletableFuture.supplyAsync`), работа с миром только на main-thread.

## Стек

- Java 25 (record, pattern matching, `var`, `List.getFirst()`).
- Paper API 1.21+ (нативно `TextDisplay`, Adventure, MiniMessage).
- Maven (`mvn clean package` → `target/CustomAirdropPlugin.jar`).

## Архитектура

```
api/                    — контракты плагина
  Airdrop                     — интерфейс аирдропа (id, локация, состояние, spawn/open/remove)
  AirdropManager              — управление событием (start/stop/getActive)
  location/LocationSearcher   — поиск безопасной точки (World, radius)
  loot/LootProvider           — поставщик лута (provideLoot)
core/                   — доменная модель и реализации
  ActiveAirdrop               — реализация Airdrop: CHEST + TextDisplay, открытие, раскладка лута по слотам
  AsyncLocationSearcher       — реализация LocationSearcher (асинхронный поиск с 32 попытками)
  AirdropState                — enum: WAITING, SPAWNING, ACTIVE, OPENED
  loot/LootItem               — record: ItemStack, chance, minAmount, maxAmount
  loot/LootContainer          — контейнер предметов, generateRandomLoot(minSlots, maxSlots)
manager/                — EventManager: оркестрация события, DI через конструктор
listener/               — PlayerInteractListener: ПКМ-перехват по сундуку
config/                 — LootConfig (загрузчик loot.yml, implements LootProvider), ConfigSettings (бета-настройки)
command/                — заготовки (команды зарегистрированы инлайн в main)
util/                   — заготовки помощников
```

### Ключевые решения

- DI через конструкторы: `EventManager(plugin, locationSearcher, lootConfig)` и `ActiveAirdrop(location, lootProvider)` — зависимости на интерфейсы, без статики `ConfigSettings.defaults()` в клиентах.
- Лут читается строго при старте/перезагрузке (`LootConfig.load()`), кэшируется в `LootContainer`.
- Гарантированное наполнение: сундук получает ровно `min-slots`…`max-slots` стаков; `chance` в модели резервируется, выбор предметов равномерный.
- Статусная машина предотвращает повторное открытие: при `OPENED` клик отдаётся ванильному сундуку.
- Раскладка лута по 27 слотам без перезаписи (до 10 попыток поиска свободного слота на предмет).
- Развязка через интерфейсы — замена `EventManager`, источника лута или поиска локации не требует трогать слушатели.

## Команды

| Команда            | Описание                      |
|--------------------|-------------------------------|
| `/airdropstart`    | Запустить событие аирдропа    |
| `/airdrop reload`  | Перечитать `loot.yml` на лету |
| (`/airdrop`)       | Вывести usage                 |

## Конфигурация

`loot.yml` — лут и число слотов наград:

```yaml
settings:
  min-slots: 3
  max-slots: 6

loot:
  diamond:
    material: DIAMOND
    chance: 0.5
    min-amount: 1
    max-amount: 4
  emerald:
    material: EMERALD
    chance: 0.4
    min-amount: 1
    max-amount: 3
```

Остальные настройки (cooldown, радиус поиска, сообщения) пока захардкожены в `ConfigSettings` — бета-статус.

## Статус

Бета: лут полностью из `loot.yml` с перезагрузкой на лету; остальные настройки плагина — константы в `ConfigSettings`.