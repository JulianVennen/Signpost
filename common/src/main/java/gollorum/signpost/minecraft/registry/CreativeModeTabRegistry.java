package gollorum.signpost.minecraft.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import gollorum.signpost.minecraft.config.Config;
import gollorum.signpost.minecraft.utils.LangKeys;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import static gollorum.signpost.Signpost.MOD_ID;

public class CreativeModeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> Register = DeferredRegister.create(MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> SIGNPOST_TAB = Register.register(
            new ResourceLocation(MOD_ID, "signpost"),
            () -> CreativeTabRegistry.create(builder -> builder
                    .title(Component.translatable(LangKeys.tabGroup))
                    .icon(() -> new ItemStack(ItemRegistry.POSTS_ITEMS.get(0).get()))
                    .displayItems((params, output) -> {
                        output.accept(ItemRegistry.BRUSH.get());
                        output.accept(ItemRegistry.WRENCH.get());
                        for (var post : ItemRegistry.POSTS_ITEMS)
                            output.accept(post.get());
                        output.accept(ItemRegistry.WAYSTONE_ITEM.get());
                        for (var modelWaystone : ItemRegistry.ModelWaystoneItems)
                            if (Config.Server.allowedWaystones.get().contains(modelWaystone._1.name))
                                output.accept(modelWaystone._2.get());
                    }))
    );

    public static void register(){
        Register.register();
    }
}
