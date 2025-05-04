package me.tooshort.entity;

import me.tooshort.PostIt;
import me.tooshort.networking.PostItNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.SignText;
import org.jetbrains.annotations.NotNull;

public class PostItEntities {
	public static final EntityDataSerializer<SignText> STICKER_TEXT_DATA = new EntityDataSerializer<>() {
		@Override
		public @NotNull StreamCodec<? super RegistryFriendlyByteBuf, SignText> codec() {
			return PostItNetworking.STICKER_TEXT_STREAM_CODEC;
		}

		public @NotNull SignText copy(SignText text) {
			return new SignText(text.getMessages(false), text.getMessages(true), text.getColor(), text.hasGlowingText());
		}
	};

	public static final ResourceKey<EntityType<?>> STICKER_ENTITY_KEY = PostIt.locateKey(Registries.ENTITY_TYPE, "sticker");

	public static final EntityType<StickerEntity> STICKER_ENTITY_TYPE = (EntityType<StickerEntity>) (Object) EntityType.Builder.of(StickerEntity::new, MobCategory.MISC)
			.noLootTable()
			.sized(0.25F, 0.1F)
			.eyeHeight(0.0F)
			.clientTrackingRange(10)
			.updateInterval(Integer.MAX_VALUE)
			.build(STICKER_ENTITY_KEY);

	public static void register() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, STICKER_ENTITY_KEY, STICKER_ENTITY_TYPE);
		EntityDataSerializers.registerSerializer(STICKER_TEXT_DATA);
	}
}
