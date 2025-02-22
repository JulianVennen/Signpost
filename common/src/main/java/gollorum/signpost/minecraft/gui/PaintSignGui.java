package gollorum.signpost.minecraft.gui;

import gollorum.signpost.blockpartdata.types.SignBlockPart;
import gollorum.signpost.minecraft.block.tiles.PostTile;
import gollorum.signpost.minecraft.gui.utils.Point;
import gollorum.signpost.minecraft.gui.utils.Rect;
import gollorum.signpost.minecraft.utils.LangKeys;
import gollorum.signpost.minecraft.utils.Texture;
import gollorum.signpost.utils.Tint;
import gollorum.signpost.utils.Tuple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PaintSignGui<T extends SignBlockPart<T>> extends PaintBlockPartGui<T> {

    private final Tuple<TextureAtlasSprite, Optional<Tint>> oldMainSprite;
    private final Tuple<TextureAtlasSprite, Optional<Tint>> oldSecSprite;

    private boolean isTargetingMainTexture;

    public PaintSignGui(PostTile tile, T sign, UUID identifier) {
        super(tile, sign, sign.copy(), identifier, sign.getMainTexture());
        oldMainSprite = oldSprite;
        oldSecSprite = Tuple.of(spriteFrom(sign.getSecondaryTexture().location()), sign.getSecondaryTexture().tint());
        isTargetingMainTexture = true;
    }

    public static <T extends SignBlockPart<T>> void display(PostTile tile, T sign, UUID identifier) {
        Minecraft.getInstance().setScreen(new PaintSignGui<>(tile, sign, identifier));
    }

    @Override
    protected void init() {
        super.init();
        Rect button1Rect = new Rect(new Point(width / 4, height / 4), 125, 20, Rect.XAlignment.Center, Rect.YAlignment.Center);
        AtomicReference<Button> b1 = new AtomicReference<>();
        AtomicReference<Button> b2 = new AtomicReference<>();
        b1.set(new Button.Builder(
            Component.translatable(LangKeys.mainTex),
            b -> {
                isTargetingMainTexture = true;
                oldSprite = oldMainSprite;
                clearSelection();
                b1.get().active = false;
                b2.get().active = true;
            }
        ).bounds(
            button1Rect.point.x, button1Rect.point.y,
            button1Rect.width, button1Rect.height
        ).build());
        Rect button2Rect = new Rect(button1Rect.max().withY(y -> y + 5), 125, 20, Rect.XAlignment.Right, Rect.YAlignment.Top);
        b2.set(new Button.Builder(
            Component.translatable(LangKeys.secondaryTex),
            b -> {
                isTargetingMainTexture = false;
                oldSprite = oldSecSprite;
                clearSelection();
                b1.get().active = true;
                b2.get().active = false;
            }
        ).bounds(
            button2Rect.point.x, button2Rect.point.y,
            button2Rect.width, button2Rect.height
        ).build());
        b1.get().active = !isTargetingMainTexture;
        b2.get().active = isTargetingMainTexture;
        addRenderableWidget(b1.get());
        addRenderableWidget(b2.get());
    }

    @Override
    protected void setTexture(T sign, Texture texture) {
        if(isTargetingMainTexture) sign.setMainTexture(texture);
        else sign.setSecondaryTexture(texture);
    }

}
