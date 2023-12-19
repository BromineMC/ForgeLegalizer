/*
 * MIT License
 *
 * Copyright (c) 2023 VidTu
 * Copyright (c) 2023 threefusii
 * Copyright (c) 2023 BromineMC
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

import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChannelRegisterEvent;
import com.velocitypowered.api.event.player.PlayerClientBrandEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.ServerInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Main plugin class.
 *
 * @author threefusii
 */
public final class ForgeLegalizerVerifier {
    /**
     * Invalid configuration player kick.
     */
    private static final Component INVALID_CONFIG_ERROR = Component.newline()
            .append(Component.text("> ForgeLegalizerVerifier", NamedTextColor.RED))
            .appendNewline().appendNewline()
            .append(Component.text("The configuration of "))
            .append(Component.text("ForgeLegalizerVerifier", NamedTextColor.YELLOW))
            .append(Component.text(" plugin didn't load correctly. Please, "))
            .append(Component.text("notify the admins", NamedTextColor.AQUA))
            .append(Component.text(". You "))
            .append(Component.text("won't be able to join the server", NamedTextColor.RED, TextDecoration.UNDERLINED))
            .append(Component.text(" until they fix the error."))
            .appendNewline()
            .compact();

    /**
     * Brand decoding error.
     */
    private static final Component BRAND_ERROR = Component.newline()
            .append(Component.text("> ForgeLegalizerVerifier", NamedTextColor.RED))
            .appendNewline().appendNewline()
            .append(Component.text("The "))
            .append(Component.text("ForgeLegalizerVerifier", NamedTextColor.YELLOW))
            .append(Component.text(" plugin couldn't decode your client brand. For security purposes, you "))
            .append(Component.text("have been kicked", NamedTextColor.RED, TextDecoration.UNDERLINED))
            .append(Component.text("."))
            .appendNewline()
            .compact();

    /**
     * Success configuration reload.
     */
    private static final Component CONFIG_RELOAD_SUCCESS = Component.text("The configuration of ")
            .append(Component.text("ForgeLegalizer"))
            .append(Component.text(" has been "))
            .append(Component.text("reloaded", NamedTextColor.GREEN))
            .append(Component.text("."))
            .compact();

    /**
     * Failure configuration reload.
     */
    private static final Component CONFIG_RELOAD_FAIL = Component.text("Unable to ")
            .append(Component.text("reload", NamedTextColor.RED))
            .append(Component.text(" the configuration of "))
            .append(Component.text("ForgeLegalizer", NamedTextColor.YELLOW))
            .append(Component.text("."))
            .compact();

    /**
     * Information message.
     */
    private static final Component INFO = Component.empty()
            .append(Component.text("ForgeLegalizerVerifier-Velocity", NamedTextColor.YELLOW))
            .append(Component.text(" version "))
            .append(Component.text(Objects.toString(ForgeLegalizerVerifier.class.getPackage().getSpecificationVersion()), NamedTextColor.GOLD))
            .appendNewline()
            .append(Component.text("Authors: "))
            .append(Component.text(Objects.toString(ForgeLegalizerVerifier.class.getPackage().getSpecificationVendor()), NamedTextColor.RED))
            .append(Component.text("."))
            .appendNewline()
            .append(Component.text("Modrinth: "))
            .append(Component.text("https://modrinth.com/mod/forgelegalizer", NamedTextColor.GREEN)
                    .clickEvent(ClickEvent.openUrl("https://modrinth.com/mod/forgelegalizer")))
            .appendNewline()
            .append(Component.text("GitHub: "))
            .append(Component.text("https://github.com/BromineMC/ForgeLegalizer", NamedTextColor.AQUA)
                    .clickEvent(ClickEvent.openUrl("https://github.com/BromineMC/ForgeLegalizer")))
            .compact();

    /**
     * ForgeLegalizer plugin channel.
     */
    private static final String CHANNEL = "forgelegalizer:v1";

    /**
     * Minimum affected protocol version {@code 1.18.2}. (inclusive)
     */
    private static final ProtocolVersion MIN_VERSION = ProtocolVersion.MINECRAFT_1_18_2;

    /**
     * Maximum affected protocol version {@code 1.19.4}. (inclusive)
     */
    private static final ProtocolVersion MAX_VERSION = ProtocolVersion.MINECRAFT_1_19_4;

    /**
     * Server instance.
     */
    private final ProxyServer server;

    /**
     * Logger instance.
     */
    private final Logger logger;

    /**
     * Plugin config directory.
     */
    private final Path dataDirectory;

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
    private Component kickMessage = null;

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
    private Component notifyMessage = null;

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
     * Player mapped to channels.
     */
    private final Map<Player, Set<String>> channels = new WeakHashMap<>();

    /**
     * Player mapped to brands.
     */
    private final Map<Player, String> brands = new WeakHashMap<>();

