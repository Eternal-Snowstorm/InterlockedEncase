package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.utils.InterlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

import static dev.celestiacraft.interlocked.utils.InterlockHelper.confirmInteraction;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EncasedPipeWrenchHandler {

	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {

		Level level = event.getLevel();
		ItemStack stack = event.getItemStack();
		BlockPos startPos = event.getPos();

		if (level.isClientSide()) {
			return;
		}
		if (!InterlockHelper.isActivated(event)) {
			return;
		}

		BlockState startState = level.getBlockState(startPos);

		if (!startState.is(AllBlocks.ENCASED_FLUID_PIPE.get())) {
			return;
		}

		if (!stack.is(AllItems.WRENCH.get())) {
			return;
		}

		confirmInteraction(event);

		Set<BlockPos> targets = InterlockHelper.getConnectedTargets(
				level,
				startPos,
				targetState -> targetState.is(AllBlocks.ENCASED_FLUID_PIPE.get())
		);

		for (BlockPos currentPos : targets) {
			BlockState currentState = level.getBlockState(currentPos);

			if (!currentState.is(AllBlocks.ENCASED_FLUID_PIPE.get())) {
				continue;
			}

			BlockState newState =
					AllBlocks.FLUID_PIPE.get().defaultBlockState();

			for (Property<?> property : currentState.getProperties()) {

				if (newState.hasProperty(property)) {
					newState = copyProperty(currentState, newState, property);
				}
			}

			level.destroyBlock(currentPos, false);
			level.setBlockAndUpdate(currentPos, newState);
		}
	}

	@SuppressWarnings( {"rawtypes", "unchecked"})
	private static BlockState copyProperty(
			BlockState from,
			BlockState to,
			Property property
	) {
		return to.setValue(property, from.getValue(property));
	}
}
