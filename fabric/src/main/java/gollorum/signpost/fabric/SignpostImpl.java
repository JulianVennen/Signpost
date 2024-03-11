package gollorum.signpost.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import gollorum.signpost.Signpost;
import gollorum.signpost.minecraft.config.Config;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraftforge.fml.config.ModConfig;

public class SignpostImpl implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        ForgeConfigRegistry.INSTANCE.register(Signpost.MOD_ID, ModConfig.Type.SERVER, Config.ServerConfig);
        ForgeConfigRegistry.INSTANCE.register(Signpost.MOD_ID, ModConfig.Type.CLIENT, Config.ClientConfig);
        ForgeConfigRegistry.INSTANCE.register(Signpost.MOD_ID, ModConfig.Type.COMMON, Config.CommonConfig);
        Signpost.init();
    }

    @Override
    public void onInitializeClient() {
        Signpost.initClient();
    }

    public static RenderingUtil getRenderingUtil() {
        return new FabricRenderingUtil();
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }
}
