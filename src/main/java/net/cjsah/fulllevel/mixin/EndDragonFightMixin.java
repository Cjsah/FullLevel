package net.cjsah.fulllevel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cjsah.fulllevel.gen.FullLevelChunkGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndDragonFight.class)
public class EndDragonFightMixin {

    @Shadow
    @Final
    private ServerLevel level;

    @WrapOperation(method = "spawnExitPortal", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getHeightmapPos(Lnet/minecraft/world/level/levelgen/Heightmap$Types;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos modifyExitPortalPos(ServerLevel instance, Heightmap.Types types, BlockPos blockPos, Operation<BlockPos> original) {
        BlockPos pos = original.call(instance, types, blockPos);

        if (this.level.getChunkSource().getGenerator() instanceof FullLevelChunkGenerator) {
            pos = pos.atY(64);
        }

        return pos;
    }

}
