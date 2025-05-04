package me.tooshort.client.render;

import me.tooshort.entity.StickerEntity;
import me.tooshort.item.StickerItem;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.Vec3;

public class StickerEntityRenderState extends EntityRenderState {
	public SignText text         = new SignText();
	public int      color        = StickerItem.DEFAULT_COLOR;
	public Direction faceDir     = Direction.NORTH;
	public Direction horiDir     = Direction.NORTH;
	public Vec3     position     = Vec3.ZERO;
	public float    textScale    = StickerEntity.TEXT_SCALE;
	public Vec3     textOffset   = StickerEntity.TEXT_OFFSET;
	public int      lineHeight   = StickerEntity.TEXT_LINE_HEIGHT;
	public int      maxTextWidth = StickerEntity.MAX_TEXT_WIDTH;
}
