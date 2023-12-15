/*
 * ForgeLegalizerVerifier-Spigot is a SpigotMC verifier plugin for ForgeLegalizer client modification.
 * Copyright (C) 2023 threefusii
 * Copyright (C) 2023 BromineMC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/
 */

package ru.brominemc.forgelegalizerverifier;

import com.viaversion.viaversion.api.Via;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 * Main plugin class.
 *
 * @author threefusii
 */
public final class ForgeLegalizerVerifier extends JavaPlugin implements Listener {
    /**
     * Invalid configuration administrator notification.
     */
    private static final String INVALID_CONFIG_NOTIFY = "§c§l(!)§r Warning! The configuration of §eForgeLegalizerVerifier§r plugin didn't load correctly. Please, reconfigure it. For security, §c§nplayers can't join the game right now§r.";

    /**
     * Invalid configuration player kick.
     */
    private static final String INVALID_CONFIG_KICK = "\n§c> Unexpected Exception§r\n\nThe configuration of §eForgeLegalizerVerifier§r plugin didn't load correctly. Please, notify the admins. You §c§nwon't be able to join the server§r until they fix the error.\n";

    /**
     * ForgeLegalizer plugin channel.
     */
    private static final String CHANNEL = "forgelegalizer:v1";

    /**
     * Maximum affected protocol version {@code 1.18.2}. (inclusive)
     */
    private static final int MIN_VERSION = 758;

    /**
     * Maximum affected protocol version {@code 1.19.4}. (inclusive)
     */
    private static final int MAX_VERSION = 762;

    /**
     * Player kick message for hack-alike-Forge.
     * Leave empty to disable.
     * <p>
     * Placeholders:
     * <ul>
     *     <li>{@code %name%} - Player name</li>
     *     <li>{@code %uuid%} - Player UUID</li>
     * </ul>
     */
    private String kickMessage = null;

    /**
     * Admin notify message for hack-alike-Forge.
     * Leave empty to disable.
     * <p>
     * Placeholders:
     * <ul>
     *     <li>{@code %name%} - Player name</li>
     *     <li>{@code %uuid%} - Player UUID</li>
     * </ul>
     */
    private String notifyMessage = null;

    /**
     * Duration in ticks after player with hack-alike-Forge will be kicked.
     * Default: 60 ticks (3 seconds)
     * Increase if players with bad internet connection are being kicked.
     */
    private int timeout = 60;

    /**
     * Commands to execute on hack-alike-Forge players.
     * Leave empty to disable.
     * <p>
     * Placeholders:
     * <ul>
     *     <li>{@code %name%} - Player name</li>
     *     <li>{@code %uuid%} - Player UUID</li>
     * </ul>
     */
    private List<String> commands = Collections.emptyList();

    /**
     * Whether the players with unknown version should be kicked as well.
     * Disable if causes false kicks.
     */
    private boolean strict = true;

    /**
     * Whether the config has been loaded by {@link #loadConfigSafe()} with errors.
     */
    private boolean error = true;

    @Override
    public void onEnable() {
        // Register the handler.
        Bukkit.getPluginManager().registerEvents(this, this);

        // Load the config.
        this.loadConfigSafe();
    }

    /**
     * Loads the config.
     *
     * @return Whether the config has been loaded successfully
     */
    public boolean loadConfigSafe() {
        try {
            // Load the config.
            this.loadConfig();

            // Mark config loading as successful.
            this.error = false;
            return true;
        } catch (Throwable t) {
            // Log the error.
            this.getLogger().log(Level.SEVERE, "Unable to load ForgeLegalizerVerifier config.", t);

            // Mark config loading as erroneous.
            this.error = true;
            return false;
        }
    }

    /**
     * Loads the config.
     *
     * @throws RuntimeException If enable to load
     */
    public void loadConfig() {
        try {
            // Save the default config if deleted.
            this.saveDefaultConfig();

            // Reload the config.
            this.reloadConfig();

            // Get the config.
            Configuration config = this.getConfig();

            // Get the values.
            this.kickMessage = ChatColor.translateAlternateColorCodes('&', String.join("\n",
                    config.getStringList("kickMessage"))).trim().intern();
            this.notifyMessage = ChatColor.translateAlternateColorCodes('&', String.join("\n",
                    config.getStringList("notifyMessage"))).trim().intern();
            this.commands = config.getStringList("commands");
            this.timeout = config.getInt("timeout", 60);
            this.strict = config.getBoolean("strict", true);
        } catch (Throwable t) {
            throw new RuntimeException("Unable to load ForgeLegalizerVerifier config.", t);
        }
    }

    // Handles player joining.
    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        // Get the player.
        Player player = event.getPlayer();

        // Plugin has configuration errors, kick everyone. (except admins, notify them)
        if (this.error) {
            // Notify the admins.
            if (player.hasPermission("forgelegalizerverifier.notify")) {
                player.sendMessage(INVALID_CONFIG_NOTIFY);
                return;
            }

            // Kick players.
            player.kickPlayer(INVALID_CONFIG_KICK);
            return;
        }

        // Skip if bypassable.
        if (player.hasPermission("forgelegalizerverifier.bypass")) return;

        // Schedule a task, player are not registering their channels right away.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Skip if logged out.
            if (!player.isOnline()) return;

            // Exempt players that are not 1.18.2 -> 1.19.4.
            int version = this.version(player);

            // Skip if version is unknown (and config is not strict), version is too low, too high, or player has ForgeLegalizer.
            if (version == -1 && !this.strict || version != -1 && (version < MIN_VERSION || version > MAX_VERSION)
                    || player.getListeningPluginChannels().contains(CHANNEL)) return;

            // Kick the player if enabled.
            if (this.kickMessage != null && !this.kickMessage.isEmpty()) {
                player.kickPlayer(this.kickMessage
                        .replace("%name%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString()));
            }

            // Notify the admins if enabled.
            if (this.notifyMessage != null && !this.notifyMessage.isEmpty()) {
                Bukkit.broadcast(this.notifyMessage
                                .replace("%name%", player.getName())
                                .replace("%uuid%", player.getUniqueId().toString()),
                        "forgelegalizerverifier.notify");
            }

            // Execute other commands. (if any)
            if (this.commands != null) {
                for (String command : this.commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("%name%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString()));
                }
            }
        }, this.timeout);
    }

    /**
     * Gets the player version.
     *
     * @param player Target player
     * @return Player protocol version, {@code -1} if unknown
     */
    private int version(Player player) {
        // Return -1 if no Via is installed.
        if (!Bukkit.getPluginManager().isPluginEnabled("ViaVersion")) {
            return -1;
        }

        // Get the player version.
        return Via.getAPI().getPlayerVersion(player.getUniqueId());
    }
}
