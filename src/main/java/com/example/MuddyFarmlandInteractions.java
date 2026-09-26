package com.example;

import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

final class MuddyFarmlandInteractions {
	private static final TagKey<Block> WATER_BOTTLE_CONVERTIBLE =
			TagKey.create(Registries.BLOCK, MuddyFarmland.id("water_bottle_convertible"));
	private static final TagKey<Block> HOE_CONVERTIBLE =
			TagKey.create(Registries.BLOCK, MuddyFarmland.id("hoe_convertible"));

	private MuddyFarmlandInteractions() {
	}

	static void initialize() {
		ItemEvents.USE_ON.register(MuddyFarmlandInteractions::useWaterBottle);
		ItemEvents.USE_ON.register(MuddyFarmlandInteractions::useHoe);
	}

	private static InteractionResult useHoe(UseOnContext context) {
		ItemStack tool = context.getItemInHand();
		if (!(tool.getItem() instanceof HoeItem)
				|| context.getClickedFace() == Direction.DOWN
				|| !context.getLevel().getBlockState(context.getClickedPos()).is(HOE_CONVERTIBLE)
				|| !context.getLevel().getBlockState(context.getClickedPos().above()).isAir()) {
			return null;
		}

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Player player = context.getPlayer();
		BlockState muddyFarmland = MuddyFarmlandBlocks.muddyFarmland().defaultBlockState();

		level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
		if (!level.isClientSide()) {
			level.setBlock(pos, muddyFarmland, 11);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, muddyFarmland));
			if (player != null) {
				tool.hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
			}
		}

		return InteractionResult.SUCCESS;
	}

	private static InteractionResult useWaterBottle(UseOnContext context) {
		ItemStack bottle = context.getItemInHand();
		if (!bottle.is(Items.POTION)
				|| !bottle.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)
				|| context.getClickedFace() == Direction.DOWN) {
			return null;
		}

		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState farmland = level.getBlockState(pos);
		if (!farmland.is(WATER_BOTTLE_CONVERTIBLE)) {
			return null;
		}

		Player player = context.getPlayer();
		if (player == null) {
			return null;
		}

		level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.0F, 1.0F);
		player.setItemInHand(
				context.getHand(),
				ItemUtils.createFilledResult(bottle, player, new ItemStack(Items.GLASS_BOTTLE))
		);

		if (!level.isClientSide()) {
			ServerLevel serverLevel = (ServerLevel) level;
			for (int i = 0; i < 5; i++) {
				serverLevel.sendParticles(
						ParticleTypes.SPLASH,
						pos.getX() + level.getRandom().nextDouble(),
						pos.getY() + 1,
						pos.getZ() + level.getRandom().nextDouble(),
						1,
						0.0,
						0.0,
						0.0,
						1.0
				);
			}

			BlockState muddyFarmland = MuddyFarmlandBlocks.muddyFarmland().defaultBlockState();
			if (farmland.hasProperty(FarmlandBlock.MOISTURE)
					&& muddyFarmland.hasProperty(FarmlandBlock.MOISTURE)) {
				muddyFarmland = muddyFarmland.setValue(
						FarmlandBlock.MOISTURE,
						farmland.getValue(FarmlandBlock.MOISTURE)
				);
			}
			level.setBlockAndUpdate(pos, muddyFarmland);
		}

		level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
		level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
		return InteractionResult.SUCCESS;
	}
}
