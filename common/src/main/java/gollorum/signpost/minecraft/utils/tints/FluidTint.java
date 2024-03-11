package gollorum.signpost.minecraft.utils.tints;

import dev.architectury.registry.registries.Registrar;
import gollorum.signpost.Signpost;
import gollorum.signpost.utils.Tint;
import gollorum.signpost.utils.serialization.CompoundSerializable;
import gollorum.signpost.utils.serialization.ResourceLocationSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.Fluid;

public record FluidTint(Fluid fluid) implements Tint {
    private static final Registrar<Fluid> FLUIDS = Signpost.REGISTRAR_MANAGER.get().get(Registries.FLUID);

    @Override
    public int getColorAt(BlockAndTintGetter level, BlockPos pos) {
        return /*TODO: net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid).getTintColor(fluid.defaultFluidState(), level, pos)*/ 0xFFFFFF;
    }

    public static void register() {
        Tint.Serialization.register("fluid", serializer);
    }

    public static final CompoundSerializable<FluidTint> serializer = new CompoundSerializable<>() {
        @Override
        public CompoundTag write(FluidTint fluidTint, CompoundTag compound) {
            ResourceLocationSerializer.Instance.write(FLUIDS.getId(fluidTint.fluid), compound);
            return compound;
        }

        @Override
        public boolean isContainedIn(CompoundTag compound) {
            return ResourceLocationSerializer.Instance.isContainedIn(compound);
        }

        @Override
        public FluidTint read(CompoundTag compound) {
            return new FluidTint(FLUIDS.get(ResourceLocationSerializer.Instance.read(compound)));
        }

        @Override
        public void write(FluidTint fluidTint, FriendlyByteBuf buffer) {
            ResourceLocationSerializer.Instance.write(FLUIDS.getId(fluidTint.fluid), buffer);
        }

        @Override
        public FluidTint read(FriendlyByteBuf buffer) {
            return new FluidTint(FLUIDS.get(ResourceLocationSerializer.Instance.read(buffer)));
        }

        @Override
        public Class<FluidTint> getTargetClass() {
            return FluidTint.class;
        }
    };

}
