package me.tooshort.client;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.tooshort.Registration;
import me.tooshort.entity.StickerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.stream.IntStream;

@Environment(EnvType.CLIENT)
public class StickerScreen extends Screen {
	private final StickerEntity sticker;
	private StickerModel model;
	private SignText text;
	private final String[] messages;
	private int frame;
	private int line;
	@Nullable
	private TextFieldHelper signField;

	public StickerScreen(StickerEntity sticker, boolean isFiltered) {
		this(sticker, isFiltered, Component.translatable("sign.edit"));
	}

	/* DUMMY */

	public int sign_getTextLineHeight() {
		return 10;
	}

	public int sign_getMaxTextLineWidth() {
		return 90;
	}

	/* DUMMY */

	public StickerScreen(StickerEntity sticker, boolean isFiltered, Component title) {
		super(title);
		this.sticker = sticker;
		this.text = sticker.text();
		this.messages = IntStream.range(0, 4).mapToObj(i -> this.text.getMessage(i, isFiltered)).map(Component::getString).toArray(String[]::new);
	}

	@Override
	protected void init() {
		this.addRenderableWidget(
				Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 100, this.height / 4 + 144, 200, 20).build()
		);
		assert this.minecraft != null;
		this.model = new StickerModel(this.minecraft.getEntityModels().bakeLayer(StickerModel.LAYER_LOCATION));
		this.signField = new TextFieldHelper(
				() -> this.messages[this.line],
				this::setMessage,
				TextFieldHelper.createClipboardGetter(this.minecraft),
				TextFieldHelper.createClipboardSetter(this.minecraft),
				string -> this.minecraft.font.width(string) <= this.sign_getMaxTextLineWidth()
		);
	}

	@Override
	public void tick() {
		this.frame++;
		if (!this.isValid()) {
			this.onDone();
		}
	}

	private boolean isValid() {
		return this.minecraft != null
				&& this.minecraft.player != null
				&& !this.sticker.isRemoved()
				&& this.minecraft.player.canInteractWithEntity(this.sticker, 4.0);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		assert this.signField != null;
		if (keyCode == 265) {
			this.line = this.line - 1 & 3;
			this.signField.setCursorToEnd();
			return true;
		} else if (keyCode == 264 || keyCode == 257 || keyCode == 335) {
			this.line = this.line + 1 & 3;
			this.signField.setCursorToEnd();
			return true;
		} else return this.signField.keyPressed(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		assert this.signField != null;
		this.signField.charTyped(codePoint);
		return true;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		super.render(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.flush();
		Lighting.setupForFlatItems();
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 40, 16777215);
		this.renderSign(guiGraphics);
		guiGraphics.flush();
		Lighting.setupFor3DItems();
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.renderTransparentBackground(guiGraphics);
	}

	@Override
	public void onClose() {
		this.onDone();
	}

	@Override
	public void removed() {
		assert this.minecraft != null;
		ClientPlayNetworking.send(Registration.UpdateStickerTextPacket.create(this.sticker, text));
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static final Vector3f TEXT_SCALE = new Vector3f(0.9765628F, 0.9765628F, 0.9765628F);
	protected Vector3f getSignTextScale() {
		return TEXT_SCALE;
	}

	protected void offsetSign(GuiGraphics guiGraphics) {
		guiGraphics.pose().translate(this.width / 2.0F, 90.0F, 50.0F);
	}

	private void renderSign(GuiGraphics guiGraphics) {
		guiGraphics.pose().pushPose();
		this.offsetSign(guiGraphics);
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -1.0F);
		//guiGraphics.pose().scale(62.500004F, 62.500004F, -62.500004F);
		guiGraphics.drawSpecial(source -> {
			VertexConsumer modelCons = source.getBuffer(this.model.renderType(StickerEntityRenderer.TEXTURE_LOCATION));
			this.model.renderToBuffer(guiGraphics.pose(), modelCons, 0xf000f0, OverlayTexture.NO_OVERLAY, sticker.color());
		});
		guiGraphics.pose().popPose();
		this.renderSignText(guiGraphics);
		guiGraphics.pose().popPose();
	}

	private void renderSignText(GuiGraphics guiGraphics) {
		assert this.signField != null;
		guiGraphics.pose().translate(0.0F, 0.0F, 4.0F);
		Vector3f vector3f = this.getSignTextScale();
		guiGraphics.pose().scale(vector3f.x(), vector3f.y(), vector3f.z());
		int i = this.text.hasGlowingText() ? this.text.getColor().getTextColor() : AbstractSignRenderer.getDarkColor(this.text);
		boolean bl = this.frame / 6 % 2 == 0;
		int j = this.signField.getCursorPos();
		int k = this.signField.getSelectionPos();
		int l = 4 * this.sign_getTextLineHeight() / 2;
		int m = this.line * this.sign_getTextLineHeight() - l;

		for (int n = 0; n < this.messages.length; n++) {
			String string = this.messages[n];
			if (string != null) {
				if (this.font.isBidirectional()) {
					string = this.font.bidirectionalShaping(string);
				}

				int o = -this.font.width(string) / 2;
				guiGraphics.drawString(this.font, string, o, n * this.sign_getTextLineHeight() - l, i, false);
				if (n == this.line && j >= 0 && bl) {
					int p = this.font.width(string.substring(0, Math.min(j, string.length())));
					int q = p - this.font.width(string) / 2;
					if (j >= string.length()) {
						guiGraphics.drawString(this.font, "_", q, m, i, false);
					}
				}
			}
		}

		for (int nx = 0; nx < this.messages.length; nx++) {
			String string = this.messages[nx];
			if (string != null && nx == this.line && j >= 0) {
				int o = this.font.width(string.substring(0, Math.min(j, string.length())));
				int p = o - this.font.width(string) / 2;
				if (bl && j < string.length()) {
					guiGraphics.fill(p, m - 1, p + 1, m + this.sign_getTextLineHeight(), ARGB.opaque(i));
				}

				if (k != j) {
					int q = Math.min(j, k);
					int r = Math.max(j, k);
					int s = this.font.width(string.substring(0, q)) - this.font.width(string) / 2;
					int t = this.font.width(string.substring(0, r)) - this.font.width(string) / 2;
					int u = Math.min(s, t);
					int v = Math.max(s, t);
					guiGraphics.fill(RenderType.guiTextHighlight(), u, m, v, m + this.sign_getTextLineHeight(), -16776961);
				}
			}
		}
	}

	private void setMessage(String message) {
		this.messages[this.line] = message;
		this.text = this.text.setMessage(this.line, Component.literal(message));
		this.sticker.setText(this.text);
	}

	private void onDone() {
		assert this.minecraft != null;
		this.minecraft.setScreen(null);
	}
}
