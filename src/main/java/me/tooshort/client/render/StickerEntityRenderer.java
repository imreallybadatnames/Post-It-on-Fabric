package me.tooshort.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import me.tooshort.PostIt;
import me.tooshort.entity.StickerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public class StickerEntityRenderer extends EntityRenderer<StickerEntity, StickerEntityRenderState> {
	public static final int              OUTLINE_RENDER_DISTANCE = Mth   .square(16);
	public static final ResourceLocation TEXTURE_LOCATION        = PostIt.locate("textures/entity/sticker.png");

	private final StickerModel model;

	public StickerEntityRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new StickerModel(context.bakeLayer(StickerModel.LAYER_LOCATION));
	}

	@Override
	public void render(StickerEntityRenderState renderState, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		super.render(renderState, poseStack, bufferSource, packedLight);
		poseStack.pushPose();

		VertexConsumer lineCons = bufferSource.getBuffer(RenderType.lines());

		var facingDir = renderState.faceDir;
		var horizDir  = renderState.horiDir;

		if (!facingDir.getAxis().isHorizontal())
			ShapeRenderer.renderVector(poseStack, lineCons, new Vector3f(), horizDir.getUnitVec3(), 0xFF00FFFF);
		poseStack.mulPose(getSignRotation(renderState.faceDir, renderState.horiDir));

		// side direction (up -> to viewer) and horizontal text direction (north -> right)
		ShapeRenderer.renderVector(poseStack, lineCons, new Vector3f(), Direction.UP.getUnitVec3(), 0xFF00FF00);
		ShapeRenderer.renderVector(poseStack, lineCons, new Vector3f(), Direction.NORTH.getUnitVec3(), 0xFFFF0000);

		poseStack.translate(0, -1F/64F, 0); // bring it closer to block face; TODO: find proper value

		VertexConsumer modelCons = bufferSource.getBuffer(this.model.renderType(TEXTURE_LOCATION));
		this.model.renderToBuffer(poseStack, modelCons, packedLight, OverlayTexture.NO_OVERLAY, renderState.color);

		poseStack.popPose();

		renderSignText(this.getFont(), poseStack, bufferSource, packedLight, renderState);
	}



	private static void renderSignText(Font font, PoseStack stack, MultiBufferSource bufferSource, int packedLight, StickerEntityRenderState renderState) {
		stack.pushPose();
		translateSignText(stack, renderState.textScale, renderState.textOffset, renderState.faceDir, renderState.horiDir);

		SignText text  = renderState.text;
		int darkColor  = AbstractSignRenderer.getDarkColor(text);
		int lineHeight = renderState.lineHeight;
		int lineOffset = 4 * lineHeight / 2;

		FormattedCharSequence[] messages = text.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), component -> {
			List<FormattedCharSequence> list = font.split(component, renderState.maxTextWidth);
			return list.isEmpty() ? FormattedCharSequence.EMPTY : list.getFirst();
		});

		boolean renderOutline = false;
		int textColor         = darkColor;
		int light             = packedLight;

		if (text.hasGlowingText()) {
			textColor     = text.getColor().getTextColor();
			renderOutline = isOutlineVisible(renderState.position, textColor);
			light         = 0xf000f0;
		}

		for (int m = 0; m < 4; m++) {
			FormattedCharSequence message = messages[m];
			float xOffset = (float) -font.width(message) / 2;
			if (renderOutline)
				 font.drawInBatch8xOutline(message, xOffset, m * lineHeight - lineOffset, textColor, darkColor, stack.last().pose(), bufferSource, light);
			else font.drawInBatch(message, xOffset, (float)(m * lineHeight - lineOffset), textColor, false, stack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, light);
		}
		stack.popPose();
	}

	private static void translateSignText(PoseStack poseStack, float textScale, Vec3 offset, Direction faceDir, Direction horiDir) {
		poseStack.mulPose(getTextRotation(faceDir, horiDir));
		poseStack.translate(offset);
		float scale = textScale / 64;
		poseStack.scale(scale, -scale, scale);
	}

	private static boolean isOutlineVisible(Vec3 pos, int color) {
		if (color == DyeColor.BLACK.getTextColor()) return true;

		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer localPlayer = minecraft.player;
		if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) return true;

		Entity entity = minecraft.getCameraEntity();
		return entity != null && entity.distanceToSqr(pos) < OUTLINE_RENDER_DISTANCE;
	}

	@Override
	public void extractRenderState(StickerEntity entity, StickerEntityRenderState reusedState, float partialTick) {
		super.extractRenderState(entity, reusedState, partialTick);

		reusedState.maxTextWidth = entity.getMaxTextLineWidth();
		reusedState.lineHeight   = entity.getTextLineHeight();
		reusedState.textOffset   = entity.getTextOffset();
		reusedState.textScale    = entity.getTextScale();
		reusedState.position     = entity.position();
		reusedState.faceDir      = entity.face();
		reusedState.horiDir      = entity.hori();
		reusedState.color        = entity.color();
		reusedState.text         = entity.text();
	}

	@Override
	public @NotNull StickerEntityRenderState createRenderState() {
		return new StickerEntityRenderState();
	}

	public static final Quaternionf[] signRotations = memoizeQuaternionRotations(StickerEntityRenderer::calculateQuaternionRotationForSign);
	public static final Quaternionf[] textRotations = memoizeQuaternionRotations(StickerEntityRenderer::calculateQuaternionRotationForText);

	public static Quaternionf getSignRotation(Direction faceDir, Direction horiDir) {
		return signRotations[horiDir.ordinal() - 2 + 4*Math.min(faceDir.ordinal(), 2)];
	}

	public static Quaternionf getTextRotation(Direction faceDir, Direction horiDir) {
		return textRotations[horiDir.ordinal() - 2 + 4*Math.min(faceDir.ordinal(), 2)];
	}

	public static Quaternionf[] memoizeQuaternionRotations(BiFunction<Direction, Direction, Quaternionf> calcFunc) {
		Quaternionf[] res    = new Quaternionf[12];
		Direction[] dirs     = Direction.values(); Arrays.sort(dirs);
		Direction[] horiDirs = Arrays.copyOfRange(dirs, 2, 6);

		for (Direction dir : horiDirs) {
			res[dir.ordinal()-2] = calcFunc.apply(Direction.DOWN,    dir);
			res[dir.ordinal()+2] = calcFunc.apply(Direction.UP,      dir);
			res[dir.ordinal()+6] = calcFunc.apply(dir.getOpposite(), dir); // faceDir == dir.getOpposite()
		}

		return res;
	}

	public static Quaternionf calculateQuaternionRotationForSign(Direction face, Direction hori) {
		// optimized version of the horizontal case:
		//private static final double QUAT_SCALE = Math.sqrt(0.5);
		//final double angle = (face.toYRot()/120-0.25) * Math.PI;
		//final double sin = Math.sin(angle);
		//final double cos = Math.cosFromSin(sin, angle);
		//dest.set((float) (-sin * QUAT_SCALE),
		//		 (float) ( sin * QUAT_SCALE),
		//		 (float) (-cos * QUAT_SCALE),
		//		 (float) ( cos * QUAT_SCALE));

		return face.getAxis().isHorizontal()
				? Axis.YP.rotationDegrees(3*face.toYRot() - 90).mul(Axis.ZP.rotationDegrees(-90))
				: Axis.ZP.rotationDegrees(180*(face.get3DDataValue() - 1))
						 .mul(Axis.YP.rotationDegrees(hori.toYRot() - 90));
	}

	public static Quaternionf calculateQuaternionRotationForText(Direction face, Direction hori) {
		return face.getAxis().isHorizontal()
				? Axis.YP.rotationDegrees(-face.toYRot())
				: Axis.XP.rotationDegrees(90*(1 - 2*face.get3DDataValue()))
						 .mul(Axis.ZP.rotationDegrees(3*hori.toYRot() + 180));
	}
}
