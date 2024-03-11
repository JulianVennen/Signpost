package gollorum.signpost.forge.event;

import gollorum.signpost.events.ChunkWatchEvents;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ChunkWatchEventCaller {
    @SubscribeEvent
    public void onWatchChunk(ChunkWatchEvent.Watch event) {
        ChunkWatchEvents.WATCH.invoker().watch(event.getPlayer(), event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public void onUnwatchChunk(ChunkWatchEvent.UnWatch event) {
        ChunkWatchEvents.UNWATCH.invoker().unWatch(event.getPlayer(), event.getLevel(), event.getPos());
    }
}
