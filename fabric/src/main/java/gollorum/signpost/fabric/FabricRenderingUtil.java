package gollorum.signpost.fabric;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import gollorum.signpost.minecraft.data.BasePostModel;
import gollorum.signpost.minecraft.gui.utils.Colors;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import gollorum.signpost.utils.math.Angle;
import gollorum.signpost.utils.math.geometry.Vector3;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.client.renderer.LevelRenderer.DIRECTIONS;

public class FabricRenderingUtil extends RenderingUtil {
    public static ModelBakery modelBakery;
    public static ModelBaker modelBaker;

    @Override
    public BakedModel loadModel(ResourceLocation location) {
        return modelBakery.getModel(location).bake(
                modelBaker,
                m -> Minecraft.getInstance().getTextureAtlas(m.atlasLocation()).apply(m.texture()),
                new SimpleFabricModelState(Transformation.identity()),
                location
        );
    }

    @Override
    public BakedModel loadModel(ResourceLocation modelLocation, ResourceLocation textureLocation) {
        final ResourceLocation textLoc = trim(textureLocation);
        return modelBakery.getModel(modelLocation).bake(
                modelBaker,
                m -> Minecraft.getInstance().getTextureAtlas(m.atlasLocation()).apply(textLoc),
                new SimpleFabricModelState(Transformation.identity()),
                modelLocation
        );
    }

    @Override
    public BakedModel loadModel(ResourceLocation modelLocation, ResourceLocation textureLocation1, ResourceLocation textureLocation2) {
        final ResourceLocation textLoc1 = trim(textureLocation1);
        final ResourceLocation textLoc2 = trim(textureLocation2);
        return modelBakery.getModel(modelLocation).bake(
                modelBaker,
                m -> Minecraft.getInstance().getTextureAtlas(m.atlasLocation()).apply(
                        m.texture().equals(BasePostModel.mainTextureMarker)
                                ? textLoc1 : textLoc2
                ),
                new SimpleFabricModelState(Transformation.identity()),
                modelLocation
        );
    }

