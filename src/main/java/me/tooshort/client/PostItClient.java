package me.tooshort.client;

import me.tooshort.PostIt;
import me.tooshort.client.render.StickerModel;
import me.tooshort.client.render.StickerEntityRenderer;
import me.tooshort.entity.PostItEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class PostItClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		PostIt.LOGGER.info("Hello Sticker world on da client!");

		EntityRendererRegistry.register(PostItEntities.STICKER_ENTITY_TYPE, StickerEntityRenderer::new);
		EntityModelLayerRegistry.registerModelLayer(StickerModel.LAYER_LOCATION, StickerModel::createBodyLayer);
	}
}
