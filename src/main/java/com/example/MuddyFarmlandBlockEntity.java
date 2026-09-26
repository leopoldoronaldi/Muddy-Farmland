package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class MuddyFarmlandBlockEntity extends BlockEntity {
	public static final int MAX_WATER = 20_000;
	private static final int WATER_PER_GROWTH_STAGE = 300;
	private static final int MATURE_CROP_WATER_PER_TICK = 1;

	private int water = MAX_WATER;

	public MuddyFarmlandBlockEntity(BlockPos pos, BlockState state) {
		super(MuddyFarmlandBlocks.blockEntityType(), pos, state);
	}

	public int consumeGrowthStages(int requestedStages, ServerLevel level, BlockPos pos) {
		if (requestedStages <= 0 || this.water == 0) {
			return 0;
		}

		int affordableStages = Math.min(requestedStages, this.water / WATER_PER_GROWTH_STAGE);
		if (affordableStages == 0) {
			this.setWater(0);
			this.turnIntoDryFarmland(level, pos);
			return 0;
		}

		this.setWater(this.water - affordableStages * WATER_PER_GROWTH_STAGE);
		if (this.water == 0) {
			this.turnIntoDryFarmland(level, pos);
		}
		return affordableStages;
	}

	void setWaterFromNetwork() {
		this.setWater(MAX_WATER);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.water = Math.clamp(input.getIntOr("water", MAX_WATER), 0, MAX_WATER);
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("water", this.water);
	}

	public int getWater() {
		return this.water;
	}

	private void setWater(int water) {
		int clampedWater = Math.clamp(water, 0, MAX_WATER);
		if (this.water != clampedWater) {
			this.water = clampedWater;
			this.setChanged();
		}
	}

	public static void serverTick(
			Level level,
			BlockPos pos,
			BlockState state,
			MuddyFarmlandBlockEntity blockEntity
	) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		boolean waterNearby = MuddyFarmlandWaterNetwork.refreshAndCheck(
				serverLevel,
				pos,
				serverLevel.getGameTime()
		);
		if (waterNearby) {
			blockEntity.setWater(MAX_WATER);
		}

		if (blockEntity.water == 0) {
			blockEntity.turnIntoDryFarmland(serverLevel, pos);
			return;
		}

		BlockState plant = serverLevel.getBlockState(pos.above());
		if (plant.getBlock() instanceof CropBlock crop
				&& crop.getAge(plant) >= crop.getMaxAge()
				&& !waterNearby) {
			blockEntity.setWater(blockEntity.water - MATURE_CROP_WATER_PER_TICK);
			if (blockEntity.water == 0) {
				blockEntity.turnIntoDryFarmland(serverLevel, pos);
			}
		}
	}

	private void turnIntoDryFarmland(ServerLevel level, BlockPos pos) {
		BlockState farmland = Blocks.FARMLAND.defaultBlockState();
		BlockState muddyFarmland = this.getBlockState();
		if (muddyFarmland.hasProperty(FarmlandBlock.MOISTURE)) {
			farmland = farmland.setValue(
					FarmlandBlock.MOISTURE,
					muddyFarmland.getValue(FarmlandBlock.MOISTURE)
			);
		}
		level.setBlockAndUpdate(pos, farmland);
		level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(farmland));
	}
}
