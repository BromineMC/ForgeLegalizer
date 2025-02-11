/*
 * MIT License
 *
 * Copyright (c) 2023-2025 VidTu
 * Copyright (c) 2023-2025 BromineMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.brominemc.forgelegalizerverifier;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
public final class ForgeLegalizerVerifier extends Plugin implements Listener {
    /**
     * Invalid configuration player kick.
     */
    private static final BaseComponent INVALID_CONFIG_ERROR = TextComponent.fromLegacy("\n§c> ForgeLegalizerVerifier§r\n\nThe configuration of §eForgeLegalizerVerifier§r plugin didn't load correctly. Please, §bnotify the admins§r. You §c§nwon't be able to join the server§r until they fix the error.\n");

    /**
     * Register decoding error.
     */
    private static final BaseComponent REGISTER_ERROR = TextComponent.fromLegacy("\n§c> ForgeLegalizerVerifier§r\n\nThe §eForgeLegalizerVerifier§r plugin couldn't decode your client channels. For security purposes, you §c§nhave been kicked§r.\n");

    /**
     * Brand decoding error.
     */
    private static final BaseComponent BRAND_ERROR = TextComponent.fromLegacy("\n§c> ForgeLegalizerVerifier§r\n\nThe §eForgeLegalizerVerifier§r plugin couldn't decode your client brand. For security purposes, you §c§nhave been kicked§r.\n");

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
     * Whether the config has been loaded with errors.
     */
    private boolean error = true;

    /**
     * Registered command.
     */
    @SuppressWarnings("ThisEscapedInObjectConstruction") // <- It's fine.
    private final Command command = new FLVCommand(this);

    /**
     * Player mapped to brands.
     */
    private final Map<ProxiedPlayer, Set<String>> channels = new WeakHashMap<>(4);

    /**
     * Player mapped to brands.
     */
    private final Map<ProxiedPlayer, String> brands = new WeakHashMap<>(4);

    @Override
    public void onEnable() {
        // Register the handler.
        this.getProxy().getPluginManager().registerListener(this, this);

        // Register the command.
        this.getProxy().getPluginManager().registerCommand(this, this.command);

        // Load the config.
        this.loadConfigSafe();
    }

    @Override
    public void onDisable() {
        // Mark config as unloaded.
        this.error = true;

        // Unregister the command.
        this.getProxy().getPluginManager().unregisterCommand(this.command);

        // Unregister the handler.
        this.getProxy().getPluginManager().unregisterListener(this);
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
            File file = new File(this.getDataFolder(), "config.yml");
            if (!file.isFile()) {
                Files.createParentDirs(file);
                URL in = ForgeLegalizerVerifier.class.getResource("/config.yml");
                Preconditions.checkNotNull(in, "Internal default config file is not found or is not accessible.");
                Resources.asByteSource(in).copyTo(Files.asByteSink(file));
            }

            // Reload the config.
            ConfigurationProvider provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
            Configuration config = provider.load(file);

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

            // Log the provider.
            this.getLogger().info("ForceLegalizerVerifier config loaded.");
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load ForgeLegalizerVerifier config.", t);
        }
    }

    // Handles config errors.
    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        // Skip if plugin is working fine.
        if (!this.error) return;

        // Kick players.
        event.setReason(INVALID_CONFIG_ERROR);
        event.setCancelled(true);
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        // Get the player.
        ProxiedPlayer player = event.getPlayer();

        // Skip if logged out or version is not applicable.
        int version = player.getPendingConnection().getVersion();
        if (!player.isConnected() || version < MIN_VERSION || version > MAX_VERSION) return;

        // Add channels.
        this.channels.put(player, new HashSet<>(4));
    }

    // Remove from sets.
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        // Remove from sets.
        ProxiedPlayer player = event.getPlayer();
        this.channels.remove(player);
        this.brands.remove(player);
    }

    @EventHandler
    public void onChannel(PluginMessageEvent event) {
        // Skip if not sent by a player.
        Connection connection = event.getSender();
        if (!(connection instanceof ProxiedPlayer)) return;
        ProxiedPlayer player = (ProxiedPlayer) connection;

        // Process packet.
        switch (event.getTag()) {
            // Register channel.
            case "REGISTER":
            case "minecraft:register": {
                this.onRegister(player, event.getData());
                break;
            }

            // Brand channel.
            case "MC|Brand":
            case "minecraft:brand": {
                this.onBrand(player, event.getData());
                break;
            }
        }
    }

    /**
     * Handles the player {@code register} packet.
     *
     * @param player Target player
     * @param data   Player register payload
     */
    private void onRegister(ProxiedPlayer player, byte[] data) {
        try {
            // Skip if logged out, version is not applicable, or no longer needed.
            int version = player.getPendingConnection().getVersion();
            Set<String> channels = this.channels.get(player);
            if (!player.isConnected() || version < MIN_VERSION || version > MAX_VERSION || channels == null) return;

            // Convert to string.
            String value = new String(data, StandardCharsets.UTF_8);

            // Split channels at null characters.
            String[] split = value.split("\0");

            // Add all.
            channels.addAll(Arrays.stream(split)
                    .map(String::intern)
                    .collect(Collectors.toSet()));
        } catch (Throwable t) {
            // Log the error.
            this.getLogger().log(Level.SEVERE, "Unable to process player's " + player.getName() + " (" + player.getUniqueId() + ") register payload for ForgeLegalizerVerifier.", t);

            // Kick the player.
            player.disconnect(REGISTER_ERROR);

            // Rethrow the error.
            throw new RuntimeException("Unable to process player's " + player + " register payload for ForgeLegalizerVerifier.", t);
        }
    }

    /**
     * Handles the player {@code brand} packet.
     *
     * @param player Target player
     * @param data   Player brand payload
     */
    private void onBrand(ProxiedPlayer player, byte[] data) {
        // Process brand.
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            // Remove the channels.
            Set<String> channels = MoreObjects.firstNonNull(this.channels.remove(player), Collections.emptySet());

            // Skip if quit.
            if (!player.isConnected()) return;

            // Read the String length.
            int length = readVarInt(in);

            // Don't allow invalid byte lengths.
            int available = in.available();
            if (length != available) {
                throw new IOException("Brand is not in the payload. Exploit attempt? (provided: " + length + " bytes; available: " + available + " bytes)");
            }

            // Read the brand.
            byte[] val = new byte[length];
            available = in.read(val);

            // Verify the read amount.
            if (length != available) {
                throw new IOException("Brand is not in the payload. Exploit attempt? (provided: " + length + " bytes; read: " + available + ")");
            }

            // Create the brand.
            String brand = new String(val, StandardCharsets.UTF_8).intern();

            // Skip if log out.
            if (!player.isConnected()) return;

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
            // Skip if logged out, version is not affected, has ForgeLegalizer, bypassed, or not a Forge.
            int version = player.getPendingConnection().getVersion();
            if (!player.isConnected() || version < MIN_VERSION || version > MAX_VERSION ||
                    channels.contains(CHANNEL) || player.hasPermission("forgelegalizerverifier.bypass") ||
                    !this.forgeBrand.test(brand) && channels.stream().noneMatch(this.forgeChannel)) return;

            // Log it.
            this.getLogger().info(player.getName() + " (" + player.getUniqueId() + ") is using hack-alike-Forge.");

            // Kick the player if enabled.
            if (this.kickMessage != null && !this.kickMessage.isEmpty()) {
                // Prepare the reason.
                Server server = player.getServer();
                BaseComponent reason = TextComponent.fromLegacy(this.kickMessage
                        .replace("%name%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .replace("%server%", server == null ? "null" : server.getInfo().getName()));

                // Kick from server.
                player.disconnect(reason);
            }

            // Notify the admins if enabled.
            if (this.notifyMessage != null && !this.notifyMessage.isEmpty()) {
                // Prepare the message.
                Server server = player.getServer();
                BaseComponent reason = TextComponent.fromLegacy(this.notifyMessage
                        .replace("%name%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .replace("%server%", server == null ? "null" : server.getInfo().getName()));

                // Write to everyone.
                for (ProxiedPlayer other : this.getProxy().getPlayers()) {
                    // Skip if no permissions.
                    if (!other.hasPermission("forgelegalizerverifier.notify")) continue;

                    // Write.
                    other.sendMessage(reason);
                }
            }

            // Execute other commands. (if any)
            if (this.commands != null) {
                Server server = player.getServer();
                for (String command : this.commands) {
                    this.getProxy().getPluginManager().dispatchCommand(this.getProxy().getConsole(), command
                            .replace("%name%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString())
                            .replace("%server%", server == null ? "null" : server.getInfo().getName()));
                }
            }
        } catch (Throwable t) {
            // Log the error.
            this.getLogger().log(Level.SEVERE, "Unable to process player's " + player.getName() + " (" + player.getUniqueId() + ") brand payload for ForgeLegalizerVerifier.", t);

            // Kick the player.
            player.disconnect(BRAND_ERROR);

            // Rethrow the error.
            throw new RuntimeException("Unable to process player's " + player + " brand payload for ForgeLegalizerVerifier.", t);
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
                ", error=" + this.error +
                ", command=" + this.command +
                ", channels=" + this.channels +
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
