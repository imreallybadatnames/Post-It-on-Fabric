package me.tooshort.client.render;
// Made with Blockbench 4.12.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import me.tooshort.PostIt;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class StickerModel extends EntityModel<StickerEntityRenderState> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath(PostIt.MOD_ID, "sticker_model"), "main");

	public StickerModel(ModelPart root) {
		super(root.getChild("sticker"));
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition model = partdefinition.addOrReplaceChild("sticker",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-2.0F, 0.1F, -2.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.001F)),
				PartPose.offset(0.0F, 0.1F, 0.0F));


		return LayerDefinition.create(meshdefinition, 16, 16);
	}
}