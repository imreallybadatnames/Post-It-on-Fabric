package me.tooshort.item;

import me.tooshort.entity.PostItEntities;
import me.tooshort.entity.StickerEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class StickerItem extends Item {
	public static final int DEFAULT_COLOR = 0xFFFFFFFF;

	public StickerItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		ItemStack stack = context.getItemInHand();
		if (player == null || !stack.is(PostItItems.STICKER_ITEM)) return super.useOn(context);

		Direction facing = Direction.fromYRot(player.getYRot());
		Level level = context.getLevel();

		if (player.isShiftKeyDown()) {
			Vec3 vec3 = context.getClickLocation();
			Direction side = context.getClickedFace();

			StickerEntity postItEntity = new StickerEntity(PostItEntities.STICKER_ENTITY_TYPE, level, side, facing, stack);
			postItEntity.setPos(vec3);
			level.addFreshEntity(postItEntity);

			stack.shrink(1);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}
}
