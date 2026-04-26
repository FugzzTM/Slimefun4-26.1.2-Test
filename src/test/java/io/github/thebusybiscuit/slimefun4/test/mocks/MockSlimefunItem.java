package io.github.thebusybiscuit.slimefun4.test.mocks;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemState;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import org.bukkit.inventory.ItemStack;

public final class MockSlimefunItem {

    private MockSlimefunItem() {
    }

    public static SlimefunItem create(ItemGroup itemGroup, ItemStack item, String id) {
        Slimefun.getItemCfg().setValue(id + ".enabled", true);
        SlimefunItemStack stack = new SlimefunItemStack(id, item);
        SlimefunItem slimefunItem = new SlimefunItem(itemGroup, stack, RecipeType.NULL, new ItemStack[9]) {
        };

        return slimefunItem;
    }

    public static void setState(SlimefunItem item, ItemState state) {
        try {
            java.lang.reflect.Field field = SlimefunItem.class.getDeclaredField("state");
            field.setAccessible(true);
            field.set(item, state);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
        }
    }


}
