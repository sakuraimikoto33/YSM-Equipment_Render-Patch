package net.okitsu.ysmequipmentrenderpatch.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

final class VisibleEquipmentLookup {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY =
            EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath("curios", "item_handler"), IItemHandler.class);

    private VisibleEquipmentLookup() {
    }

    static ItemStack findFirstVisibleCurio(LivingEntity entity, Predicate<ItemStack> itemFilter) {
        if (CuriosVisibilityAccess.isAvailable()) {
            return CuriosVisibilityAccess.findFirstVisibleItem(entity, itemFilter);
        }
        return findFirstCapabilityItem(entity, itemFilter);
    }

    static boolean hasHiddenMatchingCurio(LivingEntity entity, ItemStack expectedStack, Predicate<ItemStack> itemFilter) {
        return !expectedStack.isEmpty()
                && CuriosVisibilityAccess.isAvailable()
                && CuriosVisibilityAccess.hasHiddenMatchingItem(entity, expectedStack, itemFilter);
    }

    static List<ItemStack> findVisibleCuriosInSlot(
            LivingEntity entity,
            String slotIdentifier,
            Predicate<ItemStack> itemFilter
    ) {
        if (!CuriosVisibilityAccess.isSlotAccessAvailable()) {
            return List.of();
        }
        return CuriosVisibilityAccess.findVisibleItemsInSlot(entity, slotIdentifier, itemFilter);
    }

    private static ItemStack findFirstCapabilityItem(LivingEntity entity, Predicate<ItemStack> itemFilter) {
        IItemHandler curiosInventory = entity.getCapability(CURIOS_INVENTORY);
        if (curiosInventory == null) {
            return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < curiosInventory.getSlots(); slot++) {
            ItemStack stack = curiosInventory.getStackInSlot(slot);
            if (itemFilter.test(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static final class CuriosVisibilityAccess {
        private static final Method GET_CURIOS_INVENTORY = findMethod(
                "top.theillusivec4.curios.api.CuriosApi",
                "getCuriosInventory",
                LivingEntity.class
        );
        private static final Method GET_CURIOS = findMethod(
                "top.theillusivec4.curios.api.type.capability.ICuriosItemHandler",
                "getCurios"
        );
        private static final Method GET_STACKS = findMethod(
                "top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler",
                "getStacks"
        );
        private static final Method GET_COSMETIC_STACKS = findMethod(
                "top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler",
                "getCosmeticStacks"
        );
        private static final Method GET_IDENTIFIER = findMethod(
                "top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler",
                "getIdentifier"
        );
        private static final Method IS_VISIBLE = findMethod(
                "top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler",
                "isVisible"
        );
        private static final Method GET_RENDERS = findMethod(
                "top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler",
                "getRenders"
        );
        private static final Method GET_STACK_IN_SLOT = findMethod(
                "top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler",
                "getStackInSlot",
                int.class
        );
        private static final Method GET_SLOTS = findMethod(
                "top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler",
                "getSlots"
        );

        private CuriosVisibilityAccess() {
        }

        private static boolean isAvailable() {
            return GET_CURIOS_INVENTORY != null
                    && GET_CURIOS != null
                    && GET_STACKS != null
                    && GET_COSMETIC_STACKS != null
                    && GET_RENDERS != null
                    && GET_STACK_IN_SLOT != null
                    && GET_SLOTS != null;
        }

        private static boolean isSlotAccessAvailable() {
            return isAvailable()
                    && GET_IDENTIFIER != null
                    && IS_VISIBLE != null;
        }

        private static ItemStack findFirstVisibleItem(LivingEntity entity, Predicate<ItemStack> itemFilter) {
            Object optional = invoke(GET_CURIOS_INVENTORY, null, entity);
            Object handler = optional instanceof Optional<?> curiosOptional ? curiosOptional.orElse(null) : null;
            Object curios = invoke(GET_CURIOS, handler);
            if (!(curios instanceof Map<?, ?> curiosMap)) {
                return ItemStack.EMPTY;
            }

            for (Object stacksHandler : curiosMap.values()) {
                ItemStack stack = findFirstVisibleItem(stacksHandler, itemFilter);
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private static ItemStack findFirstVisibleItem(Object stacksHandler, Predicate<ItemStack> itemFilter) {
            Object stacks = invoke(GET_STACKS, stacksHandler);
            Object cosmeticStacks = invoke(GET_COSMETIC_STACKS, stacksHandler);
            Object renderStates = invoke(GET_RENDERS, stacksHandler);
            Object slots = invoke(GET_SLOTS, stacks);
            if (!(slots instanceof Number slotCount)) {
                return ItemStack.EMPTY;
            }

            for (int slot = 0; slot < slotCount.intValue(); slot++) {
                ItemStack stack = getStack(cosmeticStacks, slot);
                if (stack.isEmpty() && isRenderable(renderStates, slot)) {
                    stack = getStack(stacks, slot);
                }

                if (itemFilter.test(stack)) {
                    return stack;
                }
            }
            return ItemStack.EMPTY;
        }

        private static List<ItemStack> findVisibleItemsInSlot(
                LivingEntity entity,
                String slotIdentifier,
                Predicate<ItemStack> itemFilter
        ) {
            Object optional = invoke(GET_CURIOS_INVENTORY, null, entity);
            Object handler = optional instanceof Optional<?> curiosOptional ? curiosOptional.orElse(null) : null;
            Object curios = invoke(GET_CURIOS, handler);
            if (!(curios instanceof Map<?, ?> curiosMap)) {
                return List.of();
            }

            List<ItemStack> matches = new ArrayList<>();
            for (Object stacksHandler : curiosMap.values()) {
                findVisibleItemsInSlot(stacksHandler, slotIdentifier, itemFilter, matches);
            }
            return List.copyOf(matches);
        }

        private static void findVisibleItemsInSlot(
                Object stacksHandler,
                String slotIdentifier,
                Predicate<ItemStack> itemFilter,
                List<ItemStack> matches
        ) {
            if (!slotIdentifier.equals(invoke(GET_IDENTIFIER, stacksHandler))
                    || !Boolean.TRUE.equals(invoke(IS_VISIBLE, stacksHandler))) {
                return;
            }

            Object stacks = invoke(GET_STACKS, stacksHandler);
            Object cosmeticStacks = invoke(GET_COSMETIC_STACKS, stacksHandler);
            Object renderStates = invoke(GET_RENDERS, stacksHandler);
            Object slots = invoke(GET_SLOTS, stacks);
            if (!(slots instanceof Number slotCount)) {
                return;
            }

            for (int slot = 0; slot < slotCount.intValue(); slot++) {
                ItemStack stack = getStack(cosmeticStacks, slot);
                if (stack.isEmpty() && isRenderable(renderStates, slot)) {
                    stack = getStack(stacks, slot);
                }

                if (itemFilter.test(stack)) {
                    matches.add(stack);
                }
            }
        }

        private static boolean hasHiddenMatchingItem(
                LivingEntity entity,
                ItemStack expectedStack,
                Predicate<ItemStack> itemFilter
        ) {
            Object optional = invoke(GET_CURIOS_INVENTORY, null, entity);
            Object handler = optional instanceof Optional<?> curiosOptional ? curiosOptional.orElse(null) : null;
            Object curios = invoke(GET_CURIOS, handler);
            if (!(curios instanceof Map<?, ?> curiosMap)) {
                return false;
            }

            for (Object stacksHandler : curiosMap.values()) {
                if (hasHiddenMatchingItem(stacksHandler, expectedStack, itemFilter)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean hasHiddenMatchingItem(
                Object stacksHandler,
                ItemStack expectedStack,
                Predicate<ItemStack> itemFilter
        ) {
            Object stacks = invoke(GET_STACKS, stacksHandler);
            Object cosmeticStacks = invoke(GET_COSMETIC_STACKS, stacksHandler);
            Object renderStates = invoke(GET_RENDERS, stacksHandler);
            Object slots = invoke(GET_SLOTS, stacks);
            if (!(slots instanceof Number slotCount)) {
                return false;
            }

            for (int slot = 0; slot < slotCount.intValue(); slot++) {
                if (isRenderable(renderStates, slot)) {
                    continue;
                }

                if (isMatchingHiddenStack(getStack(stacks, slot), expectedStack, itemFilter)
                        || isMatchingHiddenStack(getStack(cosmeticStacks, slot), expectedStack, itemFilter)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean isMatchingHiddenStack(
                ItemStack stack,
                ItemStack expectedStack,
                Predicate<ItemStack> itemFilter
        ) {
            return !stack.isEmpty() && ItemStack.matches(stack, expectedStack) && itemFilter.test(stack);
        }

        private static ItemStack getStack(Object stackHandler, int slot) {
            Object stack = invoke(GET_STACK_IN_SLOT, stackHandler, slot);
            return stack instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
        }

        private static boolean isRenderable(Object renderStates, int slot) {
            return renderStates instanceof List<?> renders
                    && renders.size() > slot
                    && Boolean.TRUE.equals(renders.get(slot));
        }

        private static Method findMethod(String className, String methodName, Class<?>... parameterTypes) {
            try {
                Method method = Class.forName(className, false, VisibleEquipmentLookup.class.getClassLoader())
                        .getMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return null;
            }
        }

        private static Object invoke(Method method, Object target, Object... arguments) {
            if (method == null) {
                return null;
            }

            try {
                return method.invoke(target, arguments);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return null;
            }
        }
    }
}