    @Override
    public void renderGui(BakedModel model, PoseStack matrixStack, int[] tints, Vector3 offset, Angle yaw, VertexConsumer builder, RenderType renderType, int combinedLight, int combinedOverlay, Consumer<PoseStack> alsoDo) {
        wrapInMatrixEntry(matrixStack, () -> {
            matrixStack.mulPose(new Quaternionf(new AxisAngle4f(yaw.radians(), new Vector3f(0, 1, 0))));
            matrixStack.translate(offset.x, offset.y, offset.z);
            wrapInMatrixEntry(matrixStack, () -> {

                List<Direction> allDirections = new ArrayList<>(Arrays.asList(Direction.values()));
                allDirections.add(null);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                Minecraft.getInstance().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);
                RandomSource random = RandomSource.create();
                for (Direction dir : allDirections) {
                    random.setSeed(42L);
                    // Forge has ModelData.EMPTY, renderType as additional parameters here
                    // TODO: figure out how to handle renderType
                    for (BakedQuad quad : model.getQuads(null, dir, random)) {
                        float r = 1;
                        float g = 1;
                        float b = 1;
                        if (quad.isTinted()) {
                            var tint = tints[quad.getTintIndex()];
                            r *= Colors.getRed(tint) / 255f;
                            g *= Colors.getGreen(tint) / 255f;
                            b *= Colors.getBlue(tint) / 255f;
                        }
                        builder.putBulkData(matrixStack.last(), quad, r, g, b, combinedLight, combinedOverlay);
                    }

                }
            });

            alsoDo.accept(matrixStack);
        });
    }

    @Override
    protected boolean tesselateBlock(BlockAndTintGetter level, BakedModel model, BlockState state, int[] tints, BlockPos pos, PoseStack blockToView, Matrix4f localToBlock, VertexConsumer vertexConsumer, boolean checkSides, RandomSource random, long combinedLight, int combinedOverlay) {
        boolean useAmbientOcclusion = Minecraft.useAmbientOcclusion() && state.getLightEmission() == 0 && model.useAmbientOcclusion();
        var vec3 = state.getOffset(level, pos);
        blockToView.translate(vec3.x, vec3.y, vec3.z);
        try {
            return tesselate(level, model, state, tints, pos, blockToView, localToBlock, vertexConsumer, checkSides, random, combinedLight, combinedOverlay, useAmbientOcclusion);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Tesselating block model");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, level, pos, state);
            crashreportcategory.setDetail("Using AO", useAmbientOcclusion);
            throw new ReportedException(crashreport);
        }
    }

    protected boolean tesselate(
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
            int combinedOverlay,
            boolean useAmbientOcclusion) {
        boolean flag = false;
        float[] aoValues = useAmbientOcclusion ? new float[DIRECTIONS.length * 2] : null;
        BitSet bitset = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace aoFace = useAmbientOcclusion ? new ModelBlockRenderer.AmbientOcclusionFace() : null;
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        var quadLocalToBlock = new ApplyingTransformer(new Transformation(localToBlock));

        for (Direction direction : DIRECTIONS) {
            random.setSeed(combinedLight);
            List<BakedQuad> list = model.getQuads(state, direction, random);
            if (!list.isEmpty()) {
                mutablePos.setWithOffset(pos, direction);
                if (!checkSides || Block.shouldRenderFace(state, level, pos, direction, mutablePos)) {
                    if (useAmbientOcclusion)
                        renderModelFaceAO(level, state, tints, pos, blockToView, localToBlock, vertexConsumer, list, aoValues, bitset, aoFace, combinedOverlay, quadLocalToBlock);
                    else
                        renderModelWithoutAo(level, state, tints, pos, LevelRenderer.getLightColor(level, state, mutablePos), combinedOverlay, false, blockToView, localToBlock, vertexConsumer, list, bitset, quadLocalToBlock);
                    flag = true;
                }
            }
        }

        random.setSeed(combinedLight);
        List<BakedQuad> quads = model.getQuads(state, null, random);
        if (!quads.isEmpty()) {
            if (useAmbientOcclusion)
                renderModelFaceAO(level, state, tints, pos, blockToView, localToBlock, vertexConsumer, quads, aoValues, bitset, aoFace, combinedOverlay, quadLocalToBlock);
            else
                renderModelWithoutAo(level, state, tints, pos, -1, combinedOverlay, true, blockToView, localToBlock, vertexConsumer, quads, bitset, quadLocalToBlock);
            flag = true;
        }

        return flag;
    }

    private void renderModelFaceAO(BlockAndTintGetter level, BlockState state, int[] tints, BlockPos pos, PoseStack blockToView, Matrix4f localToBlock, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] aoFloats, BitSet bitset, ModelBlockRenderer.AmbientOcclusionFace aoFace, int combinedOverlay, ApplyingTransformer quadLocalToBlock) {
        var poseMatrix = blockToView.last();
        for (BakedQuad bakedquad : quads) {
            bakedquad = transform(bakedquad, quadLocalToBlock, localToBlock);
            var shadingQuad = clampWithinUnitCube(bakedquad);
            Renderer.get().calculateShape(level, state, pos, shadingQuad.getVertices(), shadingQuad.getDirection(), aoFloats, bitset);
            calculateFace(level, state, pos, bakedquad, bitset.get(0), aoFace.brightness, aoFace.lightmap); // TODO: something, something, ambient occlusion
            putQuadData(tints, vertexConsumer, poseMatrix, bakedquad, aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3], aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3], combinedOverlay);
        }

    }

    private void calculateFace(BlockAndTintGetter getter, BlockState state, BlockPos pos, BakedQuad quad, boolean isFaceCubic, float[] brightness, int[] lightmap) {
        BlockPos lightmapPos = isFaceCubic ? pos.relative(quad.getDirection()) : pos;

        brightness[0] = brightness[1] = brightness[2] = brightness[3] = getter.getShade(quad.getDirection(), quad.isShade());
        lightmap[0] = lightmap[1] = lightmap[2] = lightmap[3] = LevelRenderer.getLightColor(getter, state, lightmapPos);
    }

    private void renderModelWithoutAo(BlockAndTintGetter level, BlockState state, int[] tints, BlockPos pos, int lightColor, int combinedOverlay, boolean p_111007_, PoseStack blockToView, Matrix4f localToBlock, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet bitSet, ApplyingTransformer quadLocalToBlock) {
        var poseMatrix = blockToView.last();
        for (BakedQuad bakedquad : quads) {
            bakedquad = transform(bakedquad, quadLocalToBlock, localToBlock);
            if (p_111007_) {
                var shadingQuad = clampWithinUnitCube(bakedquad);
                Renderer.get().calculateShape(level, state, pos, shadingQuad.getVertices(), shadingQuad.getDirection(), (float[]) null, bitSet);
                BlockPos blockpos = bitSet.get(0) ? pos.relative(shadingQuad.getDirection()) : pos;
                lightColor = LevelRenderer.getLightColor(level, state, blockpos);
            }

            float f = level.getShade(bakedquad.getDirection(), bakedquad.isShade());
            putQuadData(tints, vertexConsumer, poseMatrix, bakedquad, f, f, f, f, lightColor, lightColor, lightColor, lightColor, combinedOverlay);
        }
    }

    private BakedQuad transform(BakedQuad original, ApplyingTransformer transformer, Matrix4f matrix4f) {
        var copy = transformer.process(original);
        var dir = transform(original.getDirection(), matrix4f);
        return new BakedQuad(copy.getVertices(), copy.getTintIndex(), dir, copy.getSprite(), copy.isShade());
    }

    static class ApplyingTransformer {
        static int STRIDE = DefaultVertexFormat.BLOCK.getIntegerSize();
        static int POSITION = findOffset(DefaultVertexFormat.ELEMENT_POSITION);
        static int NORMAL = findOffset(DefaultVertexFormat.ELEMENT_NORMAL);

        private Transformation transformation;

        public ApplyingTransformer(Transformation transformation) {
            this.transformation = transformation;
        }

        public BakedQuad process(BakedQuad quad) {
            var copy = copy(quad);
            processInPlace(copy);
            return copy;
        }

        public void processInPlace(BakedQuad quad) {
            var vertices = quad.getVertices();
            for (int i = 0; i < 4; i++) {
                int offset = i * ApplyingTransformer.STRIDE + ApplyingTransformer.POSITION;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);

                Vector4f pos = new Vector4f(x, y, z, 1);
                pos.mul(transformation.getMatrix());
                pos.div(pos.w);

                vertices[offset] = Float.floatToRawIntBits(pos.x());
                vertices[offset + 1] = Float.floatToRawIntBits(pos.y());
                vertices[offset + 2] = Float.floatToRawIntBits(pos.z());
            }

            Matrix3f normalMatrix = new Matrix3f(transformation.getMatrix());
            normalMatrix.invert().transpose();
            for (int i = 0; i < 4; i++) {
                int offset = i * ApplyingTransformer.STRIDE + ApplyingTransformer.NORMAL;
                int normalIn = vertices[offset];
                if ((normalIn & 0x00FFFFFF) != 0) // The ignored byte is padding and may be filled with user data
                {
                    float x = ((byte) (normalIn & 0xFF)) / 127.0f;
                    float y = ((byte) ((normalIn >> 8) & 0xFF)) / 127.0f;
                    float z = ((byte) ((normalIn >> 16) & 0xFF)) / 127.0f;

                    Vector3f pos = new Vector3f(x, y, z);
                    pos.mul(normalMatrix);
                    pos.normalize();

                    vertices[offset] = (((byte) (pos.x() * 127.0f)) & 0xFF) |
                            ((((byte) (pos.y() * 127.0f)) & 0xFF) << 8) |
                            ((((byte) (pos.z() * 127.0f)) & 0xFF) << 16) |
                            (normalIn & 0xFF000000); // Restore padding, just in case
                }
            }
        }

        private static BakedQuad copy(BakedQuad quad) {
            var vertices = quad.getVertices();
            return new BakedQuad(Arrays.copyOf(vertices, vertices.length), quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
        }

        private static int findOffset(VertexFormatElement element) {
            // Divide by 4 because we want the int offset
            var index = DefaultVertexFormat.BLOCK.getElements().indexOf(element);
            return index < 0 ? -1 : DefaultVertexFormat.BLOCK.offsets.getInt(index) / 4;
        }
    }
}
