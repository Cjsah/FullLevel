package net.cjsah.fulllevel.gen;

import com.google.common.collect.Sets;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.cjsah.fulllevel.mixin.ChunkGeneratorAccessor;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.feature.FeatureCountTracker;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class FullLevelChunkGenerator extends NoiseBasedChunkGenerator {
    private final BlockState placedBlock;

    public FullLevelChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder, Block placedBlock) {
        this(biomeSource, holder, placedBlock.defaultBlockState());
    }

    public FullLevelChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> holder, BlockState placedBlock) {
        super(biomeSource, holder);
        this.placedBlock = placedBlock;
    }

    @Override
    protected abstract MapCodec<? extends ChunkGenerator> codec();

    protected static <T extends FullLevelChunkGenerator> MapCodec<T> makeCodec(BiFunction<BiomeSource, Holder<NoiseGeneratorSettings>, T> constructor) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(NoiseBasedChunkGenerator::generatorSettings)
        ).apply(instance, instance.stable(constructor)));
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
//        return CompletableFuture.completedFuture(chunk);
        NoiseSettings settings = this.generatorSettings().value().noiseSettings().clampToHeightAccessor(chunk.getHeightAccessorForGeneration());
        int minY = settings.minY();
        int y = Mth.floorDiv(minY, settings.getCellHeight());
        int height = Mth.floorDiv(settings.height(), settings.getCellHeight());
        if (height <= 0) return CompletableFuture.completedFuture(chunk);
        return CompletableFuture.supplyAsync(() -> {
            int max = chunk.getSectionIndex(height * settings.getCellHeight() - 1 + minY);
            int min = chunk.getSectionIndex(minY);
            Set<LevelChunkSection> set = Sets.newHashSet();

            for (int i = max; i >= min; i--) {
                LevelChunkSection section = chunk.getSection(i);
                section.acquire();
                set.add(section);
            }

            try {
                return this.doFillBlock(blender, structureManager, randomState, chunk, y, height);
            } finally {
                for (LevelChunkSection levelChunkSection3 : set) {
                    levelChunkSection3.release();
                }
            }

        }, Util.backgroundExecutor().forName("wgen_fill_noise"));
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        ChunkPos chunkPos = chunk.getPos();
        if (SharedConstants.debugVoidTerrain(chunkPos)) return;
        SectionPos sectionPos = SectionPos.of(chunkPos, level.getMinSectionY());
        BlockPos minChunkPos = sectionPos.origin();
        Registry<Structure> structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Map<Integer, List<Structure>> structuresPerStep = structureRegistry.stream().collect(Collectors.groupingBy(it -> it.step().ordinal()));
        List<FeatureSorter.StepFeatureData> featuresPerStep = ((ChunkGeneratorAccessor) this).getFeaturesPerStep().get();
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long seed = random.setDecorationSeed(level.getSeed(), minChunkPos.getX(), minChunkPos.getZ());

        int featureSize = featuresPerStep.size();
        try {
            int size = Math.max(GenerationStep.Decoration.values().length, featureSize);

            for (int step = 0; step < size; ++step) {
                int structureCounter = 0;
                if (structureManager.shouldGenerateStructures()) {
                    for (Structure structure : structuresPerStep.getOrDefault(step, Collections.emptyList())) {
                        random.setFeatureSeed(seed, structureCounter, step);
                        Supplier<String> supplier = () -> structureRegistry
                            .getResourceKey(structure)
                            .map(Object::toString)
                            .orElseGet(structure::toString);

                        try {
                            if (structure instanceof StrongholdStructure) {
                                level.setCurrentlyGenerating(supplier);
                                structureManager.startsForStructure(sectionPos, structure).forEach(structureStart -> {
                                    List<StructurePiece> pieces = structureStart.getPieces();

                                    Optional<StructurePiece> optional = pieces.stream()
                                        .filter(it -> it.getType() == StructurePieceType.STRONGHOLD_PORTAL_ROOM)
                                        .findFirst();

                                    if (optional.isPresent()) {
                                        StructurePiece piece = optional.get();

                                        BoundingBox writableBox = ChunkGeneratorAccessor.invokeGetWritableArea(chunk);
                                        BoundingBox totalBox = (pieces.getFirst()).getBoundingBox();
                                        BlockPos center = totalBox.getCenter();
                                        BlockPos bottomCenter = new BlockPos(center.getX(), totalBox.minY(), center.getZ());

                                        new StrongholdOnlyPortalRoom(piece)
                                            .postProcess(level, structureManager, this, random, writableBox, chunkPos, bottomCenter);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            CrashReport crashReport = CrashReport.forThrowable(e, "Feature placement");
                            crashReport.addCategory("Feature").setDetail("Description", supplier::get);
                            throw new ReportedException(crashReport);
                        }

                        ++structureCounter;
                    }
                }
            }

            level.setCurrentlyGenerating(null);
            if (SharedConstants.DEBUG_FEATURE_COUNT) {
                FeatureCountTracker.chunkDecorated(level.getLevel());
            }

        } catch (Exception e) {
            CrashReport crashReport3 = CrashReport.forThrowable(e, "Biome decoration");
            crashReport3
                .addCategory("Generation")
                .setDetail("CenterX", chunkPos.x)
                .setDetail("CenterZ", chunkPos.z)
                .setDetail("Decoration Seed", seed);
            throw new ReportedException(crashReport3);
        }

    }

    protected ChunkAccess doFillBlock(Blender blender, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess, int i, int j) {
        NoiseChunk chunk = chunkAccess.getOrCreateNoiseChunk(chunkAccessx -> this.createNoiseChunk(chunkAccessx, structureManager, blender, randomState));
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        ChunkPos chunkPos = chunkAccess.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        chunk.initializeForFirstCellX();
        int cellWidth = chunk.cellWidth();
        int cellHeight = chunk.cellHeight();
        int chunkWidth = 16 / cellWidth;
        int chunkHeight = 16 / cellWidth;

        for (int cx = 0; cx < chunkWidth; cx++) {
            chunk.advanceCellX(cx);

            for (int cz = 0; cz < chunkHeight; cz++) {
                int cs = chunkAccess.getSectionsCount() - 1;
                LevelChunkSection section = chunkAccess.getSection(cs);

                for (int cy = j - 1; cy >= 0; cy--) {
                    chunk.selectCellYZ(cy, cz);

                    for (int iy = cellHeight - 1; iy >= 0; iy--) {
                        int actY = (i + cy) * cellHeight + iy;
                        int y = actY & 15;
                        int ccs = chunkAccess.getSectionIndex(actY);
                        if (cs != ccs) {
                            cs = ccs;
                            section = chunkAccess.getSection(ccs);
                        }

                        double ucy = (double) iy / cellHeight;
                        chunk.updateForY(actY, ucy);

                        for (int xz = 0; xz < cellWidth; xz++) {
                            int actX = minX + cx * cellWidth + xz;
                            int x = actX & 15;
                            double ucx = (double) xz / cellWidth;
                            chunk.updateForX(actX, ucx);

                            for (int ab = 0; ab < cellWidth; ab++) {
                                int actZ = minZ + cz * cellWidth + ab;
                                int z = actZ & 15;
                                double ucz = (double) ab / cellWidth;
                                chunk.updateForZ(actZ, ucz);

                                BlockState state = this.getPlacedBlock(actX, actY, actZ);

                                section.setBlockState(x, y, z, state, false);
                                heightmap.update(x, actY, z, state);
                                heightmap2.update(x, actY, z, state);
                            }
                        }
                    }
                }
            }

            chunk.swapSlices();
        }

        chunk.stopInterpolation();
        return chunkAccess;
    }

    protected BlockState getPlacedBlock(int x, int y, int z) {
        return this.placedBlock;
    }

}