package gollorum.signpost.minecraft.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import gollorum.signpost.Signpost;
import gollorum.signpost.minecraft.storage.loot.RegisteredWaystoneLootNbtProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;

import static gollorum.signpost.Signpost.MOD_ID;

public class LootProviderRegistry {

    private static final DeferredRegister<LootNbtProviderType> Register = DeferredRegister.create(Signpost.MOD_ID, Registries.LOOT_NBT_PROVIDER_TYPE);

    public static final RegistrySupplier<LootNbtProviderType> RegisteredWaystone =
        Register.register("waystone", RegisteredWaystoneLootNbtProvider::createProviderType);

    public static void register(){
        Register.register();
    }

}
