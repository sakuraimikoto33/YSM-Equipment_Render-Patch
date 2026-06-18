package net.okitsu.ysmequipmentrenderpatch.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

public final class ElytraEquipmentLookup {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY =
            EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath("curios", "item_handler"), IItemHandler.class);

    private ElytraEquipmentLookup() {
    }

    public static ItemStack findElytra(LivingEntity entity) {
        ItemStack chestStack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (shouldRenderElytra(chestStack, entity)) {
            return chestStack;
        }

        IItemHandler curiosInventory = entity.getCapability(CURIOS_INVENTORY);
        if (curiosInventory == null) {
            return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < curiosInventory.getSlots(); slot++) {
            ItemStack stack = curiosInventory.getStackInSlot(slot);
            if (shouldRenderElytra(stack, entity)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean shouldRenderElytra(ItemStack stack, LivingEntity entity) {
        return stack.getItem() == Items.ELYTRA || stack.canElytraFly(entity);
    }
}