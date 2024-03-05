package gollorum.signpost.forge.minecraft.data;

import gollorum.signpost.forge.minecraft.block.PostBlock;
import gollorum.signpost.forge.minecraft.block.WaystoneBlock;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.ItemLike;

import java.util.function.Consumer;

import static net.minecraft.data.recipes.RecipeProvider.has;

public class PostRecipe {

    public static void build(Consumer<FinishedRecipe> consumer) {
        for(PostBlock.Variant variant : PostBlock.AllVariants) {
            postBuilder(variant.getBlock(), variant.type).save(consumer);
        }
    }

    private static ShapedRecipeBuilder postBuilder(ItemLike block, PostBlock.ModelType type) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
            .define('s', type.signIngredient.get())
            .define('b', type.baseIngredient.get())
            .pattern("s")
            .pattern("s")
            .pattern("b")
            .unlockedBy("has_sign", has(ItemTags.SIGNS))
            .unlockedBy("has_signpost", has(gollorum.signpost.forge.minecraft.data.ItemTags.SignpostTag))
            .unlockedBy("has_waystone", has(WaystoneBlock.getInstance()))
            .group("Signpost");
    }

}
