package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.client.key.EncaseKeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FluidPipeEncaseHandler {

	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {

		Level level = event.getLevel();
		Player player = event.getEntity();
		ItemStack stack = event.getItemStack();
		BlockPos startPos = event.getPos();

		if (level.isClientSide()) {
			return;
		}

		if (!EncaseKeyMapping.ACTIVATE.isDown()) {
			return;
		}

		if (player.isShiftKeyDown()) {
			return;
		}

		BlockState startState = level.getBlockState(startPos);

		if (!startState.is(AllBlocks.FLUID_PIPE.get())) {
			return;
		}

		if (!stack.is(AllBlocks.COPPER_CASING.asItem())) {
			return;
		}

		event.setCanceled(true);
		player.swing(event.getHand());
		level.playSound(
				null,
				startPos.getX() + 0.5,
				startPos.getY() + 1.0,
				startPos.getZ() + 0.5,
				SoundEvents.COPPER_PLACE,
				SoundSource.BLOCKS,
				3,
				1
		);

		int encaseLimit = 32;

		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new LinkedList<>();

		queue.add(startPos);
		visited.add(startPos);

		int count = 0;

		while (!queue.isEmpty() && count < encaseLimit) {

			BlockPos currentPos = queue.poll();
			BlockState currentState = level.getBlockState(currentPos);

			if (!currentState.is(AllBlocks.FLUID_PIPE.get())) {
				continue;
			}

			BlockState newState =
					AllBlocks.ENCASED_FLUID_PIPE.get().defaultBlockState();

			for (Property<?> property : currentState.getProperties()) {

				if (newState.hasProperty(property)) {
					newState = copyProperty(currentState, newState, property);
				}
			}

			level.setBlockAndUpdate(currentPos, newState);

			for (Direction direction : Direction.values()) {

				BlockPos nextPos = currentPos.relative(direction);

				if (visited.contains(nextPos)) {
					continue;
				}

				BlockState nextState = level.getBlockState(nextPos);

				if (nextState.is(AllBlocks.FLUID_PIPE.get())) {

					visited.add(nextPos);
					queue.add(nextPos);
				}
			}

			count++;
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