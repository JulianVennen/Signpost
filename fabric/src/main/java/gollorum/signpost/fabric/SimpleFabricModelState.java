package gollorum.signpost.fabric;

import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import org.jetbrains.annotations.NotNull;

public class SimpleFabricModelState implements ModelState {
    private final Transformation transformation;

    public SimpleFabricModelState(Transformation transformation) {
        this.transformation = transformation;
    }

    @Override
    public @NotNull Transformation getRotation() {
        return transformation;
    }

}
