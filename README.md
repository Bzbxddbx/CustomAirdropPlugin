# CustomAirdropPlugin

Плагин для Paper (1.21+) с системой кастомных аирдропов: случайная безопасная точка на карте, появление «Мистического сундука», голограмма-табличка над ним. Лут полностью настраивается через `loot.yml` и перезагружается на лету.

## Возможности

- **Как работает**: `/airdropstart` запускает событие — сервер асинхронно находит безопасную точку и спавнит сундук с голограммой. Игрок кликает ПКМ по сундуку в статусе `ACTIVE` — тот открывается с частицами и звуком, голограмма меняется на «ОТКРЫТ», а сундук заполняется лотом из конфига.
- **Тотальная защита сундука**: блок нельзя сломать руками, он не уничтожается взрывами TNT/криперов (блок просто вычёркивается из списка взрыва) и не двигается/не ломается поршнями — поршень целиком отменяется (`BlockBreakListener`).
- **Динамический лут (`loot.yml`)**: предметы, число слотов наград (`min-slots`/`max-slots`) и диапазоны стаков задаются в YAML. Файл читается один раз при старте — без I/O лагов в момент открытия. `/airdrop reload` перечитывает его без перезапуска сервера.
- **Отказоустойчивость**: повреждённый/пустой файл даёт предупреждение в консоль и аварийный лут (1 алмаз); пустая секция `loot` — fallback DIAMOND/EMERALD.
- **Настройки (`config.yml`)**: радиус поиска, cooldown запуска, автоудаление неоткрытого сундука, стартовое сообщение — всё из конфига, читается при старте.
- **LootProvider**: `ActiveAirdrop` зависит только от интерфейса поставщика лута (`api/loot`), а не от конфига или плагина.
- **Развязка**: слушатели и команды работают с `AirdropManager` (интерфейс), `api` не зависит от `core`.
- **Тексты** — только Adventure `Component`, для голограмм и сообщений — MiniMessage.
- **Голограмма** — нативный `TextDisplay` из Paper API, без сторонних библиотек.
- **Поиск локации** — координаты генерируются асинхронно (`CompletableFuture.supplyAsync`), чтение блоков (`getHighestBlockAt`) и спавн — строго на main-thread.

## Стек

- Java 25 (record, pattern matching, `var`, `List.getFirst()`).
- Paper API 1.21+ (нативно `TextDisplay`, Adventure, MiniMessage).
- Maven (`mvn clean package` → `target/CustomAirdropPlugin.jar`). Версия `paper-api` ограничена диапазоном `[26.3.build,27)` — без сюрпризов при мажорных обновлениях.

## Архитектура

```
api/                    — контракты плагина
  AirdropState                — enum: WAITING, SPAWNING, ACTIVE, OPENED
  Airdrop                     — интерфейс аирдропа (id, локация, состояние, spawn/open/remove)
  AirdropManager              — управление событием (start/stop/getActive)
  location/LocationSearcher   — поиск безопасной точки (World, CompletableFuture)
  loot/LootProvider           — поставщик лута (provideLoot)
core/                   — доменная модель и реализации
  ActiveAirdrop               — реализация Airdrop: CHEST + TextDisplay, открытие, раскладка лута по слотам
  AsyncLocationSearcher       — реализация LocationSearcher: кандидаты в supplyAsync, getHighestBlockAt на main-thread
  loot/LootItem               — record: ItemStack, chance, minAmount, maxAmount
  loot/LootContainer          — контейнер предметов, generateRandomLoot(minSlots, maxSlots)
manager/                — EventManager: оркестрация события (guard, cooldown, auto-despawn), DI через конструктор
listener/               — PlayerInteractListener (ПКМ по сундуку), BlockBreakListener (защита от ломания/взрывов/поршней)
command/                — AirdropCommand (/airdropstart, /airdrop reload), AirdropTabCompleter (reload)
config/                 — LootConfig (загрузчик loot.yml, implements LootProvider), ConfigSettings (config.yml)
util/                   — LocationUtil (isSameBlock — сравнение блоков)
```

### Ключевые решения

- DI через конструкторы: `EventManager(plugin, locationSearcher, lootConfig, settings)`, `AsyncLocationSearcher(plugin, settings)`, `ActiveAirdrop(location, lootProvider)` — зависимости на интерфейсы, статика `defaults()` в клиентах не используется.
- Лут читается строго при старте/перезагрузке (`LootConfig.load()`), кэшируется в `LootContainer`.
- Гарантированное наполнение: сундук получает ровно `min-slots`…`max-slots` стаков; `chance` в модели резервируется, выбор предметов равномерный.
- Статусная машина предотвращает повторное открытие: при `OPENED` клик отдаётся ванильному сундуку.
- Раскладка лута по 27 слотам без перезаписи (до 10 попыток поиска свободного слота на предмет).
- `EventManager`: повторный `/airdropstart` сначала останавливает старый аирдроп; cooldown блокирует запуск чаще, чем `cooldown-seconds`; неоткрытый сундук автоудаляется через `despawn-minutes`.
- Развязка через интерфейсы — замена `EventManager`, источника лута или поиска локации не требует трогать слушатели и команды.

## Команды

| Команда            | Описание                      |
|--------------------|-------------------------------|
| `/airdropstart`    | Запустить событие аирдропа    |
| `/airdrop reload`  | Перечитать `loot.yml` на лету |
| (`/airdrop`)       | Вывести usage                 |

## Конфигурация

`config.yml` — общие настройки:

```yaml
settings:
  search-radius: 1000        # радиус поиска от спавна мира
  cooldown-seconds: 60       # пауза между запусками
  despawn-minutes: 10        # автоудаление неоткрытого сундука
  messages:
    event-start: "<gold><b>[Аирдроп]</b> Мистический аирдроп начал падать! Координаты: X=<x>, Y=<y>, Z=<z></gold>"
```

В сообщении `event-start` доступны плейсхолдеры координат: `<x>`, `<y>`, `<z>`.

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

## Статус

Рабочая версия: лут и настройки из конфигов, защита сундука, диспетчер событий с cooldown и auto-despawn, развязка на интерфейсах. Из дорожной карты нереализованным осталась анимация «падения» сундука с высоты.