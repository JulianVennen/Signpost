package gollorum.signpost.minecraft.items;

import gollorum.signpost.minecraft.block.PostBlock;
import gollorum.signpost.minecraft.rendering.PostItemRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;

import java.util.function.Consumer;

public class PostItem extends BlockItem {

    public PostItem(PostBlock block, Properties properties) {
        super(block, properties);
    }
}
