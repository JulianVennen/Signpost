package gollorum.signpost;

import com.google.common.base.Suppliers;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.registry.registries.RegistrarManager;
import gollorum.signpost.compat.Compat;
import gollorum.signpost.compat.ExternalWaystoneLibrary;
import gollorum.signpost.events.BlockEntityRenderersEvents;
import gollorum.signpost.events.JoinServerEvent;
import gollorum.signpost.minecraft.block.BlockEventListener;
import gollorum.signpost.minecraft.block.tiles.PostTile;
import gollorum.signpost.minecraft.registry.BlockRegistry;
import gollorum.signpost.minecraft.registry.CreativeModeTabRegistry;
import gollorum.signpost.minecraft.registry.ItemRegistry;
import gollorum.signpost.minecraft.registry.LootItemConditionRegistry;
import gollorum.signpost.minecraft.registry.LootProviderRegistry;
import gollorum.signpost.minecraft.registry.RecipeRegistry;
import gollorum.signpost.minecraft.registry.TileEntityRegistry;
import gollorum.signpost.minecraft.rendering.PostRenderer;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import gollorum.signpost.minecraft.worldgen.JigsawDeserializers;
import gollorum.signpost.minecraft.worldgen.WaystoneDiscoveryEventListener;
import gollorum.signpost.networking.PacketHandler;
import gollorum.signpost.utils.Delay;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class Signpost {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "signpost";
    public static final Supplier<RegistrarManager> REGISTRAR_MANAGER = Suppliers.memoize(() -> RegistrarManager.get(MOD_ID));

    public static void initClient() {
        Delay.registerClient();
        BlockEntityRenderersEvents.REGISTER.register(() ->
                BlockEntityRenderers.register(PostTile.getBlockEntityType(), PostRenderer::new));
    }

    public static void init() {
        Compat.register();
        Delay.registerServer();
        ServerHolder.register();
        new BlockEventListener();
        new WaystoneDiscoveryEventListener();

        LifecycleEvent.SETUP.register(() -> {
            PacketHandler.initialize();
            PacketHandler.register(new JoinServerEvent());
            ExternalWaystoneLibrary.initialize();
            WaystoneLibrary.registerNetworkPackets();
            JigsawDeserializers.register();
//            if(isModLoaded(Compat.AntiqueAtlasId))
//                AntiqueAtlasAdapter.registerNetworkPacket();
        });

        LifecycleEvent.SERVER_LEVEL_LOAD.register(world -> {
            if (world.dimension().equals(Level.OVERWORLD)) {
                if (!WaystoneLibrary.getInstance().hasStorageBeenSetup())
                    WaystoneLibrary.getInstance().setupStorage(world);
                if (!BlockRestrictions.getInstance().hasStorageBeenSetup())
                    BlockRestrictions.getInstance().setupStorage(world);
            }
        });

        BlockRegistry.register();
        CreativeModeTabRegistry.register();
        ItemRegistry.register();
        LootItemConditionRegistry.register();
        LootProviderRegistry.register();
        RecipeRegistry.register();
        TileEntityRegistry.register();
    }

    public static Logger logger() {
        return LOGGER;
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modid) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static RenderingUtil getRenderingUtil() {
        throw new AssertionError();
    }
}
