package net.okitsu.ysmequipmentrenderpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.okitsu.ysmequipmentrenderpatch.client.KaleidoscopeDollYsmRenderer;
import net.okitsu.ysmequipmentrenderpatch.client.LanternYsmRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.okitsu.ysmequipmentrenderpatch.ysmref.ElytraLayer")
public abstract class YsmElytraLayerMixin {
    private static final String RENDER =
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;"
                    + "Lnet/minecraft/client/renderer/MultiBufferSource;I"
                    + "Lnet/okitsu/ysmequipmentrenderpatch/ysmref/CustomPlayer;FFFFFF)V";

    @Inject(method = RENDER, at = @At("RETURN"), require = 1)
    private void ysmEquipmentRenderPatch$renderEquipment(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            @Coerce Object customPlayer,
            float limbSwing,
            float limbSwingAmount,
            float partialTick,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo callback
    ) {
        LanternYsmRenderer.render(poseStack, bufferSource, packedLight, customPlayer, partialTick);
        KaleidoscopeDollYsmRenderer.render(poseStack, bufferSource, packedLight, customPlayer, partialTick);
    }
}
