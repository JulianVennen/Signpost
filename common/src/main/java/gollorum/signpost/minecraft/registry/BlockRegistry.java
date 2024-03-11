package gollorum.signpost.minecraft.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import gollorum.signpost.minecraft.block.ModelWaystone;
import gollorum.signpost.minecraft.block.PostBlock;
import gollorum.signpost.minecraft.block.WaystoneGeneratorBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

import java.util.List;

import static gollorum.signpost.Signpost.MOD_ID;

public class BlockRegistry {
    public static final DeferredRegister<Block> Register = DeferredRegister.create(MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> WaystoneBlock =
        Register.register(gollorum.signpost.minecraft.block.WaystoneBlock.REGISTRY_NAME,
            gollorum.signpost.minecraft.block.WaystoneBlock::createInstance);

    public static final List<RegistrySupplier<ModelWaystone>> ModelWaystoneBlocks =
        ModelWaystone.variants.stream().map(BlockRegistry::registerModelWaystone).toList();

    public static final List<RegistrySupplier<PostBlock>> PostBlocks =
        PostBlock.AllVariants.stream().map(BlockRegistry::registerPostBlock).toList();

    private static RegistrySupplier<PostBlock> registerPostBlock(PostBlock.Variant variant) {
        return Register.register(variant.registryName, variant::createBlock);
    }

    private static RegistrySupplier<ModelWaystone> registerModelWaystone(ModelWaystone.Variant variant) {
        return Register.register(variant.registryName, variant::createBlock);
    }

    public static final RegistrySupplier<WaystoneGeneratorBlock> WaystoneGenerator =
        Register.register(WaystoneGeneratorBlock.REGISTRY_NAME, WaystoneGeneratorBlock::new);

    public static void register(){
        Register.register();
    }
}