package gollorum.signpost.forge.minecraft.items;

import gollorum.signpost.WaystoneHandle;
import gollorum.signpost.forge.minecraft.block.ModelWaystone;
import gollorum.signpost.forge.minecraft.block.WaystoneBlock;
import gollorum.signpost.forge.minecraft.utils.LangKeys;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaystoneItem extends BlockItem {

    public WaystoneItem(WaystoneBlock waystone, Properties properties) {
        super(waystone, properties);
    }

    public WaystoneItem(ModelWaystone waystone, Properties properties) {
        super(waystone, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> hoverText, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, hoverText, flag);

        if(stack.hasTag() && stack.getTag().contains("Handle")) {
            hoverText.add(Component.translatable(LangKeys.waystoneHasId));
            if(flag.isAdvanced()) hoverText.add(Component.translatable(LangKeys.waystoneId,
                WaystoneHandle.Vanilla.Serializer.read(stack.getTag().getCompound("Handle")).id.toString()));
        }
    }
}
