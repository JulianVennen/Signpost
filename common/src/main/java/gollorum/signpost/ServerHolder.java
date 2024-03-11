package gollorum.signpost;

import dev.architectury.event.events.common.LifecycleEvent;
import gollorum.signpost.minecraft.worldgen.WaystoneDiscoveryEventListener;
import gollorum.signpost.utils.ServerType;
import gollorum.signpost.worldgen.Villages;
import net.minecraft.server.MinecraftServer;

public class ServerHolder {
    private static MinecraftServer server;

    private static class ServerStarting implements LifecycleEvent.ServerState {
        @Override
        public void stateChanged(MinecraftServer instance) {
            server = instance;
            WaystoneLibrary.initialize();
            BlockRestrictions.initialize();
            Villages.instance.initialize(instance.registryAccess());
            WaystoneDiscoveryEventListener.initialize();
        }
    }

    public static class ServerStopping implements LifecycleEvent.ServerState {
        @Override
        public void stateChanged(MinecraftServer instance) {
            server = null;
        }
    }

    public static void register() {
        LifecycleEvent.SERVER_BEFORE_START.register(new ServerStarting());
        LifecycleEvent.SERVER_STOPPED.register(new ServerStopping());
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static ServerType getServerType() {
        return server == null
                ? ServerType.ConnectedClient
                : server.isDedicatedServer()
                ? ServerType.Dedicated
                : ServerType.HostingClient;
    }
}
