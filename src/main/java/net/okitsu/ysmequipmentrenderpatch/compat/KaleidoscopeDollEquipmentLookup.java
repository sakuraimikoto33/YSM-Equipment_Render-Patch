package net.okitsu.ysmequipmentrenderpatch.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class KaleidoscopeDollEquipmentLookup {
    private static final TagKey<Item> ALL_DOLLS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("kaleidoscope_doll", "all_dolls")
    );

    private KaleidoscopeDollEquipmentLookup() {
    }

    public static List<ItemStack> findVisibleHeadDolls(LivingEntity entity) {
        return VisibleEquipmentLookup.findVisibleCuriosInSlot(
                entity,
                "head",
                KaleidoscopeDollEquipmentLookup::isKaleidoscopeDoll
        );
    }

    private static boolean isKaleidoscopeDoll(ItemStack stack) {
        return !stack.isEmpty() && stack.is(ALL_DOLLS);
    }
}
