package gollorum.signpost.forge.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import gollorum.signpost.forge.PlayerHandle;
import gollorum.signpost.WaystoneHandle;
import gollorum.signpost.forge.WaystoneLibrary;
import gollorum.signpost.forge.minecraft.config.Config;
import gollorum.signpost.forge.minecraft.gui.utils.Colors;
import gollorum.signpost.forge.minecraft.utils.LangKeys;
import gollorum.signpost.forge.minecraft.utils.TextComponents;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class DiscoverWaystone {

	public static ArgumentBuilder<CommandSourceStack, ?> register() {
		return Commands.literal("discover")
			.requires(source -> source.hasPermission(Config.Server.permissions.discoverPermissionLevel.get()))
			.then(Commands.argument("waystone", new WaystoneArgument())
				.requires(source -> {
					try {
						source.getPlayerOrException();
						return true;
					} catch (CommandSyntaxException e) {
						return false;
					}
				})
				.executes(context -> execute(
					context.getArgument("waystone", String.class),
					context.getSource().getPlayerOrException()
			)))
			.then(Commands.argument("waystone", new WaystoneArgument())
				.then(Commands.argument("player", EntityArgument.player())
					.executes(context -> execute(
						context.getArgument("waystone", String.class),
						EntityArgument.getPlayer(context, "player")
					))));
	}

	private static int execute(String name, ServerPlayer player) throws CommandSyntaxException {
		WaystoneHandle.Vanilla handle = WaystoneLibrary.getInstance().getHandleByName(name)
			.orElseThrow(() -> new SimpleCommandExceptionType(Component.translatable(LangKeys.waystoneNotFound, Colors.wrap(name, Colors.highlight))).create());
		if(WaystoneLibrary.getInstance().addDiscovered(new PlayerHandle(player), handle)) {
			player.sendSystemMessage(Component.translatable(LangKeys.discovered, TextComponents.waystone(player, name)));
		}
		return Command.SINGLE_SUCCESS;
	}

}
