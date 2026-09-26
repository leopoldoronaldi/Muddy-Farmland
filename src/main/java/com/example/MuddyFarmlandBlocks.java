package com.example;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

public final class MuddyFarmlandBlocks {
	private static final ResourceKey<Block> MUDDY_FARMLAND_KEY =
			ResourceKey.create(BuiltInRegistries.BLOCK.key(), MuddyFarmland.id("muddy_farmland"));
	private static final Block MUDDY_FARMLAND = Blocks.register(
			MUDDY_FARMLAND_KEY,
			MuddyFarmlandBlock::new,
			BlockBehaviour.Properties.ofFullCopy(Blocks.FARMLAND).sound(SoundType.MUD)
	);
	private static final BlockEntityType<MuddyFarmlandBlockEntity> BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			MuddyFarmland.id("muddy_farmland"),
			FabricBlockEntityTypeBuilder.create(MuddyFarmlandBlockEntity::new, MUDDY_FARMLAND).build()
	);
	private static final ResourceKey<Item> MUDDY_FARMLAND_ITEM_KEY =
			ResourceKey.create(BuiltInRegistries.ITEM.key(), MuddyFarmland.id("muddy_farmland"));
	private static final BlockItem MUDDY_FARMLAND_ITEM = registerBlockItem();

	private MuddyFarmlandBlocks() {
	}

	public static void initialize() {
		MuddyFarmlandInteractions.initialize();
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
				.register(output -> output.accept(MUDDY_FARMLAND_ITEM));
	}

	static Block muddyFarmland() {
		return MUDDY_FARMLAND;
	}

	static BlockEntityType<MuddyFarmlandBlockEntity> blockEntityType() {
		return BLOCK_ENTITY_TYPE;
	}

	public static boolean isMuddyFarmland(BlockState state) {
		return state.is(MUDDY_FARMLAND);
	}

	private static BlockItem registerBlockItem() {
		BlockItem item = new BlockItem(
				MUDDY_FARMLAND,
				new Item.Properties().useBlockDescriptionPrefix().setId(MUDDY_FARMLAND_ITEM_KEY)
		);
		item.registerBlocks(Item.BY_BLOCK, item);
		return Registry.register(BuiltInRegistries.ITEM, MUDDY_FARMLAND_ITEM_KEY, item);
	}
}
