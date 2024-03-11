package gollorum.signpost.minecraft.block;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import gollorum.signpost.BlockRestrictions;
import gollorum.signpost.PlayerHandle;
import gollorum.signpost.blockpartdata.types.PostBlockPart;
import gollorum.signpost.minecraft.block.tiles.PostTile;
import gollorum.signpost.minecraft.block.tiles.WaystoneTile;
import gollorum.signpost.networking.PacketHandler;
import gollorum.signpost.security.WithCountRestriction;
import gollorum.signpost.utils.Delay;
import gollorum.signpost.utils.WorldLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class BlockEventListener implements BlockEvent.Place, BlockEvent.Break {

    public BlockEventListener() {
        BlockEvent.PLACE.register(this);
    }

    @Override
    public EventResult placeBlock(Level level, BlockPos pos, BlockState state, @Nullable Entity placer) {
        if (state.getBlock() instanceof WithCountRestriction) {
            BlockRestrictions.Type restrictionType = ((WithCountRestriction) state.getBlock()).getBlockRestrictionType();
            PlayerHandle player = PlayerHandle.from(placer);
            if(!BlockRestrictions.getInstance().tryDecrementRemaining(restrictionType, player))
                return EventResult.interrupt(false);
        }
        return EventResult.pass();
    }

    @Override
    public EventResult breakBlock(Level level, BlockPos pos, BlockState state, ServerPlayer player, @Nullable IntValue xp) {
        Block block = state.getBlock();
        BlockEntity tile = level.getBlockEntity(pos);
        if(tile instanceof PostTile) {
            PostTile postTile = (PostTile) tile;
            Optional<PostTile.TraceResult> traceResult = postTile.trace(player);
            if(traceResult.isPresent() && !(traceResult.get().part.blockPart instanceof PostBlockPart)) {
                Delay.onServerForFrames(1, () -> {

                    // The client destroys the tile entity instantly. When the event gets cancelled, the server
                    // sends a block update to the client to restore the block. For some reason, it can happen
                    // that the entity update packet arrives **before** the entity has been reconstructed, which
                    // leaves an empty, and thus invisible, post. To fix that, we manually send another update
                    // one frame later.
                    PacketHandler.sendToTracing(tile, () -> new PostTile.UpdateAllPartsEvent.Packet(tile.getUpdateTag(), WorldLocation.from(tile).get()));

                    postTile.removePart(traceResult.get().id);
                    if (level instanceof ServerLevel world) {
                        if (!player.isCreative()) {
                            for (ItemStack item : (Collection<ItemStack>) traceResult.get().part.blockPart.getDrops(postTile)) {
                                ItemEntity itementity = new ItemEntity(
                                    world,
                                    pos.getX() + world.getRandom().nextFloat() * 0.5 + 0.25,
                                    pos.getY() + world.getRandom().nextFloat() * 0.5 + 0.25,
                                    pos.getZ() + world.getRandom().nextFloat() * 0.5 + 0.25,
                                    item
                                );
                                itementity.setDefaultPickUpDelay();
                                world.addFreshEntity(itementity);
                            }
                        }
                    }
                });
                return EventResult.interrupt(false);
            } else postTile.onDestroy();
        }
        if (block instanceof WithCountRestriction) {
            BlockRestrictions.Type restrictionType = ((WithCountRestriction)block).getBlockRestrictionType();
            restrictionType.tryGetOwner.apply(tile).ifPresent(owner ->
                BlockRestrictions.getInstance().incrementRemaining(restrictionType, owner));

            if(level instanceof ServerLevel serverLevel) {
                WaystoneTile.onRemoved(serverLevel, pos);
            }
        }

        return EventResult.pass();
    }

}
