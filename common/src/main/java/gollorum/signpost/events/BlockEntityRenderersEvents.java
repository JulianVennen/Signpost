package gollorum.signpost.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;

public interface BlockEntityRenderersEvents {
    Event<Register> REGISTER = EventFactory.createEventResult();

    interface Register {
        /**
         * Invoked when the block entity renderer should be registered.
         * On fabric this is called during client setup.
         * On forge this is called during the FMLClientSetupEvent event.
         */
        void register();
    }
}
