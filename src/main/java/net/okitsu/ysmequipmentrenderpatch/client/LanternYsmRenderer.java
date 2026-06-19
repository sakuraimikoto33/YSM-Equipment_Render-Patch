package net.okitsu.ysmequipmentrenderpatch.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.okitsu.ysmequipmentrenderpatch.compat.LanternEquipmentLookup;
import org.joml.Quaternionf;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class LanternYsmRenderer {
    private static final String CUSTOM_PLAYER_ENTITY_CLASS = "com.elfmcys.yesstevemodel.OOoOoO0o0o0o000OO0o0ooo0";
    private static final String ANIMATED_GEO_MODEL_CLASS = "com.elfmcys.yesstevemodel.o0ooO0ooO00oo0o00Oo00000";
    private static final String RENDER_UTILS_CLASS = "com.elfmcys.yesstevemodel.oOOOOOOO0ooOo0OoOOO0ooOO";

    private static final double LANTERN_CHAIN_TIP = 1.0D;
    private static final Map<UUID, Pendulum> PENDULUM_STATES = new ConcurrentHashMap<>();
    private static final Method GET_ENTITY = findMethod(CUSTOM_PLAYER_ENTITY_CLASS, "ooo00OoO00OOOO0oOooOo0Oo");
    private static final Method GET_CURRENT_MODEL = findMethod(CUSTOM_PLAYER_ENTITY_CLASS, "O00OOOo00Oo0OO0000oOo0oo");
    private static final Method RIGHT_WAIST_BONES = findMethod(ANIMATED_GEO_MODEL_CLASS, "oo0o0Oooo0OOO00OOo0OO000");
    private static final Method PREP_MATRIX_FOR_LOCATOR = findMethod(
            RENDER_UTILS_CLASS,
            "oOo0OO0O0o000OO0O000oo0o",
            PoseStack.class,
            List.class
    );

    private LanternYsmRenderer() {
    }

    public static void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            Object customPlayerEntity,
            float partialTick
    ) {
        LivingEntity entity = getLivingEntity(customPlayerEntity);
        Object model = getCurrentModel(customPlayerEntity);
        if (entity == null || model == null) {
            return;
        }

        List<LanternEquipmentLookup.LanternEntry> lanterns = LanternEquipmentLookup.findLanterns(entity);
        if (lanterns.isEmpty()) {
            return;
        }

        ItemStack stack = lanterns.get(0).stack();
        if (!LanternEquipmentLookup.isRenderableBlockLantern(stack)) {
            return;
        }

        List<?> rightWaistBones = getBoneList(RIGHT_WAIST_BONES, model);
        if (rightWaistBones.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        if (applyWaistLocatorTransform(poseStack, rightWaistBones)) {
            renderLanternBlock(entity, stack, poseStack, bufferSource, packedLight, partialTick);
        }
        poseStack.popPose();
    }

    private static LivingEntity getLivingEntity(Object customPlayerEntity) {
        Object entity = invoke(GET_ENTITY, customPlayerEntity);
        return entity instanceof LivingEntity livingEntity ? livingEntity : null;
    }

    private static Object getCurrentModel(Object customPlayerEntity) {
        return invoke(GET_CURRENT_MODEL, customPlayerEntity);
    }

    private static List<?> getBoneList(Method method, Object model) {
        Object bones = invoke(method, model);
        return bones instanceof List<?> list ? list : Collections.emptyList();
    }

    private static boolean applyWaistLocatorTransform(PoseStack poseStack, List<?> waistBones) {
        if (waistBones.isEmpty() || PREP_MATRIX_FOR_LOCATOR == null) {
            return false;
        }

        return invoke(PREP_MATRIX_FOR_LOCATOR, null, poseStack, waistBones) instanceof Boolean;
    }

    private static void renderLanternBlock(
            LivingEntity entity,
            ItemStack stack,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            float partialTick
    ) {
        Block block = LanternEquipmentLookup.blockFor(stack);
        BlockState blockState = block.defaultBlockState();
        if (LanternEquipmentLookup.shouldHang(block) && blockState.hasProperty(LanternBlock.HANGING)) {
            blockState = blockState.setValue(LanternBlock.HANGING, true);
        }

        poseStack.translate(0.0D, 0.0D, -0.2D);
        Vec3 pivotPosition = getSwingPivotPosition(entity, partialTick);
        Vec3 swing = PENDULUM_STATES.computeIfAbsent(entity.getUUID(), ignored -> new Pendulum())
                .update(entity, pivotPosition, partialTick);

        poseStack.mulPose(new Quaternionf().rotationZYX((float) swing.x, 0.0F, (float) swing.z));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(-0.5D, -LANTERN_CHAIN_TIP, -0.5D);

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                blockState,
                poseStack,
                bufferSource,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );
    }

    private static Vec3 getSwingPivotPosition(LivingEntity entity, float partialTick) {
        return entity.getPosition(partialTick).add(0.0D, entity.getBbHeight() * 0.5D, 0.0D);
    }

    private static Method findMethod(String className, String methodName, Class<?>... parameterTypes) {
        try {
            Method method = Class.forName(className).getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static Object invoke(Method method, Object target, Object... arguments) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(target, arguments);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static final class Pendulum {
        private static final float FORCE_STRENGTH = 1.0F;
        private static final float GRAVITY = 9.81F;
        private static final float HORIZONTAL_FORCE_SCALE = -50.0F;
        private static final float VERTICAL_FORCE_SCALE = -20.0F;
        private static final float DAMPING = 0.98F;
        private static final float CLAMP_ANGLE = (float) (Math.PI / 3.0D);
        private static final float CLAMP_VEL = 3.0F;

        private float xAngle;
        private float xVel;
        private float zAngle;
        private float zVel;
        private float lastFrameTime = Float.NaN;
        private boolean wasCrouching;
        private Vec3 lastHipPosition;

        private Vec3 update(LivingEntity entity, Vec3 newHipPosition, float partialTick) {
            if (Minecraft.getInstance().screen != null) {
                return Vec3.ZERO;
            }

            boolean crouching = entity.isCrouching();
            if (crouching && !this.wasCrouching) {
                this.xAngle = (float) Math.PI / 8.0F;
                this.zAngle = (float) Math.PI / 10.0F;
            }
            this.wasCrouching = crouching;

            float frameTime = entity.tickCount + partialTick;
            if (Float.isNaN(this.lastFrameTime)) {
                this.lastFrameTime = frameTime;
                this.lastHipPosition = newHipPosition;
                return currentSwing();
            }

            float deltaTicks = frameTime - this.lastFrameTime;
            if (deltaTicks <= 0.0001F) {
                return currentSwing();
            }
            if (deltaTicks > 2.0F) {
                this.lastFrameTime = frameTime;
                this.lastHipPosition = newHipPosition;
                return currentSwing();
            }

            Vec3 oldHipPosition = this.lastHipPosition != null ? this.lastHipPosition : newHipPosition;
            Vec3 localDelta = toEntityLocal(newHipPosition.subtract(oldHipPosition), entity.getForward());
            float timeStep = deltaTicks / 20.0F;

            this.zVel = updateVelocity(this.zAngle, this.zVel, (float) localDelta.z, (float) localDelta.y, timeStep);
            this.zAngle = updateAngle(this.zAngle, this.zVel, timeStep);

            this.xVel = updateVelocity(this.xAngle, this.xVel, (float) localDelta.x, (float) localDelta.y, timeStep);
            this.xAngle = updateAngle(this.xAngle, this.xVel, timeStep);

            this.zVel = Mth.clamp(this.zVel, -CLAMP_VEL, CLAMP_VEL);
            this.xVel = Mth.clamp(this.xVel, -CLAMP_VEL, CLAMP_VEL);
            this.lastFrameTime = frameTime;
            this.lastHipPosition = newHipPosition;

            return currentSwing();
        }

        private Vec3 currentSwing() {
            return new Vec3(
                    Math.abs(this.xAngle) < 0.01F ? 0.0F : this.xAngle,
                    0.0D,
                    Math.abs(this.zAngle) < 0.01F ? 0.0F : this.zAngle
            );
        }

        private static float updateVelocity(float angle, float velocity, float horizontalDelta, float verticalDelta, float timeStep) {
            float force = GRAVITY * (float) Math.sin(angle);
            force += horizontalDelta * FORCE_STRENGTH * HORIZONTAL_FORCE_SCALE;
            force += verticalDelta * FORCE_STRENGTH * VERTICAL_FORCE_SCALE;
            return (velocity - force * timeStep) * DAMPING;
        }

        private static float updateAngle(float angle, float velocity, float timeStep) {
            return Mth.clamp(angle + velocity * timeStep, -CLAMP_ANGLE, CLAMP_ANGLE);
        }

        private static Vec3 toEntityLocal(Vec3 worldDelta, Vec3 forwardVector) {
            Vec3 forward = forwardVector.normalize();
            Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
            Vec3 right = forward.cross(up).normalize();
            up = right.cross(forward).normalize();
            return new Vec3(worldDelta.dot(right), worldDelta.dot(up), worldDelta.dot(forward));
        }
    }
}