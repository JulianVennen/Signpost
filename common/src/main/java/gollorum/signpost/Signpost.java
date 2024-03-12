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
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.function.Function;
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

    /**
     * Get the font for the given item stack.
     * This appears to not be possible on Fabric so the default font is used.
     * @param itemStack The item stack.
     * @return The font.
     */
    @ExpectPlatform
    public static Font getFont(ItemStack itemStack) {
        throw new AssertionError();
    }

    /**
     * Get a collection of texture atlas sprites for the given fluid.
     * @param atlasSpriteGetter A function to get a texture atlas sprite from a resource location.
     * @param fluid The fluid.
     * @return The collection of texture atlas sprites.
     */
    @ExpectPlatform
    public static Collection<TextureAtlasSprite> getFluidTextures(
            Function<ResourceLocation, TextureAtlasSprite> atlasSpriteGetter,
            Fluid fluid
    ) {
        throw new AssertionError();
    }

    /**
     * Get the fluid color at the given position.
     * @param fluid The fluid.
     * @param level The block and tint getter.
     * @param pos The position.
     * @return The fluid color.
     */
    @ExpectPlatform
    public static int getFluidColorAt(Fluid fluid, BlockAndTintGetter level, BlockPos pos) {
        throw new AssertionError();
    }
}
