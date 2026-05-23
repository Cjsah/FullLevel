package net.cjsah.fulllevel.gen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class WaterChunkGenerator extends FullLevelChunkGenerator {
    public static final MapCodec<WaterChunkGenerator> CODEC = makeCodec(WaterChunkGenerator::new);
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public WaterChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder) {
        super(biomeSource, holder, Blocks.WATER);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    protected BlockState getPlacedBlock(int x, int y, int z) {
        return y == 319 ? AIR : super.getPlacedBlock(x, y, z);
    }
}