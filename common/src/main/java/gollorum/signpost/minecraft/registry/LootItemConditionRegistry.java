package gollorum.signpost.minecraft.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import gollorum.signpost.Signpost;
import gollorum.signpost.minecraft.storage.loot.PermissionCheck;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

import static gollorum.signpost.Signpost.MOD_ID;

public class LootItemConditionRegistry {

    private static final DeferredRegister<LootItemConditionType> Register = DeferredRegister.create(Signpost.MOD_ID, Registries.LOOT_CONDITION_TYPE);

    public static final RegistrySupplier<LootItemConditionType> permissionCheck =
        Register.register(new ResourceLocation(MOD_ID, "permission_check"), PermissionCheck::createConditionType);

    public static void register() {
        Register.register();
    }
}
