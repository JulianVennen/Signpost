package gollorum.signpost.forge;

import dev.architectury.platform.forge.EventBuses;
import gollorum.signpost.Signpost;
import gollorum.signpost.compat.Compat;
import gollorum.signpost.forge.compat.waystones.WaystonesAdapter;
import gollorum.signpost.forge.event.ChunkWatchEventCaller;
import gollorum.signpost.forge.minecraft.ConfigImpl;
import gollorum.signpost.forge.minecraft.data.DataGeneration;
import gollorum.signpost.forge.minecraft.ForgeRenderingUtil;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModList;

@Mod(Signpost.MOD_ID)
public class SignpostImpl {
    public SignpostImpl() {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        forgeBus.register(new ChunkWatchEventCaller());
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
}
