package gollorum.signpost.minecraft.rendering;

import gollorum.signpost.Signpost;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public class FlippableModel {

	public final BakedModel model;
	public final BakedModel flippedModel;

	public FlippableModel(BakedModel model, BakedModel flippedModel) {
		this.model = model;
		this.flippedModel = flippedModel;
	}

	public BakedModel get(boolean isFlipped) { return isFlipped ? flippedModel : model; }

	public static FlippableModel loadFrom(ResourceLocation modelLocation, ResourceLocation modelLocationFlipped, ResourceLocation texture) {
		return new FlippableModel(
			Signpost.getRenderingUtil().loadModel(modelLocation, texture),
			Signpost.getRenderingUtil().loadModel(modelLocationFlipped, texture)
		);
	}

	public static FlippableModel loadFrom(ResourceLocation modelLocation, ResourceLocation modelLocationFlipped, ResourceLocation mainTexture, ResourceLocation secondaryTexture) {
		return new FlippableModel(
			Signpost.getRenderingUtil().loadModel(modelLocation, mainTexture, secondaryTexture),
			Signpost.getRenderingUtil().loadModel(modelLocationFlipped, mainTexture, secondaryTexture)
		);
	}

	public static FlippableModel loadSymmetrical(ResourceLocation modelLocation, ResourceLocation texture) {
		BakedModel model = Signpost.getRenderingUtil().loadModel(modelLocation, texture);
		return new FlippableModel(model, model);
	}

	public static FlippableModel loadSymmetrical(ResourceLocation modelLocation, ResourceLocation mainTexture, ResourceLocation secondaryTexture) {
		BakedModel model = Signpost.getRenderingUtil().loadModel(modelLocation, mainTexture, secondaryTexture);
		return new FlippableModel(model, model);
	}

}
