/*
 * ForgeLegalizerVerifier-Spigot is a SpigotMC verifier plugin for ForgeLegalizer client modification.
 * Copyright (C) 2023-2025 VidTu
 * Copyright (C) 2023-2025 BromineMC
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.brominemc.forgelegalizerverifier;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.Via;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Main plugin class.
 *
 * @author threefusii
 */
@SuppressWarnings("DynamicRegexReplaceableByCompiledPattern") // <- I do sincerely hope people won't run this on J8.
public final class ForgeLegalizerVerifier extends JavaPlugin implements Listener, PluginMessageListener {
    /**
     * Invalid configuration player kick.
     */
    private static final String INVALID_CONFIG_ERROR = "\n§c> ForgeLegalizerVerifier§r\n\nThe configuration of §eForgeLegalizerVerifier§r plugin didn't load correctly. Please, §bnotify the admins§r. You §c§nwon't be able to join the server§r until they fix the error.\n";

    /**
     * Brand decoding error.
     */
    private static final String BRAND_ERROR = "\n§c> ForgeLegalizerVerifier§r\n\nThe §eForgeLegalizerVerifier§r plugin couldn't decode your client brand. For security purposes, you §c§nhave been kicked§r.\n";

    /**
     * Success configuration reload.
     */
    private static final String CONFIG_RELOAD_SUCCESS = "The configuration of §eForgeLegalizer§r has been §areloaded§r.";

    /**
     * Failure configuration reload.
     */
    private static final String CONFIG_RELOAD_FAIL = "Unable to §creload§r the configuration of §eForgeLegalizer§r.";

    /**
     * Information message.
     */
    private static final String INFO = String.format("§eForgeLegalizerVerifier-Spigot§r version §6%s§r.\nAuthors: §c%s§r.\nModrinth: §ahttps://modrinth.com/mod/forgelegalizer§r\nGitHub: §bhttps://github.com/BromineMC/ForgeLegalizer",
            ForgeLegalizerVerifier.class.getPackage().getSpecificationVersion(),
            ForgeLegalizerVerifier.class.getPackage().getSpecificationVendor());

    /**
     * ForgeLegalizer plugin channel.
     */
    private static final String CHANNEL = "forgelegalizer:v1";

    /**
     * Minimum affected protocol version {@code 1.18.2}. (inclusive)
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
     * Commands to execute on hack-alike-Forge players.
     * Leave empty to disable.
     * <p>
     * Placeholders:
     * <ul>
     *     <li>{@code %name%} - Player name</li>
     *     <li>{@code %uuid%} - Player UUID</li>
     * </ul>
     */
    private List<String> commands = null;

    /**
     * Regular expression (regex) of the Forge-alike brands.
     * Clients with these brands OR with any of the channels below will be checked for ForgeLegalizer.
     */
    private Predicate<String> forgeBrand = null;

    /**
     * Regular expression (regex) of the Forge-alike channels.
     * Clients with any of these channels OR with brands above will be checked for ForgeLegalizer.
     */
    private Predicate<String> forgeChannel = null;

    /**
     * Whether the unknown versions should be treated as prone
     * to the Forge bug. Disable if causes unexpected problems.
     */
    private boolean blockUnknownVersions = true;

    /**
     * Whether the config has been loaded with errors.
     */
    private boolean error = true;

    /**
     * Player mapped to brands.
     */
    private final Map<Player, String> brands = new WeakHashMap<>(8);

    @Override
    public void onEnable() {
        // Register the handler.
        this.getServer().getPluginManager().registerEvents(this, this);

        // Register brand channels.
        try {
            // Channel used before 1.13. (in 1.12 and below)
            this.getServer().getMessenger().registerIncomingPluginChannel(this, "MC|Brand", this);
        } catch (IllegalArgumentException ignored) {
            // 1.13 throws on 1.12 channel.
        }

        // Nag for J8.
        try {
            // Modules were added in J9.
            Class.forName("java.lang.Module");
        } catch (Throwable ignored) {
            // Nag about J8.
            if (!Boolean.getBoolean("forgelegalizerverifier.shut.up")) {
                this.getLogger().warning("Java 8 is outdated. Please, update to a more recent Java version. This is just a warning, the ForgeLegalizerVerifier will work anyway.");
            }
        }

        // Channel used in 1.13.
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", this);

        // Register the BungeeCord channel.
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // Load the config.
        this.loadConfigSafe();
    }

    @Override
    public void onDisable() {
        // Mark config as unloaded.
        this.error = true;

        // Unregister the BungeeCord channel.
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");

        // Unregister brand channels.
        try {
            // Channel used before 1.13. (in 1.12 and below)
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "MC|Brand", this);
        } catch (IllegalArgumentException ignored) {
            // 1.13 throws on 1.12 channel.
        }

