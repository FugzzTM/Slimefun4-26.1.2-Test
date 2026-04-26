package io.github.bakedlibs.dough.skins;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

/**
 * Compatibility shim for legacy Dough skin APIs.
 */
public class PlayerSkin {

    private static final String ERROR_TOKEN = "error";

    private final CustomGameProfile profile;

    PlayerSkin(@Nonnull UUID uuid, @Nonnull String texture, @Nonnull URL skinUrl) {
        this.profile = new CustomGameProfile(uuid, texture, skinUrl);
    }

    public final @Nonnull CustomGameProfile getProfile() {
        return profile;
    }

    public static @Nonnull PlayerSkin fromBase64(@Nonnull UUID uuid, @Nonnull String texture, @Nonnull URL skinUrl) {
        return new PlayerSkin(uuid, texture, skinUrl);
    }

    public static @Nonnull PlayerSkin fromBase64(@Nonnull UUID uuid, @Nonnull String base64) {
        return new PlayerSkin(uuid, base64, decodeTextureUrl(base64));
    }

    public static @Nonnull PlayerSkin fromBase64(@Nonnull String base64) {
        return fromBase64(UUID.nameUUIDFromBytes(base64.getBytes(StandardCharsets.UTF_8)), base64);
    }

    public static @Nonnull PlayerSkin fromURL(@Nonnull UUID uuid, @Nonnull String url) {
        return new PlayerSkin(uuid, encodeUrlAsTexture(url), toUrl(url));
    }

    public static @Nonnull PlayerSkin fromURL(@Nonnull String url) {
        return fromURL(UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)), url);
    }

    public static @Nonnull PlayerSkin fromHashCode(@Nonnull UUID uuid, @Nonnull String hash) {
        return fromURL(uuid, "https://textures.minecraft.net/texture/" + hash);
    }

    public static @Nonnull PlayerSkin fromHashCode(@Nonnull String hash) {
        return fromHashCode(UUID.nameUUIDFromBytes(hash.getBytes(StandardCharsets.UTF_8)), hash);
    }

    public static @Nonnull CompletableFuture<PlayerSkin> fromPlayerUUID(@Nonnull Plugin plugin, @Nonnull UUID uuid) {
        Validate.notNull(plugin, "The plugin cannot be null");
        Validate.notNull(uuid, "The uuid cannot be null");
        return CompletableFuture.completedFuture(fromHashCode(uuid, uuid.toString().replace("-", "") + ERROR_TOKEN));
    }

    private static @Nonnull String encodeUrlAsTexture(@Nonnull String url) {
        return Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes(StandardCharsets.UTF_8));
    }

    private static @Nonnull URL decodeTextureUrl(@Nonnull String base64) {
        try {
            String decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
            int start = decoded.indexOf("https://");
            if (start < 0) {
                start = decoded.indexOf("http://");
            }
            if (start < 0) {
                throw new IllegalArgumentException("Missing URL in texture payload");
            }
            int end = decoded.indexOf('"', start);
            return new URL(decoded.substring(start, end < 0 ? decoded.length() : end));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid base64 skin texture", e);
        }
    }

    private static @Nonnull URL toUrl(@Nonnull String url) {
        try {
            return new URL(url);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid skin URL", e);
        }
    }
}

