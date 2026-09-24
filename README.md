# CustomAirdropPlugin

Плагин для Paper 26.2 с системой кастомных аирдропов: случайная безопасная точка на карте, сундук **падает с неба** (нативный `FallingBlock` + голограмма-`TextDisplay` + след частиц), открытие по ПКМ с лутом, тотальная защита сундука и **встроенная SQLite-база статистики открытий** (личный счёт + топ игроков). Только современный Paper API: команды на **Brigadier**, планирование через **Paper Scheduler**, тексты — **Adventure/MiniMessage**.

## Возможности

- **Как работает**: `/airdropstart` запускает событие — сервер асинхронно находит безопасную точку и в режиме `spawn-mode: falling` запускает анимацию: сундук (FallingBlock с blockdata `CHEST`) падает с неба вместе с голограммой и следом частиц. При приземлении ставится настоящий сундук, голограмма переключается в «активную», игрок кликает ПКМ — сундук открывается со звуком и частицами, голограмма меняется на «ОТКРЫТ», внутрь раскладывается лут из конфига.
- **Тотальная защита сундука**: блок нельзя сломать руками, он вычёркивается из списка взрывов TNT/криперов и не двигается/не ломается поршнями (`BlockBreakListener`).
- **Два режима появления** (`spawn-mode`): `falling` — падение с неба; `instant` — сундук сразу в точке. Это стратегии за интерфейсом `AirdropSpawner` — новые режимы добавляются без изменения менеджера и слушателей (OCP).
- **Динамический лут (`loot.yml`)**: предметы, число слотов наград (`min-slots`/`max-slots`) и диапазоны стаков задаются в YAML. `chance` — относительный вес: предмет с `chance: 0.9` в 9 раз вероятнее, чем с `chance: 0.1`. Файл читается при старте и перечитывается `/airdrop reload` на лету (`LootConfig implements Reloadable`).
- **Тексты — `messages.yml`**: все строки для игрока — MiniMessage-шаблоны из одного файла, дефолты встроены в код как защита от повреждённого конфига. В коде нет ни одной литеральной строки для игрока.
- **Без гонок**: `EventEpoch` — поколение запроса; устаревший асинхронный поиск или незавершённая анимация падения отбрасываются при `stop` / повторном `start`.
- **Настройки (`config.yml`)**: радиус поиска, cooldown, автоудаление неоткрытого сундука, режим и параметры падения.
- **Развязка**: слушатели и команды работают только с интерфейсами из `api`; `api` не зависит от `core`.
- **Самовосстанавливающаяся голограмма**: если entity-вывеска (`TextDisplay`) удалена извне — выгрузка чанка, сторонний плагин — плагин пересоздаёт её на месте сундука под его состояние (ACTIVE → «Кликни, чтобы открыть», OPENED → «[ОТКРЫТ]»), а в консоль пишет `WARNING`.
- **Статистика игроков (SQLite)**: каждый факт открытия аирдропа пишется в локальную базу (`database.db`). `/airdrop me` — личный счёт (доступен всем), `/airdrop top [n]` — лидерборд по открытым аирдропам (доступен операторам). Подробнее — в разделе [«Статистика игроков (SQLite)»](#статистика-игроков-sqlite).

## Статистика игроков (SQLite)

Каждое открытие аирдропа фиксируется во встроенной SQLite-базе `database.db` (создаётся автоматически в папке плагина) — таблица `airdrop_stats`:

| Поле             | Назначение                              |
|------------------|------------------------------------------|
| `uuid`           | идентификатор игрока (primary key)       |
| `name`           | последний известный ник                  |
| `opened`         | сколько аирдропов открыл игрок           |
| `last_opened_at` | время последнего открытия (для сортировки топа) |

Команды:

- `/airdrop me` — личный счёт игрока (право `airdrop.command.me`, доступна всем игрокам). Если открытий ещё нет — сообщение из `messages.yml`.
- `/airdrop top [n]` — лидерборд по открытым аирдропам (право `airdrop.command.top`, операторы). Сортировка по убыванию счёта, при равенстве выше тот, кто открыл раньше. `n` по умолчанию — `stats.top-default` (10), не больше `stats.top-max` (20).

Запись — UPSERT `ON CONFLICT(uuid)`: повторные открытия аккумулируются в `opened`, ник игрока обновляется. При сбое открытия БД плагин логирует `severe` и продолжает работать без статистики.

## Стек

- Java 25 (record, pattern matching, `var`, `List.getFirst()`).
- Paper API 26.2 (пин `26.2.build.129-stable` в pom; версия `paper-api` — provided).
- **Brigadier**: `BasicCommand` + нативное `LiteralCommandNode`-дерево через `LifecycleEvents.COMMANDS` — никаких `CommandExecutor`/`TabCompleter`.
- **Paper Scheduler**: `GlobalRegionScheduler` (`run` / `runDelayed` / `runAtFixedRate`) — никакого `Bukkit.getScheduler().runTask(...)`.
- Adventure `Component` + `MiniMessage` (голограммы, чат, плейсхолдеры координат `<x>`, `<y>`, `<z>`).
- Maven (`mvn clean package` → `target/CustomAirdropPlugin.jar`). `mvn verify` — сборка + unit-тесты (42 теста, без MockBukkit).
- **Встроенный SQLite**: `sqlite-jdbc` упаковывается в jar через `maven-shade-plugin` с relocation `org.sqlite` → `Bzbxddbx.customAirdropPlugin.libs.sqlite` (никаких конфликтов с другими плагинами).
- CI — GitHub Actions: Temurin 25, jar на main.

[![Build](https://github.com/anomalyco/CustomAirdropPlugin/actions/workflows/build.yml/badge.svg)](https://github.com/anomalyco/CustomAirdropPlugin/actions/workflows/build.yml)

## Архитектура

```
api/                       — контракты плагина (не зависят от core)
  Airdrop                  — id/location/state, spawn/open/remove
  AirdropManager           — startEvent/stopEvent/getActiveAirdrop
  AirdropState             — enum WAITING/SPAWNING/ACTIVE/OPENED
  location/LocationSearcher
  loot/LootProvider        — поставщик лута
  loot/AirdropSpawner      — стратегия появления (instant/falling) — OCP-шов
  stats/PlayerStatsStore   — хранилище статистики игроков
  stats/PlayerAirdropStats — record (ник, число открытий)
core/                      — доменная модель и реализации
  ActiveAirdrop            — конечный автомат состояния; делегирует голограмме, эффектам, раскладке; самовосстановление голограммы
  AsyncLocationSearcher    — координаты в supplyAsync, чтение блоков (isSolid) и возврат — на main-thread; точка — блок НАД поверхностью
  hologram/AirdropHologram — жизненный цикл TextDisplay (спавн/текст/перемещение/удаление/проверка isValid)
  fx/AirdropEffects        — звук и частицы при открытии
  loot/LootItem            — record (ItemStack, chance, min/max)
  loot/LootContainer       — выбор лута по весам, generateRandomLoot(min,max)
  loot/SlotPlacer          — чистая раскладка стаков по слотам без перезаписи (тестируется без сервера)
  animation/FallPath       — чистая математика стартовой высоты падения (тестируется)
  animation/FallingAirdropAnimator — FallingBlock + движущаяся голограмма + след + watchdog таймаута
  animation/FallingBlockRegistry    — реестр активных падающих блоков для слушателя
  InstantAirdropSpawner / FallingAirdropSpawner / AirdropSpawnerFactory
  stats/
    SqlitePlayerStatsStore — SQLite: UPSERT открытий, топ с tie-break по времени
    NoOpPlayerStatsStore   — fallback при сбое БД (плагин работает без статистики)
manager/
  EventManager             — оркестрация (cooldown, auto-despawn, периодический refresh голограммы, token поколения)
  EventEpoch               — поколение запроса (защита от гонок async-поиска)
listener/
  PlayerInteractListener   — ПКМ по сундуку (ACTIVE) → open()
  BlockBreakListener       — защита: ломание/взрывы/поршни
  FallingBlockListener     — перехват приземления FallingBlock → аниматор
command/
  StartAirdropCommand      — /airdropstart (BasicCommand)
  AirdropCommand           — /airdrop reload|stop (Brigadier-дерево)
config/
  ConfigSettings           — настройки (radius/cooldown/despawn/spawn-mode/fall)
  LootConfig               — загрузчик loot.yml, implements LootProvider + Reloadable
  Messages                 — шаблоны из messages.yml (MiniMessage)
  SpawnMode                — enum INSTANT/FALLING
util/
  LocationUtil             — сравнение блоков по миру и целочисленным координатам
  MainThread               — гарантированное выполнение на главном потоке
```

### Ключевые решения

- **DIP**: `EventManager` и команды зависят от интерфейсов (`LocationSearcher`, `LootProvider`, `AirdropSpawner`, `Reloadable`, `Messages`) и `JavaPlugin`; инфраструктура Paper спрятана в `core`-имплементациях.
- **SRP**: `ActiveAirdrop` — только state-machine; голограмма, эффекты и раскладка лута вынесены в отдельные узкие классы. Публичные API-геттеры не отдают мутабельный `Location` наружу (возвращается копия).
- **OCP**: точка появления — стратегия `AirdropSpawner`; выбор через `AirdropSpawnerFactory` по `spawn-mode`. Добавление режима не трогает менеджер и слушателей.
- **Без legacy Bukkit**: команды на Brigadier (LifecycleEvents.COMMANDS), задачи на `GlobalRegionScheduler`, привет-диспетчер команд через `CommandSourceStack`.
- **Защита от гонок**: `EventEpoch` инвалидирует устаревший async-поиск и отменяет незавершённую анимацию падения (`AirdropSpawner.dispose()`); `CancellationException` при штатной отмене не логируется как ошибка.
- **Падение с неба**: `FallPath` считает стартовую Y с запасом от потолка мира; `FallingAirdropAnimator` ведёт голограмму за FallingBlock и рисует след; приземление перехватывает `FallingBlockListener` (физика НЕ ставит ванильный блок), watchdog и проверка «пролетел мимо» форсят приземление в цель — сундук не теряется.
- **Лут читается при старте/reload, кэшируется в `LootContainer`; способность сундук получает ровно `min-slots`…`max-slots` стаков; выбор предметов — по весам `chance` с возвратом.
- **Голограмма не «теряется» молча**: раз в 20 тиков (`GlobalRegionScheduler.runAtFixedRate`) `EventManager` зовёт `ActiveAirdrop.refreshHologramIfMissing()` — если вывеска исчезла (выгрузка чанка/сторонний плагин), а чанк сундука загружен, она пересоздаётся под текущее состояние с `WARNING`-логом. Домен остаётся чистым: метод возвращает результат (`boolean`), логгирование в менеджере; выбор текста вынесен в `hologramKeyFor(AirdropState)`.
- **Тестовые seams без MockBukkit**: `ConfigSettings.fromConfig(FileConfiguration)`, `Messages.fromConfig` → `render`, `SlotPlacer.occupy`, `FallPath.spawnY`, `EventEpoch`, `LootContainer.pickWeighted`, `ActiveAirdrop.hologramKeyFor`, `SqlitePlayerStatsStore(Connection)` — тесты на in-memory базе, без сервера.
- **Статистика не роняет плагин**: одно подключение к SQLite на весь жизненный цикл (закрывается в `onDisable`), вызовы только с main-thread, методы `synchronized`; при сбое открытия БД — `NoOpPlayerStatsStore` + `severe`-лог, остальное продолжает работать. `/airdrop me` доступен всем игрокам (`default: true`), `/airdrop top` — операторам.

## Команды

| Команда           | Право                     | Описание                              |
|-------------------|---------------------------|---------------------------------------|
| `/airdropstart`   | `airdrop.command.start`   | Запустить событие аирдропа            |
| `/airdrop reload` | `airdrop.command.reload`  | Перечитать `loot.yml` на лету         |
| `/airdrop stop`   | `airdrop.command.stop`    | Остановить активное событие           |
| `/airdrop top [n]`| `airdrop.command.top`     | Топ игроков по открытым аирдропам     |
| `/airdrop me`     | `airdrop.command.me`      | Личная статистика (доступна всем)     |
| (`/airdrop`)      | —                         | Usage                                 |

Права `start/reload/stop/top` по умолчанию — только у операторов; `me` — у всех игроков. У игроков без прав — сообщение «У вас нет прав» из `messages.yml`. Команды выполнены нативным Brigadier-деревом Paper.

## Конфигурация

`config.yml` — настройки:

```yaml
settings:
  search-radius: 1000        # радиус поиска от спавна мира
  cooldown-seconds: 60       # пауза между запусками
  despawn-minutes: 10        # автоудаление неоткрытого сундука
  spawn-mode: falling        # falling — падение с неба | instant — сразу
  fall-distance: 40          # высота начала падения над точкой (блоков)
  fall-timeout-seconds: 30   # форс-приземление, если падение «зависло»

stats:
  file: database.db          # файл SQLite-базы статистики в папке плагина
  top-default: 10            # размер топа по умолчанию (/airdrop top)
  top-max: 20                # максимум для /airdrop top <n>
```

`messages.yml` — все тексты (MiniMessage), включая координаты в `chat.event-start` через плейсхолдеры `<x>`, `<y>`, `<z>`:

```yaml
hologram:
  active: "<gold><b>[Мистический сундук]</b></gold>\n<gray>Кликни, чтобы открыть</gray>"
  opened: "<red><b>[ОТКРЫТ]</b></red>"
chat:
  event-start: "<gold><b>[Аирдроп]</b> Мистический аирдроп начал падать! Координаты: X=<x>, Y=<y>, Z=<z></gold>"
  event-stopped: "<yellow>[Аирдроп] Событие аирдропа завершено</yellow>"
block:
  cannot-break: "<red>Вы не можете сломать мистический сундук!</red>"
command:
  no-permission: "<red>У вас нет прав</red>"
  airdrop-started: "<green>[Аирдроп] Событие запущено!</green>"
  airdrop-cooldown: "<red>[Аирдроп] Подождите немного перед следующим запуском!</red>"
  airdrop-reloaded: "<green>[Аирдроп] Конфигурация лута успешно перезагружена!</green>"
  airdrop-usage: "<gray>Использование: /airdrop reload|stop|top [count]|me</gray>"
  players-only: "<red>Команда доступна только игрокам</red>"
  airdrop-stats: "<green>[Аирдроп] <gold><player></gold> открыл аирдропов: <count></green>"
  airdrop-stats-empty: "<gray>[Аирдроп] У вас пока нет открытых аирдропов</gray>"
  airdrop-top-header: "<gold><b>Топ <count> по открытым аирдропам:</b></gold>"
  airdrop-top-entry: "<white><place>. <aqua><player></aqua> — <green><opened></green></white>"
  airdrop-top-empty: "<gray>[Аирдроп] Статистика пуста</gray>"
```

`loot.yml` — лут и число слотов наград:

```yaml
settings:
  min-slots: 3
  max-slots: 6

loot:
  diamond:
    material: DIAMOND
    chance: 0.5        # относительный вес: чем больше, тем чаще выпадает
    min-amount: 1
    max-amount: 4
  emerald:
    material: EMERALD
    chance: 0.4
    min-amount: 1
    max-amount: 3
```

`chance` трактуется как относительный вес при выборе с возвратом: `0.0` исключает предмет, при нулевой сумме весов — равномерный выбор.

## Статус

Реализовано полностью: режимы появления (`falling` с анимацией падения и `instant`), лут по весам с reload на лету, защита сундука от ломания/взрывов/поршней, cooldown и auto-despawn, Brigadier-команды, `messages.yml`, защита от гонок через `EventEpoch`, самовосстановление голограммы, статистика игроков на встроенном SQLite (`/airdrop top` + `/airdrop me`), unit-тесты (`mvn verify`, 42 теста) и CI. Публичный API — `AirdropManager`, `Airdrop`, `LocationSearcher`, `LootProvider`, `AirdropSpawner`, `AirdropState`, `PlayerStatsStore`.

## Скриншоты

> Заглушки — изображения зальёте сами (`screenshots/`).

- `images/AirDropLoot.png` — настраиваемый лут сундука
- `images/AirDropSpawn.png` — сундук с голограммой на точке спавна
- `images/AirDropOpen.png` — сундук после открытия с лотом
- `images/AirDropMessage.png` — стартовое сообщение с координатами в чате

