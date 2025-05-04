package me.tooshort.item;

import me.tooshort.PostIt;
import me.tooshort.networking.PostItNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.entity.SignText;

public class PostItItems {
	public static final DataComponentType<SignText> STICKER_TEXT_COMPONENT =
			DataComponentType.<SignText>builder()
					.persistent(SignText.DIRECT_CODEC)
					.networkSynchronized(PostItNetworking.STICKER_TEXT_STREAM_CODEC)
					.build();

	public static final ResourceKey<Item> STICKER_ITEM_KEY = PostIt.locateKey(Registries.ITEM,"sticker");
	public static final Item STICKER_ITEM = new StickerItem(
			new Item.Properties()
					.setId(STICKER_ITEM_KEY)
					.component(STICKER_TEXT_COMPONENT, new SignText())
					.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFFFFFFFF))
	);

	public static void register() {
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, PostIt.locate("sticker_text"), STICKER_TEXT_COMPONENT);
		Registry.register(BuiltInRegistries.ITEM, STICKER_ITEM_KEY, STICKER_ITEM);
	}
}
