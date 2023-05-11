package ru.vidtu.forgelegalizer;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkRegistry;

import java.util.Objects;

/**
 * Main mod class.
 *
 * @author VidTu
 */
@Mod("forgelegalizer")
public class ForgeLegalizer {
    private static final String VERSION = Objects.toString(ForgeLegalizer.class.getPackage().getImplementationVersion(), "UNKNOWN");
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation("forgelegalizer", "v1");

    public ForgeLegalizer() {
        NetworkRegistry.newEventChannel(CHANNEL_NAME, () -> VERSION, ver -> true, ver -> true);
    }
}