    @ApiStatus.Internal
    @Inject
    public ForgeLegalizerVerifier(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onInit(ProxyInitializeEvent event) {
        // Create the command.
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder.<CommandSource>literal("forgelegalizerverifier")

                // Main command.
                .executes(context -> {
                    // Get the source.
                    CommandSource source = context.getSource();

                    // Send the info message.
                    source.sendMessage(INFO);

                    // Return success.
                    return Command.SINGLE_SUCCESS;
                })

                // Reload command.
                .then(LiteralArgumentBuilder.<CommandSource>literal("reload")

                        // Permission.
                        .requires(source -> source.hasPermission("forgelegalizerverifier.reload"))

                        // Reload command.
                        .executes(context -> {
                            // Get the source.
                            CommandSource source = context.getSource();

                            // Reload the config.
                            boolean result = this.loadConfigSafe();

                            // Send the state.
                            source.sendMessage(result ? CONFIG_RELOAD_SUCCESS : CONFIG_RELOAD_FAIL);

                            // Return success.
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();

        // Register the command and aliases.
        this.server.getCommandManager().register(new BrigadierCommand(node));
        this.server.getCommandManager().register(new BrigadierCommand(LiteralArgumentBuilder.<CommandSource>literal("flv").redirect(node)));
        this.server.getCommandManager().register(new BrigadierCommand(LiteralArgumentBuilder.<CommandSource>literal("flvvelocity").redirect(node)));

        // Load the config.
        this.loadConfigSafe();
    }

    @Subscribe
    public void onShutdown(ProxyShutdownEvent event) {
        // Mark config as unloaded.
        this.error = true;

        // Unregister the command and aliases.
        this.server.getCommandManager().unregister("forgelegalizerverifier");
        this.server.getCommandManager().unregister("flv");
        this.server.getCommandManager().unregister("flvvelocity");
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
            this.logger.error("Unable to load ForgeLegalizerVerifier config.", t);

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
    @SuppressWarnings("UnstableApiUsage")
    public void loadConfig() {
        try {
            // Save the default config if deleted.
            Path path = this.dataDirectory.resolve("config.yml");
            if (!Files.isRegularFile(path)) {
                Files.createDirectories(this.dataDirectory);
                URL in = ForgeLegalizerVerifier.class.getResource("/config.yml");
                Preconditions.checkNotNull(in, "Internal default config file is not found or is not accessible.");
                Resources.asByteSource(in).copyTo(MoreFiles.asByteSink(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.SYNC, StandardOpenOption.DSYNC, StandardOpenOption.WRITE));
            }

            // Reload the config.
            YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(path).build();
            ConfigurationNode config = loader.load();

            // Get the values.
            ConfigurationNode node = config.getNode("kickMessage");
            Preconditions.checkState(!node.isVirtual() || !node.isList(), "'kickMessage' is absent");
            this.kickMessage = Component.join(JoinConfiguration.newlines(), node.getList(TypeToken.of(String.class)).stream()
                    .map(MiniMessage.miniMessage()::deserialize)
                    .toArray(Component[]::new)).compact();

            node = config.getNode("notifyMessage");
            Preconditions.checkState(!node.isVirtual() || !node.isList(), "'notifyMessage' is absent");
            this.notifyMessage = Component.join(JoinConfiguration.newlines(), node.getList(TypeToken.of(String.class)).stream()
                    .map(MiniMessage.miniMessage()::deserialize)
                    .toArray(Component[]::new)).compact();

            node = config.getNode("commands");
            Preconditions.checkState(!node.isVirtual() || !node.isList(), "'commands' is absent");
            this.commands = node.getList(TypeToken.of(String.class)).stream()
                    .map(String::intern)
                    .collect(Collectors.toList());

            node = config.getNode("forgeBrand");
            Preconditions.checkState(!node.isVirtual(), "'forgeBrand' is absent");
            String raw = node.getString();
            Preconditions.checkNotNull(raw, "'forgeBrand' is invalid or null");
            try {
                this.forgeBrand = Pattern.compile(raw).asPredicate();
            } catch (Throwable th) {
                throw new IllegalArgumentException("Unable to parse 'forgeBrand': " + raw, th);
            }

            node = config.getNode("forgeChannel");
            Preconditions.checkState(!node.isVirtual(), "'forgeChannel' is absent");
            raw = node.getString();
            Preconditions.checkNotNull(raw, "'forgeChannel' is invalid or null");
            try {
                this.forgeChannel = Pattern.compile(raw).asPredicate();
            } catch (Throwable th) {
                throw new IllegalArgumentException("Unable to parse 'forgeChannel': " + raw, th);
            }

            // Log the provider.
            this.logger.info("ForceLegalizerVerifier config loaded.");
        } catch (Throwable t) {
            // Rethrow.
            throw new RuntimeException("Unable to load ForgeLegalizerVerifier config.", t);
        }
    }

    // Handles config errors.
    @Subscribe(order = PostOrder.EARLY)
    public void onPreLogin(PreLoginEvent event) {
        // Skip if plugin is working fine.
        if (!this.error) return;

        // Kick players.
        event.setResult(PreLoginEvent.PreLoginComponentResult.denied(INVALID_CONFIG_ERROR));
    }

    @Subscribe(order = PostOrder.LATE)
    public void onPostLogin(PostLoginEvent event) {
        // Get the player.
        Player player = event.getPlayer();

        // Skip if logged out or version is not applicable.
        ProtocolVersion version = player.getProtocolVersion();
        if (!player.isActive() || version.compareTo(MIN_VERSION) < 0 || version.compareTo(MAX_VERSION) > 0) return;

        // Add channels.
        this.channels.put(player, new HashSet<>());
    }

    // Remove from sets.
    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        // Remove from sets.
        Player player = event.getPlayer();
        this.channels.remove(player);
        this.brands.remove(player);
    }

    // Add channels.
    @Subscribe
    public void onRegister(PlayerChannelRegisterEvent event) {
        // Get the player.
        Player player = event.getPlayer();

        // Skip if logged out, version is not applicable, or no longer needed.
        ProtocolVersion version = player.getProtocolVersion();
        Set<String> channels = this.channels.get(player);
        if (!player.isActive() || version.compareTo(MIN_VERSION) < 0 ||
                version.compareTo(MAX_VERSION) > 0 || channels == null) return;

        // Add all.
        channels.addAll(event.getChannels().stream()
                .map(ChannelIdentifier::getId)
                .map(String::intern)
                .collect(Collectors.toSet()));
    }

    // Handle brand packet.
    @Subscribe
    public void onBrand(PlayerClientBrandEvent event) {
        // Get the player.
        Player player = event.getPlayer();

        // Remove the channels.
        Set<String> channels = Objects.requireNonNullElse(this.channels.remove(player), Set.of());

        // Put the brand.
        String brand = event.getBrand().intern();
        String already = this.brands.putIfAbsent(player, brand);
        // Brand is already present.
        if (already != null) {
            // Skip if matches - don't verify brand either.
            if (already.equals(brand)) return;

            // Verify brand manipulation.
            // Custom clients sometimes send multiple brand packets,
            // but they shouldn't differ.
            // Skip if already sent one brand.
            this.logger.error("{} ({}) is suspected of brand mismatch. Exploit attempt? Received brand '{}', had different already '{}'", player, player.getUniqueId(), brand, already);
            player.disconnect(BRAND_ERROR);
            return;
        }

        // Skip if logged out, version is not affected, has ForgeLegalizer, bypassed, or not a Forge.
        ProtocolVersion version = player.getProtocolVersion();
        if (!player.isActive() || version.compareTo(MIN_VERSION) < 0 || version.compareTo(MAX_VERSION) > 0 ||
                channels.contains(CHANNEL) || player.hasPermission("forgelegalizerverifier.bypass") ||
                !this.forgeBrand.test(brand) && channels.stream().noneMatch(this.forgeChannel)) return;

        // Log it.
        this.logger.info("{} ({}) is using hack-alike-Forge.", player, player.getUniqueId());

        // Kick the player if enabled.
        if (this.kickMessage != null && !this.kickMessage.equals(Component.empty())) {
            // Prepare the reason.
            Component reason = this.kickMessage
                    .replaceText(TextReplacementConfig.builder().matchLiteral("%name%")
                            .replacement(player.getUsername()).build())
                    .replaceText(TextReplacementConfig.builder().matchLiteral("%uuid%")
                            .replacement(player.getUniqueId().toString()).build())
                    .replaceText(TextReplacementConfig.builder().matchLiteral("%server%")
                            .replacement(player.getCurrentServer()
                                    .map(ServerConnection::getServerInfo)
                                    .map(ServerInfo::getName)
                                    .orElse("null")).build())
                    .compact();

            // Kick from server.
            player.disconnect(reason);
        }

        // Notify the admins if enabled.
        if (this.notifyMessage != null && !this.notifyMessage.equals(Component.empty())) {
            // Prepare the reason.
            Component reason = this.notifyMessage
                    .replaceText(TextReplacementConfig.builder().matchLiteral("%name%")
                            .replacement(player.getUsername()).build())
                    .replaceText(TextReplacementConfig.builder().matchLiteral("%uuid%")
                            .replacement(player.getUniqueId().toString()).build())
                    .replaceText(TextReplacementConfig.builder().matchLiteral("%server%")
                            .replacement(player.getCurrentServer()
                                    .map(ServerConnection::getServerInfo)
                                    .map(ServerInfo::getName)
                                    .orElse("null")).build())
                    .compact();

            // Write to everyone.
            for (Player other : this.server.getAllPlayers()) {
                // Skip if no permissions.
                if (!other.hasPermission("forgelegalizerverifier.notify")) continue;

                // Write.
                other.sendMessage(reason);
            }
        }

        // Execute other commands. (if any)
        if (this.commands != null) {
            for (String command : this.commands) {
                this.server.getCommandManager().executeAsync(this.server.getConsoleCommandSource(), command
                        .replace("%name%", player.getUsername())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .replace("%server%", player.getCurrentServer()
                                .map(ServerConnection::getServerInfo)
                                .map(ServerInfo::getName)
                                .orElse("null")));
            }
        }
    }
}
