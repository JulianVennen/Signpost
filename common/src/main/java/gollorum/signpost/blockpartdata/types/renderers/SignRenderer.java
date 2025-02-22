package gollorum.signpost.blockpartdata.types.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import gollorum.signpost.Signpost;
import gollorum.signpost.blockpartdata.Overlay;
import gollorum.signpost.blockpartdata.types.BlockPartRenderer;
import gollorum.signpost.blockpartdata.types.SignBlockPart;
import gollorum.signpost.minecraft.config.Config;
import gollorum.signpost.minecraft.gui.utils.Colors;
import gollorum.signpost.minecraft.gui.utils.Point;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import gollorum.signpost.utils.math.Angle;
import gollorum.signpost.utils.math.geometry.Vector3;
import gollorum.signpost.utils.modelGeneration.SignModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.*;

import java.lang.Math;

public abstract class SignRenderer<T extends SignBlockPart<T>> extends BlockPartRenderer<T> {

	private final boolean shouldRenderBaked = true;

	protected abstract BakedModel makeBakedModel(T sign);
	protected abstract BakedModel makeBakedOverlayModel(T sign, Overlay overlay);
	protected abstract SignModel makeModel(T sign);
	protected abstract SignModel makeOverlayModel(T sign, Overlay overlay);

	@Override
	public void render(
		T sign,
		BlockEntity tileEntity,
		BlockEntityRenderDispatcher renderDispatcher,
		PoseStack blockToView,
		PoseStack localToBlock,
		MultiBufferSource buffer,
		int combinedLights,
		int combinedOverlay,
		RandomSource random,
		long randomSeed
	) {
		if(sign.isMarkedForGeneration() && !Config.Server.worldGen.debugMode()) return;
		if(!tileEntity.hasLevel()) throw new RuntimeException("TileEntity without world cannot be rendered.");
		Signpost.getRenderingUtil().wrapInMatrixEntry(localToBlock, () -> {
			Quaternionf rotation = new Quaternionf(new AxisAngle4f(sign.getAngle().get().radians(), new Vector3f(0,1,0)));
			localToBlock.mulPose(rotation);
			Signpost.getRenderingUtil().wrapInMatrixEntry(blockToView, () -> {
				blockToView.mulPoseMatrix(localToBlock.last().pose());
				if(!sign.isFlipped()) blockToView.mulPose(new Quaternionf(new AxisAngle4d(Math.PI, new Vector3f(0,1,0))));
				renderText(sign, blockToView, renderDispatcher.font, buffer, combinedLights);
			});
//			if(sign.isFlipped()) rotation.mul(new Quaternion(Vector3f.ZP, 180, true));
//			Matrix4f rotationMatrix = new Matrix4f().rotation(rotation);
			var tints = new int[2];
			tints[0] = sign.getMainTexture().tint().map(t -> t.getColorAt(tileEntity.getLevel(), tileEntity.getBlockPos())).orElse(Colors.white);
			tints[1] = sign.getSecondaryTexture().tint().map(t -> t.getColorAt(tileEntity.getLevel(), tileEntity.getBlockPos())).orElse(Colors.white);
			if(shouldRenderBaked)
				Signpost.getRenderingUtil().render(
					blockToView,
					localToBlock.last().pose(),
					makeBakedModel(sign),
					tileEntity.getLevel(),
					tileEntity.getBlockState(),
					tileEntity.getBlockPos(),
					buffer.getBuffer(RenderType.solid()), false, random, randomSeed, combinedOverlay,
					tints
				);
			else makeModel(sign).render(
				blockToView.last().pose(),
				localToBlock.last().pose(),
				buffer,
				RenderType.solid(),
				combinedLights,
				combinedOverlay,
				true,
				tileEntity.getLevel(),
				tileEntity.getBlockState(),
				tileEntity.getBlockPos(),
				tints
			);
			sign.getOverlay().ifPresent(o -> {
				if(shouldRenderBaked)
					Signpost.getRenderingUtil().render(
						blockToView,
						localToBlock.last().pose(),
						makeBakedOverlayModel(sign, o),
						tileEntity.getLevel(),
						tileEntity.getBlockState(),
						tileEntity.getBlockPos(),
						buffer.getBuffer(RenderType.cutoutMipped()), false, random, randomSeed, combinedOverlay,
						new int[]{o.tint.map(t -> t.getColorAt(tileEntity.getLevel(), tileEntity.getBlockPos())).orElse(Colors.white)}
					);
				else {
					makeOverlayModel(sign, o).render(
						blockToView.last().pose(),
						localToBlock.last().pose(),
						buffer,
						RenderType.cutoutMipped(),
						combinedLights,
						combinedOverlay,
						true,
						tileEntity.getLevel(),
						tileEntity.getBlockState(),
						tileEntity.getBlockPos(),
						new int[]{o.tint.map(t -> t.getColorAt(tileEntity.getLevel(), tileEntity.getBlockPos())).orElse(Colors.white)}
					);
				}
			});
		});
	}

