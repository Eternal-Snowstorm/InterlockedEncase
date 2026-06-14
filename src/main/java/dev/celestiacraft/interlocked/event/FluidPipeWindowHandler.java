package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.utils.InterlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FluidPipeWindowHandler {
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {

		Level level = event.getLevel();
		Player player = event.getEntity();
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
			Direction.Axis axis = InterlockHelper.getAxis(level, pos, state);
			Set<BlockPos> targets = InterlockHelper.getInterlockedByAxis(pos, axis, level);

			event.setCanceled(true);
			player.swing(event.getHand());
			for (BlockPos targetPos : targets) {
				BlockState newState = AllBlocks.GLASS_FLUID_PIPE.getDefaultState().setValue(GlassFluidPipeBlock.AXIS, axis);

				level.setBlockAndUpdate(targetPos, newState);
				FluidTransportBehaviour.loadFlows(level, targetPos);
			}
		} else if (state.is(AllBlocks.GLASS_FLUID_PIPE.get())) {
			Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
			Set<BlockPos> targets = InterlockHelper.getInterlocked(pos, AllBlocks.GLASS_FLUID_PIPE.get(), axis, level);

			event.setCanceled(true);
			player.swing(event.getHand());
			for (BlockPos targetPos : targets) {
				BlockState targetState = level.getBlockState(targetPos);
				BlockState newState = InterlockHelper.toRegularPipe(level, targetPos, targetState);

				level.setBlockAndUpdate(targetPos, newState);
				FluidTransportBehaviour.loadFlows(level, targetPos);
			}
		}
	}
}
