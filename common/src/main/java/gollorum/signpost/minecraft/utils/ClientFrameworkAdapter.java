package gollorum.signpost.minecraft.utils;

import gollorum.signpost.Signpost;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ClientFrameworkAdapter {

    public static void showStatusMessage(Component message, boolean inActionBar) {
        LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) Signpost.logger().error("Client player was null, failed to show status message");
        else player.displayClientMessage(message, inActionBar);
    }

}