        // Channel used in 1.13.
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "minecraft:brand", this);

        // Unregister the handler.
        HandlerList.unregisterAll((Listener) this);
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

            // Return success.
            this.error = false;
            return true;
        } catch (Throwable t) {
            // Log the error.
            this.getLogger().log(Level.SEVERE, "Unable to load ForgeLegalizerVerifier config.", t);

            // Return fail.
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
            Preconditions.checkState(config.contains("kickMessage"), "'kickMessage' is absent");
            this.kickMessage = ChatColor.translateAlternateColorCodes('&', String.join("\n",
                    config.getStringList("kickMessage"))).trim().intern();

            Preconditions.checkState(config.contains("notifyMessage"), "'notifyMessage' is absent");
            this.notifyMessage = ChatColor.translateAlternateColorCodes('&', String.join("\n",
                    config.getStringList("notifyMessage"))).trim().intern();

            Preconditions.checkState(config.contains("commands"), "'commands' is absent");
            this.commands = config.getStringList("commands").stream()
                    .map(String::intern)
                    .collect(Collectors.toList());

            Preconditions.checkState(config.contains("forgeBrand"), "'forgeBrand' is absent");
            String raw = config.getString("forgeBrand");
            Preconditions.checkNotNull(raw, "'forgeBrand' is invalid or null");
            try {
                this.forgeBrand = Pattern.compile(raw).asPredicate();
            } catch (Throwable th) {
                throw new IllegalArgumentException("Unable to parse 'forgeBrand': " + raw, th);
            }

            Preconditions.checkState(config.contains("forgeChannel"), "'forgeChannel' is absent");
            raw = config.getString("forgeChannel");
            Preconditions.checkNotNull(raw, "'forgeChannel' is invalid or null");
            try {
                this.forgeChannel = Pattern.compile(raw).asPredicate();
            } catch (Throwable th) {
                throw new IllegalArgumentException("Unable to parse 'forgeChannel': " + raw, th);
            }

            Preconditions.checkState(config.contains("blockUnknownVersions"), "'blockUnknownVersions' is absent");
            this.blockUnknownVersions = config.getBoolean("blockUnknownVersions", true);

            // Log the provider.
            this.getLogger().info("ForceLegalizerVerifier config loaded.");
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load ForgeLegalizerVerifier config.", t);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Reload the plugin if intended and has permission.
        if (args.length != 0 && "reload".equalsIgnoreCase(args[0]) && sender.hasPermission("forgelegalizerverifier.reload")) {
            // Reload the config.
            boolean result = this.loadConfigSafe();

            // Send the state.
            sender.sendMessage(result ? CONFIG_RELOAD_SUCCESS : CONFIG_RELOAD_FAIL);

            // Return success.
            return true;
        }

        // Send the info message.
        sender.sendMessage(INFO);

        // Return success.
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // Add reload argument.
        if (args.length == 1 && StringUtil.startsWithIgnoreCase("reload", args[0]) && sender.hasPermission("forgelegalizerverifier.reload")) {
            return Collections.singletonList("reload");
        }

        // Return no completions.
        return Collections.emptyList();
    }

    // Handles config errors.
    @EventHandler(ignoreCancelled = true)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        // Skip if plugin is working fine.
        if (!this.error) return;

        // Kick players.
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, INVALID_CONFIG_ERROR);
    }

    // Clears the brand on logout.
    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent event) {
        // Remove the brand on logout.
        this.brands.remove(event.getPlayer());
    }

    // Handles checking.
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Prepare to check.
        try (ByteArrayInputStream in = new ByteArrayInputStream(message)) {
            // Skip if quit.
            if (!player.isOnline()) return;

            // Read the String length.
            int length = readVarInt(in);

            // Don't allow invalid byte lengths.
            int available = in.available();
            if (length != available) {
                throw new IOException("Brand is not in the payload. Exploit attempt? (provided: " + length + " bytes; available: " + available + " bytes)");
            }

            // Read the brand.
            byte[] data = new byte[length];
            available = in.read(data);

            // Verify the read amount.
            if (length != available) {
                throw new IOException("Brand is not in the payload. Exploit attempt? (provided: " + length + " bytes; read: " + available + ")");
            }

            // Create the brand.
            String brand = new String(data, StandardCharsets.UTF_8).intern();

            // Skip if log out.
            if (!player.isOnline()) return;

            // Put the brand.
            String already = this.brands.putIfAbsent(player, brand);

            // Brand is already present.
            if (already != null) {
                // Skip if matches - don't verify brand either.
                if (already.equals(brand)) return;

                // Verify brand manipulation.
                // Custom clients sometimes send multiple brand packets,
                // but they shouldn't differ.
                // Skip if already sent one brand.
                throw new IOException("Brand mismatch. Exploit attempt? Received brand '" + brand + "', had different already '" + already + "'");
            }

            // Schedule the task next tick.
            this.getServer().getScheduler().runTaskLater(this, () -> {
                // Skip if logged out, version is not affected, has ForgeLegalizer, bypassed, or not a Forge.
                Set<String> channels = player.getListeningPluginChannels();
                if (!player.isOnline() || !this.checkVersion(player) ||
                        player.getListeningPluginChannels().contains(CHANNEL) ||
                        player.hasPermission("forgelegalizerverifier.bypass") ||
                        !this.forgeBrand.test(brand) && channels.stream().noneMatch(this.forgeChannel)) return;

                // Log it.
                this.getLogger().info(player.getName() + " (" + player.getUniqueId() + ") is using hack-alike-Forge.");

                // Kick the player if enabled.
                if (this.kickMessage != null && !this.kickMessage.isEmpty()) {
                    // Prepare the reason.
                    String reason = this.kickMessage
                            .replace("%name%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString());

                    // Kick from proxy.
                    this.kickFromBungee(player, reason);

                    // Kick from server.
                    player.kickPlayer(reason);
                }

                // Notify the admins if enabled.
                if (this.notifyMessage != null && !this.notifyMessage.isEmpty()) {
                    // Prepare the message.
                    String reason = this.notifyMessage
                            .replace("%name%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString());

                    // Write to everyone.
                    for (Player other : this.getServer().getOnlinePlayers()) {
                        // Skip if no permissions.
                        if (!other.hasPermission("forgelegalizerverifier.notify")) continue;

                        // Write.
                        other.sendMessage(reason);
                    }
                }

                // Execute other commands. (if any)
                if (this.commands != null) {
                    for (String command : this.commands) {
                        this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command
                                .replace("%name%", player.getName())
                                .replace("%uuid%", player.getUniqueId().toString()));
                    }
                }
            }, 1L);
        } catch (Throwable t) {
            // Log the error.
            this.getLogger().log(Level.SEVERE, "Unable to process player's " + player.getName() + " (" + player.getUniqueId() + ") brand payload for ForgeLegalizerVerifier.", t);

            // Kick the player.
            player.kickPlayer(BRAND_ERROR);

            // Rethrow the error.
            throw new RuntimeException("Unable to process player's " + player + " brand payload for ForgeLegalizerVerifier.", t);
        }
    }

    /**
     * Gets whether the player version is checked.
     *
     * @param player Target player
     * @return Whether the current version should be checker
     */
    private boolean checkVersion(Player player) {
        // Get player version, -1 if unknown or Via is not enabled.
        int version = this.getServer().getPluginManager().isPluginEnabled("ViaVersion") ? Via.getAPI().getPlayerVersion(player.getUniqueId()) : -1;

        // Special case for unknown versions.
        if (version == -1) return this.blockUnknownVersions;

        // Check the player version.
        return version >= MIN_VERSION && version <= MAX_VERSION;
    }

    /**
     * Tries to kick the player from the proxy using BungeeCord messaging.
     *
     * @param player Target player
     * @param reason Kick reason
     */
    private void kickFromBungee(Player player, String reason) {
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteOut)) {
            // Create the Bungee kick message.
            out.writeUTF("KickPlayer");
            out.writeUTF(player.getName());
            out.writeUTF(reason);

            // Flush it.
            player.sendPluginMessage(this, "BungeeCord", byteOut.toByteArray());
        } catch (Throwable t) {
            // Log the error.
            this.getLogger().log(Level.SEVERE, "Unable to kick " + player.getName() + " (" + player.getUniqueId() + ") via BungeeCord messaging.", t);
        }
    }

    @Override
    public String toString() {
        return "ForgeLegalizerVerifier{" +
                "kickMessage='" + this.kickMessage + '\'' +
                ", notifyMessage='" + this.notifyMessage + '\'' +
                ", commands=" + this.commands +
                ", forgeBrand=" + this.forgeBrand +
                ", forgeChannel=" + this.forgeChannel +
                ", blockUnknownVersions=" + this.blockUnknownVersions +
                ", error=" + this.error +
                ", brands=" + this.brands +
                '}';
    }

    /**
     * Reads the VarInt from the input.
     *
     * @param in Target input
     * @return Read VarInt
     * @throws IOException On I/O error or if VarInt is too large
     * @see <a href="https://wiki.vg/VarInt_And_VarLong">wiki.vg/VarInt_And_VarLong</a>
     */
    private static int readVarInt(InputStream in) throws IOException {
        int value = 0;
        int pos = 0;
        while (true) {
            int b = in.read();
            value |= (b & 0x7F) << pos;
            if ((b & 0x80) == 0) break;
            pos += 7;
            if (pos >= 32) {
                throw new IOException("Too large VarInt. Exploit attempt?");
            }
        }
        return value;
    }
}
