package gollorum.signpost.forge.minecraft.registry;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gollorum.signpost.forge.Signpost;
import gollorum.signpost.forge.minecraft.commands.BlockRestrictions;
import gollorum.signpost.forge.minecraft.commands.DiscoverWaystone;
import gollorum.signpost.forge.minecraft.commands.ListWaystones;
import gollorum.signpost.forge.minecraft.commands.Teleport;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE;

@Mod.EventBusSubscriber(modid = Signpost.MOD_ID, bus = FORGE)
public class CommandRegistry {

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		dispatcher.register(
			LiteralArgumentBuilder.<CommandSourceStack>literal(Signpost.MOD_ID)
				.then(ListWaystones.register())
				.then(DiscoverWaystone.register())
				.then(Teleport.register())
				.then(BlockRestrictions.register())
		);
	}

}
