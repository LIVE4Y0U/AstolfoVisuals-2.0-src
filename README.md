# Astolfo Visuals 2.0

Исходный код клиентского мода **Astolfo Visuals 2.0** для Minecraft 1.21.4 (Fabric), восстановленный из
распространённого бинарного jar-файла.

**Автор оригинала:** SRS — https://fakecrime.bio/SRS
**Лицензия:** GPL-3.0-only

## Что нужно для сборки

- JDK 21
- Gradle (wrapper в комплекте)
- Локальный jar `minecraft-render-enhancer-2D` — библиотека рендера от
  [sxmurxy0](https://github.com/sxmurxy0/Minecraft-Render-Enhancer-2D-1.21), ветка `fabric-1.21.4`.
  Собирается из исходников и кладётся в `libs/minecraft-render-enhancer-2D-1.0-1.21.4.jar`
  (в Maven Central и JitPack её нет).

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew build
```

Артефакт: `build/libs/astolfovisuals-2.0.jar`

## Блокеры сборки

Исходный код декомпилирован (Vineflower), поэтому часть данных потеряна. Сборка проходит,
но при запуске возможны проблемы:

1. **`dev.sxmurxy.mre` нет в публичных репозиториях** — библиотеку рендера приходится собирать
   из исходников вручную и подкладывать в `libs/` (см. выше).
2. **Синтетические имена Vineflower** (`var13`, `var14`, `field_53536`) в отдельных местах —
   на компиляцию не влияет, читаемость страдает.
3. **Дженерики в миксинах потеряны** — компилятор не восстанавливает generic-параметры у
   `EntityRenderer<?,?>` и `Packet<?>`, поэтому в `RagdollRenderer`, `ClientProtectionManager`
   и `AstolfoclientClient` стоят raw-type заглушки с `@SuppressWarnings`.
4. **Отсутствующие внешние моды** — `ru.vidtu.ias` переведён на рефлексию (мод соберётся и
   запустится без IAS, но функция случайных оффлайн-аккаунтов работать не будет);
   `media-player-info` подключён как `compileOnly` из `META-INF/jars/`.
5. **`Builder.liquidGlass()` отсутствует в MRE 1.21.4** — эффект liquid glass есть только
   в 1.21.5-ветке библиотеки, здесь заменён на blur.
6. **`fabric-datagen` entrypoint убран** — `AstolfoclientDataGenerator` в исходниках не было,
   генерировать данные нечем.
7. **Миксины не проверялись в рантайме** — все 56 объявленных в
   `astolfoclient.client.mixins.json` присутствуют как файлы, но корректность их таргетов
   под 1.21.4 не подтверждена (проверяется только запуском игры).