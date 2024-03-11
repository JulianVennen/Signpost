package gollorum.signpost.minecraft.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import gollorum.signpost.Signpost;
import gollorum.signpost.minecraft.crafting.CutWaystoneRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import static gollorum.signpost.Signpost.MOD_ID;

public class RecipeRegistry {

	private static final DeferredRegister<RecipeSerializer<?>> Register = DeferredRegister.create(Signpost.MOD_ID, Registries.RECIPE_SERIALIZER);

	public static final RegistrySupplier<CutWaystoneRecipe.Serializer> CutWaystoneSerializer =
		Register.register(
			CutWaystoneRecipe.RegistryName,
			CutWaystoneRecipe.Serializer::new
		);

	public static void register() {
		Register.register();
	}
}
