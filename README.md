# ForgeLegalizer

## Language

- **English** *(current)*
- [Русский](README_RU.md)

## What?

*Fixes Forge player reach for 1.18.2 -> 1.19.4.*  
Made for BromineMC. (`brominemc.ru`, [Discord](https://dsc.gg/brominemc))

## Download

[GitHub](https://github.com/BromineMC/ForgeLegalizer/releases)

## Building

You will need:

- Java JDK 17 or higher. (e.g. [Temurin](https://adoptium.net/))
- 4 GB of available RAM.
- A bit of storage.

How to:

- Ensure your JDK is set up properly. (i.e. JDK path is in `JAVA_HOME` environment variable)
- Clone this repo or download it. (e.g. via `git clone https://github.com/BromineMC/ForgeLegalizer`)
- Open the terminal (command prompt) there.
- Run `./gradlew build`.
- Grab JARs from `<version>/build/libs/`

## License

This project is licensed under [MIT License](https://github.com/BromineMC/ForgeLegalizer/blob/master/LICENSE).

## FAQ

**Q**: I don't understand [insert something here].  
**A**: [Discord](https://dsc.gg/brominemc).

**Q**: How to download?  
**A**: Releases are available via [GitHub](https://github.com/BromineMC/ForgeLegalizer/releases).

**Q**: Fabric, Quilt, 1.20+, 1.18.1, 1.17.1, 1.16.5?  
**A**: This mod fixes a bug in Forge versions for Minecarft 1.18.2 -> 1.19.4 ONLY.

**Q**: How to detect if the player is using this mod?  
**A**: The mod [registers](https://wiki.vg/Plugin_channels#minecraft:register) the `forgelegalizer:v1` channel.