package gollorum.signpost.minecraft.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import gollorum.signpost.Signpost;
import gollorum.signpost.minecraft.block.tiles.PostTile;
import gollorum.signpost.minecraft.block.tiles.WaystoneGeneratorEntity;
import gollorum.signpost.minecraft.block.tiles.WaystoneTile;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

import static gollorum.signpost.Signpost.MOD_ID;

public class TileEntityRegistry {

    private static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    private static final RegistrySupplier<BlockEntityType<PostTile>> POST =
        REGISTER.register(PostTile.REGISTRY_NAME, PostTile::createType);

    private static final RegistrySupplier<BlockEntityType<WaystoneTile>> WAYSTONE =
        REGISTER.register(WaystoneTile.REGISTRY_NAME, WaystoneTile::createType);

    private static final RegistrySupplier<BlockEntityType<WaystoneGeneratorEntity>> WaystoneGenerator =
        REGISTER.register(WaystoneGeneratorEntity.REGISTRY_NAME, WaystoneGeneratorEntity::createType);

    public static void register(){
        REGISTER.register();
    }
}