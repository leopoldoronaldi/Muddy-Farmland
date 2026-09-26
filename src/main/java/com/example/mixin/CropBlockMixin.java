package com.example.mixin;

import com.example.MuddyFarmlandBlocks;
import com.example.MuddyFarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
abstract class CropBlockMixin {
	@Inject(method = "hasSufficientLight", at = @At("RETURN"), cancellable = true)
	private static void muddyFarmland$allowGrowthWithoutLight(
			LevelReader level,
			BlockPos pos,
			CallbackInfoReturnable<Boolean> callback
	) {
		if (MuddyFarmlandBlocks.isMuddyFarmland(level.getBlockState(pos.below()))) {
			callback.setReturnValue(true);
		}
	}

	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void muddyFarmland$growWithoutLight(
			BlockState state,
			ServerLevel level,
			BlockPos pos,
			RandomSource random,
			CallbackInfo callback
	) {
		if (!MuddyFarmlandBlocks.isMuddyFarmland(level.getBlockState(pos.below()))) {
			return;
		}

		CropBlock crop = (CropBlock) (Object) this;
		int age = crop.getAge(state);
		BlockEntity soilBlockEntity = level.getBlockEntity(pos.below());
		if (age < crop.getMaxAge() && soilBlockEntity instanceof MuddyFarmlandBlockEntity soil) {
			int nextAge = age + 1;
			while (nextAge < crop.getMaxAge() && random.nextFloat() < 0.8F) {
				nextAge++;
			}
			int grownStages = soil.consumeGrowthStages(nextAge - age, level, pos.below());
			if (grownStages > 0) {
				level.setBlock(pos, crop.getStateForAge(age + grownStages), 2);
			}
		}
		callback.cancel();
	}
}
