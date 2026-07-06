package dev.celestiacraft.interlocked.utils;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import dev.celestiacraft.interlocked.client.key.EncaseKeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

import static dev.celestiacraft.interlocked.config.CommonConfig.INTERLOCK_LIMIT;

public class InterlockHelper {
	public static boolean isActivated(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();

		if (!EncaseKeyMapping.ACTIVATE.isDown()) {
			return false;
		}
		if (player == null) {
			return false;
		}
		if (player.isShiftKeyDown()) {
			return false;
		}
		return true;
	}

	public static void confirmInteraction(PlayerInteractEvent.RightClickBlock event) {
		event.setCanceled(true);
		Player player = event.getEntity();
		if (player != null) {
			player.swing(event.getHand());
		}
	}

	public static Set<BlockPos> getLineTargets(BlockPos pos, Direction.Axis axis, Level level, Predicate<BlockState> matcher) {
		Set<BlockPos> targets = new HashSet<>();
		targets.add(pos);

		Vec3i offset = axis == Direction.Axis.X ? new Vec3i(1, 0, 0)
				: axis == Direction.Axis.Y ? new Vec3i(0, 1, 0)
				  : new Vec3i(0, 0, 1);

		for (int dir = -1; dir <= 1; dir += 2) {
			for (int i = 1; i < INTERLOCK_LIMIT.get(); i++) {
				BlockPos targetPos = pos.offset(offset.multiply(i * dir));
				BlockState targetState = level.getBlockState(targetPos);
				if (!matcher.test(targetState)) break;
				targets.add(targetPos);
			}
		}

		return targets;
	}

	public static Set<BlockPos> getConnectedTargets(
			Level level,
			BlockPos startPos,
			Predicate<BlockState> matcher
	) {
		Set<BlockPos> targets = new HashSet<>();
		Set<BlockPos> visited = new HashSet<>();
		Queue<BlockPos> queue = new LinkedList<>();

		queue.add(startPos);
		visited.add(startPos);

		while (!queue.isEmpty() && targets.size() < INTERLOCK_LIMIT.get()) {
			BlockPos currentPos = queue.poll();
			BlockState currentState = level.getBlockState(currentPos);

			if (!matcher.test(currentState)) {
				continue;
			}

			targets.add(currentPos);

			for (Direction direction : Direction.values()) {
				BlockPos nextPos = currentPos.relative(direction);
				if (visited.add(nextPos) && matcher.test(level.getBlockState(nextPos))) {
					queue.add(nextPos);
				}
			}
		}

		return targets;
	}

	@Nullable
	public static Direction.Axis getAxis(BlockState state) {
		return FluidPropagator.getStraightPipeAxis(state);
	}

	public static BlockState toRegularPipe(LevelAccessor world, BlockPos pos, BlockState state) {
		Direction side = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(BlockStateProperties.AXIS));
		Map<Direction, BooleanProperty> facingToPropertyMap = FluidPipeBlock.PROPERTY_BY_DIRECTION;
		return AllBlocks.FLUID_PIPE.get()
				.updateBlockState(AllBlocks.FLUID_PIPE.getDefaultState()
						.setValue(facingToPropertyMap.get(side), true)
						.setValue(facingToPropertyMap.get(side.getOpposite()), true), side, null, world, pos);
	}
}
