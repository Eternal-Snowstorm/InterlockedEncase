package dev.celestiacraft.interlocked.event;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.deployer.ManualApplicationRecipe;
import dev.celestiacraft.interlocked.Interlocked;
import dev.celestiacraft.interlocked.utils.InterlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.Optional;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Interlocked.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ItemApplicationHandler {
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

		RecipeType<Recipe<RecipeWrapper>> type = AllRecipeTypes.ITEM_APPLICATION.getType();
		Optional<Recipe<RecipeWrapper>> foundRecipe = level.getRecipeManager()
				.getAllRecipesFor(type)
				.stream()
				.filter(r -> {
					ManualApplicationRecipe mar = (ManualApplicationRecipe) r;
					return mar.testBlock(blockState) && mar.getIngredients().get(1)
							.test(heldItem);
				})
				.findFirst();

		if (foundRecipe.isEmpty()) {
			return;
		}

		if (level.isClientSide()) {
			return;
		}

		level.playSound(null, startPos, SoundEvents.COPPER_BREAK, SoundSource.PLAYERS, 1, 1.45f);
		ManualApplicationRecipe recipe = (ManualApplicationRecipe) foundRecipe.get();

		Set<BlockPos> targets = InterlockHelper.getConnectedTargets(
				level,
				startPos,
				targetState -> targetState.is(blockState.getBlock())
		);

		for (BlockPos currentPos : targets) {
			BlockState currentState = level.getBlockState(currentPos);

			if (!currentState.is(blockState.getBlock())) {
				continue;
			}

			BlockState newState =
					recipe.transformBlock(blockState);

			level.destroyBlock(currentPos, false);
			level.setBlock(currentPos, newState, 3);
			recipe.rollResults()
					.forEach(stack -> Block.popResource(level, currentPos, stack));

			boolean creative = event.getEntity() != null && event.getEntity()
					.isCreative();
			boolean keepHeld = recipe.shouldKeepHeldItem() || creative;

			if (!keepHeld) {
				if (heldItem.getMaxDamage() > 0) {
					heldItem.hurtAndBreak(1, event.getEntity(),
							s -> s.broadcastBreakEvent(InteractionHand.MAIN_HAND));
				} else {
					Player player = event.getEntity();
					InteractionHand hand = event.getHand();
					ItemStack leftover = heldItem.getCraftingRemainingItem();
					heldItem.shrink(1);
					if (heldItem.isEmpty()) {
						player.setItemInHand(hand, leftover);
					} else {
						if (!player.getInventory().add(leftover)) {
							player.drop(leftover, false);
						}
					}
				}
			}
		}
	}
}
