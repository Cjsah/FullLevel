package net.cjsah.fulllevel.registry;

import net.cjsah.fulllevel.FullLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import org.jetbrains.annotations.NotNull;

public class ModWorldPresets {
    public static final ResourceKey<WorldPreset> FLUID = preset("fluid");

    private static @NotNull ResourceKey<WorldPreset> preset(String path) {
        return ResourceKey.create(Registries.WORLD_PRESET, FullLevel.of(path));
    }
}
