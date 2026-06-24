package net.okitsu.ysmequipmentrenderpatch.compat;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.lang.reflect.Method;

public final class ElytraEquipmentLookup {
    private ElytraEquipmentLookup() {
    }

    public static ItemStack findElytra(LivingEntity entity) {
        ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (shouldRenderElytra(chestStack, entity)) {
            return chestStack;
        }

        return VisibleEquipmentLookup.findFirstVisibleCurio(entity, stack -> shouldRenderElytra(stack, entity));
    }

    public static ItemStack findElytraOrNull(LivingEntity entity) {
        ItemStack stack = findElytra(entity);
        return stack.isEmpty() ? null : stack;
    }

    public static boolean isHiddenCuriosElytra(LivingEntity entity, ItemStack stack) {
        return VisibleEquipmentLookup.hasHiddenMatchingCurio(entity, stack, hiddenStack -> shouldRenderElytra(hiddenStack, entity));
    }

    public static ItemStack filterHiddenCuriosElytra(LivingEntity entity, ItemStack stack) {
        return isHiddenCuriosElytra(entity, stack) ? ItemStack.EMPTY : stack;
    }

    private static boolean shouldRenderElytra(ItemStack stack, LivingEntity entity) {
        if (DraconicEvolutionElytraCompat.isDraconicModularArmor(stack)) {
            return DraconicEvolutionElytraCompat.shouldRenderElytra(stack, entity);
        }
        return stack.getItem() == Items.ELYTRA || stack.canElytraFly(entity);
    }

    private static final class DraconicEvolutionElytraCompat {
        private static final Class<?> MODULAR_ARMOR_CLASS = findClass(
                "com.brandon3055.draconicevolution.items.equipment.IModularArmor"
        );
        private static final Method DE_ELYTRA_VISIBLE = findMethod(
                "com.brandon3055.draconicevolution.init.DEClient",
                "deElytraVisible",
                ItemStack.class,
                LivingEntity.class
        );
        private static final Method CAN_ELYTRA_FLY_BC = findMethod(
                "com.brandon3055.draconicevolution.items.equipment.IModularArmor",
                "canElytraFlyBC",
                ItemStack.class,
                LivingEntity.class
        );

        private DraconicEvolutionElytraCompat() {
        }

        private static boolean isDraconicModularArmor(ItemStack stack) {
            return !stack.isEmpty()
                    && MODULAR_ARMOR_CLASS != null
                    && MODULAR_ARMOR_CLASS.isInstance(stack.getItem());
        }

        private static boolean shouldRenderElytra(ItemStack stack, LivingEntity entity) {
            if (DE_ELYTRA_VISIBLE != null) {
                try {
                    return Boolean.TRUE.equals(DE_ELYTRA_VISIBLE.invoke(null, stack, entity));
                } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                    return false;
                }
            }

            if (CAN_ELYTRA_FLY_BC == null) {
                return false;
            }
            try {
                return Boolean.TRUE.equals(CAN_ELYTRA_FLY_BC.invoke(stack.getItem(), stack, entity));
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return false;
            }
        }

        private static Class<?> findClass(String className) {
            try {
                return Class.forName(className, false, ElytraEquipmentLookup.class.getClassLoader());
            } catch (ClassNotFoundException | LinkageError exception) {
                return null;
            }
        }

        private static Method findMethod(String className, String methodName, Class<?>... parameterTypes) {
            Class<?> owner = findClass(className);
            if (owner == null) {
                return null;
            }

            try {
                Method method = owner.getMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException | LinkageError exception) {
                return null;
            }
        }
    }
}
