package net.cjsah.fulllevel.gen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;

public class StrongholdOnlyPortalRoom extends StrongholdPieces.StrongholdPiece {

    protected StrongholdOnlyPortalRoom(StructurePiece piece) {
        super(StructurePieceType.STRONGHOLD_PORTAL_ROOM, piece.getGenDepth(), piece.getBoundingBox());
        this.setOrientation(piece.getOrientation());
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox box, ChunkPos chunkPos, BlockPos blockPos) {
        BlockState frameNorth = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
        BlockState frameSouth = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
        BlockState frameEast = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
        BlockState frameWest = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
        boolean genPortal = true;
        boolean[] hasEye = new boolean[12];

        for (int l = 0; l < hasEye.length; l++) {
            hasEye[l] = randomSource.nextFloat() > 0.9F;
            genPortal &= hasEye[l];
        }

        this.placeBlock(worldGenLevel, frameNorth.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[0]), 4, 3, 8, box);
        this.placeBlock(worldGenLevel, frameNorth.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[1]), 5, 3, 8, box);
        this.placeBlock(worldGenLevel, frameNorth.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[2]), 6, 3, 8, box);
        this.placeBlock(worldGenLevel, frameSouth.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[3]), 4, 3, 12, box);
        this.placeBlock(worldGenLevel, frameSouth.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[4]), 5, 3, 12, box);
        this.placeBlock(worldGenLevel, frameSouth.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[5]), 6, 3, 12, box);
        this.placeBlock(worldGenLevel, frameEast.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[6]), 3, 3, 9, box);
        this.placeBlock(worldGenLevel, frameEast.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[7]), 3, 3, 10, box);
        this.placeBlock(worldGenLevel, frameEast.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[8]), 3, 3, 11, box);
        this.placeBlock(worldGenLevel, frameWest.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[9]), 7, 3, 9, box);
        this.placeBlock(worldGenLevel, frameWest.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[10]), 7, 3, 10, box);
        this.placeBlock(worldGenLevel, frameWest.setValue(EndPortalFrameBlock.HAS_EYE, hasEye[11]), 7, 3, 11, box);
        if (genPortal) {
            BlockState portal = Blocks.END_PORTAL.defaultBlockState();
            this.placeBlock(worldGenLevel, portal, 4, 3, 9, box);
            this.placeBlock(worldGenLevel, portal, 5, 3, 9, box);
            this.placeBlock(worldGenLevel, portal, 6, 3, 9, box);
            this.placeBlock(worldGenLevel, portal, 4, 3, 10, box);
            this.placeBlock(worldGenLevel, portal, 5, 3, 10, box);
            this.placeBlock(worldGenLevel, portal, 6, 3, 10, box);
            this.placeBlock(worldGenLevel, portal, 4, 3, 11, box);
            this.placeBlock(worldGenLevel, portal, 5, 3, 11, box);
            this.placeBlock(worldGenLevel, portal, 6, 3, 11, box);
        }
    }
}
