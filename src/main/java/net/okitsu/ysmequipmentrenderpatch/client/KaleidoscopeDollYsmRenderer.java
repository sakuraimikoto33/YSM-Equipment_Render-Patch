package net.okitsu.ysmequipmentrenderpatch.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.okitsu.ysmequipmentrenderpatch.compat.KaleidoscopeDollEquipmentLookup;
import net.okitsu.ysmequipmentrenderpatch.runtime.Reflector;
import net.okitsu.ysmequipmentrenderpatch.runtime.YsmRuntimeSymbolsReflective;

import java.util.Collections;
import java.util.List;

public final class KaleidoscopeDollYsmRenderer {
    private KaleidoscopeDollYsmRenderer() {
    }

    public static void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            Object customPlayerEntity,
            float partialTick
    ) {
        YsmRuntimeSymbolsReflective.ResolvedMethods methods = YsmRuntimeSymbolsReflective.methods();
        if (!methods.hasHeadDollSupport()) {
            return;
        }

        LivingEntity entity = getLivingEntity(methods, customPlayerEntity);
        Object model = getCurrentModel(methods, customPlayerEntity);
        if (entity == null || model == null) {
            return;
        }

        List<ItemStack> dolls = KaleidoscopeDollEquipmentLookup.findVisibleHeadDolls(entity);
        if (dolls.isEmpty()) {
            return;
        }

        List<?> headBones = getBoneList(methods, model);
        if (headBones.isEmpty()) {
            return;
        }

        for (ItemStack stack : dolls) {
            renderDoll(entity, stack, poseStack, bufferSource, packedLight, methods, headBones);
        }
    }

    private static LivingEntity getLivingEntity(
            YsmRuntimeSymbolsReflective.ResolvedMethods methods,
            Object customPlayerEntity
    ) {
        Object entity = Reflector.invoke(methods.getEntity(), customPlayerEntity);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private static Object getCurrentModel(
            YsmRuntimeSymbolsReflective.ResolvedMethods methods,
            Object customPlayerEntity
    ) {
        return Reflector.invoke(methods.getCurrentModel(), customPlayerEntity);
    }

    private static List<?> getBoneList(YsmRuntimeSymbolsReflective.ResolvedMethods methods, Object model) {
        Object bones = Reflector.invoke(methods.headBones(), model);
        return bones instanceof List<?> list ? list : Collections.emptyList();
    }

    private static void renderDoll(
            LivingEntity entity,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            YsmRuntimeSymbolsReflective.ResolvedMethods methods,
            List<?> headBones
    ) {
        poseStack.pushPose();
        if (applyHeadLocatorTransform(methods, poseStack, headBones)) {
            poseStack.scale(0.625F, 0.625F, 0.625F);
            poseStack.translate(0.0F, 0.25F, 0.0F);
            Minecraft.getInstance()
                    .getEntityRenderDispatcher()
                    .getItemInHandRenderer()
                    .renderItem(entity, stack, ItemDisplayContext.HEAD, false, poseStack, bufferSource, packedLight);
        }
        poseStack.popPose();
    }

    private static boolean applyHeadLocatorTransform(
            YsmRuntimeSymbolsReflective.ResolvedMethods methods,
            PoseStack poseStack,
            List<?> headBones
    ) {
        return Reflector.invoke(methods.prepMatrixForLocator(), null, poseStack, headBones) instanceof Boolean;
    }
}
