package net.okitsu.ysmequipmentrenderpatch.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.okitsu.ysmequipmentrenderpatch.compat.ElytraEquipmentLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "net.okitsu.ysmequipmentrenderpatch.ysmref.EquipmentLookup")
public abstract class YsmElytraEquipmentMixin {
    private static final String ELYTRA_ITEM_GETTER =
            "elytraItemGetter(Lnet/minecraft/world/entity/LivingEntity;)"
                    + "Lnet/minecraft/world/item/ItemStack;";

    @Inject(method = ELYTRA_ITEM_GETTER, at = @At("HEAD"), cancellable = true, require = 1)
    private static void ysmEquipmentRenderPatch$useSupportedEquipment(
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        ItemStack stack = ElytraEquipmentLookup.findElytra(entity);
        if (stack != null && !stack.isEmpty()) {
            callback.setReturnValue(stack);
        }
    }

    @Inject(method = ELYTRA_ITEM_GETTER, at = @At("RETURN"), cancellable = true, require = 1)
    private static void ysmEquipmentRenderPatch$hideDisabledCuriosEquipment(
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> callback
    ) {
        ItemStack stack = callback.getReturnValue();
        if (stack != null
                && !stack.isEmpty()
                && ElytraEquipmentLookup.isHiddenCuriosElytra(entity, stack)) {
            callback.setReturnValue(ItemStack.EMPTY);
        }
    }
}