	protected abstract void renderText(T sign, PoseStack matrix, Font fontRenderer, MultiBufferSource buffer, int combinedLights);

	@Override
	public void renderGui(T sign, PoseStack matrixStack, Point center, Angle yaw, Angle pitch, boolean isFlipped, float scale, Vector3 offset) {
		if(sign.isMarkedForGeneration() && !Config.Server.worldGen.debugMode()) return;
		var tints = new int[]{
			sign.getMainTexture().tint().map(t -> t.getColorAt(Minecraft.getInstance().level, Minecraft.getInstance().player.blockPosition())).orElse(Colors.white),
			sign.getSecondaryTexture().tint().map(t -> t.getColorAt(Minecraft.getInstance().level, Minecraft.getInstance().player.blockPosition())).orElse(Colors.white)
		};
		Signpost.getRenderingUtil().renderGui(makeBakedModel(sign), matrixStack, tints, center, yaw.add(sign.getAngle().get()), pitch, isFlipped, scale, offset, RenderType.solid(),
			ms -> Signpost.getRenderingUtil().wrapInMatrixEntry(ms, () -> {
				if(!sign.isFlipped())
					ms.mulPose(new Quaternionf(new AxisAngle4d(Math.PI, new Vector3f(0, 1, 0))));
				renderText(sign, ms, Minecraft.getInstance().font, Minecraft.getInstance().renderBuffers().bufferSource(), 0xf000f0);
			})
		);
		sign.getOverlay().ifPresent(o ->
			Signpost.getRenderingUtil().renderGui(makeBakedOverlayModel(sign, o), matrixStack, new int[]{o.tint.map(t -> t.getColorAt(Minecraft.getInstance().level, Minecraft.getInstance().player.blockPosition())).orElse(Colors.white)}, center, yaw.add(sign.getAngle().get()), pitch, isFlipped, scale, offset, RenderType.cutout(), m -> {}));
	}

	@Override
	public void renderGui(T sign, PoseStack matrixStack, Vector3 offset, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
		if(sign.isMarkedForGeneration() && !Config.Server.worldGen.debugMode()) return;
		var tints = new int[]{
			sign.getMainTexture().tint().map(t -> t.getColorAt(Minecraft.getInstance().level, Minecraft.getInstance().player.blockPosition())).orElse(Colors.white),
			sign.getSecondaryTexture().tint().map(t -> t.getColorAt(Minecraft.getInstance().level, Minecraft.getInstance().player.blockPosition())).orElse(Colors.white)
		};
		Signpost.getRenderingUtil().renderGui(makeBakedModel(sign), matrixStack, tints, offset, sign.getAngle().get(), buffer.getBuffer(RenderType.solid()), RenderType.solid(), combinedLight, combinedOverlay,
			ms -> Signpost.getRenderingUtil().wrapInMatrixEntry(matrixStack, () -> {
				if(!sign.isFlipped())
					matrixStack.mulPose(new Quaternionf(new AxisAngle4d(Math.PI, new Vector3f(0, 1, 0))));
				renderText(sign, ms, Minecraft.getInstance().font, buffer, combinedLight);
			})
		);
		sign.getOverlay().ifPresent(o -> {
			Signpost.getRenderingUtil().renderGui(makeBakedOverlayModel(sign, o), matrixStack, new int[]{o.tint.map(t -> t.getColorAt(Minecraft.getInstance().level, Minecraft.getInstance().player.blockPosition())).orElse(Colors.white)}, offset, sign.getAngle().get(), buffer.getBuffer(RenderType.cutout()), RenderType.solid(), combinedLight, combinedOverlay, m -> {});
		});
	}

}
