package com.example.mixin;

import com.example.MuddyFarmlandBlocks;
import com.example.MuddyFarmlandWaterNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelWaterNetworkMixin {
	@Inject(
			method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
			at = @At("HEAD")
	)
	private void muddyFarmland$invalidateWaterNetwork(
			BlockPos pos,
			BlockState newState,
			int updateFlags,
			int updateLimit,
			CallbackInfoReturnable<Boolean> cir
	) {
		if (!((Object) this instanceof ServerLevel serverLevel)) {
			return;
		}

		BlockState oldState = serverLevel.getBlockState(pos);
		if (oldState.equals(newState)) {
			return;
		}

		if (MuddyFarmlandBlocks.isMuddyFarmland(oldState)
				|| MuddyFarmlandBlocks.isMuddyFarmland(newState)
				|| oldState.getFluidState().is(FluidTags.WATER)
				|| newState.getFluidState().is(FluidTags.WATER)) {
			MuddyFarmlandWaterNetwork.invalidate(serverLevel);
		}
	}
}
