package net.okitsu.curiosysmrenderpatch.compat;

import net.minecraft.core.NonNullList;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;

public final class CuriosLanternLookup {
    private static final TagKey<Item> CURIOS_LANTERN_TAG = itemTag("curios", "lantern");
    private static final TagKey<Item> CURIOS_BELT_TAG = itemTag("curios", "belt");
    private static final TagKey<Item> ACCESSORIFY_LANTERNS_TAG = itemTag("accessorify", "lanterns");

    private CuriosLanternLookup() {
    }

    public static List<LanternCurio> findVisibleLanterns(LivingEntity entity) {
        List<LanternCurio> lanterns = new ArrayList<>();
        CuriosApi.getCuriosInventory(entity).ifPresent(handler -> handler.getCurios().forEach((slotId, stacksHandler) -> {
            IDynamicStackHandler stackHandler = stacksHandler.getStacks();
            IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();
            NonNullList<Boolean> renderStates = stacksHandler.getRenders();

            for (int slot = 0; slot < stackHandler.getSlots(); slot++) {
                boolean renderable = renderStates.size() > slot && renderStates.get(slot);
                ItemStack stack = cosmeticStacksHandler.getStackInSlot(slot);
                boolean cosmetic = true;

                if (stack.isEmpty() && renderable) {
                    stack = stackHandler.getStackInSlot(slot);
                    cosmetic = false;
                }

                if (isSupportedLantern(stack)) {
                    lanterns.add(new LanternCurio(stack, new SlotContext(slotId, entity, slot, cosmetic, renderable)));
                }
            }
        }));
        return lanterns;
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

    public record LanternCurio(ItemStack stack, SlotContext slotContext) {
    }
}
