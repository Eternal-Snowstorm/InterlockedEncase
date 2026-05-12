package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.client.key.EncaseKeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EncasedShaftReplaceHandler {
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		Player player = event.getEntity();
		ItemStack stack = event.getItemStack();
		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		int encaseLimit = 32;

		if (level.isClientSide()) {
			return;
		}
		if (player == null) {
			return;
		}
		if (!EncaseKeyMapping.ACTIVATE.isDown()) {
			return;
		}
		if (player.isShiftKeyDown()) {
			return;
		}
		if (!stack.is(AllBlocks.ANDESITE_CASING.asItem()) && !stack.is(AllBlocks.BRASS_CASING.asItem())) {
			return;
		}
		if (!state.is(AllBlocks.ANDESITE_ENCASED_SHAFT.get()) && !state.is(AllBlocks.BRASS_ENCASED_SHAFT.get())) {
			return;
		}

		Set<BlockPos> targets = new HashSet<>();
		targets.add(pos);
		Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
		Vec3i offset;

		if (axis == Direction.Axis.X) offset = new Vec3i(1, 0, 0);
		else if (axis == Direction.Axis.Y) offset = new Vec3i(0, 1, 0);
		else offset = new Vec3i(0, 0, 1);
		for (int dir = -1; dir <= 1; dir += 2) {
			for (int i = 1; i < encaseLimit; i++) {
				BlockPos targetPos = pos.offset(offset.multiply(i * dir));
				BlockState targetState = level.getBlockState(targetPos);

				if (!targetState.is(AllBlocks.ANDESITE_ENCASED_SHAFT.get()) &&
						!targetState.is(AllBlocks.BRASS_ENCASED_SHAFT.get())) {
					break;
				}
				if (targetState.getValue(BlockStateProperties.AXIS) != axis) {
					break;
				}
				targets.add(targetPos);
			}
		}
		event.setCanceled(true);
		player.swing(event.getHand());
		for (BlockPos targetPos : targets) {
			level.destroyBlock(targetPos, false);
			BlockState newState;

			if (stack.is(AllBlocks.ANDESITE_CASING.asItem())) {
				newState = AllBlocks.ANDESITE_ENCASED_SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis);
			} else {
				newState = AllBlocks.BRASS_ENCASED_SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis);
			}
			level.setBlockAndUpdate(targetPos, newState);
		}
	}
}
