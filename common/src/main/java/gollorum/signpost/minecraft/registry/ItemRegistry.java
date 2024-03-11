package gollorum.signpost.minecraft.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import gollorum.signpost.Signpost;
import gollorum.signpost.minecraft.block.ModelWaystone;
import gollorum.signpost.minecraft.block.PostBlock;
import gollorum.signpost.minecraft.block.WaystoneBlock;
import gollorum.signpost.minecraft.block.WaystoneGeneratorBlock;
import gollorum.signpost.minecraft.items.Brush;
import gollorum.signpost.minecraft.items.GenerationWand;
import gollorum.signpost.minecraft.items.PostItem;
import gollorum.signpost.minecraft.items.WaystoneItem;
import gollorum.signpost.minecraft.items.Wrench;
import gollorum.signpost.utils.Tuple;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.List;

import static gollorum.signpost.Signpost.MOD_ID;

public class ItemRegistry {

    private static final DeferredRegister<Item> REGISTER = DeferredRegister.create(MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> WAYSTONE_ITEM = REGISTER.register(
            new ResourceLocation(MOD_ID, WaystoneBlock.REGISTRY_NAME),
            () -> new WaystoneItem(WaystoneBlock.getInstance(), new Item.Properties()));

    public static final List<Tuple<ModelWaystone.Variant, RegistrySupplier<Item>>> ModelWaystoneItems =
        ModelWaystone.variants.stream().map(ItemRegistry::registerModelWaystoneItem).toList();

    public static final List<RegistrySupplier<Item>> POSTS_ITEMS =
        PostBlock.AllVariants.stream().map(ItemRegistry::registerPostItem).toList();

    public static final RegistrySupplier<Item> WaystoneGeneratorItem = REGISTER.register(
            new ResourceLocation(MOD_ID, WaystoneGeneratorBlock.REGISTRY_NAME),
            () -> new BlockItem(BlockRegistry.WaystoneGenerator.get(), new Item.Properties()
                    .arch$tab(CreativeModeTabRegistry.SIGNPOST_TAB)));

    public static final RegistrySupplier<Item> WRENCH = REGISTER.register(new ResourceLocation(MOD_ID, Wrench.registryName), Wrench::new);

    public static final RegistrySupplier<Item> BRUSH = REGISTER.register(new ResourceLocation(MOD_ID, Brush.registryName), Brush::new);

    public static final RegistrySupplier<Item> GENERATION_WAND = REGISTER.register(new ResourceLocation(MOD_ID, GenerationWand.registryName), GenerationWand::new);

    private static RegistrySupplier<Item> registerPostItem(PostBlock.Variant postVariant){
        return REGISTER.register(
            new ResourceLocation(MOD_ID, postVariant.registryName),
            () -> new PostItem(postVariant.getBlock(), new Item.Properties()));
    }

    private static Tuple<ModelWaystone.Variant, RegistrySupplier<Item>> registerModelWaystoneItem(ModelWaystone.Variant variant){
        return new Tuple<>(variant, REGISTER.register(
            new ResourceLocation(MOD_ID, variant.registryName),
            () -> new WaystoneItem(variant.getBlock(), new Item.Properties())));
    }

    public static void register(){
        REGISTER.register();
    }
}
