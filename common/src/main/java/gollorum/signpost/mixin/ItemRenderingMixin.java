package gollorum.signpost.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import gollorum.signpost.minecraft.items.PostItem;
import gollorum.signpost.minecraft.rendering.PostItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityWithoutLevelRenderer.class)
public abstract class ItemRenderingMixin {
    @Inject(method = "renderByItem", at = @At("HEAD"), cancellable = true)
    private void renderByItem(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, CallbackInfo ci) {
        Item item = itemStack.getItem();
        if (item instanceof PostItem) {
            PostItemRenderer.getInstance().renderByItem(itemStack, itemDisplayContext, poseStack, multiBufferSource, i, j);
            ci.cancel();
        }
    }
}
