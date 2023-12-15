# ForgeLegalizer

## Язык (Language)

- [English](README.md)
- **Русский** *(текущий)*

## Что?

*Исправляет радиус ударов игрока в Forge для 1.18.2 -> 1.19.4.*  
Исправляет [MinecraftForge/#9309](https://github.com/MinecraftForge/MinecraftForge/issues/9309), уменьшая радиус удара
игрока до ванильного и позволяет серверам проверять наличие этого мода.

## Скачать

- [GitHub](https://github.com/BromineMC/ForgeLegalizer/releases)
- [Modrinth](https://modrinth.com/mod/forgelegalizer)

## Сборка

Вам потребуется:

- Java JDK 17 или выше. (например, [Temurin](https://adoptium.net/))
- 4 ГБ свободной ОЗУ.
- Немного места для хранения файлов.

Как:

- Убедитесь, что JDK правильно установлен. (т.е. путь JDK есть в переменной среды `JAVA_HOME`)
- Склонируйте репозиторий или скачайте его. (например,
  через `git clone https://github.com/BromineMC/ForgeLegalizer.git`)
- Откройте терминал (командную строку) в папке репозитория.
- Пропишите `./gradlew build`. (`gradlew build` для командной строки)
- Заберите JAR-файлы из `<версия>/build/libs/`.

## Лицензия

Этот проект лицензирован под [MIT License](https://github.com/BromineMC/ForgeLegalizer/blob/main/LICENSE).

## FAQ (ЧаВО)

**В**: Я не понимаю [что-либо].  
**О**: [Discord](https://dsc.gg/vidtu).

**В**: Как скачать?  
**О**: Релизы доступны на [GitHub](https://github.com/BromineMC/ForgeLegalizer/releases)
и [Modrinth](https://modrinth.com/mod/forgelegalizer).

**В**: Fabric, Quilt, 1.20+, 1.18.1, 1.17.1, 1.16.5, любая другая версия или загрузчик модов?  
**О**: Этот мод фиксит [баг](https://github.com/MinecraftForge/MinecraftForge/issues/9309) ТОЛЬКО в Forge версиях для
Minecraft 1.18.2 -> 1.19.4.

**В**: Как определить, что игрок использует этот мод?  
**О**: Мод [регистрирует](https://wiki.vg/Plugin_channels#minecraft:register) канал `forgelegalizer:v1`. Вы можете установить этот мод или плагин на сервер, чтобы запретить игрокам, использующим Forge без мода, заходить на сервер.

**В**: Является ли этот мод полноценным решением?  
**О**: Нет. Этот мод может быть легко подделан кем угодно, кто знает как кодить, чтобы казалось, что этот мод "существует", когда в реальности он не будет установлен на клиенте. Вы можете поставить плагин-античит с проверками на Reach, например [GrimAC](https://github.com/GrimAnticheat/Grim), [uNCP](https://github.com/Updated-NoCheatPlus/NoCheatPlus) или любой другой на свой выбор для защиты от Reach-подобных читов и багов.

**В**: Тогда зачем этот мод существует?  
**О**: Чтобы не дать людям, которые не хотят читерить и не знают об этом баге Forge, случайно получать нечестное преимущество в игре на вашем сервере.
