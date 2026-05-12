package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.client.key.EncaseKeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FluidPipeWindowHandler {
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {

		Level level = event.getLevel();
		Player player = event.getEntity();
		ItemStack stack = event.getItemStack();
		BlockPos startPos = event.getPos();
		BlockState startState = level.getBlockState(startPos);
		int switchLimit = 32;

		if (level.isClientSide()) {
			return;
		}
		if (!EncaseKeyMapping.ACTIVATE.isDown()) {
			return;
		}
		if (player.isShiftKeyDown()) {
			return;
		}
		if (!startState.is(AllBlocks.FLUID_PIPE.get()) && !startState.is(AllBlocks.GLASS_FLUID_PIPE.get())) {
			return;
		}
		if (!stack.is(AllItems.WRENCH.get())) {
			return;
		}

		Direction.Axis axis = getAxis(level, startPos, startState);
		Vec3i offset = new Vec3i(0, 0, 0);

		if (axis == Direction.Axis.X) {
			offset = new Vec3i(1, 0, 0);
		} else if (axis == Direction.Axis.Y) {
			offset = new Vec3i(0, 1, 0);
		} else if (axis == Direction.Axis.Z) {
			offset = new Vec3i(0, 0, 1);
		} else if (axis == null) {
			return;
		}

		if (startState.is(AllBlocks.FLUID_PIPE.get())) {
			for (int dir = -1; dir <= 1; dir += 2) {
				for (int i = (dir == 1 ? 0 : 1); i < switchLimit; i++) {
					Vec3i targetOffset = offset.multiply(i * dir);
					BlockPos targetPos = startPos.offset(targetOffset);
					BlockState targetState = level.getBlockState(targetPos);
					Direction.Axis targetAxis = getAxis(level, targetPos, targetState);

					event.setCanceled(true);
					player.swing(event.getHand());
					if (targetAxis != axis) {
						break;
					}
					if (targetState.is(AllBlocks.FLUID_PIPE.get())) {
						level.setBlockAndUpdate(targetPos, AllBlocks.GLASS_FLUID_PIPE.getDefaultState()
								.setValue(GlassFluidPipeBlock.AXIS, axis)
								.setValue(BlockStateProperties.WATERLOGGED, targetState.getValue(BlockStateProperties.WATERLOGGED)));
						FluidTransportBehaviour.loadFlows(level, targetPos);
					}
				}
			}
		} else if (startState.is(AllBlocks.GLASS_FLUID_PIPE.get())) {
			for (int dir = -1; dir <= 1; dir += 2) {
				for (int i = (dir == 1 ? 0 : 1); i < switchLimit; i++) {
					Vec3i targetOffset = offset.multiply(i * dir);
					BlockPos targetPos = startPos.offset(targetOffset);
					BlockState targetState = level.getBlockState(targetPos);
					Direction.Axis targetAxis = getAxis(level, targetPos, targetState);

					event.setCanceled(true);
					player.swing(event.getHand());
					if (targetAxis != axis) {
						break;
					}
					if (targetState.is(AllBlocks.GLASS_FLUID_PIPE.get())) {
						BlockState newState = toRegularPipe(level, targetPos, targetState).setValue(BlockStateProperties.WATERLOGGED, targetState.getValue(BlockStateProperties.WATERLOGGED));
						level.setBlock(targetPos, newState, 3);
						FluidTransportBehaviour.loadFlows(level, targetPos);
					}
				}
			}
		}
	}

	@Nullable
	private static Direction.Axis getAxis(BlockGetter world, BlockPos pos, BlockState state) {
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
