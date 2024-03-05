package gollorum.signpost.forge.compat;

import com.telepathicgrunt.repurposedstructures.modinit.RSConditionsRegistry;
import gollorum.signpost.forge.minecraft.config.Config;

import java.util.function.Supplier;

public class RepurposedStructuresAdapter {

    public static void register() {
        RSConditionsRegistry.RS_JSON_CONDITIONS_REGISTRY.register(
            "signpost_config",
            RepurposedStructuresAdapter::villageGenerationCheck
        );
    }

    private static Supplier<Boolean> villageGenerationCheck() {
        return () -> !Config.ServerConfig.isLoaded() || Config.Server.worldGen.isVillageGenerationEnabled();
    }

}
