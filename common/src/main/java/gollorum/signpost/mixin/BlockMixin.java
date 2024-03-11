package gollorum.signpost.mixin;

import gollorum.signpost.PlayerHandle;
import gollorum.signpost.WaystoneHandle;
import gollorum.signpost.minecraft.block.ModelWaystone;
import gollorum.signpost.minecraft.block.WaystoneBlock;
import gollorum.signpost.minecraft.block.tiles.WaystoneTile;
import gollorum.signpost.minecraft.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(Block.class)
public abstract class BlockMixin {

    /**
     * This mixin exists because Fabric does not pass the player to the getCloneItemStack method in the WaystoneBlock class.
     * To get the player, we need to use the Minecraft instance, which is only available on the client side.
     * If we were to use the Minecraft instance on the server side, it would throw a NoClassDefFoundError.
     * This mixin is only applied on the client side, so it is safe to use the Minecraft instance here.
     *
     * This also isn't really a secure way to check for permissions as it can be easily bypassed by modifying the client.
     */
    @Inject(method = "getCloneItemStack", at = @At("RETURN"), cancellable = true)
    public void getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, CallbackInfoReturnable<ItemStack> cir) {
        Block block = (Block) (Object) this;

        if (block instanceof WaystoneBlock || block instanceof ModelWaystone) {
            ItemStack stack = cir.getReturnValue();
            BlockEntity untypedEntity = blockGetter.getBlockEntity(blockPos);
            Player player = Minecraft.getInstance().player;
            if(untypedEntity instanceof WaystoneTile) {
                WaystoneTile tile = (WaystoneTile) untypedEntity;
                if(player.hasPermissions(Config.Server.permissions.pickUnownedWaystonePermissionLevel.get())
                        || tile.getWaystoneOwner().map(o -> o.equals(PlayerHandle.from(player))).orElse(true)) {

                    tile.getHandle().ifPresent(h -> stack.addTagElement("Handle", WaystoneHandle.Vanilla.Serializer.write(h)));
                    tile.getName().ifPresent(n -> stack.setHoverName(Component.literal(n)));
                }
            }
            cir.setReturnValue(stack);
        }
    }
}
