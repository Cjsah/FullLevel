package net.cjsah.fulllevel.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.cjsah.fulllevel.registry.ModWorldPresets;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mixin(WorldCreationUiState.class)
public class WorldCreationUiStateMixin {
    @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
    @WrapOperation(method = "updatePresetLists", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElseGet(Ljava/util/function/Supplier;)Ljava/lang/Object;"))
    private <T> T reSortPresets(Optional<T> instance, Supplier<? extends T> supplier, Operation<T> original) {
        List<WorldCreationUiState.WorldTypeEntry> presets = (List<WorldCreationUiState.WorldTypeEntry>) original.call(instance, supplier);
        List<WorldCreationUiState.WorldTypeEntry> result = new ArrayList<>(presets.size());

        for (WorldCreationUiState.WorldTypeEntry preset : presets) {
            if (preset.preset().is(ModWorldPresets.FLUID)) {
                if (result.isEmpty()) result.add(preset);
                else result.add(1, preset);
            } else {
                result.add(preset);
            }
        }
        return (T) result;
    }

}

