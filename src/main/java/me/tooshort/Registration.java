package me.tooshort;

import io.netty.buffer.ByteBuf;
import me.tooshort.entity.StickerEntity;
import me.tooshort.item.StickerItem;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// ad-hoc registry stuff
// TODO: sort this out
public class Registration {

	public static final StreamCodec<ByteBuf, SignText> STICKER_TEXT_STREAM_CODEC = ByteBufCodecs.fromCodec(SignText.DIRECT_CODEC);

	public static final DataComponentType<SignText> STICKER_TEXT_COMPONENT =
			DataComponentType.<SignText>builder()
					.persistent(SignText.DIRECT_CODEC)
					.networkSynchronized(STICKER_TEXT_STREAM_CODEC)
					.build();

	public static final EntityDataSerializer<SignText> STICKER_TEXT_DATA = new EntityDataSerializer<>() {
		@Override
		public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, SignText> codec() {
			return STICKER_TEXT_STREAM_CODEC;
		}

		public @NotNull SignText copy(SignText text) {
			return new SignText(text.getMessages(false), text.getMessages(true), text.getColor(), text.hasGlowingText());
		}
	};

	public static final ResourceKey<Item> STICKER_ITEM_KEY = PostItOnFabric.locateKey(Registries.ITEM,"sticker");

	public static final Item STICKER_ITEM = new StickerItem(
			new Item.Properties()
					.setId(STICKER_ITEM_KEY)
					.component(STICKER_TEXT_COMPONENT, new SignText())
					.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFFFF))
	);

	public static final ResourceKey<EntityType<?>> STICKER_ENTITY_KEY = PostItOnFabric.locateKey(Registries.ENTITY_TYPE, "sticker");

	public static final EntityType<StickerEntity> STICKER_ENTITY_TYPE = (EntityType<StickerEntity>) (Object) EntityType.Builder.of(StickerEntity::new, MobCategory.MISC)
							.noLootTable()
							.sized(0.25F, 0.1F)
							.eyeHeight(0.0F)
							.clientTrackingRange(10)
							.updateInterval(Integer.MAX_VALUE)
							.build(STICKER_ENTITY_KEY);

	public record UpdateStickerTextPacket(UUID stickerId, SignText text) implements CustomPacketPayload {
		public static final Type<UpdateStickerTextPacket> TYPE = new Type<>(PostItOnFabric.locate("update_sticker_text"));
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

	public static void init() {
		Registry.register(BuiltInRegistries.ITEM, STICKER_ITEM_KEY, STICKER_ITEM);
		Registry.register(BuiltInRegistries.ENTITY_TYPE, STICKER_ENTITY_KEY, STICKER_ENTITY_TYPE);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PostItOnFabric.locate("sticker_text"), STICKER_TEXT_COMPONENT);

		EntityDataSerializers.registerSerializer(STICKER_TEXT_DATA);

		PayloadTypeRegistry.playC2S().register(UpdateStickerTextPacket.TYPE, UpdateStickerTextPacket.CODEC);

		ServerPlayNetworking.registerGlobalReceiver(UpdateStickerTextPacket.TYPE, UpdateStickerTextPacket::handle);
	}
}
