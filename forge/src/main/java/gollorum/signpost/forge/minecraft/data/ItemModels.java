package gollorum.signpost.forge.minecraft.data;

import gollorum.signpost.forge.Signpost;
import gollorum.signpost.forge.minecraft.block.ModelWaystone;
import gollorum.signpost.forge.minecraft.block.WaystoneBlock;
import gollorum.signpost.forge.minecraft.block.WaystoneGeneratorBlock;
import gollorum.signpost.forge.minecraft.items.Brush;
import gollorum.signpost.forge.minecraft.items.GenerationWand;
import gollorum.signpost.forge.minecraft.items.Wrench;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.Map;

public class ItemModels extends ItemModelProvider {

    private final BlockModels blockModelProvider;

    public ItemModels(BlockModels blockModelProvider, PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Signpost.MOD_ID, existingFileHelper);
        this.blockModelProvider = blockModelProvider;
    }

    @Override
    protected void registerModels() {

        PostModel.Item.registerModels(this::getBuilder, blockModelProvider.postModelProvider);
        getBuilder(WaystoneBlock.REGISTRY_NAME).parent(blockModelProvider.waystoneModelProvider.waystoneModel);
        for(Map.Entry<ModelWaystone.Variant, ModelFile> variant : blockModelProvider.waystoneModelProvider.variantModels.entrySet()) {
            getBuilder(variant.getKey().registryName).parent(variant.getValue());
        }

        getBuilder(Wrench.registryName)
            .parent(new ModelFile.ExistingModelFile(new ResourceLocation("item/handheld"), existingFileHelper))
            .texture("layer0", new ResourceLocation(Signpost.MOD_ID, "item/tool"));
        getBuilder(Brush.registryName)
            .parent(new ModelFile.ExistingModelFile(new ResourceLocation("item/handheld"), existingFileHelper))
            .texture("layer0", new ResourceLocation(Signpost.MOD_ID, "item/brush"));

        getBuilder(WaystoneGeneratorBlock.REGISTRY_NAME).parent(blockModelProvider.generatorModelProvider.generatorModel);
        getBuilder(GenerationWand.registryName)
            .parent(new ModelFile.ExistingModelFile(new ResourceLocation("item/handheld"), existingFileHelper))
            .texture("layer0", new ResourceLocation(Signpost.MOD_ID, "item/generation_wand"));
    }

}
