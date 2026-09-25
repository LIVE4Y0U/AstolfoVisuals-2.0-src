# Astolfo Visuals 2.0

Исходники **Astolfo Visuals 2.0** для 1.21.4 (Fabric), восстановленный из
[этих сурсов](https://github.com/SRS-0/AstolfoVisuals).

**Автор оригинала:** SRS — https://fakecrime.bio/SRS

## Что нужно для сборки

- JDK 21
- Gradle (wrapper в комплекте)
- Локальный jar `mre-1.0-1.21.4.jar` в `libs/` — библиотека рендера **MRE**
  (`dev.sxmurxy.mre`), извлечённая из оригинального `astolfovisuals-2.0.jar`.
  В Maven Central и JitPack её нет, а публичная ветка
  [sxmurxy0/Minecraft-Render-Enhancer-2D-1.21](https://github.com/sxmurxy0/Minecraft-Render-Enhancer-2D-1.21)
  (`fabric-1.21.4`) **старее** той, на которой собран мод: в ней нет
  `MsdfFont.Builder.glyphMapper()`, `data(Identifier)` и `Builder.liquidGlass()`,
  из-за чего иконки ClickGUI и liquid-glass не работают.

## Блокеры сборки

Исходный код декомпилирован (Vineflower), поэтому часть данных потеряна.

1. **`dev.sxmurxy.mre` нет в публичных репозиториях.** Библиотека лежит в `libs/mre-1.0-1.21.4.jar` —
   это классы MRE, вырезанные из оригинального jar'а, плюс дописанный `fabric.mod.json`
   (`id: mre`), без которого Loom не ремапит jar и компилятор видит intermediary-имена
   (`class_2960` вместо `Identifier`).
2. **Синтетические имена Vineflower** (`var13`, `var14`, `field_53536`) в отдельных местах —
   на компиляцию не влияет, читаемость страдает.
3. **Дженерики в миксинах потеряны** — в `RagdollRenderer`, `ClientProtectionManager`
   и `AstolfoclientClient` стоят raw-type заглушки с `@SuppressWarnings`.
4. **Отсутствующие внешние моды** — `ru.vidtu.ias` переведён на рефлексию (мод соберётся и
   запустится без IAS, но функция случайных оффлайн-аккаунтов работать не будет);
   `media-player-info` подключён как `compileOnly` из `META-INF/jars/`.
5. **Звуковые события без файлов** — `astolfoclient:sounds/*.ogg` (crash_detection, hit_bell,
   hit_beng, hit_bonk, hit_bubble, hit_glass, hit_metallic, hit_rust) объявлены в
   `sounds.json`, но самих `.ogg` в ресурсах нет. В логе — WARN, на работу не влияет.
6. **`fabric-datagen` entrypoint убран** — `AstolfoclientDataGenerator` в исходниках не было,
   генерировать данные нечем.
7. **Миксины не проверялись в рантайме** — все 56 объявленных в
   `astolfoclient.client.mixins.json` присутствуют как файлы, но корректность их таргетов
   под 1.21.4 не подтверждена (проверяется только запуском игры).
8. **Иконки-атласы требуют маппинга глифов.** В `icons/*.json` у глифов есть только `index`,
   а `unicode` отсутствует. Код рисует иконки строками (`"D"`, `"F"`, `"I"`…), а
   `MsdfFont.applyGlyphs()` ищет глиф по коду символа. Поэтому в `glyphMapper` для всех
   четырёх атласов стоит `g -> 'A' + g.index()`: `index=0` → `'A'`=65, `index=3` → `'D'`=68.
   Значения `ICON_*` в `ClickGuiIcons` жёстко привязаны к этой формуле.
