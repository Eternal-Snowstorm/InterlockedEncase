package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
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
public class EncasedShaftWrenchHandler {

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
		if (!stack.is(AllItems.WRENCH.get())) {
			return;
		}
		if (!state.is(AllBlocks.ANDESITE_ENCASED_SHAFT.get()) && !state.is(AllBlocks.BRASS_ENCASED_SHAFT.get())) {
			return;
		}

		event.setCanceled(true);
		player.swing(event.getHand());

		Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
		Set<BlockPos> targets = InterlockHelper.getEncasedShaft(pos, axis, level);

		event.setCanceled(true);
		player.swing(event.getHand());
		for (BlockPos targetPos : targets) {
			level.destroyBlock(targetPos, false);
			BlockState newState;

			newState = AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis);
			level.setBlockAndUpdate(targetPos, newState);
		}
	}
}