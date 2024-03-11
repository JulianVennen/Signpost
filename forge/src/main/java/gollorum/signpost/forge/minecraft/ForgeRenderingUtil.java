package gollorum.signpost.forge.minecraft;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import gollorum.signpost.minecraft.data.BasePostModel;
import gollorum.signpost.minecraft.gui.utils.Colors;
import gollorum.signpost.minecraft.rendering.RenderingUtil;
import gollorum.signpost.utils.math.Angle;
import gollorum.signpost.utils.math.geometry.Vector3;
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
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.ModelData;
import org.apache.commons.lang3.NotImplementedException;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Consumer;

import static net.minecraft.client.renderer.LevelRenderer.DIRECTIONS;

public class ForgeRenderingUtil extends RenderingUtil {
    private static final ModelBakery modelBakery = Minecraft.getInstance().getModelManager().getModelBakery();
    private static final ModelBaker modelBaker = modelBakery.new ModelBakerImpl(
            (l, r) -> {
                throw new NotImplementedException();
            },
            null
    );

    @Override
    public BakedModel loadModel(ResourceLocation location) {
        return modelBakery.getModel(location).bake(
                modelBaker,
                m -> Minecraft.getInstance().getTextureAtlas(m.atlasLocation()).apply(m.texture()),
                new SimpleModelState(Transformation.identity()),
                location
        );
    }

    @Override
    public BakedModel loadModel(ResourceLocation modelLocation, ResourceLocation textureLocation) {
        final ResourceLocation textLoc = trim(textureLocation);
        return modelBakery.getModel(modelLocation).bake(
                modelBaker,
                m -> Minecraft.getInstance().getTextureAtlas(m.atlasLocation()).apply(textLoc),
                new SimpleModelState(Transformation.identity()),
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
                new SimpleModelState(Transformation.identity()),
                modelLocation
        );
    }

