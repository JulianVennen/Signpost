package gollorum.signpost.minecraft.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import gollorum.signpost.Signpost;
import gollorum.signpost.minecraft.commands.BlockRestrictions;
import gollorum.signpost.minecraft.commands.DiscoverWaystone;
import gollorum.signpost.minecraft.commands.ListWaystones;
import gollorum.signpost.minecraft.commands.Teleport;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;


public class CommandRegistry implements CommandRegistrationEvent {

	@Override
	public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, Commands.CommandSelection selection) {
		dispatcher.register(
			LiteralArgumentBuilder.<CommandSourceStack>literal(Signpost.MOD_ID)
				.then(ListWaystones.register())
				.then(DiscoverWaystone.register())
				.then(Teleport.register())
				.then(BlockRestrictions.register())
		);
	}

}
