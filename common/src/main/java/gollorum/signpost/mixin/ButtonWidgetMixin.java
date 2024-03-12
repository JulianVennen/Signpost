package gollorum.signpost.mixin;

import gollorum.signpost.minecraft.ButtonExtensions;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractButton.class)
public abstract class ButtonWidgetMixin extends AbstractWidget implements ButtonExtensions {
    @Unique
    private @Nullable Integer signpost$color = null;

    public ButtonWidgetMixin(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    public @Nullable Integer signpost$getColor() {
        return signpost$color;
    }

    public void signpost$setColor(@Nullable Integer color) {
        signpost$color = color;
    }

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/AbstractButton;renderString(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    private void onRenderButton(AbstractButton instance, GuiGraphics arg, Font arg2, int i) {
        if (signpost$color != null) {
            i = signpost$color | Mth.ceil(this.alpha * 255.0f) << 24;
        }
        instance.renderString(arg, arg2, i);
    }
}
