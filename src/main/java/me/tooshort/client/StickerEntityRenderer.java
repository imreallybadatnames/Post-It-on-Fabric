package me.tooshort.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.tooshort.PostItOnFabric;
import me.tooshort.entity.StickerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class StickerEntityRenderer extends EntityRenderer<StickerEntity, StickerEntityRenderState> {
	private final StickerModel model;
	public static final ResourceLocation TEXTURE_LOCATION = PostItOnFabric.locate("textures/entity/sticker.png");
	protected StickerEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new StickerModel(context.bakeLayer(StickerModel.LAYER_LOCATION));
	}

	@Override
	public void render(StickerEntityRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		poseStack.pushPose();

		VertexConsumer lineCons = bufferSource.getBuffer(RenderType.lines());

		var facingDir = renderState.faceDir;
		var horizDir  = renderState.horiDir;

		if (!facingDir.getAxis().isHorizontal())
			ShapeRenderer.renderVector(poseStack, lineCons, new Vector3f(), horizDir.getUnitVec3(), 0xFF00FFFF);
		poseStack.mulPose(renderState.rotation);

		ShapeRenderer.renderVector(poseStack, lineCons, new Vector3f(), Direction.UP.getUnitVec3(), 0xFF00FF00);
		ShapeRenderer.renderVector(poseStack, lineCons, new Vector3f(), Direction.NORTH.getUnitVec3(), 0xFFFF0000);

		VertexConsumer modelCons = bufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
		this.model.renderToBuffer(poseStack, modelCons, packedLight, OverlayTexture.NO_OVERLAY, renderState.color);

		poseStack.popPose();
		super.render(renderState, poseStack, bufferSource, packedLight);
	}

	@Override
	public void extractRenderState(StickerEntity entity, StickerEntityRenderState reusedState, float partialTick) {
		super.extractRenderState(entity, reusedState, partialTick);

		reusedState.color = entity.color();
		reusedState.text  = entity.text();

		if (reusedState.faceDir == entity.face() && reusedState.horiDir == entity.hori()) return;
		reusedState.rotation = StickerEntityRenderState.calculateQuaternionRotation(
				reusedState.faceDir = entity.face(),
				reusedState.horiDir = entity.hori());
	}

	@Override
	public @NotNull StickerEntityRenderState createRenderState() {
		return new StickerEntityRenderState();
	}
}
