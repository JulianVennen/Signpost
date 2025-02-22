package gollorum.signpost.minecraft.rendering;

import gollorum.signpost.Signpost;
import gollorum.signpost.blockpartdata.Overlay;
import gollorum.signpost.blockpartdata.types.LargeSignBlockPart;
import gollorum.signpost.blockpartdata.types.SignBlockPart;
import gollorum.signpost.blockpartdata.types.SmallShortSignBlockPart;
import gollorum.signpost.blockpartdata.types.SmallWideSignBlockPart;
import gollorum.signpost.minecraft.data.BasePostModel;
import gollorum.signpost.utils.modelGeneration.SignModel;
import gollorum.signpost.utils.modelGeneration.SignModelFactory;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModelRegistry<M> {

	public static ModelRegistry<SignModel> LargeSign = new ModelRegistry<>(
		(mainTexture, secondaryTexture) -> new SignModelFactory<ResourceLocation>()
			.makeLargeSign(mainTexture, secondaryTexture).build(new SignModel(), SignModel::addCube),
		overlayTexture -> new SignModelFactory<ResourceLocation>()
			.makeLargeSignOverlay(overlayTexture).build(new SignModel(), SignModel::addCube),
		(mainTexture, secondaryTexture) -> new SignModelFactory<ResourceLocation>()
			.makeLargeSign(mainTexture, secondaryTexture)
			.flipZ()
			.build(new SignModel(), SignModel::addCube),
		overlayTexture -> new SignModelFactory<ResourceLocation>()
			.makeLargeSignOverlay(overlayTexture)
			.flipZ()
			.build(new SignModel(), SignModel::addCube),
		LargeSignBlockPart.class
	);

	public static ModelRegistry<SignModel> WideSign = new ModelRegistry<>(
		(mainTexture, secondaryTexture) -> new SignModelFactory<ResourceLocation>()
			.makeWideSign(mainTexture, secondaryTexture).build(new SignModel(), SignModel::addCube),
		overlayTexture -> new SignModelFactory<ResourceLocation>()
			.makeWideSignOverlay(overlayTexture).build(new SignModel(), SignModel::addCube),
		(mainTexture, secondaryTexture) -> new SignModelFactory<ResourceLocation>()
			.makeWideSign(mainTexture, secondaryTexture)
			.flipZ()
			.build(new SignModel(), SignModel::addCube),
		overlayTexture -> new SignModelFactory<ResourceLocation>()
			.makeWideSignOverlay(overlayTexture)
			.flipZ()
			.build(new SignModel(), SignModel::addCube),
		SmallWideSignBlockPart.class
	);

	public static ModelRegistry<SignModel> ShortSign = new ModelRegistry<>(
		(mainTexture, secondaryTexture) -> new SignModelFactory<ResourceLocation>()
			.makeShortSign(mainTexture, secondaryTexture).build(new SignModel(), SignModel::addCube),
		overlayTexture -> new SignModelFactory<ResourceLocation>()
			.makeShortSignOverlay(overlayTexture).build(new SignModel(), SignModel::addCube),
		(mainTexture, secondaryTexture) -> new SignModelFactory<ResourceLocation>()
			.makeShortSign(mainTexture, secondaryTexture)
			.flipZ()
			.build(new SignModel(), SignModel::addCube),
		overlayTexture -> new SignModelFactory<ResourceLocation>()
			.makeShortSignOverlay(overlayTexture)
			.flipZ()
			.build(new SignModel(), SignModel::addCube),
		SmallShortSignBlockPart.class
	);

	public static ModelRegistry<BakedModel> LargeBakedSign = new ModelRegistry<>(
		(mainTexture, secondaryTexture) -> Signpost.getRenderingUtil().loadModel(
			BasePostModel.largeLocation, mainTexture, secondaryTexture
		),
		overlayTexture -> Signpost.getRenderingUtil().loadModel(BasePostModel.largeOverlayLocation, overlayTexture),
		(mainTexture, secondaryTexture) -> Signpost.getRenderingUtil().loadModel(
			BasePostModel.largeFlippedLocation, mainTexture, secondaryTexture
		),
		overlayTexture -> Signpost.getRenderingUtil().loadModel(BasePostModel.largeOverlayFlippedLocation, overlayTexture),
		LargeSignBlockPart.class
	);

	public static ModelRegistry<BakedModel> WideBakedSign = new ModelRegistry<>(
		(mainTexture, secondaryTexture) -> Signpost.getRenderingUtil().loadModel(
			BasePostModel.wideLocation, mainTexture, secondaryTexture
		),
		overlayTexture -> Signpost.getRenderingUtil().loadModel(BasePostModel.wideOverlayLocation, overlayTexture),
		(mainTexture, secondaryTexture) -> Signpost.getRenderingUtil().loadModel(
			BasePostModel.wideFlippedLocation, mainTexture, secondaryTexture
		),
		overlayTexture -> Signpost.getRenderingUtil().loadModel(BasePostModel.wideOverlayFlippedLocation, overlayTexture),
		SmallWideSignBlockPart.class
	);

	public static ModelRegistry<BakedModel> ShortBakedSign = new ModelRegistry<>(
		(mainTexture, secondaryTexture) -> Signpost.getRenderingUtil().loadModel(
			BasePostModel.shortLocation, mainTexture, secondaryTexture
		),
		overlayTexture -> Signpost.getRenderingUtil().loadModel(BasePostModel.shortOverlayLocation, overlayTexture),
		(mainTexture, secondaryTexture) -> Signpost.getRenderingUtil().loadModel(
			BasePostModel.shortFlippedLocation, mainTexture, secondaryTexture
		),
		overlayTexture -> Signpost.getRenderingUtil().loadModel(BasePostModel.shortOverlayFlippedLocation, overlayTexture),
		SmallShortSignBlockPart.class
	);

	public interface ModelConstructor<M> {
		M makeModel(ResourceLocation mainTexture, ResourceLocation secondaryTexture);
	}

	public interface OverlayModelConstructor<M> {
		M makeOverlayModel(ResourceLocation overlayTexture);
	}

	private final Map<ResourceLocation, Map<ResourceLocation, M>> cachedModels = new ConcurrentHashMap<>();
	private final Map<ResourceLocation, M> cachedOverlayModels = new ConcurrentHashMap<>();

	private final Map<ResourceLocation, Map<ResourceLocation, M>> cachedFlippedModels = new ConcurrentHashMap<>();
	private final Map<ResourceLocation, M> cachedFlippedOverlayModels = new ConcurrentHashMap<>();

	private final ModelConstructor<M> modelConstructor;
	private final OverlayModelConstructor<M> overlayModelConstructor;

	private final ModelConstructor<M> flippedModelConstructor;
	private final OverlayModelConstructor<M> flippedOverlayModelConstructor;

	private final Class<? extends SignBlockPart> signClass;

	public ModelRegistry(
		ModelConstructor<M> modelConstructor,
		OverlayModelConstructor<M> overlayModelConstructor,
		ModelConstructor<M> flippedModelConstructor,
		OverlayModelConstructor<M> flippedOverlayModelConstructor,
		Class<? extends SignBlockPart> signClass
	) {
		this.modelConstructor = modelConstructor;
		this.overlayModelConstructor = overlayModelConstructor;
		this.flippedModelConstructor = flippedModelConstructor;
		this.flippedOverlayModelConstructor = flippedOverlayModelConstructor;
		this.signClass = signClass;
	}

	public M makeModel(SignBlockPart sign) {
		return (sign.isFlipped() ? cachedFlippedModels : cachedModels)
			.computeIfAbsent(sign.getMainTexture().location(), x -> new ConcurrentHashMap<>())
			.computeIfAbsent(sign.getSecondaryTexture().location(),
				x -> (sign.isFlipped() ? flippedModelConstructor : modelConstructor)
					.makeModel(sign.getMainTexture().location(), sign.getSecondaryTexture().location())
			);
	}

	public M makeOverlayModel(SignBlockPart sign, Overlay overlay) {
		ResourceLocation texture = overlay.textureFor(signClass);
		return (sign.isFlipped() ? cachedFlippedOverlayModels : cachedOverlayModels)
			.computeIfAbsent(texture,
				x -> (sign.isFlipped() ? flippedOverlayModelConstructor : overlayModelConstructor)
					.makeOverlayModel(texture));
	}

}
