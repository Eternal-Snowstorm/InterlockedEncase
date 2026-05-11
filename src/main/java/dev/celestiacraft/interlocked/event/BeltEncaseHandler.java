package dev.celestiacraft.interlocked.event;


import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltHelper;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.client.EncaseKeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeltEncaseHandler {
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
		if (player == null) {
			return;
		}
		if (player.isShiftKeyDown()) {
			return;
		}
		if (!EncaseKeyMapping.ACTIVATE.isDown()) {
			return;
		}
		if (!state.is(AllBlocks.BELT.get())) {
			return;
		}

		BeltBlockEntity controller = BeltHelper.getControllerBE(level, pos);
		int beltLength = controller.beltLength;

		for (int i = 0; i < beltLength; i++) {
			Vec3 beltVector = BeltHelper.getBeltVector(state);
			BlockPos controllerPos = BeltHelper.getPositionForOffset(controller, 0);
			BlockPos beltBlock = controllerPos.offset((int) (beltVector.x * i), (int) (beltVector.y * i), (int) (beltVector.z * i));
			BeltBlockEntity targetEntity = BeltHelper.getSegmentBE(level, beltBlock);

			if (targetEntity == null) {
				return;
			}
			if (stack.is(AllBlocks.ANDESITE_CASING.asItem())) {
				targetEntity.setCasingType(BeltBlockEntity.CasingType.ANDESITE);
			} else if (stack.is(AllBlocks.BRASS_CASING.asItem())) {
				targetEntity.setCasingType(BeltBlockEntity.CasingType.BRASS);
			} else if (stack.is(AllItems.WRENCH.get())) {
				event.setCanceled(true);
				player.swing(InteractionHand.MAIN_HAND);
				targetEntity.setCasingType(BeltBlockEntity.CasingType.NONE);
			}
		}
	}
}
