package gollorum.signpost.forge;

import dev.architectury.platform.forge.EventBuses;
import gollorum.signpost.Signpost;
import gollorum.signpost.compat.Compat;
import gollorum.signpost.forge.compat.waystones.WaystonesAdapter;
import gollorum.signpost.forge.event.ForgeBusEventCaller;
import gollorum.signpost.forge.event.ModBusEventCaller;
import gollorum.signpost.forge.minecraft.ConfigImpl;
import gollorum.signpost.forge.minecraft.ForgeRenderingUtil;
import gollorum.signpost.forge.minecraft.data.DataGeneration;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

@Mod(Signpost.MOD_ID)
public class SignpostImpl {
    public SignpostImpl() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        forgeBus.register(new ForgeBusEventCaller());
        modBus.register(new ModBusEventCaller());
        EventBuses.registerModEventBus(Signpost.MOD_ID, modBus);
        ConfigImpl.register();

        Signpost.init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> Signpost::initClient);

        if (Signpost.isModLoaded(Compat.WaystonesId))
            WaystonesAdapter.register();

        // TODO: I also need to run Datagen in fabric, right?
        DataGeneration.register(modBus);
    }

    public static RenderingUtil getRenderingUtil() {
        return new ForgeRenderingUtil();
    }

    public static boolean isModLoaded(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static Font getFont(ItemStack itemStack) {
        Font font = IClientItemExtensions.of(itemStack).getFont(itemStack, IClientItemExtensions.FontContext.ITEM_COUNT);
        return font != null ? font : Minecraft.getInstance().font;
    }

    public static Collection<TextureAtlasSprite> getFluidTextures(
            Function<ResourceLocation, TextureAtlasSprite> atlasSpriteGetter,
            Fluid fluid
    ) {
        List<TextureAtlasSprite> ret = new ArrayList<>(3);
        var typeExtensions = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation loc;
        if((loc = typeExtensions.getFlowingTexture()) != null)
            ret.add(atlasSpriteGetter.apply(loc));
        if((loc = typeExtensions.getOverlayTexture()) != null)
            ret.add(atlasSpriteGetter.apply(loc));
        if((loc = typeExtensions.getStillTexture()) != null)
            ret.add(atlasSpriteGetter.apply(loc));
        return ret;
    }

    public static int getFluidColorAt(Fluid fluid, BlockAndTintGetter level, BlockPos pos) {
        return IClientFluidTypeExtensions.of(fluid).getTintColor(fluid.defaultFluidState(), level, pos);
    }
}
