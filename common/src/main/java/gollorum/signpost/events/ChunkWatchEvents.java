package gollorum.signpost.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public interface ChunkWatchEvents {
    Event<Watch> WATCH = EventFactory.createEventResult();
    Event<UnWatch> UNWATCH = EventFactory.createEventResult();

    interface Watch {
        /**
         * Invoked when a player watches a chunk.
         * Equivalent to Forge's {@code ChunkWatchEvent.Watch} event.
         *
         * @param player The player watching the chunk.
         * @param level The level being watched.
         * @param pos The position being watched.
         */
        void watch(ServerPlayer player, Level level, ChunkPos pos);
    }

    interface UnWatch {
        /**
         * Invoked when a player unwatches a chunk.
         * Equivalent to Forge's {@code ChunkWatchEvent.UnWatch} event.
         *
         * @param player The player unwatching the chunk.
         * @param level The level being unwatched.
         * @param pos The position being unwatched.
         */
        void unWatch(ServerPlayer player, Level level, ChunkPos pos);
    }
}
