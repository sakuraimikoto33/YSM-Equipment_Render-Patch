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

import java.util.List;

public final class LanternEquipmentLookup {
    private static final TagKey<Item> CURIOS_LANTERN_TAG = itemTag("curios", "lantern");
    private static final TagKey<Item> CURIOS_BELT_TAG = itemTag("curios", "belt");
    private static final TagKey<Item> ACCESSORIFY_LANTERNS_TAG = itemTag("accessorify", "lanterns");

    private LanternEquipmentLookup() {
    }

    public static List<LanternEntry> findLanterns(LivingEntity entity) {
        ItemStack stack = VisibleEquipmentLookup.findFirstVisibleCurio(entity, LanternEquipmentLookup::isVisibleLantern);
        return stack.isEmpty() ? List.of() : List.of(new LanternEntry(stack));
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

    private static boolean isVisibleLantern(ItemStack stack) {
        return isSupportedLantern(stack) && isRenderableBlockLantern(stack);
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