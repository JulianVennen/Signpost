package gollorum.signpost.minecraft.gui;

import dev.architectury.networking.NetworkManager;
import gollorum.signpost.WaystoneLibrary;
import gollorum.signpost.networking.PacketHandler;
import gollorum.signpost.utils.WaystoneData;
import gollorum.signpost.utils.WorldLocation;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

public class RequestWaystoneGui implements PacketHandler.Event<RequestWaystoneGui.Package> {

	public static class Package {
		public final WorldLocation location;
		public final Optional<WaystoneData> oldData;

		public Package(WorldLocation location, Optional<WaystoneData> oldData) {
			this.location = location;
			this.oldData = oldData;
		}
	}

	@Override
	public Class<RequestWaystoneGui.Package> getMessageClass() { return RequestWaystoneGui.Package.class; }

	@Override
	public void encode(RequestWaystoneGui.Package message, FriendlyByteBuf buffer) {
		WorldLocation.SERIALIZER.write(message.location, buffer);
		WaystoneData.SERIALIZER.optional().write(message.oldData, buffer);
	}

	@Override
	public RequestWaystoneGui.Package decode(FriendlyByteBuf buffer) {
		return new RequestWaystoneGui.Package(
			WorldLocation.SERIALIZER.read(buffer),
			WaystoneData.SERIALIZER.optional().read(buffer)
		);
	}

	@Override
	public void handle(
		RequestWaystoneGui.Package message, NetworkManager.PacketContext context
	) {
		WaystoneGui.display(message.location, message.oldData);
	}

}
