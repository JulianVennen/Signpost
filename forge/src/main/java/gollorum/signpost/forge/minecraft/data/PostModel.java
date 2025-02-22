package gollorum.signpost.forge.minecraft.data;

import gollorum.signpost.Signpost;
import gollorum.signpost.blockpartdata.Overlay;
import gollorum.signpost.blockpartdata.types.LargeSignBlockPart;
import gollorum.signpost.blockpartdata.types.SmallShortSignBlockPart;
import gollorum.signpost.blockpartdata.types.SmallWideSignBlockPart;
import gollorum.signpost.minecraft.block.PostBlock;
import gollorum.signpost.minecraft.data.BasePostModel;
import gollorum.signpost.utils.math.geometry.Vector3;
import gollorum.signpost.utils.modelGeneration.Cube;
import gollorum.signpost.utils.modelGeneration.FaceData;
import gollorum.signpost.utils.modelGeneration.FaceRotation;
import gollorum.signpost.utils.modelGeneration.SignModelFactory;
import gollorum.signpost.utils.modelGeneration.TextureArea;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.*;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PostModel extends BasePostModel {
    private static final String texturePost = "post";
    private static final ResourceLocation previewLocation = new ResourceLocation(Signpost.MOD_ID, "block/post_preview");

    public final Map<PostBlock.Variant, BlockModelBuilder> allModels;

    private static final ModelBuilder.FaceRotation mainTextureRotation = ModelBuilder.FaceRotation.CLOCKWISE_90;
    private static final ModelBuilder.FaceRotation secondaryTextureRotation = ModelBuilder.FaceRotation.CLOCKWISE_90;

    private final BlockModelBuilder previewModel;
    private final BlockModels blockModelProvider;

    public PostModel(BlockModels blockModelProvider) {
        previewModel = new BlockModelBuilder(previewLocation, blockModelProvider.existingFileHelper);
        allModels = PostBlock.AllVariants.stream().collect(Collectors.<PostBlock.Variant, PostBlock.Variant, BlockModelBuilder>toMap(
            i -> i,
            i -> new BlockModelBuilder(new ResourceLocation(Signpost.MOD_ID, "block/" + i.registryName), blockModelProvider.existingFileHelper)
        ));
        this.blockModelProvider = blockModelProvider;
//        generator.addProvider(true, new Item(output, fileHelper));
    }

    public static class Item {

        public static void registerModels(Function<String, ItemModelBuilder> getBuilder, PostModel postModelProvider) {
            for (Map.Entry<PostBlock.Variant, BlockModelBuilder> entry : postModelProvider.allModels.entrySet()) {
                getBuilder.apply(entry.getKey().registryName)
                    .parent(new ModelFile.UncheckedModelFile("builtin/entity"))
                    .transforms()
                        .transform(ItemDisplayContext.GUI)
                            .rotation(30, 35, 0)
                            .translation(0, 0, 0)
                            .scale(0.625f)
                        .end()
                        .transform(ItemDisplayContext.GROUND)
                            .rotation(0, 0, 0)
                            .translation(0, 3, 0)
                            .scale(0.25f)
                        .end()
                        .transform(ItemDisplayContext.FIXED)
                            .rotation(0, 180, 0)
                            .translation(0, 0, 0)
                            .scale(0.5f)
                        .end()
                        .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                            .rotation(75, 135, 0)
                            .translation(0, 2.5f, 0)
                            .scale(0.375f)
                        .end()
                        .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                            .rotation(0, 315, 0)
                            .translation(0, 0, 0)
                            .scale(0.4f)
                        .end()
                        .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                            .rotation(0, 315, 0)
                            .translation(0, 0, 0)
                            .scale(0.4f)
                        .end()
                    .end();
//                    .parent(entry.getValue());
            }
        }
    }

    private BlockModelBuilder getBuilder(String path) { return blockModelProvider.getBuilder(path); }

    public void registerModels() {
        BlockModelBuilder previewBuilder = getBuilder(previewLocation.toString())
            .parent(new ModelFile.ExistingModelFile(new ResourceLocation("block/block"), blockModelProvider.existingFileHelper))
            .transforms()
                .transform(ItemDisplayContext.GUI)
                    .rotation(30, 315, 0)
                    .scale(0.625f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                    .rotation(0, 315, 0)
                    .scale(0.4f)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                    .rotation(0, 315, 0)
                    .scale(0.4f)
                .end()
            .end();
        makePostAt(new Vector3(8, 8, 8), previewBuilder);
        new SignModelFactory<String>().makeWideSign(new Vector3(8, 12, 8), "#" + textureSign, "#" + secondaryTexture)
            .build(previewBuilder, Builder.BlockModel);

        makePostAt(new Vector3(0, 8, 0), getBuilder(postLocation.toString()))
            .texture(texturePost, PostBlock.ModelType.Oak.postTexture.location());

        buildDefaultAndFlipped(
            new SignModelFactory<String>().makeWideSign("#" + textureSign, "#" + secondaryTexture),
            wideLocation,
            wideFlippedLocation
        );

        buildDefaultAndFlipped(
            new SignModelFactory<String>().makeShortSign("#" + textureSign, "#" + secondaryTexture),
            shortLocation,
            shortFlippedLocation
        );

        buildDefaultAndFlipped(
            new SignModelFactory<String>().makeLargeSign("#" + textureSign, "#" + secondaryTexture),
            largeLocation,
            largeFlippedLocation
        );

        buildDefaultAndFlippedOverlay(
            new SignModelFactory<String>().makeWideSignOverlay("#" + textureSign),
            wideOverlayLocation, wideOverlayFlippedLocation,
            Overlay.Gras.textureFor(SmallWideSignBlockPart.class)
        );
        buildDefaultAndFlippedOverlay(
            new SignModelFactory<String>().makeShortSignOverlay("#" + textureSign),
            shortOverlayLocation, shortOverlayFlippedLocation,
            Overlay.Gras.textureFor(SmallShortSignBlockPart.class)
        );
        buildDefaultAndFlippedOverlay(
            new SignModelFactory<String>().makeLargeSignOverlay("#" + textureSign),
            largeOverlayLocation, largeOverlayFlippedLocation,
            Overlay.Gras.textureFor(LargeSignBlockPart.class)
        );

        for(PostBlock.Variant variant : PostBlock.AllVariants) {
            getBuilder(variant.registryName)
                .parent(previewModel)
                .texture("particle", variant.type.postTexture.location())
                .texture(texturePost, variant.type.postTexture.location())
                .texture(textureSign, variant.type.mainTexture.location())
                .texture(secondaryTexture, variant.type.secondaryTexture.location());
        }
    }

    private void buildDefaultAndFlipped(SignModelFactory<String> factory, ResourceLocation main, ResourceLocation flipped) {
        factory.build(getBuilder(main.toString()), Builder.BlockModel)
            .texture(textureSign, mainTextureMarker)
            .texture(secondaryTexture, PostBlock.ModelType.Oak.secondaryTexture.location());
        factory.build(getBuilder(flipped.toString()), Builder.BlockModelFlipped)
            .texture(textureSign, mainTextureMarker)
            .texture(secondaryTexture, PostBlock.ModelType.Oak.secondaryTexture.location());
    }

    private void buildDefaultAndFlippedOverlay(SignModelFactory<String> factory, ResourceLocation main, ResourceLocation flipped, ResourceLocation texture) {
        factory.build(getBuilder(main.toString()), Builder.BlockModel)
            .texture(textureSign, texture);
        factory.build(getBuilder(flipped.toString()), Builder.BlockModelFlipped)
            .texture(textureSign, texture);
    }

    private static BlockModelBuilder makePostAt(Vector3 center, BlockModelBuilder builder) {
        builder
            .element()
                .from(center.x - 2, center.y - 8, center.z - 2)
                .to(center.x + 2, center.y + 8, center.z + 2)
            .face(Direction.SOUTH)
                .texture("#" + texturePost)
                .uvs(0, 0, 4, 16)
                .tintindex(0)
            .end()
            .face(Direction.EAST)
                .texture("#" + texturePost)
                .uvs(4, 0, 8, 16)
                .tintindex(0)
            .end()
            .face(Direction.NORTH)
                .texture("#" + texturePost)
                .uvs(8, 0, 12, 16)
                .tintindex(0)
            .end()
            .face(Direction.WEST)
                .texture("#" + texturePost)
                .uvs(12, 0, 16, 16)
                .tintindex(0)
            .end()
            .face(Direction.DOWN)
                .texture("#" + texturePost)
                .uvs(0, 4, 4, 0)
                .tintindex(0)
            .end()
            .face(Direction.UP)
                .texture("#" + texturePost)
                .uvs(0, 16, 4, 12)
                .tintindex(0)
            .end();
        return builder;
    }

    public static class Builder {
        private static ModelBuilder.FaceRotation convertRotation(FaceRotation rotation) {
            return switch (rotation) {
                case Zero -> ModelBuilder.FaceRotation.ZERO;
                case Clockwise90 -> ModelBuilder.FaceRotation.CLOCKWISE_90;
                case CounterClockwise90 -> ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90;
                case UpsideDown -> ModelBuilder.FaceRotation.UPSIDE_DOWN;
            };
        }

        public static final BiConsumer<BlockModelBuilder, Cube<String>> BlockModel = (b, cube) -> {
            BlockModelBuilder.ElementBuilder builder = b.element()
                    .from(cube.from.x, cube.from.y, cube.from.z)
                    .to(cube.to.x, cube.to.y, cube.to.z);
            for(Map.Entry<Direction, FaceData<String>> face: cube.sides.entrySet()) {
                Direction dir = face.getKey();
                FaceData<String> faceData = face.getValue();
                TextureArea textureArea = faceData.textureArea().rotate(faceData.rotation(), true);
                ModelBuilder<BlockModelBuilder>.ElementBuilder.FaceBuilder faceBuilder = builder.face(dir)
                        .texture(faceData.texture())
                        .uvs(textureArea.u.from, textureArea.v.from, textureArea.u.to, textureArea.v.to)
                        .tintindex(faceData.tintIndex());
                if(!faceData.rotation().equals(FaceRotation.Zero))
                    faceBuilder.rotation(convertRotation(faceData.rotation()));
            }
        };

        public static final BiConsumer<BlockModelBuilder, Cube<String>> BlockModelFlipped = (b, cube) -> {
            BlockModelBuilder.ElementBuilder builder = b.element()
                    .from(cube.from.x, cube.from.y, -cube.to.z)
                    .to(cube.to.x, cube.to.y, -cube.from.z);
            for(Map.Entry<Direction, FaceData<String>> face: cube.sides.entrySet()) {
                Direction dir = face.getKey();
                Direction.Axis axis = dir.getAxis();
                FaceData<String> faceData = face.getValue();
                TextureArea textureArea = faceData.textureArea();
                if(axis.equals(Direction.Axis.Z)) {
                    dir = dir.getOpposite();
                    textureArea = textureArea.flipU();
                } else if(axis.equals(Direction.Axis.X)) {
                    textureArea = textureArea.flipU();
                } else {
                    textureArea = textureArea.flipV();
                }
                textureArea = textureArea.rotate(faceData.rotation(), true);
                ModelBuilder<BlockModelBuilder>.ElementBuilder.FaceBuilder faceBuilder = builder.face(dir)
                        .texture(faceData.texture())
                        .uvs(textureArea.u.from, textureArea.v.from, textureArea.u.to, textureArea.v.to)
                        .tintindex(faceData.tintIndex());;
                if(!faceData.rotation().equals(FaceRotation.Zero))
                    faceBuilder.rotation(convertRotation(faceData.rotation()));
            }
        };
    }
}
