package io.github.bakedlibs.dough.skins;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import io.github.bakedlibs.dough.versions.UnknownServerVersionException;

/**
 * Compatibility shim for legacy Dough head APIs.
 */
public final class PlayerHead {

    private PlayerHead() {}

    public static @Nonnull ItemStack getItemStack(@Nonnull OfflinePlayer player) {
        return getItemStack(meta -> {
            if (player.getPlayerProfile() != null) {
                try {
                    meta.setOwnerProfile(player.getPlayerProfile());
                } catch (NoSuchMethodError ignored) {
                    meta.setOwningPlayer(player);
                }
            } else {
                meta.setOwningPlayer(player);
            }
        });
    }

    public static @Nonnull ItemStack getItemStack(@Nonnull PlayerSkin skin) {
        return getItemStack(meta -> {
            try {
                skin.getProfile().apply(meta);
            } catch (ReflectiveOperationException | UnknownServerVersionException | RuntimeException e) {
                // Fall back to plain head below.
            }
        });
    }

    private static @Nonnull ItemStack getItemStack(@Nonnull Consumer<SkullMeta> metaConsumer) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }

        try {
            metaConsumer.accept(meta);
            head.setItemMeta(meta);
            return head;
        } catch (RuntimeException ex) {
            return new ItemStack(Material.PLAYER_HEAD);
        }
    }

    public static void setSkin(@Nonnull Block block, @Nonnull PlayerSkin skin, boolean force) {
        Validate.notNull(block, "The block cannot be null");
        Validate.notNull(skin, "The skin cannot be null");

        if (!(block.getState() instanceof Skull skull)) {
            return;
        }

        SlimefunUtils.setCustomHead(block, skin.getProfile().getBase64Texture());
        if (force) {
            skull.update(true, false);
        }
    }
}


