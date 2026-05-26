package net.okitsu.curiosysmrenderpatch.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.okitsu.curiosysmrenderpatch.compat.CuriosElytraLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.elfmcys.yesstevemodel.OOOOo0O0o0ooOoOooOoOOOOo", remap = false)
public final class YesSteveModelElytraHelperMixin {
    @Inject(
            method = "oOo0OO0O0o000OO0O000oo0o(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private static void curiosYsmRenderPatch$findCuriosElytra(
            LivingEntity entity,
            CallbackInfoReturnable<ItemStack> cir
    ) {
        if (!cir.getReturnValue().isEmpty()) {
            return;
        }

        ItemStack curiosElytra = CuriosElytraLookup.findElytra(entity);
        if (!curiosElytra.isEmpty()) {
            cir.setReturnValue(curiosElytra);
        }
    }
}
