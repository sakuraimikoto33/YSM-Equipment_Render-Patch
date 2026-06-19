package net.okitsu.ysmequipmentrenderpatch.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public final class LanternEquipmentLookup {
    private static final EntityCapability<IItemHandler, Void> CURIOS_INVENTORY =
            EntityCapability.createVoid(ResourceLocation.fromNamespaceAndPath("curios", "item_handler"), IItemHandler.class);
    private static final TagKey<Item> CURIOS_LANTERN_TAG = itemTag("curios", "lantern");
    private static final TagKey<Item> CURIOS_BELT_TAG = itemTag("curios", "belt");
    private static final TagKey<Item> ACCESSORIFY_LANTERNS_TAG = itemTag("accessorify", "lanterns");

    private LanternEquipmentLookup() {
    }

    public static List<LanternEntry> findLanterns(LivingEntity entity) {
        IItemHandler curiosInventory = entity.getCapability(CURIOS_INVENTORY);
        if (curiosInventory == null) {
            return List.of();
        }

        for (int slot = 0; slot < curiosInventory.getSlots(); slot++) {
            ItemStack stack = curiosInventory.getStackInSlot(slot);
            if (isSupportedLantern(stack) && isRenderableBlockLantern(stack)) {
                return List.of(new LanternEntry(stack));
            }
        }
        return List.of();
    }

    public static boolean isSupportedLantern(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.is(Items.LANTERN) || stack.is(Items.SOUL_LANTERN)) {
            return true;
        }

        if (stack.is(CURIOS_LANTERN_TAG) || stack.is(ACCESSORIFY_LANTERNS_TAG)) {
            return true;
        }

        return stack.is(CURIOS_BELT_TAG) && isLanternBlock(stack);
    }

    public static boolean isRenderableBlockLantern(ItemStack stack) {
        return blockFor(stack) != Blocks.AIR;
    }

    public static Block blockFor(ItemStack stack) {
        return Block.byItem(stack.getItem());
    }

    public static boolean shouldHang(Block block) {
        return block instanceof LanternBlock;
    }

    private static boolean isLanternBlock(ItemStack stack) {
        return blockFor(stack) instanceof LanternBlock;
    }

    private static TagKey<Item> itemTag(String namespace, String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public record LanternEntry(ItemStack stack) {
    }
}