package gollorum.signpost.compat;

import gollorum.signpost.Signpost;

public class Compat {

    public static final String WaystonesId = "waystones";
    public static final String AntiqueAtlasId = "antiqueatlas";
    public static final String RepurposedStructuresId = "repurposed_structures";

    public static void register() {

//        if (Signpost.isModLoaded(Compat.AntiqueAtlasId))
//            AntiqueAtlasAdapter.register();

        if (Signpost.isModLoaded(Compat.RepurposedStructuresId))
            RepurposedStructuresAdapter.register();
    }

}
