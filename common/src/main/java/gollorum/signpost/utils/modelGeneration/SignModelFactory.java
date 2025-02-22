package gollorum.signpost.utils.modelGeneration;

import gollorum.signpost.minecraft.gui.utils.TextureResource;
import gollorum.signpost.utils.Tuple;
import gollorum.signpost.utils.math.geometry.Vector3;
import net.minecraft.core.Direction;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SignModelFactory<TextureIdentifier> {

    private final List<Cube<TextureIdentifier>> cubes = new ArrayList<>();

    private static final float overlayOffset = 0.1f;

    public SignModelFactory<TextureIdentifier> addCube(Vector3 min, Vector3 max, Map<Direction, FaceData<TextureIdentifier>> sides) {
        cubes.add(new Cube<>(min, max, sides));
        return this;
    }

    public SignModelFactory<TextureIdentifier> makePartialCube(
        Vector3 min,
        Vector3 size,
        float minU,
        float minV,
        boolean clampCoords,
        Collection<CubeFacesData<TextureIdentifier>> directions
    ) {
        Vector3 max = min.add(size);
        int textureDepth = Math.round(size.z);
        float maxFrontU = minU + Math.round(size.x);
        float maxFrontV = minV + Math.round(size.y);
        addCube(min, max, directions.stream().collect(Collectors.toMap(
            cubeFacesData -> cubeFacesData.direction(),
            cubeFacesData -> {
                TextureArea texCoords = switch (cubeFacesData.direction()) {
                    case DOWN -> new TextureArea(
                        new TextureSegment(minU, maxFrontU, clampCoords),
                        new TextureSegment(maxFrontV, maxFrontV + textureDepth, clampCoords));
                    case UP -> new TextureArea(
                        new TextureSegment(minU, maxFrontU, clampCoords),
                        new TextureSegment(minV - textureDepth, minV, clampCoords));
                    case SOUTH -> new TextureArea(
                        new TextureSegment(minU, maxFrontU, clampCoords),
                        new TextureSegment(minV, maxFrontV, clampCoords));
                    case NORTH -> new TextureArea(
                        new TextureSegment(maxFrontU, minU, clampCoords),
                        new TextureSegment(minV, maxFrontV, clampCoords));
                    case WEST -> new TextureArea(
                        new TextureSegment(minU - textureDepth, minU, clampCoords),
                        new TextureSegment(minV, maxFrontV, clampCoords));
                    case EAST -> new TextureArea(
                        new TextureSegment(maxFrontU, maxFrontU + textureDepth, clampCoords),
                        new TextureSegment(minV, maxFrontV, clampCoords));
                };
                return new FaceData<>(texCoords, cubeFacesData.rotation(), cubeFacesData.texture(), cubeFacesData.textureSize(), cubeFacesData.tintIndex());
            })));
        return this;
    }

    public SignModelFactory<TextureIdentifier> makeSliceWithRim(
        Vector3 min, Vector3 size,
        int rimHeight,
        float uMin, float vMin,
        boolean shouldRenderWest, boolean shouldRenderEast,
        boolean isBothSided,
        FaceRotation mainTextureRotation, TextureIdentifier secondaryTexture,
        FaceRotation secondaryTextureRotation, TextureIdentifier mainTexture,
        int textureSize
    ) {
        assert size.y > 2;
        Predicate<Direction> sideNotCulled = d ->
            (!d.equals(Direction.WEST) || shouldRenderWest) &&
                (!d.equals(Direction.EAST) || shouldRenderEast);
        makePartialCube(
            min,
            new Vector3(size.x, rimHeight, size.z),
            uMin, vMin + size.y - rimHeight, true,
            CubeFacesData.all(secondaryTexture, secondaryTextureRotation, textureSize, 1, d -> sideNotCulled.test(d) && !d.equals(Direction.UP))
        );
        makePartialCube(
            min.withY(y -> y + size.y - rimHeight),
            new Vector3(size.x, rimHeight, size.z),
            uMin, vMin, true,
            CubeFacesData.all(secondaryTexture, secondaryTextureRotation, textureSize, 1, d -> sideNotCulled.test(d) && !d.equals(Direction.DOWN))
        );
        makePartialCube(
            min.withY(y -> y + rimHeight),
            new Vector3(size.x, size.y - 2 * rimHeight, size.z),
            uMin, vMin + rimHeight, true,
            CubeFacesData.from(d ->
                d.equals(Direction.SOUTH) || isBothSided
                    ? Optional.of(Tuple.of(Tuple.of(mainTexture, mainTextureRotation), Tuple.of(textureSize, 0)))
                    : d.equals(Direction.NORTH)
                        ? Optional.of(Tuple.of(Tuple.of(secondaryTexture, secondaryTextureRotation), Tuple.of(textureSize, 1)))
                        : Optional.empty())
        );
        return this;
    }

    public SignModelFactory<TextureIdentifier> makeSignOverlayAt(
        Vector3 center,
        float height,
        float widthLeft,
        float widthRight,
        float arrowWidth,
        boolean shouldAddLeftRim,
        TextureIdentifier mainTexture,
        int textureSize
    ) {
        if(shouldAddLeftRim) makePartialCube(
            center.add(-widthLeft - 1 - overlayOffset, -height / 2 - overlayOffset, -0.5f - overlayOffset),
            new Vector3(1, height + 2 * overlayOffset - 1, 1 + 2 * overlayOffset),
            1, 2, false,
            CubeFacesData.all(mainTexture, FaceRotation.Zero, textureSize, 0, dir -> dir != Direction.EAST)
        );
        makePartialCube(
            center.add(-widthLeft - overlayOffset, -height / 2 - overlayOffset - 1, -0.5f - overlayOffset),
            new Vector3(widthLeft + widthRight + 2 * overlayOffset, height + 1 + 2 * overlayOffset, 1 + 2 * overlayOffset),
            2, 1, false,
            CubeFacesData.all(mainTexture, FaceRotation.Zero, textureSize, 0, dir -> true)
        );
        int stairsCount = Math.round(Math.min((height - 2) / 2, arrowWidth));
        int stairStep = Math.round((height - 2) / (2 * stairsCount));
        float stairsWidth = arrowWidth / (float) stairsCount;
        for(int i = 0; i < stairsCount; i++) {
            makePartialCube(
                center.add(widthRight + stairsWidth * i + overlayOffset, -height / 2 - overlayOffset + stairStep * i, -0.5f - overlayOffset),
                new Vector3(stairsWidth, height + 1 + 2 * overlayOffset - 2 * (1 + stairStep * i), 1 + 2 * overlayOffset),
                2 + widthLeft + widthRight + stairsWidth * i, 2 + stairStep * i, false,
                CubeFacesData.all(mainTexture, FaceRotation.Zero, textureSize, 0, dir -> dir != Direction.WEST)
            );
        }
        return this;
    }

    private static final FaceRotation signTextureRotation = FaceRotation.Clockwise90;

    public SignModelFactory<TextureIdentifier> makeSignAt(
        Vector3 center,
        float height,
        float widthLeft, float widthRight,
        float arrowWidth,
        boolean shouldAddLeftRim, boolean isBothSided,
        TextureIdentifier secondaryTexture, TextureIdentifier mainTexture
    ) {
        if(shouldAddLeftRim) makePartialCube(
            center.add(-widthLeft - 1, -height / 2 + 1, -0.5f),
            new Vector3(1, height - 2, 1),
            15, 8 - height / 2 + 1, true,
            CubeFacesData.uniform(secondaryTexture, signTextureRotation, TextureResource.defaultTextureSize, 1, Direction.UP, Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.SOUTH)
        );
        if(widthLeft + widthRight > 16){
            makeSliceWithRim(
                center.add(-widthLeft, -height / 2, -0.5f),
                new Vector3(16, height, 1),
                1,
                0, 8 - height / 2,
                true, false,
                isBothSided, signTextureRotation, secondaryTexture, signTextureRotation, mainTexture,
                TextureResource.defaultTextureSize
            );
            makeSliceWithRim(
                center.add(16 - widthLeft, -height / 2, -0.5f),
                new Vector3(widthLeft + widthRight - 16, height, 1),
                1,
                0, 8 - height / 2,
                false, true,
                isBothSided, signTextureRotation, secondaryTexture, signTextureRotation, mainTexture,
                TextureResource.defaultTextureSize
            );
        } else makeSliceWithRim(
            center.add(-widthLeft, -height / 2, -0.5f),
            new Vector3(widthLeft + widthRight, height, 1),
            1,
            0, 8 - height / 2,
            true, true,
            isBothSided, signTextureRotation, secondaryTexture, signTextureRotation, mainTexture,
            TextureResource.defaultTextureSize
        );
        int stairsCount = Math.round(Math.min((height - 2) / 2, arrowWidth));
        int stairStep = Math.round((height - 2) / (2 * stairsCount));
        float stairsWidth = arrowWidth / stairsCount;
        for(int i = 0; i < stairsCount - 1; i++) {
            makeSliceWithRim(
                center.add(widthRight + stairsWidth * i, -height / 2 + 1 + stairStep * i, -0.5f),
                new Vector3(stairsWidth, height - 2 * (1 + stairStep * i), 1),
                stairStep,
                (widthLeft + widthRight + Math.round(stairsWidth) * i) % 16, 8 - height / 2 + 1 + stairStep * i,
                false, true,
                isBothSided, signTextureRotation, secondaryTexture, signTextureRotation, mainTexture,
                TextureResource.defaultTextureSize
            );
        }
        int lastI = stairsCount - 1;
        makePartialCube(
            center.add(widthRight + stairsWidth * lastI, -height / 2 + 1 + stairStep * lastI, -0.5f),
            new Vector3(stairsWidth, height - 2 * (1 + stairStep * lastI), 1),
            (widthLeft + widthRight + Math.round(stairsWidth) * lastI) % 16, 8 - height / 2 + 1 + stairStep * lastI, true,
            CubeFacesData.uniform(secondaryTexture, signTextureRotation, TextureResource.defaultTextureSize, 1,
                Direction.UP, Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.SOUTH)
        );
        return this;
    }

    public SignModelFactory<TextureIdentifier> makeWideSign(
        TextureIdentifier mainTexture,
        TextureIdentifier secondaryTexture
    ) {
        return makeWideSign(Vector3.ZERO, mainTexture, secondaryTexture);
    }

    public SignModelFactory<TextureIdentifier> makeWideSign(
        Vector3 offset,
        TextureIdentifier mainTexture,
        TextureIdentifier secondaryTexture
    ) {
        return makeSignAt(offset.add(0, 0, 2.5f), 6, 8, 12, 4, true,
            false, secondaryTexture, mainTexture);
    }

    public SignModelFactory<TextureIdentifier> makeShortSign(
        TextureIdentifier mainTexture,
        TextureIdentifier secondaryTexture
    ) {
        return makeSignAt(new Vector3(0, 0, 0), 6, -2, 14, 4, false,
            true, secondaryTexture, mainTexture);
    }

    public SignModelFactory<TextureIdentifier> makeLargeSign(
        TextureIdentifier mainTexture,
        TextureIdentifier secondaryTexture
    ) {
        return makeSignAt(new Vector3(0, 0, 2.5f), 12, 8, 10, 3, true,
            false, secondaryTexture, mainTexture);
    }

    public SignModelFactory<TextureIdentifier> makeWideSignOverlay(TextureIdentifier mainTexture) {
        return makeSignOverlayAt(new Vector3(0, 0, 2.5f), 6, 8, 12, 4, true, mainTexture, TextureResource.wideOverlaySize)
            .map(c -> c.withSides(s -> s.withTextureArea(ta -> ta.map(
                u -> u * (TextureResource.defaultTextureSize / (float) TextureResource.wideOverlaySize),
                v -> v * (TextureResource.defaultTextureSize / (float) TextureResource.wideOverlaySize)
            ))));
    }

    public SignModelFactory<TextureIdentifier> makeShortSignOverlay(TextureIdentifier mainTexture) {
        return makeSignOverlayAt(new Vector3(0, 0, 0), 6, -2, 14, 4, false, mainTexture, TextureResource.shortOverlaySize)
            .map(c -> c.withSides(s -> s.withTextureArea(ta -> ta.map(
                u -> u * (TextureResource.defaultTextureSize / (float) TextureResource.shortOverlaySize),
                v -> v * (TextureResource.defaultTextureSize / (float) TextureResource.shortOverlaySize)
            ))));
    }

    public SignModelFactory<TextureIdentifier> makeLargeSignOverlay(TextureIdentifier mainTexture) {
        return makeSignOverlayAt(new Vector3(0, 0, 2.5f), 12, 8, 10, 3, true, mainTexture, TextureResource.largeOverlaySize)
            .map(c -> c.withSides(s -> s.withTextureArea(ta -> ta.map(
                u -> u * (TextureResource.defaultTextureSize / (float) TextureResource.largeOverlaySize),
                v -> v * (TextureResource.defaultTextureSize / (float) TextureResource.largeOverlaySize)
            ))));
    }

    public SignModelFactory<TextureIdentifier> makePost(TextureIdentifier textureIdentifier) {
        return addCube(
            new Vector3(-2, 0, -2), new Vector3(2, 16, 2),
            Arrays.stream(Direction.values()).collect(Collectors.toMap(
                d -> d,
                d -> {
                    int offset = switch (d) {
                        case EAST -> 4;
                        case NORTH -> 8;
                        case WEST -> 12;
                        default -> 0;
                    };
                    TextureArea textureArea = switch (d) {
                        case UP -> new TextureArea(new TextureSegment(0, 4, false), new TextureSegment(16, 12, false));
                        case DOWN -> new TextureArea(new TextureSegment(0, 4, false), new TextureSegment(4, 0, false));
                        default -> new TextureArea(new TextureSegment(offset, offset + 4, false), new TextureSegment(0, 16, false));
                    };
                    return new FaceData<>(textureArea, FaceRotation.Zero, textureIdentifier, TextureResource.defaultTextureSize, 0);
                }
            ))
        );
    }

    public SignModelFactory<TextureIdentifier> map(Function<Cube<TextureIdentifier>, Cube<TextureIdentifier>> func) {
        SignModelFactory<TextureIdentifier> ret = new SignModelFactory<>();
        ret.cubes.addAll(cubes.stream().map(func).collect(Collectors.toList()));
        return ret;
    }

    public SignModelFactory<TextureIdentifier> flipZ() {
        return this.map(cube -> {
            Map<Direction, FaceData<TextureIdentifier>> sides = new HashMap<>();
            for(Map.Entry<Direction, FaceData<TextureIdentifier>> face : cube.sides.entrySet()) {
                Direction dir = face.getKey();
                FaceData<TextureIdentifier> faceData = face.getValue();
                sides.put(dir, new FaceData<>(
                    faceData.textureArea(),
                    faceData.rotation(),
                    faceData.texture(),
                    true,
                    faceData.textureSize(),
                    faceData.tintIndex()
                ));
            }
            return new Cube<>(
                cube.from.withZ(z -> -z),
                cube.to.withZ(z -> -z),
                sides
            );
        });
    }

    public <Result> Result build(Result builder, BiConsumer<Result, Cube<TextureIdentifier>> mkCube) {
        for(Cube<TextureIdentifier> cube: cubes) {
            mkCube.accept(builder, cube);
        }
        return builder;
    }
}
