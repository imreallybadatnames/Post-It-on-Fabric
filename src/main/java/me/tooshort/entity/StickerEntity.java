package me.tooshort.entity;

import me.tooshort.PostIt;
import me.tooshort.client.screen.StickerScreen;
import me.tooshort.item.PostItItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StickerEntity extends Entity {
	public static final int TEXT_LINE_HEIGHT = 10;
	public static final int MAX_TEXT_WIDTH   = 90;

	public static final EntityDataAccessor<Direction> FACE_DIRECTION = SynchedEntityData.defineId(StickerEntity.class, EntityDataSerializers.DIRECTION);
	public static final EntityDataAccessor<Direction> HORI_DIRECTION = SynchedEntityData.defineId(StickerEntity.class, EntityDataSerializers.DIRECTION);

	public static final EntityDataAccessor<Integer>   STICKER_COLOR  = SynchedEntityData.defineId(StickerEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<SignText>  STICKER_TEXT   = SynchedEntityData.defineId(StickerEntity.class, PostItEntities.STICKER_TEXT_DATA);

	protected final ItemStack stack;

	public StickerEntity(EntityType<?> entityType, Level level) {
		this(entityType, level, Direction.UP, Direction.NORTH, new ItemStack(PostItItems.STICKER_ITEM));
	}

	public StickerEntity(EntityType<?> entityType, Level level, @NotNull Direction faceDirection, @NotNull Direction horiDirection, @NotNull ItemStack stack) {
		super(entityType, level);
		this.stack = stack.copyWithCount(1);

		SignText text = this.stack.getOrDefault(PostItItems.STICKER_TEXT_COMPONENT, new SignText());
		int color     = DyedItemColor.getOrDefault(this.stack,  0xFFFFFFFF);

		setText(text);
		setColor(color);
		setHoriDirection(horiDirection);
		setFaceDirection(faceDirection, true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(FACE_DIRECTION, Direction.UP);
		builder.define(HORI_DIRECTION, Direction.NORTH);
		builder.define(STICKER_COLOR, 0xFFFFFFFF);
		builder.define(STICKER_TEXT, new SignText());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> dataAccessor) {
		if (dataAccessor == FACE_DIRECTION) rotateToFace(face());

		super.onSyncedDataUpdated(dataAccessor);
	}

	protected void rotateToFace(@NotNull Direction faceDirection) {
		if (faceDirection.getAxis().isHorizontal()) {
			this.setXRot(0.0F);
			this.setYRot(faceDirection.get2DDataValue() * 90);
		} else {
			this.setXRot(-90 * faceDirection.getAxisDirection().getStep());
			this.setYRot(0.0F);
		}

		this.xRotO = this.getXRot();
		this.yRotO = this.getYRot();
		this.recalculateBoundingBox(faceDirection);
	}

	// added forceUpdate flag to optionally force update (i.e. rotating to face) even when direction is default [Direction.UP]
	public void setFaceDirection(@NotNull Direction faceDirection, boolean forceUpdate) {
		Objects.requireNonNull(faceDirection);
		this.entityData.set(FACE_DIRECTION, faceDirection, forceUpdate);
	}

	public void setHoriDirection(@NotNull Direction horiDirection) {
		Objects.requireNonNull(horiDirection);
		this.entityData.set(HORI_DIRECTION, horiDirection);
	}

	public void setText(@NotNull SignText text) {
		Objects.requireNonNull(text);
		this.entityData.set(STICKER_TEXT, text);
	}

	public void setColor(int color) {
		this.entityData.set(STICKER_COLOR, color);
	}

	public @NotNull Direction face() {
		return Objects.requireNonNull(this.entityData.get(FACE_DIRECTION));
	}

	public @NotNull Direction hori() {
		return Objects.requireNonNull(this.entityData.get(HORI_DIRECTION));
	}

	public @NotNull SignText text() {
		return Objects.requireNonNull(this.entityData.get(STICKER_TEXT));
	}

	public int color() {
		return Objects.requireNonNull(this.entityData.get(STICKER_COLOR));
	}

	protected final void recalculateBoundingBox(Direction faceDirection) {
		AABB aABB = this.calculateBoundingBox(faceDirection);
		Vec3 vec3 = aABB.getCenter();
		this.setPosRaw(vec3.x, vec3.y, vec3.z);
		this.setBoundingBox(aABB);
	}

	protected AABB calculateBoundingBox(Direction direction) {
		float thickness = 0.05f;
		float length    = 0.25f;

		Vec3i normal = direction.getUnitVec3i();
		Vec3  offset = new Vec3(normal).scale(thickness / 2);

		Direction.Axis axis = direction.getAxis();
		double x = axis == Direction.Axis.X ? thickness : length;
		double y = axis == Direction.Axis.Y ? thickness : length;
		double z = axis == Direction.Axis.Z ? thickness : length;
		return AABB.ofSize(position().add(offset), x, y, z);
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
		return false;
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public @NotNull InteractionResult interact(Player player, InteractionHand hand) {
		if (player.isShiftKeyDown()) {
			PostIt.LOGGER.info("Interaction 1");
			if (player.level().isClientSide()) return InteractionResult.SUCCESS;

			player.getInventory().placeItemBackInInventory(getPickupStack());
			remove(RemovalReason.DISCARDED);
		} else {
			PostIt.LOGGER.info("Interaction 2");
			if (player.level().isClientSide()) openScreen();
		}
		return InteractionResult.SUCCESS;
	}

	public ItemStack getPickupStack() {
		ItemStack stack = this.stack;

		stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color()));
		stack.set(PostItItems.STICKER_TEXT_COMPONENT, text());

		return stack;
	}

	protected void openScreen() {
		Minecraft.getInstance().setScreen(new StickerScreen(this, false));
	}

	public int getTextLineHeight() {
		return TEXT_LINE_HEIGHT;
	}

	public int getMaxTextLineWidth() {
		return MAX_TEXT_WIDTH;
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setColor(tag.getInt("StickerColor")                       .orElse(0xFFFFFFFF));
		setText (tag.read  ("StickerText",  SignText.DIRECT_CODEC).orElse(new SignText()));

		setHoriDirection(tag.read("HorizontalDirection", Direction.CODEC).orElse(Direction.NORTH));
		setFaceDirection(tag.read("FacingDirection",     Direction.CODEC).orElse(Direction.UP), true);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt("StickerColor", color());
		tag.store ("StickerText",  SignText.DIRECT_CODEC, text());

		tag.store("HorizontalDirection", Direction.CODEC, hori());
		tag.store("FacingDirection",     Direction.CODEC, face());
	}
}
