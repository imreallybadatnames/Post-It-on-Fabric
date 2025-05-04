package me.tooshort;

import net.fabricmc.api.ModInitializer;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostItOnFabric implements ModInitializer {
	public static final String MOD_ID = "post-it-on-fabric";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static ResourceLocation locate(String name) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
	}

	public static <T> ResourceKey<T> locateKey(ResourceKey<Registry<T>> registry, String name) {
		return ResourceKey.create(registry, locate(name));
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		LOGGER.info("Hello Sticker world!");

		Registration.init();
	}
}