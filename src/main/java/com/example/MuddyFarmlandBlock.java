package com.example;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jspecify.annotations.Nullable;

public final class MuddyFarmlandBlock extends FarmlandBlock implements EntityBlock {
	public MuddyFarmlandBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		if (!this.defaultBlockState().canSurvive(context.getLevel(), context.getClickedPos())) {
			return Blocks.MUD.defaultBlockState();
		}
		return super.getStateForPlacement(context);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.canSurvive(level, pos)) {
			BlockState mud = Blocks.MUD.defaultBlockState();
			level.setBlockAndUpdate(pos, mud);
			level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(mud));
		}
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int moisture = state.getValue(MOISTURE);
		boolean waterNearby = false;
		for (int x = -4; x <= 4 && !waterNearby; x++) {
			for (int z = -4; z <= 4 && !waterNearby; z++) {
				for (int y = 0; y <= 1; y++) {
					if (level.getFluidState(pos.offset(x, y, z)).is(FluidTags.WATER)) {
						waterNearby = true;
						break;
					}
				}
			}
		}

		if (waterNearby || level.isRainingAt(pos.above())) {
			if (moisture < MAX_MOISTURE) {
				level.setBlock(pos, state.setValue(MOISTURE, MAX_MOISTURE), 2);
			}
		} else if (moisture > 0) {
			level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), 2);
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MuddyFarmlandBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
			Level level,
			BlockState state,
			BlockEntityType<T> type
	) {
		if (level.isClientSide() || type != MuddyFarmlandBlocks.blockEntityType()) {
			return null;
		}

		return (tickerLevel, pos, tickerState, blockEntity) -> {
			if (blockEntity instanceof MuddyFarmlandBlockEntity farmland) {
				MuddyFarmlandBlockEntity.serverTick(tickerLevel, pos, tickerState, farmland);
			}
		};
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
		entity.causeFallDamage(fallDistance, 1.0F, entity.damageSources().fall());
	}
}
