# Astolfo Visuals 2.0 — Recovered Source

Восстановленный исходный код мода **Astolfo Visuals 2.0** (Fabric, Minecraft 1.21.4).

Исходники получены декомпиляцией `astolfovisuals-2.0.jar` через **Vineflower 1.12.0**,
затем все intermediary-имена (`class_XXXX` / `method_XXXX` / `field_XXXX`) переведены
в читаемые yarn-имена скриптом. Это не побайтовая копия утраченного оригинала:
комментарии и часть исходных конструкций из байткода не восстанавливаются.

## Состояние

| | |
|---|---|
| Файлов Java | 206 |
| Строк переименований | ~5000 |
| Остатков `class_` / `method_` / `field_` | 0 |
| `./gradlew build` | **не проходит** — см. «Блокеры» |

Рабочие коммиты:

- `8b75320` — pristine: декомпилированный intermediary-код (точка отката)
- `f20dd58` — конвертация intermediary → yarn (`class_`/`method_`/`field_`)
- `4b6a4de` — фикс ссылок в теле: простые имена без усечённых пакетных префиксов

## Структура

```
src/main/java/xyz/angames/astolfoclient/client/
├── AstolfoclientClient.java      entrypoint
├── command/                      команды + commands/
├── config/                       конфигурация
├── effects/                      ESP, партиклы (~30)
├── gui/clickgui/                 ClickGUI
├── hud/                          HUD-менеджеры (~20)
├── mixin/                        миксины (~30 из 55 объявленных)
├── module/                       модули + modules/ + setting/
├── render/                       рендер-утилиты
└── util/                         утилиты

src/main/resources/
├── fabric.mod.json
├── astolfoclient.client.mixins.json
├── client-astolfoclient-refmap.json
├── assets/astolfoclient/         шейдеры (mre: liquidglass, blur, msdf_font), шрифты, звуки
└── META-INF/jars/                jlayer-1.0.1, luaj-jse-3.0.1, media-player-info-0.1.0
```

## Блокеры сборки

1. **`build.gradle`: пустой `dependencies {}`** — подключены только minecraft/mappings/loader.
   Нужны: `fabric-api`, `dev.sxmurxy.mre`, gson, joml, brigadier.
2. **`dev.sxmurxy.mre` не найдена** — библиотека рендера, без неё сборка невозможна.
3. **25 миксинов из `astolfoclient.client.mixins.json` отсутствуют как файлы**
   (в `mixin/` лежит ~30 из 55) → краш при загрузке мода, т.к. `required: true`.
4. **`fabricloader >= 0.19.3`** в `fabric.mod.json` против `loader_version=0.16.10`
   в `gradle.properties` — противоречие.
5. `AstolfoclientDataGenerator` объявлен в `fabric-datagen` entrypoint, но файла нет.
6. Синтетические имена Vineflower (`var13`, `var14`) — чистка читаемости, не блокер.

## Сборка (после устранения блокеров)

```bash
./gradlew build          # артефакт в build/libs/
./gradlew runClient      # запуск dev-клиента
```

Требуется JDK 21.

## Лицензия

`GPL-3.0-only` — как указано в оригинальном `fabric.mod.json`.

Оригинальный автор: **SRS**.