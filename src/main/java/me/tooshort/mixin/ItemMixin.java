package me.tooshort.mixin;

import me.tooshort.Registration;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
	@Inject(method = "overrideOtherStackedOnMe", at = @At("HEAD"), cancellable = true)
	private void postit$doPaperShearing(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access, CallbackInfoReturnable<Boolean> cir) {
		if (stack.is(Items.PAPER) && other.is(Items.SHEARS) && action == ClickAction.SECONDARY) {
			var level = player.level();
			if (!level.isClientSide) {
				stack.shrink(1);
				other.hurtAndBreak(1, (ServerLevel) level, (ServerPlayer) player, (i) -> {});
				player.addItem(new ItemStack(Registration.STICKER_ITEM, 4));
				player.playSound(SoundEvents.SHEEP_SHEAR, 0.8F, 0.8F + level.getRandom().nextFloat() * 0.4F);
			}
			cir.setReturnValue(true);
		}
	}
}
