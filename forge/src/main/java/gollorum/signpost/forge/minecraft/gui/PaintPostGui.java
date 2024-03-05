package gollorum.signpost.forge.minecraft.gui;

import gollorum.signpost.forge.blockpartdata.types.PostBlockPart;
import gollorum.signpost.forge.minecraft.block.tiles.PostTile;
import gollorum.signpost.forge.minecraft.utils.Texture;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class PaintPostGui extends PaintBlockPartGui<PostBlockPart> {

    public PaintPostGui(PostTile tile, PostBlockPart post, UUID identifier) {
        super(tile, post, new PostBlockPart(post.getTexture()), identifier, post.getTexture());
    }

	public static void display(PostTile tile, PostBlockPart post, UUID identifier) {
        Minecraft.getInstance().setScreen(new PaintPostGui(tile, post, identifier));
	}

    @Override
    protected void setTexture(PostBlockPart part, Texture texture) {
        part.setTexture(texture);
    }

}
