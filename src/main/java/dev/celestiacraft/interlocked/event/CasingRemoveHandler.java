package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.utils.InterlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

import static dev.celestiacraft.interlocked.utils.InterlockHelper.confirmInteraction;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CasingRemoveHandler {
	@SubscribeEvent
	public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
		Level level = event.getLevel();
		ItemStack heldItem = event.getItemStack();
		BlockPos startPos = event.getPos();
		BlockState blockState = level.getBlockState(startPos);

		if (event.isCanceled()) {
			return;
		}
		if (!InterlockHelper.isActivated(event)) {
			return;
		}
		if (level.isClientSide()) {
			return;
		}
		if (!AllTags.AllItemTags.WRENCH.matches(heldItem)) {
			return;
		}
		if (!AllTags.AllBlockTags.CASING.matches(blockState)) {
			return;
		}

		Player player = event.getEntity();
		if (player == null) {
			return;
		}

		confirmInteraction(event);

		// 与 ItemApplicationHandler 相同的选取逻辑:
		// BFS 连通同一种方块, 数量上限由 INTERLOCK_LIMIT 控制
		Set<BlockPos> targets = InterlockHelper.getConnectedTargets(
				level,
				startPos,
				targetState -> targetState.is(blockState.getBlock())
		);

		AllSoundEvents.WRENCH_REMOVE.playOnServer(level, player.blockPosition(), 1, Create.RANDOM.nextFloat() * .5f + .5f);
		player.swing(event.getHand(), true);
		for (BlockPos currentPos : targets) {
			BlockState currentState = level.getBlockState(currentPos);

			if (!currentState.is(blockState.getBlock())) {
				continue;
			}

			// 创造模式: 直接移除方块, 不收集掉落物、不装入背包、不产生掉落
			if (player.isCreative()) {
				level.destroyBlock(currentPos, false);
				continue;
			}

			// 先收集掉落物再破坏方块, 避免重复掉落
			List<ItemStack> drops = Block.getDrops(
					currentState,
					(ServerLevel) level,
					currentPos,
					level.getBlockEntity(currentPos),
					player,
					heldItem
			);
			level.destroyBlock(currentPos, false);

			for (ItemStack drop : drops) {
				if (!drop.isEmpty()) {
					// 优先存入玩家背包, 背包满时剩余部分在玩家处掉落
					player.getInventory().placeItemBackInInventory(drop);
				}
			}
		}
	}
}
