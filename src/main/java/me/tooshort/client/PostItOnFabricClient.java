package me.tooshort.client;

import me.tooshort.PostItOnFabric;
import me.tooshort.Registration;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class PostItOnFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PostItOnFabric.LOGGER.info("Hello Sticker world on da client!");

		EntityRendererRegistry.register(Registration.STICKER_ENTITY_TYPE, StickerEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(StickerModel.LAYER_LOCATION, StickerModel::createBodyLayer);
	}
}
