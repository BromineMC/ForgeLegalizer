/*
 * MIT License
 *
 * Copyright (c) 2023 VidTu
 * Copyright (c) 2023-2024 BromineMC
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

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Collections;
import java.util.Locale;

/**
 * ForgeLegalizerVerifier command.
 *
 * @author threefusii
 */
final class FLVCommand extends Command implements TabExecutor {
    /**
     * Success configuration reload.
     */
    private static final BaseComponent CONFIG_RELOAD_SUCCESS = TextComponent.fromLegacy("The configuration of §eForgeLegalizer§r has been §areloaded§r.");

    /**
     * Failure configuration reload.
     */
    private static final BaseComponent CONFIG_RELOAD_FAIL = TextComponent.fromLegacy("Unable to §creload§r the configuration of §eForgeLegalizer§r.");

    /**
     * Information message.
     */
    private static final BaseComponent INFO = TextComponent.fromLegacy(String.format("§eForgeLegalizerVerifier-Bungee§r version §6%s§r.\nAuthors: §c%s§r.\nModrinth: §ahttps://modrinth.com/mod/forgelegalizer§r\nGitHub: §bhttps://github.com/BromineMC/ForgeLegalizer",
            ForgeLegalizerVerifier.class.getPackage().getSpecificationVersion(),
            ForgeLegalizerVerifier.class.getPackage().getSpecificationVendor()));

    /**
     * Plugin instance.
     */
    private final ForgeLegalizerVerifier plugin;

    /**
     * Creates a new command.
     *
     * @param plugin Plugin instance
     */
    FLVCommand(ForgeLegalizerVerifier plugin) {
        super("forgelegalizerverifier", null, "flv", "flvbungee");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Reload the plugin if intended and has permission.
        if (args.length != 0 && "reload".equalsIgnoreCase(args[0]) && sender.hasPermission("forgelegalizerverifier.reload")) {
            // Reload the config.
            boolean result = this.plugin.loadConfigSafe();

            // Send the state.
            sender.sendMessage(result ? CONFIG_RELOAD_SUCCESS : CONFIG_RELOAD_FAIL);

            // Return.
            return;
        }

        // Send the info message.
        sender.sendMessage(INFO);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        // Add reload argument.
        if (args.length == 1 && "reload".toLowerCase(Locale.ROOT).startsWith(args[0].toLowerCase(Locale.ROOT)) && sender.hasPermission("forgelegalizerverifier.reload")) {
            return Collections.singletonList("reload");
        }

        // Return no completions.
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "FLVCommand{}";
    }
}
