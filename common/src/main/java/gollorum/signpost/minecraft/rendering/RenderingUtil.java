package gollorum.signpost.minecraft.rendering;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.*;
import gollorum.signpost.minecraft.gui.utils.Colors;
import gollorum.signpost.minecraft.gui.utils.Point;
import gollorum.signpost.minecraft.gui.utils.Rect;
import gollorum.signpost.utils.math.Angle;
import gollorum.signpost.utils.math.geometry.Vector3;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.*;

import java.lang.Math;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.minecraft.client.renderer.LevelRenderer.DIRECTIONS;

public abstract class RenderingUtil {
    abstract public BakedModel loadModel(ResourceLocation location);

    abstract public BakedModel loadModel(ResourceLocation modelLocation, ResourceLocation textureLocation);

    abstract public BakedModel loadModel(ResourceLocation modelLocation, ResourceLocation textureLocation1, ResourceLocation textureLocation2);

    public static final Supplier<ModelBlockRenderer> Renderer = Suppliers.memoize(() -> Minecraft.getInstance().getBlockRenderer().getModelRenderer());

    public static ResourceLocation trim(ResourceLocation textureLocation){
        if(textureLocation.getPath().startsWith("textures/"))
            textureLocation = new ResourceLocation(textureLocation.getNamespace(), textureLocation.getPath().substring("textures/".length()));
        if(textureLocation.getPath().endsWith(".png"))
            textureLocation = new ResourceLocation(textureLocation.getNamespace(), textureLocation.getPath().substring(0, textureLocation.getPath().length() - ".png".length()));
        return textureLocation;
    }

    public void render(
        PoseStack blockToView,
        Matrix4f localToBlock,
        BakedModel model,
        Level world,
        BlockState state,
        BlockPos pos,
        VertexConsumer buffer,
        boolean checkSides,
        RandomSource random,
        long rand,
        int combinedOverlay,
        int[] tints
    ){
        wrapInMatrixEntry(blockToView, () ->
            tesselateBlock(
                world,
                model,
                state,
                tints,
                pos,
                blockToView,
                localToBlock,
                buffer,
                checkSides,
                random,
                rand,
                combinedOverlay
            )
        );
    }

    public int drawString(Font fontRenderer, String text, Point point, Rect.XAlignment xAlignment, Rect.YAlignment yAlignment, int color, int maxWidth, boolean dropShadow){
        MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int textWidth = fontRenderer.width(text);
        float scale = Math.min(1f, maxWidth / (float) textWidth);
        Matrix4f matrix = new Matrix4f().translation(
            Rect.xCoordinateFor(point.x, maxWidth, xAlignment) + maxWidth * 0.5f,
            Rect.yCoordinateFor(point.y, fontRenderer.lineHeight, yAlignment) + fontRenderer.lineHeight * 0.5f,
            100
        );
        if(scale < 1) matrix.scale(scale, scale, scale);
        int i = fontRenderer.drawInBatch(
            text,
            (maxWidth - Math.min(maxWidth, textWidth)) * 0.5f,
            -fontRenderer.lineHeight * 0.5f,
            color,
            dropShadow,
            matrix,
            buffer,
            Font.DisplayMode.NORMAL,
            0,
            0xf000f0
        );
        buffer.endBatch();
        return i;
    }

    public void renderGui(BakedModel model, PoseStack matrixStack, int[] tints, Point center, Angle yaw, Angle pitch, boolean isFlipped, float scale, Vector3 offset, RenderType renderType, Consumer<PoseStack> alsoDo) {
        wrapInMatrixEntry(matrixStack, () -> {
            matrixStack.translate(center.x, center.y, 0);
            matrixStack.scale(scale, -scale, scale);
            matrixStack.mulPose(new Quaternionf(new AxisAngle4f(pitch.radians(), new Vector3f(1, 0, 0))));
            if(isFlipped) matrixStack.mulPose(new Quaternionf(new AxisAngle4d(Math.PI, new Vector3f(0, 1, 0))));
            MultiBufferSource.BufferSource renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
            renderGui(model, matrixStack, tints, offset, yaw, renderTypeBuffer.getBuffer(renderType), renderType, 0xf000f0, OverlayTexture.NO_OVERLAY, alsoDo);
            renderTypeBuffer.endBatch();
        });
    }

