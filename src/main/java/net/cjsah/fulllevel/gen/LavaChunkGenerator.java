package net.cjsah.fulllevel.gen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class LavaChunkGenerator extends FullLevelChunkGenerator {
    public static final MapCodec<LavaChunkGenerator> CODEC = makeCodec(LavaChunkGenerator::new);

    public LavaChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
        super(biomeSource, holder, Blocks.LAVA);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}