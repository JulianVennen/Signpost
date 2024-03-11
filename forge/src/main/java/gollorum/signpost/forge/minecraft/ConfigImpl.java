package gollorum.signpost.forge.minecraft;

import gollorum.signpost.minecraft.config.Config;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigImpl {
	public static void register() {
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.ServerConfig);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CommonConfig);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.ClientConfig);
	}
}
