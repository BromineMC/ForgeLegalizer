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

package ru.vidtu.forgelegalizer.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin that fixes Forge reach.
 *
 * @author VidTu
 * @author threefusii
 */
@Mixin(GameRenderer.class)
public final class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    private GameRendererMixin() {
        throw new AssertionError("No instances.");
    }

    // Injects into pick method to prevent hitting entities too far away.
    @Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", shift = At.Shift.BEFORE))
    public void forgelegalizer$pick$pop(float tickDelta, CallbackInfo ci) {
        // Skip if not enabled.
        if (!(this.minecraft.hitResult instanceof EntityHitResult result)) return;

        // Skip if not in world or if in creative.
        Entity camera = this.minecraft.getCameraEntity();
        if (camera == null || this.minecraft.player == null || this.minecraft.gameMode == null
                || this.minecraft.gameMode.hasFarPickRange()) return;

        // Get the distance and maximum range.
        Vec3 eyePosition = camera.getEyePosition(tickDelta);
        Vec3 viewVector = camera.getViewVector(1.0F);
        Vec3 targetLocation = result.getLocation();
        double distance = eyePosition.distanceToSqr(targetLocation);
        double range = this.minecraft.player.getAttackRange();

        // Check if range is too big.
        if (distance > range * range) {
            // Remove the hit.
            this.minecraft.hitResult = BlockHitResult.miss(targetLocation, Direction.getNearest(viewVector.x, viewVector.y, viewVector.z), BlockPos.containing(targetLocation));
            this.minecraft.crosshairPickEntity = null;
        }
    }
}
