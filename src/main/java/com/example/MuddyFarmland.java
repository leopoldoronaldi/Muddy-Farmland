package com.example;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuddyFarmland implements ModInitializer {
	public static final String MOD_ID = "muddy-farmland";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		MuddyFarmlandBlocks.initialize();
		LOGGER.info("Muddy Farmland block registered.");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
