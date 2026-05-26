package net.okitsu.curiosysmrenderpatch.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

public final class CuriosElytraLookup {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY =
            EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath("curios", "item_handler"), IItemHandler.class);

    private CuriosElytraLookup() {
    }

    public static ItemStack findElytra(LivingEntity entity) {
        IItemHandler curiosInventory = entity.getCapability(CURIOS_INVENTORY);
        if (curiosInventory == null) {
            return ItemStack.EMPTY;
        }

        for (int slot = 0; slot < curiosInventory.getSlots(); slot++) {
            ItemStack stack = curiosInventory.getStackInSlot(slot);
            if (isElytra(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isElytra(ItemStack stack) {
        return stack.getItem() == Items.ELYTRA;
    }
}
