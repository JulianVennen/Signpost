package gollorum.signpost;

import gollorum.signpost.utils.WaystoneLocationData;

public interface WaystoneDataBase {

    String name();
    WaystoneLocationData loc();

    WaystoneHandle handle();

}
