package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
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

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EncasedShaftWrenchHandler {
	
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
		if (!stack.is(AllItems.WRENCH.get())) {
			return;
		}
		if (!player.isShiftKeyDown()) {
			return;
		}
		if (!state.is(AllBlocks.ANDESITE_ENCASED_SHAFT.get()) && !state.is(AllBlocks.BRASS_ENCASED_SHAFT.get())) {
			return;
		}

		Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
		Vec3i offset = new Vec3i(0, 0, 0);

		if (axis == Direction.Axis.X) {
			offset = new Vec3i(1, 0, 0);
		} else if (axis == Direction.Axis.Y) {
			offset = new Vec3i(0, 1, 0);
		} else if (axis == Direction.Axis.Z) {
			offset = new Vec3i(0, 0, 1);
		}

		for (int i = 0; i < encaseLimit; i++) {
			Vec3i targetOffset = offset.multiply(i);
			BlockPos targetPos = pos.offset(targetOffset);
			BlockState targetState = level.getBlockState(targetPos);

			if (targetState.getValue(BlockStateProperties.AXIS) != axis) {
				break;
			}
			level.setBlockAndUpdate(targetPos, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis));
		}

		for (int i = 1; i < encaseLimit; i++) {
			Vec3i targetOffset = offset.multiply(-i);
			BlockPos targetPos = pos.offset(targetOffset);
			BlockState targetState = level.getBlockState(targetPos);

			if (targetState.getValue(BlockStateProperties.AXIS) != axis) {
				break;
			}
			level.setBlockAndUpdate(targetPos, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, axis));
		}
	}
}