    abstract public void renderGui(BakedModel model, PoseStack matrixStack, int[] tints, Vector3 offset, Angle yaw, VertexConsumer builder, RenderType renderType, int combinedLight, int combinedOverlay, Consumer<PoseStack> alsoDo);

    public void wrapInMatrixEntry(PoseStack matrixStack, Runnable thenDo) {
        matrixStack.pushPose();
        thenDo.run();
        matrixStack.popPose();
    }

    // These are modified copies of stuff in the ModelBlockRenderer to allow custom tints and dynamic ao.
    abstract protected boolean tesselateBlock(
        BlockAndTintGetter level,
        BakedModel model,
        BlockState state,
        int[] tints,
        BlockPos pos,
        PoseStack blockToView,
        Matrix4f localToBlock,
        VertexConsumer vertexConsumer,
        boolean checkSides,
        RandomSource random,
        long combinedLight,
        int combinedOverlay
    );

//    public static BakedModel withReplacedTexture(BakedModel original, Map<TextureAtlasSprite, TextureAtlasSprite> mapping) {
//        return new BakedModel() {
//            @Override
//            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
//                return original.getQuads(state, side, rand).stream().map(q -> {
//                    var tas = mapping.get(q.getSprite());
//                    return tas == null
//                        ? q
//                        : new BakedQuad(q.getVertices(), q.getTintIndex(), q.getDirection(), tas, q.isShade());
//                }).collect(Collectors.toList());
//            }
//
//            @Override
//            public boolean useAmbientOcclusion() {
//                return original.useAmbientOcclusion();
//            }
//
//        return flag;
//    };
//}


    protected Direction transform(Direction dir, Matrix4f localPose) {
        var rawNormal = dir.getNormal();
        var normal = new Vector4f(rawNormal.getX(), rawNormal.getY(), rawNormal.getZ(), 0);
        normal.mul(localPose);
        return Direction.getNearest(normal.x(), normal.y(), normal.z());
    }

    protected void putQuadData(int[] tints, VertexConsumer vertexConsumer, PoseStack.Pose pose, BakedQuad quad, float aor, float aog, float aob, float aoa, int lr, int lg, int lb, int la, int combinedOverlay) {
        float r;
        float g;
        float b;
        if (quad.isTinted()) {
            int i = tints[quad.getTintIndex()];
            r = (float)(i >> 16 & 255) / 255.0F;
            g = (float)(i >> 8 & 255) / 255.0F;
            b = (float)(i & 255) / 255.0F;
        } else {
            r = 1.0F;
            g = 1.0F;
            b = 1.0F;
        }

        vertexConsumer.putBulkData(pose, quad, new float[]{aor, aog, aob, aoa}, r, g, b, new int[]{lr, lg, lb, la}, combinedOverlay, true);
    }


    protected BakedQuad clampWithinUnitCube(BakedQuad quad){
        var oldData = quad.getVertices();
        var newData = new int[oldData.length];
        for (int i = 0; i < 4; i++)
        {
            float x = Math.min(1, Math.max(0, Float.intBitsToFloat(oldData[i * 8])));
            float y = Math.min(1, Math.max(0, Float.intBitsToFloat(oldData[i * 8 + 1])));
            float z = Math.min(1, Math.max(0, Float.intBitsToFloat(oldData[i * 8 + 2])));

            newData[i * 8] = Float.floatToRawIntBits(x);
            newData[i * 8 + 1] = Float.floatToRawIntBits(y);
            newData[i * 8 + 2] = Float.floatToRawIntBits(z);
        }
        return new BakedQuad(newData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }
}
