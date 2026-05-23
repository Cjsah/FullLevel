package net.cjsah.fulllevel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cjsah.fulllevel.gen.FullLevelChunkGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.PortalForcer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PortalForcer.class)
public class PortalForcerMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @WrapOperation(method = "createPortal", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 0))
    private int modifyPortalMaxY(int a, int b, Operation<Integer> original) {
        if (this.level.getChunkSource().getGenerator() instanceof FullLevelChunkGenerator) {
            b += 10;
        }

        return original.call(a, b);
    }
}
