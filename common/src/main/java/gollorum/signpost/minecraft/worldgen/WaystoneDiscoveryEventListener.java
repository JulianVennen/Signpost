package gollorum.signpost.minecraft.worldgen;

import dev.architectury.event.events.common.ChunkEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import gollorum.signpost.PlayerHandle;
import gollorum.signpost.ServerHolder;
import gollorum.signpost.Signpost;
import gollorum.signpost.WaystoneHandle;
import gollorum.signpost.WaystoneLibrary;
import gollorum.signpost.events.ChunkWatchEvents;
import gollorum.signpost.minecraft.utils.LangKeys;
import gollorum.signpost.minecraft.utils.TextComponents;
import gollorum.signpost.utils.WaystoneData;
import io.netty.util.internal.PlatformDependent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

public class WaystoneDiscoveryEventListener implements ChunkWatchEvents.Watch, ChunkWatchEvents.UnWatch, TickEvent.Server {

    private static final int discoveryDistance = 8;


    private static ConcurrentMap<ServerPlayer, ConcurrentMap<WaystoneHandle.Vanilla, BlockPos>> trackedPlayers;

    public WaystoneDiscoveryEventListener() {
        ChunkWatchEvents.WATCH.register(this);
        ChunkWatchEvents.UNWATCH.register(this);
        TickEvent.SERVER_PRE.register(this);
    }

    public static void initialize() {
        trackedPlayers = PlatformDependent.newConcurrentHashMap();
    }

    @Override
    public void watch(ServerPlayer player, Level level, ChunkPos pos) {
        if(!WaystoneLibrary.hasInstance()) return;
        VillageWaystone.ChunkEntryKey key = new VillageWaystone.ChunkEntryKey(
            pos,
            level.dimension().location()
        );
        Map<VillageWaystone.ChunkEntryKey, WaystoneHandle.Vanilla> allEntries = VillageWaystone.getAllEntriesByChunk();
        WaystoneHandle.Vanilla handle = allEntries.get(key);
        if(handle != null && !WaystoneLibrary.getInstance().isDiscovered(PlayerHandle.from(player), handle)) {
            Optional<WaystoneData> dataOption = WaystoneLibrary.getInstance().getData(handle);
            dataOption.ifPresentOrElse(
                data -> trackedPlayers.computeIfAbsent(player, p -> PlatformDependent.newConcurrentHashMap())
                    .putIfAbsent(handle, data.location.block.blockPos),
                () -> allEntries.remove(key)
            );
        }
    }

    @Override
    public void unWatch(ServerPlayer player, Level level, ChunkPos pos) {
        ConcurrentMap<WaystoneHandle.Vanilla, BlockPos> set = trackedPlayers.get(player);
        if(set == null) return;
        WaystoneHandle.Vanilla handle = VillageWaystone.getAllEntriesByChunk().get(
            new VillageWaystone.ChunkEntryKey(
                pos,
                player.serverLevel().dimension().location()
            )
        );
        if(handle == null) return;
        set.remove(handle);
    }

    @Override
    public void tick(MinecraftServer instance) {
        if(!WaystoneLibrary.hasInstance()) return;
        for(Map.Entry<ServerPlayer, ConcurrentMap<WaystoneHandle.Vanilla, BlockPos>> map : trackedPlayers.entrySet()) {
            for(Map.Entry<WaystoneHandle.Vanilla, BlockPos> inner : map.getValue().entrySet()) {
                if(inner.getValue().closerThan(map.getKey().blockPosition(), discoveryDistance)) {
                    WaystoneLibrary.getInstance().getData(inner.getKey()).ifPresent(data -> {
                        if(WaystoneLibrary.getInstance().addDiscovered(new PlayerHandle(map.getKey()), inner.getKey())) {
                            map.getKey().sendSystemMessage(
                                    Component.translatable(
                                            LangKeys.discovered,
                                            TextComponents.waystone(map.getKey(), data.name)
                                    ));
                        }
                    });
                    map.getValue().remove(inner.getKey());
                }
            }
            if(map.getValue().isEmpty()) trackedPlayers.remove(map.getKey());
        }
    }

    public static void registerNew(WaystoneHandle.Vanilla handle, ServerLevel world, BlockPos pos) {
        ServerHolder.getServer().getPlayerList().getPlayers().forEach(
            player -> {
                if(player.serverLevel().equals(world) && player.blockPosition().closerThan(pos, 100))
                    trackedPlayers.computeIfAbsent(player, p -> PlatformDependent.newConcurrentHashMap())
                        .putIfAbsent(handle, pos);
            }
        );
    }
}
