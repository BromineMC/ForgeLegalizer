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
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow @Final Minecraft minecraft;

    @Inject(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", shift = At.Shift.BEFORE))
    public void forgelegalizer$pick$pop(float tickDelta, CallbackInfo ci) {
        if (!(minecraft.hitResult instanceof EntityHitResult result)) return;
        Entity camera = minecraft.getCameraEntity();
        if (camera == null || minecraft.player == null || minecraft.gameMode == null || minecraft.gameMode.hasFarPickRange()) return;
        Vec3 eyePosition = camera.getEyePosition(tickDelta);
        Vec3 viewVector = camera.getViewVector(1.0F);
        Vec3 targetLocation = result.getLocation();
        double distance = eyePosition.distanceToSqr(targetLocation);
        double range = minecraft.player.getAttackRange();
        if (distance > range * range) {
            minecraft.hitResult = BlockHitResult.miss(targetLocation, Direction.getNearest(viewVector.x, viewVector.y, viewVector.z), BlockPos.containing(targetLocation));
            minecraft.crosshairPickEntity = null;
        }
    }
}
