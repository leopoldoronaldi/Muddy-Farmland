package com.example;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class MuddyFarmlandWaterNetwork {
	private static final int WATER_RANGE = 4;
	private static final long NETWORK_REFRESH_TICKS = 20;
	private static final long CACHE_CLEANUP_INTERVAL = 200;
	private static final Direction[] CONNECTED_DIRECTIONS = Direction.values();
	private static final Map<ServerLevel, Map<Long, NetworkStatus>> NETWORK_CACHE = new WeakHashMap<>();

	private MuddyFarmlandWaterNetwork() {
	}

	public static void invalidate(ServerLevel level) {
		NETWORK_CACHE.remove(level);
	}

	static boolean refreshAndCheck(ServerLevel level, BlockPos start, long gameTime) {
		Map<Long, NetworkStatus> cache = NETWORK_CACHE.computeIfAbsent(level, ignored -> new HashMap<>());
		long startKey = start.asLong();
		NetworkStatus cachedStatus = cache.get(startKey);
		if (cachedStatus != null && gameTime < cachedStatus.nextRefreshTick()) {
			return cachedStatus.hasNearbyWater();
		}

		if (gameTime % CACHE_CLEANUP_INTERVAL == 0) {
			Iterator<NetworkStatus> iterator = cache.values().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().nextRefreshTick() <= gameTime) {
					iterator.remove();
				}
			}
		}

		List<BlockPos> connectedBlocks = findConnectedBlocks(level, start);
		boolean hasNearbyWater = connectedBlocks.stream().anyMatch(pos -> hasWaterNearby(level, pos));
		NetworkStatus status = new NetworkStatus(gameTime + NETWORK_REFRESH_TICKS, hasNearbyWater);
		for (BlockPos pos : connectedBlocks) {
			cache.put(pos.asLong(), status);
			if (hasNearbyWater) {
				level.getBlockEntity(pos, MuddyFarmlandBlocks.blockEntityType())
						.ifPresent(MuddyFarmlandBlockEntity::setWaterFromNetwork);
			}
		}

		return hasNearbyWater;
	}

	private static List<BlockPos> findConnectedBlocks(ServerLevel level, BlockPos start) {
		List<BlockPos> connectedBlocks = new ArrayList<>();
		ArrayDeque<BlockPos> toVisit = new ArrayDeque<>();
		Set<Long> visited = new HashSet<>();
		toVisit.add(start.immutable());

		while (!toVisit.isEmpty()) {
			BlockPos pos = toVisit.removeFirst();
			if (!visited.add(pos.asLong())
					|| !level.hasChunkAt(pos)
					|| !MuddyFarmlandBlocks.isMuddyFarmland(level.getBlockState(pos))) {
				continue;
			}

			connectedBlocks.add(pos);
			for (Direction direction : CONNECTED_DIRECTIONS) {
				BlockPos neighbor = pos.relative(direction);
				if (level.hasChunkAt(neighbor) && !visited.contains(neighbor.asLong())) {
					toVisit.addLast(neighbor);
				}
			}
		}

		return connectedBlocks;
	}

	private static boolean hasWaterNearby(ServerLevel level, BlockPos farmlandPos) {
		for (int x = -WATER_RANGE; x <= WATER_RANGE; x++) {
			for (int z = -WATER_RANGE; z <= WATER_RANGE; z++) {
				for (int y = 0; y <= 1; y++) {
					BlockPos waterPos = farmlandPos.offset(x, y, z);
					if (level.hasChunkAt(waterPos)
							&& level.getFluidState(waterPos).is(FluidTags.WATER)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private record NetworkStatus(long nextRefreshTick, boolean hasNearbyWater) {
	}
}
