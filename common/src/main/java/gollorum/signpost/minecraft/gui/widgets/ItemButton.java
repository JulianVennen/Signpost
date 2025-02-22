package gollorum.signpost.minecraft.gui.widgets;

import gollorum.signpost.minecraft.gui.utils.Rect;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public class ItemButton extends Button {

    public static final int width = 20;
    public static final int height = 20;

    private static final int itemModelWidth = 16;
    private static final int itemModelHeight = 16;

    public ItemStack stack;
    private final ItemRenderer itemRenderer;
    private final Font font;

    public ItemButton(
        int x, int y, Rect.XAlignment xAlignment, Rect.YAlignment yAlignment, ItemStack stack,
        Consumer<ItemButton> pressedAction, ItemRenderer itemRenderer, Font font
    ) {
        super(
            Rect.xCoordinateFor(x, width, xAlignment),
            Rect.yCoordinateFor(y, height, yAlignment),
            width, height,
            Component.literal(""),
            b -> pressedAction.accept((ItemButton)b),
            Button.DEFAULT_NARRATION
        );
        this.stack = stack;
        this.itemRenderer = itemRenderer;
        this.font = font;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(graphics, mouseX, mouseY, partialTicks);

        int xTL = getX() + (width - itemModelWidth) / 2;
        int yTL = getY() + (height - itemModelHeight) / 2;
        graphics.renderItem(stack, xTL, yTL);
        graphics.renderItemDecorations(font, stack, xTL, yTL, null);
    }
}
