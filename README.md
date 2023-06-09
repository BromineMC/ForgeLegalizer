# ForgeLegalizer

## Language

- **English** *(current)*
- [Русский](README_RU.md)

## What?

*Fixes Forge player reach for 1.18.2 -> 1.19.4.*  
Fixes [MinecraftForge/#9309](https://github.com/MinecraftForge/MinecraftForge/issues/9309) by reducing player reach back to vanilla and allows servers to check whether this mod is installed.

## Download

- [GitHub](https://github.com/BromineMC/ForgeLegalizer/releases)
- [Modrinth](https://modrinth.com/mod/forgelegalizer)

## Building

You will need:

- Java JDK 17 or higher. (e.g. [Temurin](https://adoptium.net/))
- 4 GB of available RAM.
- A bit of storage.

How to:

- Ensure your JDK is set up properly. (i.e. JDK path is in `JAVA_HOME` environment variable)
- Clone this repo or download it. (e.g. via `git clone https://github.com/BromineMC/ForgeLegalizer`)
- Open the terminal (command prompt) in the repository folder.
- Run `./gradlew build`. (`gradlew build` for command prompt)
- Grab JARs from `<version>/build/libs/`.

## License

This project is licensed under [MIT License](https://github.com/BromineMC/ForgeLegalizer/blob/main/LICENSE).

## FAQ

**Q**: I don't understand [insert something here].  
**A**: [Discord](https://dsc.gg/brominemc).

**Q**: How to download?  
**A**: Releases are available via [GitHub](https://github.com/BromineMC/ForgeLegalizer/releases) and [Modrinth](https://modrinth.com/mod/forgelegalizer).

**Q**: Fabric, Quilt, 1.20+, 1.18.1, 1.17.1, 1.16.5, any other version or mod-loader?  
**A**: This mod fixes [a bug](https://github.com/MinecraftForge/MinecraftForge/issues/9309) in Forge versions for Minecarft 1.18.2 -> 1.19.4 ONLY.

**Q**: How to detect if the player is using this mod?  
**A**: The mod [registers](https://wiki.vg/Plugin_channels#minecraft:register) the `forgelegalizer:v1` channel.