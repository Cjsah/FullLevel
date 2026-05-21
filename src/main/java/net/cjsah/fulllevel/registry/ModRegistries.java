package net.cjsah.fulllevel.registry;

import net.cjsah.fulllevel.FullLevel;
import net.cjsah.fulllevel.gen.WaterChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class ModRegistries {
    public static void registerBuiltIn() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, FullLevel.of("water"), WaterChunkGenerator.CODEC);
    }
}
