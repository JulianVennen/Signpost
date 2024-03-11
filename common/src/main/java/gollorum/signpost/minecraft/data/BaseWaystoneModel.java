package gollorum.signpost.minecraft.data;

import gollorum.signpost.Signpost;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseWaystoneModel {
    public static final ResourceLocation inPostLocation = new ResourceLocation(Signpost.MOD_ID, "block/in_post_waystone");

    abstract public void registerModels();
}
