package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.utils.InterlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

import static dev.celestiacraft.interlocked.utils.InterlockHelper.confirmInteraction;
import static dev.celestiacraft.interlocked.utils.InterlockHelper.getAxis;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FluidPipeWindowHandler {
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {

		Level level = event.getLevel();
		ItemStack stack = event.getItemStack();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);

		if (level.isClientSide()) {
			return;
		}
		if (!InterlockHelper.isActivated(event)) {
			return;
		}
		if (!state.is(AllBlocks.FLUID_PIPE.get()) && !state.is(AllBlocks.GLASS_FLUID_PIPE.get())) {
			return;
		}
		if (!stack.is(AllItems.WRENCH.get())) {
			return;
		}

		if (state.is(AllBlocks.FLUID_PIPE.get())) {
			Direction.Axis axis = getAxis(state);
			if (axis == null) {
				return;
			}
			Set<BlockPos> targets = InterlockHelper.getLineTargets(
					pos,
					axis,
					level,
					targetState -> targetState.is(AllBlocks.FLUID_PIPE.get())
							&& getAxis(targetState) == axis
			);

			confirmInteraction(event);
			for (BlockPos targetPos : targets) {
				BlockState newState = AllBlocks.GLASS_FLUID_PIPE.getDefaultState().setValue(GlassFluidPipeBlock.AXIS, axis);

				level.setBlockAndUpdate(targetPos, newState);
				FluidTransportBehaviour.loadFlows(level, targetPos);
			}
		} else if (state.is(AllBlocks.GLASS_FLUID_PIPE.get())) {
			Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
			Set<BlockPos> targets = InterlockHelper.getLineTargets(
					pos,
					axis,
					level,
					targetState -> targetState.is(AllBlocks.GLASS_FLUID_PIPE.get())
							&& targetState.getValue(BlockStateProperties.AXIS) == axis
			);

			confirmInteraction(event);
			for (BlockPos targetPos : targets) {
				BlockState targetState = level.getBlockState(targetPos);
				BlockState newState = InterlockHelper.toRegularPipe(level, targetPos, targetState);

				level.setBlockAndUpdate(targetPos, newState);
				FluidTransportBehaviour.loadFlows(level, targetPos);
			}
		}
	}
}
