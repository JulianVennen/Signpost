package gollorum.signpost.minecraft.items;

import dev.architectury.registry.CreativeTabRegistry;
import gollorum.signpost.minecraft.block.PostBlock;
import gollorum.signpost.minecraft.block.tiles.PostTile;
import gollorum.signpost.minecraft.registry.CreativeModeTabRegistry;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public final class GenerationWand extends Item {

    public static final String registryName = "generation_wand";

    public GenerationWand() {
        super(new Properties().arch$tab(CreativeModeTabRegistry.SIGNPOST_TAB));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getLevel().getBlockEntity(context.getClickedPos(), PostTile.getBlockEntityType())
                .map(tile -> PostBlock.onActivate(tile, context.getLevel(), context.getPlayer(), context.getHand()))
                .orElse(InteractionResult.PASS);
    }
}
