package gollorum.signpost.networking;

import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.architectury.networking.NetworkChannel;
import dev.architectury.networking.NetworkManager;
import gollorum.signpost.ServerHolder;
import gollorum.signpost.Signpost;
import gollorum.signpost.BlockRestrictions;
import gollorum.signpost.Teleport;
import gollorum.signpost.minecraft.block.tiles.PostTile;
import gollorum.signpost.minecraft.gui.RequestSignGui;
import gollorum.signpost.minecraft.gui.RequestWaystoneGui;
import gollorum.signpost.utils.EventDispatcher;
import net.fabricmc.api.EnvType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler {

    private static final Event<?>[] EVENTS = new Event<?>[]{
        new PostTile.PartAddedEvent(),
        new PostTile.PartMutatedEvent(),
        new PostTile.PartRemovedEvent(),
        new PostTile.UpdateAllPartsEvent(),
        new Teleport.Request(),
        new Teleport.RequestGui(),
        new RequestSignGui(),
        new RequestSignGui.ForNewSign(),
        new RequestWaystoneGui(),
        new BlockRestrictions.NotifyCountChanged(),
    };

    private static final String PROTOCOL_VERSION = "1";
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(Signpost.MOD_ID, "main");
    private static final NetworkChannel CHANNEL = NetworkChannel.create(CHANNEL_ID);

    private static final EventDispatcher.Impl.WithPublicDispatch<Unit> onInitialize = new EventDispatcher.Impl.WithPublicDispatch<>();
    public static EventDispatcher<Unit> onInitialize() { return onInitialize; }

    public static void initialize(){
        for(Event<?> event : EVENTS){
            register(event);
        }
        onInitialize.dispatch(Unit.INSTANCE, false);
    }

    public static <T> void register(Event<T> event){
        register(event.getMessageClass(), event::encode, event::decode, event::handle);
    }

    public static <T> void register(
        Class<T> messageClass,
        BiConsumer<T, FriendlyByteBuf> encode,
        Function<FriendlyByteBuf, T> decode,
        BiConsumer<T, Supplier<NetworkManager.PacketContext>> handle
    ){
        CHANNEL.register(messageClass, encode, decode, handle);
    }

    public static <T> void sendToServer(T message) {
        CHANNEL.sendToServer(message);
    }

    public static <T> void send(ServerPlayer target, T message) {
        CHANNEL.sendToPlayer(target, message);
    }

    public static <T> void send(Iterable<ServerPlayer> target, T message) {
        CHANNEL.sendToPlayers(target, message);
    }

    public static <T> void sendToTracing(Level world, BlockPos pos, Supplier<T> t) {
        if(world == null) Signpost.logger().warn("No world to notify mutation");
        else if(pos == null) Signpost.logger().warn("No position to notify mutation");
        else {
            LevelChunk chunk = world.getChunkAt(pos);
            ChunkMap chunkMap = ((ServerChunkCache) world.getChunkSource()).chunkMap;
            send(chunkMap.getPlayers(chunk.getPos(), false), t.get());
        }
    }

    public static <T> void sendToTracing(BlockEntity tile, Supplier<T> t) {
        sendToTracing(tile.getLevel(), tile.getBlockPos(), t);
    }

    public static <T> void sendToAll(T message) {
        CHANNEL.sendToPlayers(ServerHolder.getServer().getPlayerList().getPlayers(), message);
    }

    public static interface Event<T> {
        Class<T> getMessageClass();
        void encode(T message, FriendlyByteBuf buffer);
        T decode(FriendlyByteBuf buffer);
        void handle(T message, NetworkManager.PacketContext context);

        default void handle(T message, Supplier<NetworkManager.PacketContext> context) {
            NetworkManager.PacketContext c = context.get();
            c.queue(() -> {
                handle(message, c);
                /*if(c.getDirection().getReceptionSide().isClient())
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handle(message, c));
                else handle(message, c);*/
            });
            //c.setPacketHandled(true);
        }
    }
}
