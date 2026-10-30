# INTERLOPER — хоррор-мод для Minecraft 1.20.1 (Forge)

Мод по мотивам ARG **INTERLOPER** от **Anomidae** (PROJECT SKY//BOX). В игре появляется
команда, которой не должно существовать, демо-записи движка, скайбокс с изнанкой и
существо, которое стоит слишком близко, когда вы оборачиваетесь.

> **Внимание:** мод содержит мерцающие изображения, резкие звуки и скримеры.
> Если у вас фотосенситивность — отключите мерцание в конфиге (`interloper-common.toml`, `visuals.flashing = false`).
>
> Это фанатский проект, не связанный с Anomidae. Все тексты и ассеты сгенерированы
> процедурно для этого мода.

---

## Скачать

Готовый `.jar` лежит в разделе **Releases** этого репозитория:

**`interloper-1.20.1-1.0.0.jar`** → положить в `.minecraft/mods`, нужен **Forge 47.x для 1.20.1**.

Сборка из исходников: `./gradlew build` → `build/libs/`.

---

## Что внутри

### Сущности

| Сущность | Поведение |
|---|---|
| **Interloper** («Человек с зонтом») | 3 блока ростом, тонкий, с зонтом. Появляется ночью или по ритуалу, идёт за игроком, телепортируется за спину, **замирает и исчезает, если на него смотреть**. При ударе отправляет экран в «capture»-режим и наносит урон своим типом урона (`interloper:interloped`). Убивается, дропает фрагменты скайбокса. |
| **Skyghost** | Парит на расстоянии 15–30 блоков, не двигается, пока вы смотрите, приближается, когда отворачиваетесь. Исчезает вплотную. |

### Предметы и блок

* **Demo File** (5 типов) — при ПКМ проигрывает «запись демо»: помехи, кубмап, шёпот в чат,
  у Типа 5 — шанс призвать Интерлопера прямо рядом с вами.
* **Skybox Fragment** — дроп с Интерлопера, материал для крафтов и «топливо» для терминала.
* **Anomaly Scanner** — сканирует скайбокс: показывает тип ближайшей аномалии, расстояние и
  направление; вблизи начинает глючить.
* **The Umbrella** — ПКМ открывает зонт и вызывает охоту. Вам же говорили не открывать.
* **Engine Terminal** (блок) — мертвая консоль движка: скормите ей фрагмент, получите случайное
  демо; с пустой рукой она иногда отвечает `s.interlope.pull:25565`.

### Случайные события (Ambience Director)

Раз в ~45 секунд (настраивается) мод выбирает игрока и делает что-нибудь неприятное:
шёпот за спиной, помехи, сердцебиение, тряска камеры, сообщение в чате «от никого»,
спавн Skyghost, ночная охота Интерлопера.

### Клиентские эффекты

Полноэкранные помехи, «порывы» экрана, затемнение, тряска камеры и FOV, эмиссивные глаза
сущности, виньетка при приближении, отрисовка кубмапа и надпись `I N T E R L O P E`
в режиме capture. Всё можно выключить/ослабить в конфиге.

---

## Команда `/interlope`

```
/interlope                           — «Unknown or incomplete command, see below for error»
/interlope help
/interlope hunt                      — начать охоту (требует прав оператора)
/interlope skyghost                  — поставить призрака за спину
/interlope demo <1-5>                — выдать демо-запись
/interlope effect <glitch|darkness|flash|shake|cubemap|capture|whisper>
/interlope terminal                  — выдать терминал движка
/interlope fragment                  — выдать фрагменты скайбокса
```

Алиас: `/interloper`.

---

## Рецепты

```
Engine Terminal          Umbrella                 Anomaly Scanner
D G D                    W W W                    . I .
G R G                    . S .                    I C I
D G D                    . S .                    . R .
D = deepslate            W = black wool           I = iron ingot
G = glass                S = stick                C = compass
R = redstone                                      R = redstone

Skybox Fragment (x2)
. A .
A E A         A = amethyst shard, E = ender pearl
. A .
```

Демо-записи не крафтятся — их выдаёт Engine Terminal за фрагменты скайбокса.

---

## Конфиг (`config/interloper-common.toml`)

```toml
[ambience]
    enabled = true
    interval = 900          # средний интервал между событиями в тиках (20 = 1 сек)
    chat_messages = true    # шёпот в чат

[entities]
    natural_interloper = true
    hunt_chance = 6         # 1 из N при ночном ролле
    natural_skyghost = true

[visuals]
    flashing = true         # мерцание/кубмап — выключить при фотосенситивности
    scanner_glitch = true
    intro_message = true
```

---

## Сборка и CI

* Java 17, ForgeGradle 6, Forge 47.2.20, mappings `official` (1.20.1).
* GitHub Actions workflow `.github/workflows/build.yml` собирает jar на каждый push,
  выкладывает артефакт и прикрепляет jar к GitHub Release при пуше тега `v*`.

```bash
./gradlew build          # build/libs/interloper-1.20.1-1.0.0.jar
./gradlew runClient      # тестовый клиент
./gradlew runServer      # тестовый сервер
```

### Структура

```
src/main/java/net/anomidae/interloper/
├── Interloper.java             точка входа, регистрация
├── block/                      Engine Terminal
├── client/                     эффекты, оверлей, рендер, обработка пакетов
│   ├── model/                  модели Интерлопера и Skyghost
│   └── render/                 рендереры + эмиссивный слой глаз
├── command/                    /interlope
├── config/                     ForgeConfigSpec
├── entity/                     InterloperEntity, SkyghostEntity
├── event/                      Forge/MOD bus события
├── item/                       демо-записи, сканер, зонт, фрагмент
├── network/                    пакеты эффектов и «шёпота»
├── registry/                   DeferredRegister'ы
├── util/                       типы демо, кастомный урон
└── world/                      HorrorDirector — режиссёр случайных событий
```

Все текстуры и звуки генерируются скриптом `tools/generate_assets.py`
(`pillow`, `numpy`, `soundfile`) — сторонних ассетов в моде нет.

---

## Лицензия

MIT — см. [LICENSE](LICENSE).
