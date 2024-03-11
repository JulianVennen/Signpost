package gollorum.signpost.events;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import gollorum.signpost.ServerHolder;
import gollorum.signpost.WaystoneLibrary;
import gollorum.signpost.networking.PacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class JoinServerEvent implements PacketHandler.Event<JoinServerEvent.Package>, PlayerEvent.PlayerJoin {
    public static final class Package {}

    @Override
    public Class<Package> getMessageClass() { return Package.class; }

    @Override
    public void encode(Package message, FriendlyByteBuf buffer) { }

    @Override
    public Package decode(FriendlyByteBuf buffer) { return new Package(); }

    @Override
    public void handle(
            Package message, NetworkManager.PacketContext context
    ) {
        WaystoneLibrary.initialize();
    }

    @Override
    public void join(ServerPlayer player) {
        if (ServerHolder.getServer().isDedicatedServer())
            PacketHandler.send(
                    player,
                    new Package()
            );
    }
}