    @Override
    public void renderGui(BakedModel model, PoseStack matrixStack, int[] tints, Vector3 offset, Angle yaw, VertexConsumer builder, RenderType renderType, int combinedLight, int combinedOverlay, Consumer<PoseStack> alsoDo)  {
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
                for(Direction dir : allDirections) {
                    random.setSeed(42L);
                    for(BakedQuad quad: model.getQuads(null, dir, random, ModelData.EMPTY, renderType)) {
                        float r = 1;
                        float g = 1;
                        float b = 1;
                        if(quad.isTinted()) {
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
        boolean useAmbientOcclusion = Minecraft.useAmbientOcclusion() && state.getLightEmission(level, pos) == 0 && model.useAmbientOcclusion();
        var vec3 = state.getOffset(level, pos);
        blockToView.translate(vec3.x, vec3.y, vec3.z);
        var modelData = model.getModelData(level, pos, state, ModelData.EMPTY);
        try {
            return tesselate(level, model, state, tints, pos, blockToView, localToBlock, vertexConsumer, checkSides, random, combinedLight, combinedOverlay, modelData, useAmbientOcclusion);
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
            ModelData modelData,
            boolean useAmbientOcclusion) {
        boolean flag = false;
        float[] aoValues = useAmbientOcclusion ? new float[DIRECTIONS.length * 2] : null;
        BitSet bitset = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace aoFace = useAmbientOcclusion ? new ModelBlockRenderer.AmbientOcclusionFace() : null;
        BlockPos.MutableBlockPos mutablePos = pos.mutable();

        var quadLocalToBlock = QuadTransformers.applying(new Transformation(localToBlock));

        for(Direction direction : DIRECTIONS) {
            random.setSeed(combinedLight);
            List<BakedQuad> list = model.getQuads(state, direction, random, modelData, null);
            if (!list.isEmpty()) {
                mutablePos.setWithOffset(pos, direction);
                if (!checkSides || Block.shouldRenderFace(state, level, pos, direction, mutablePos)) {
                    if(useAmbientOcclusion)
                        renderModelFaceAO(level, state, tints, pos, blockToView, localToBlock, vertexConsumer, list, aoValues, bitset, aoFace, combinedOverlay, quadLocalToBlock);
                    else
                        renderModelWithoutAo(level, state, tints, pos, LevelRenderer.getLightColor(level, state, mutablePos), combinedOverlay, false, blockToView, localToBlock, vertexConsumer, list, bitset, quadLocalToBlock);
                    flag = true;
                }
            }
        }

        random.setSeed(combinedLight);
        List<BakedQuad> quads = model.getQuads(state, null, random, modelData, null);
        if (!quads.isEmpty()) {
            if(useAmbientOcclusion)
                renderModelFaceAO(level, state, tints, pos, blockToView, localToBlock, vertexConsumer, quads, aoValues, bitset, aoFace, combinedOverlay, quadLocalToBlock);
            else
                renderModelWithoutAo(level, state, tints, pos, -1, combinedOverlay, true, blockToView, localToBlock, vertexConsumer, quads, bitset, quadLocalToBlock);
            flag = true;
        }

        return flag;
    }

    private void renderModelFaceAO(BlockAndTintGetter level, BlockState state, int[] tints, BlockPos pos, PoseStack blockToView, Matrix4f localToBlock, VertexConsumer vertexConsumer, List<BakedQuad> quads, float[] aoFloats, BitSet bitset, ModelBlockRenderer.AmbientOcclusionFace aoFace, int combinedOverlay, IQuadTransformer quadLocalToBlock) {
        var poseMatrix = blockToView.last();
        for(BakedQuad bakedquad : quads) {
            bakedquad = transform(bakedquad, quadLocalToBlock, localToBlock);
            var shadingQuad = clampWithinUnitCube(bakedquad);
            Renderer.get().calculateShape(level, state, pos, shadingQuad.getVertices(), shadingQuad.getDirection(), aoFloats, bitset);
            if (!ForgeHooksClient.calculateFaceWithoutAO(level, state, pos, bakedquad, bitset.get(0), aoFace.brightness, aoFace.lightmap))
                aoFace.calculate(level, state, pos, shadingQuad.getDirection(), aoFloats, bitset, shadingQuad.isShade());
            putQuadData(tints, vertexConsumer, poseMatrix, bakedquad, aoFace.brightness[0], aoFace.brightness[1], aoFace.brightness[2], aoFace.brightness[3], aoFace.lightmap[0], aoFace.lightmap[1], aoFace.lightmap[2], aoFace.lightmap[3], combinedOverlay);
        }

    }

    private void renderModelWithoutAo(BlockAndTintGetter level, BlockState state, int[] tints, BlockPos pos, int lightColor, int combinedOverlay, boolean p_111007_, PoseStack blockToView, Matrix4f localToBlock, VertexConsumer vertexConsumer, List<BakedQuad> quads, BitSet bitSet, IQuadTransformer quadLocalToBlock) {
        var poseMatrix = blockToView.last();
        for(BakedQuad bakedquad : quads) {
            bakedquad = transform(bakedquad, quadLocalToBlock, localToBlock);
            if (p_111007_) {
                var shadingQuad = clampWithinUnitCube(bakedquad);
                Renderer.get().calculateShape(level, state, pos, shadingQuad.getVertices(), shadingQuad.getDirection(), (float[])null, bitSet);
                BlockPos blockpos = bitSet.get(0) ? pos.relative(shadingQuad.getDirection()) : pos;
                lightColor = LevelRenderer.getLightColor(level, state, blockpos);
            }

            float f = level.getShade(bakedquad.getDirection(), bakedquad.isShade());
            putQuadData(tints, vertexConsumer, poseMatrix, bakedquad, f, f, f, f, lightColor, lightColor, lightColor, lightColor, combinedOverlay);
        }
    }

    private BakedQuad transform(BakedQuad original, IQuadTransformer transformer, Matrix4f matrix4f) {
        var copy = transformer.process(original);
        var dir = transform(original.getDirection(), matrix4f);
        return new BakedQuad(copy.getVertices(), copy.getTintIndex(), dir, copy.getSprite(), copy.isShade());
    }
}
