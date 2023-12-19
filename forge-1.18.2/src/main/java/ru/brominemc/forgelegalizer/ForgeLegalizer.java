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

package ru.brominemc.forgelegalizer;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;

/**
 * Main mod class.
 *
 * @author VidTu
 * @author threefusii
 */
@Mod("forgelegalizer")
public final class ForgeLegalizer {
    /**
     * Mod channel version, will change sometimes.
     */
    private static final String CHANNEL_VERSION = "v1";

    /**
     * Mod channel name.
     */
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation("forgelegalizer", "v1");

    /**
     * Mod initializer.
     */
    public ForgeLegalizer() {
        // Create the channel to identify everyone.
        NetworkRegistry.newEventChannel(CHANNEL_NAME, () -> CHANNEL_VERSION, ignored -> true, version -> version.equals(NetworkRegistry.ACCEPTVANILLA) || version.equals(CHANNEL_VERSION));

        // Register the checker.
        MinecraftForge.EVENT_BUS.register(this);
    }
}
