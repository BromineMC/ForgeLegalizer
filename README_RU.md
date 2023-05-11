# ForgeLegalizer

## Язык (Language)

- [English](README.md)
- **Русский** *(текущий)*

## Что?

*Исправляет радиус ударов игрока в Forge для 1.18.2 -> 1.19.4.*  
Сделано для BromineMC. (`brominemc.ru`, [Discord](https://dsc.gg/brominemc))

## Скачать

скоро™

## Сборка

Вам потребуется:

- Java JDK 17 или выше. (например, [Temurin](https://adoptium.net/))
- 4 ГБ свободной ОЗУ.
- Немного места для хранения файлов.

Как:

- Убедитесь, что JDK правильно установлена. (т.е. путь JDK есть в переменной среды `JAVA_HOME`)
- Клонируйте репозиторий или скачайте его. (например, через `git clone https://github.com/BromineMC/ForgeLegalizer.git`)
- Откройте терминал (командную строку) там.
- Пропишите `./gradlew build`.
- Заберите JAR-файлыиз `<версия>/build/libs/`

## Лицензия

Этот проект лицензирован под [MIT License](https://github.com/BromineMC/ForgeLegalizer/blob/master/LICENSE).

## FAQ (ЧаВО)

**В**: Я не понимаю [что-либо].  
**О**: [Discord](https://dsc.gg/brominemc).

**В**: Как скачать?  
**О**: Релизы будут доступны скоро™, снэпшоты могут быть доступны скоро™ тоже.

**В**: Fabric, Quilt, 1.20+, 1.18.1, 1.17.1, 1.16.5?  
**О**: Этот мод фиксит баг ТОЛЬКО в Forge версиях для Minecraft 1.18.2 -> 1.19.4.

**В**: Как определить, что игрок использует этот мод?  
**О**: Мод [регистрирует](https://wiki.vg/Plugin_channels#minecraft:register) канал `forgelegalizer:v1`.