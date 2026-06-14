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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InterlockHelper {
	public static boolean isActivated(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
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

	public static Set<BlockPos> getInterlocked(BlockPos pos, Block targetBlock, Direction.Axis axis, Level level) {
		Set<BlockPos> interlockedTargets = new HashSet<>();
		interlockedTargets.add(pos);
		Vec3i offset;
		int encaseLimit = 32;

		if (axis == Direction.Axis.X) offset = new Vec3i(1, 0, 0);
		else if (axis == Direction.Axis.Y) offset = new Vec3i(0, 1, 0);
		else offset = new Vec3i(0, 0, 1);
		for (int dir = -1; dir <= 1; dir += 2) {
			for (int i = 1; i < encaseLimit; i++) {
				BlockPos targetPos = pos.offset(offset.multiply(i * dir));
				BlockState targetState = level.getBlockState(targetPos);

				if (!targetState.is(targetBlock)) {
					break;
				}
				if (targetState.getValue(BlockStateProperties.AXIS) != axis) {
					break;
				}
				interlockedTargets.add(targetPos);
			}
		}
		return interlockedTargets;
	}

	public static Set<BlockPos> getEncasedShaft(BlockPos pos, Direction.Axis axis, Level level) {
		Set<BlockPos> interlockedTargets = new HashSet<>();
		interlockedTargets.add(pos);
		Vec3i offset;
		int encaseLimit = 32;

		if (axis == Direction.Axis.X) offset = new Vec3i(1, 0, 0);
		else if (axis == Direction.Axis.Y) offset = new Vec3i(0, 1, 0);
		else offset = new Vec3i(0, 0, 1);
		for (int dir = -1; dir <= 1; dir += 2) {
			for (int i = 1; i < encaseLimit; i++) {
				BlockPos targetPos = pos.offset(offset.multiply(i * dir));
				BlockState targetState = level.getBlockState(targetPos);

				if (!targetState.is(AllBlocks.ANDESITE_ENCASED_SHAFT.get()) && !targetState.is(AllBlocks.BRASS_ENCASED_SHAFT.get())) {
					break;
				}
				if (targetState.getValue(BlockStateProperties.AXIS) != axis) {
					break;
				}
				interlockedTargets.add(targetPos);
			}
		}
		return interlockedTargets;
	}

	public static Set<BlockPos> getInterlockedByAxis(BlockPos pos, Direction.Axis axis, Level level) {
		Set<BlockPos> interlockedTargets = new HashSet<>();
		interlockedTargets.add(pos);
		Vec3i offset;
		int encaseLimit = 32;

		if (axis == Direction.Axis.X) offset = new Vec3i(1, 0, 0);
		else if (axis == Direction.Axis.Y) offset = new Vec3i(0, 1, 0);
		else offset = new Vec3i(0, 0, 1);
		for (int dir = -1; dir <= 1; dir += 2) {
			for (int i = 1; i < encaseLimit; i++) {
				BlockPos targetPos = pos.offset(offset.multiply(i * dir));
				BlockState targetState = level.getBlockState(targetPos);

				if (getAxis(level, targetPos, targetState) != axis) {
					break;
				}
				interlockedTargets.add(targetPos);
			}
		}
		return interlockedTargets;
	}

	@Nullable
	public static Direction.Axis getAxis(BlockGetter world, BlockPos pos, BlockState state) {
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
