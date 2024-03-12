package gollorum.signpost.forge.event;

import gollorum.signpost.events.BlockEntityRenderersEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ModBusEventCaller {

    @SubscribeEvent
    public void clientSetup(FMLClientSetupEvent event) {
        BlockEntityRenderersEvents.REGISTER.invoker().register();
    }
}
