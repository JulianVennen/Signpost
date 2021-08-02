package gollorum.signpost.minecraft.gui;

import gollorum.signpost.Signpost;
import gollorum.signpost.blockpartdata.types.Sign;
import gollorum.signpost.minecraft.block.Post;
import gollorum.signpost.minecraft.block.tiles.PostTile;
import gollorum.signpost.networking.PacketHandler;
import gollorum.signpost.utils.BlockPartInstance;
import gollorum.signpost.minecraft.utils.TileEntityUtils;
import gollorum.signpost.utils.WorldLocation;
import gollorum.signpost.utils.math.geometry.Vector3;
import gollorum.signpost.utils.serialization.ItemStackSerializer;
import javafx.util.Pair;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

public class RequestSignGui implements PacketHandler.Event<RequestSignGui.Package> {

	public static class Package {
		public final PostTile.TilePartInfo tilePartInfo;

		public Package(PostTile.TilePartInfo tilePartInfo) {
			this.tilePartInfo = tilePartInfo;
		}
	}

	@Override
	public Class<Package> getMessageClass() { return Package.class; }

	@Override
	public void encode(Package message, PacketBuffer buffer) {
		PostTile.TilePartInfo.Serializer.write(message.tilePartInfo, buffer);
	}

	@Override
	public Package decode(PacketBuffer buffer) {
		return new Package(PostTile.TilePartInfo.Serializer.read(buffer));
	}

	@Override
	public void handle(
		Package message, Supplier<NetworkEvent.Context> context
	) {
		Optional<Pair<PostTile, BlockPartInstance>> pairO = TileEntityUtils.findTileEntityClient(
			message.tilePartInfo.dimensionKey, message.tilePartInfo.pos, PostTile.class
		).flatMap(tile -> tile.getPart(message.tilePartInfo.identifier)
			.flatMap(part -> (part.blockPart instanceof Sign ? Optional.of(new Pair<>(tile, part)) : Optional.empty())));
		if(pairO.isPresent()) {
			Pair<PostTile, BlockPartInstance> pair = pairO.get();
			SignGui.display(pair.getKey(), (Sign) pair.getValue().blockPart, pair.getValue().offset, message.tilePartInfo);
		} else {
			Signpost.LOGGER.error("Tried to open sign gui, but something was missing.");
		}
	}

	public static class ForNewSign implements PacketHandler.Event<ForNewSign.Package> {

		public static class Package {
			private final WorldLocation loc;
			private final Post.ModelType modelType;
			private final Vector3 localHitPos;
			private final ItemStack itemToDropOnBreak;

			public Package(WorldLocation loc, Post.ModelType modelType, Vector3 localHitPos, ItemStack itemToDropOnBreak) {
				this.loc = loc;
				this.modelType = modelType;
				this.localHitPos = localHitPos;
				this.itemToDropOnBreak = itemToDropOnBreak;
			}
		}

		@Override
		public Class<Package> getMessageClass() { return Package.class; }

		@Override
		public void encode(Package message, PacketBuffer buffer) {
			WorldLocation.SERIALIZER.write(message.loc, buffer);
			Post.ModelType.Serializer.write(message.modelType, buffer);
			Vector3.Serializer.write(message.localHitPos, buffer);
			ItemStackSerializer.Instance.write(message.itemToDropOnBreak, buffer);
		}

		@Override
		public Package decode(PacketBuffer buffer) {
			return new Package(
				WorldLocation.SERIALIZER.read(buffer),
				Post.ModelType.Serializer.read(buffer),
				Vector3.Serializer.read(buffer),
				ItemStackSerializer.Instance.read(buffer)
			);
		}

		@Override
		public void handle(
			Package message, Supplier<NetworkEvent.Context> context
		) {
			TileEntityUtils.delayUntilTileEntityExistsAt(
				message.loc, PostTile.class,
				tile -> SignGui.display(tile, message.modelType, message.localHitPos, message.itemToDropOnBreak),
				100,
				Optional.of(() -> Signpost.LOGGER.error("Tried to open sign gui for a new block, but the tile was missing."))
			);
		}

	}

}
