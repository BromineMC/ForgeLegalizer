# ForgeLegalizer

Fixes Forge player reach for 1.18.2 -> 1.19.4.

## What?

This mod fixes [MinecraftForge/#9309](https://github.com/MinecraftForge/MinecraftForge/issues/9309) by reducing player
reach back
to vanilla and allows servers to check whether this mod is installed.

## FAQ

**Q**: I don't understand [insert something here].  
**A**: [Discord](https://dsc.gg/vidtu).

**Q**: How to download?  
**A**: Releases are available via [GitHub](https://github.com/BromineMC/ForgeLegalizer/releases)
and [Modrinth](https://modrinth.com/mod/forgelegalizer).

**Q**: Fabric, Quilt, 1.20+, 1.18.1, 1.17.1, 1.16.5, any other version or mod-loader?  
**A**: This mod fixes [a bug](https://github.com/MinecraftForge/MinecraftForge/issues/9309) in Forge versions for
Minecarft 1.18.2 -> 1.19.4 ONLY. Other platforms will only kick Forge 1.18.2 -> 1.19.4 without the mod.

**Q**: Is it open source?  
**A**: [Yes.](https://github.com/BromineMC/ForgeLegalizer) (Everything except Spigot plugin is licensed
under [MIT License](https://github.com/BromineMC/ForgeLegalizer/blob/main/LICENSE); Spigot plugin is licensed under [GNU General Public License 3.0](https://github.com/BromineMC/ForgeLegalizer/blob/main/spigot/COPYING))

**Q**: How to detect if the player is using this mod?  
**A**: The mod [registers](https://wiki.vg/Plugin_channels#minecraft:register) the `forgelegalizer:v1` channel. You can
install this mod or plugin on the server to prevent Forge players without the mod from joining.

**Q**: Is this the complete solution?  
**A**: No. This mod can easily be spoofed by someone who knows how to code to appear to "exist", but in reality it won't
be installed on the client. You should install an anti-cheat plugin with Reach checks
like [GrimAC](https://github.com/GrimAnticheat/Grim), [uNCP](https://github.com/Updated-NoCheatPlus/NoCheatPlus), or any
other at your choice to prevent Reach-alike hacks and bugs from working.

**Q**: Why does this mod exists then?  
**A**: To prevent people who do not want to hack and are blind to Forge bug from unintentionally gaining unfair
advantage on your server.
