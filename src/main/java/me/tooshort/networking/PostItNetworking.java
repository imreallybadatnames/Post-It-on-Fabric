package me.tooshort.networking;

import io.netty.buffer.ByteBuf;
import me.tooshort.PostIt;
import me.tooshort.entity.StickerEntity;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PostItNetworking {
	public static final StreamCodec<ByteBuf, SignText> STICKER_TEXT_STREAM_CODEC = ByteBufCodecs.fromCodec(SignText.DIRECT_CODEC);
	
	public record UpdateStickerTextPacket(UUID stickerId, SignText text) implements CustomPacketPayload {
		public static final Type<UpdateStickerTextPacket> TYPE = new Type<>(PostIt.locate("update_sticker_text"));
		public static final StreamCodec<FriendlyByteBuf, UpdateStickerTextPacket> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, UpdateStickerTextPacket::stickerId, STICKER_TEXT_STREAM_CODEC, UpdateStickerTextPacket::text, UpdateStickerTextPacket::new);

		@Override
		public @NotNull Type<UpdateStickerTextPacket> type() {
			return TYPE;
		}

		public static UpdateStickerTextPacket create(Entity sticker, SignText text) {
			assert sticker instanceof StickerEntity;
			return new UpdateStickerTextPacket(sticker.getUUID(), text);
		}

		// no validation/filtering for now cuz im too lazy
		public static void handle(UpdateStickerTextPacket packet, ServerPlayNetworking.Context ctx) {
			if (ctx.player() == null || ctx.server() == null) return;
			final ServerLevel level = ctx.player().serverLevel();
			ctx.server().execute(() -> {
				if (!(level.getEntity(packet.stickerId) instanceof StickerEntity sticker)) return;
				sticker.setText(packet.text);
			});
		}
	}
	
	public static void register() {
		PayloadTypeRegistry.playC2S()     .register(UpdateStickerTextPacket.TYPE, UpdateStickerTextPacket.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(UpdateStickerTextPacket.TYPE, UpdateStickerTextPacket::handle);
	}
}
