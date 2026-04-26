package io.github.bakedlibs.dough.skins;

import java.net.URL;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.inventory.meta.SkullMeta;

import io.github.bakedlibs.dough.versions.UnknownServerVersionException;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;

/**
 * Backward-compatible profile shim for addons compiled against the legacy Dough API.
 */
public final class CustomGameProfile {

    private static final String PLAYER_NAME = "Slimefun";

    private final UUID uuid;
    private final String texture;
    private final URL skinUrl;

    CustomGameProfile(@Nonnull UUID uuid, @Nonnull String texture, @Nonnull URL skinUrl) {
        Validate.notNull(uuid, "The profile uuid cannot be null");
        Validate.notNull(texture, "The profile texture cannot be null");
        Validate.notNull(skinUrl, "The profile skin URL cannot be null");

        this.uuid = uuid;
        this.texture = texture;
        this.skinUrl = skinUrl;
    }

    public void apply(@Nonnull SkullMeta meta) throws NoSuchFieldException, IllegalAccessException, UnknownServerVersionException {
        Validate.notNull(meta, "The skull meta cannot be null");

        if (!SlimefunUtils.applyCustomHeadTexture(meta, skinUrl)) {
            throw new IllegalStateException("Unable to apply a custom skull texture on this server for " + PLAYER_NAME + " profile " + uuid);
        }
    }

    public @Nonnull String getBase64Texture() {
        return texture;
    }
}

