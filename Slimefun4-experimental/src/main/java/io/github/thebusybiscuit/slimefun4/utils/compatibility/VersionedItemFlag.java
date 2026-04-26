package io.github.thebusybiscuit.slimefun4.utils.compatibility;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.inventory.ItemFlag;

public class VersionedItemFlag {
    
    public static final ItemFlag HIDE_ADDITIONAL_TOOLTIP;

    static {
        ItemFlag additionalTooltip = getKey("HIDE_ADDITIONAL_TOOLTIP");

        if (additionalTooltip == null) {
            additionalTooltip = getKey("HIDE_POTION_EFFECTS");
        }

        HIDE_ADDITIONAL_TOOLTIP = additionalTooltip;
    }

    @Nullable
    private static ItemFlag getKey(@Nonnull String key) {
        try {
            Field field = ItemFlag.class.getDeclaredField(key);
            return (ItemFlag) field.get(null);
        } catch(Exception e) {
            return null;
        }
    }
}
