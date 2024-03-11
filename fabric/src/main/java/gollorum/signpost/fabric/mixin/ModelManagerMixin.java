package gollorum.signpost.fabric.mixin;

import gollorum.signpost.fabric.FabricRenderingUtil;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.lang3.NotImplementedException;
import org.intellij.lang.annotations.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelManager.class)
public abstract class ModelManagerMixin {
    @Inject(method = "apply", at = @At("HEAD"))
    private void loadModel(ModelManager.ReloadState reloadState, ProfilerFiller profilerFiller, CallbackInfo ci) {
        FabricRenderingUtil.modelBakery = reloadState.modelBakery();
        FabricRenderingUtil.modelBaker = FabricRenderingUtil.modelBakery.new ModelBakerImpl(
                (l, r) -> {
                    throw new NotImplementedException();
                },
                null
        );
    }
}
