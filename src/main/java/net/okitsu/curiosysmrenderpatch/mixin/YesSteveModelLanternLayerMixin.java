package net.okitsu.curiosysmrenderpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.okitsu.curiosysmrenderpatch.client.CuriosLanternYsmRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.elfmcys.yesstevemodel.oO0OO0o0oo00OOOoOoOO00Oo", remap = false)
public final class YesSteveModelLanternLayerMixin {
    @Inject(
            method = "oOo0OO0O0o000OO0O000oo0o(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILcom/elfmcys/yesstevemodel/OOoOoO0o0o0o000OO0o0ooo0;FFFFFF)V",
            at = @At("RETURN"),
            remap = false
    )
    private void curiosYsmRenderPatch$renderCuriosLanterns(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            @Coerce Object customPlayerEntity,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {
        CuriosLanternYsmRenderer.render(poseStack, bufferSource, packedLight, customPlayerEntity, partialTick);
    }
}
