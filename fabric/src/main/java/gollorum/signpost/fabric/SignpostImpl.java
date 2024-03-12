package gollorum.signpost.fabric;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import gollorum.signpost.Signpost;
import gollorum.signpost.events.BlockEntityRenderersEvents;
import gollorum.signpost.minecraft.config.Config;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;

public class SignpostImpl implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        Signpost.logger().error("Initializing Signpost common");
        ForgeConfigRegistry.INSTANCE.register(Signpost.MOD_ID, ModConfig.Type.SERVER, Config.ServerConfig);
        ForgeConfigRegistry.INSTANCE.register(Signpost.MOD_ID, ModConfig.Type.CLIENT, Config.ClientConfig);
        ForgeConfigRegistry.INSTANCE.register(Signpost.MOD_ID, ModConfig.Type.COMMON, Config.CommonConfig);
        Signpost.init();
    }

    @Override
    public void onInitializeClient() {
        Signpost.logger().error("Initializing Signpost client");
        Signpost.initClient();
        BlockEntityRenderersEvents.REGISTER.invoker().register();
    }

    public static RenderingUtil getRenderingUtil() {
        return new FabricRenderingUtil();
    }

    public static boolean isModLoaded(String modid) {
        return FabricLoader.getInstance().isModLoaded(modid);
    }

    public static Font getFont(ItemStack itemStack) {
        return Minecraft.getInstance().font;
    }

    public static Collection<TextureAtlasSprite> getFluidTextures(
            Function<ResourceLocation, TextureAtlasSprite> atlasSpriteGetter,
            Fluid fluid
    ) {
        var ret = new HashSet<TextureAtlasSprite>();
        var renderer = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        for (var state : fluid.getStateDefinition().getPossibleStates()) {
            ret.addAll(Arrays.asList(renderer.getFluidSprites(null, null, state)));
        }
        return ret;
    }

    public static int getFluidColorAt(Fluid fluid, BlockAndTintGetter level, BlockPos pos) {
        var renderer = FluidRenderHandlerRegistry.INSTANCE.get(fluid);
        return renderer.getFluidColor(level, pos, fluid.defaultFluidState());
    }
}
