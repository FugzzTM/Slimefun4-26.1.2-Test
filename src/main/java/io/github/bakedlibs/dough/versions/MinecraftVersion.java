package io.github.bakedlibs.dough.versions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * Lenient Minecraft version parser that accepts experimental server strings such as
 * {@code 26.1.2.build.2575-experimental}.
 */
public class MinecraftVersion extends SemanticVersion {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    public MinecraftVersion(int majorVersion, int minorVersion, int patchVersion) {
        super(majorVersion, minorVersion, patchVersion);
    }

    private MinecraftVersion(@Nonnull SemanticVersion version) {
        super(version.getMajorVersion(), version.getMinorVersion(), version.getPatchVersion());
    }

    public static @Nonnull MinecraftVersion of(@Nonnull Server server) throws UnknownServerVersionException {
        Validate.notNull(server, "The server cannot be null");

        SemanticVersion parsed = tryParse(server.getMinecraftVersion());
        if (parsed == null) {
            parsed = tryParse(server.getBukkitVersion());
        }
        if (parsed == null) {
            parsed = tryParse(server.getVersion());
        }

        if (parsed == null) {
            throw new UnknownServerVersionException("Could not recognize version string: " + server.getMinecraftVersion(), new Exception(server.getVersion()));
        }

        return new MinecraftVersion(parsed);
    }

    public static @Nonnull MinecraftVersion get() throws UnknownServerVersionException {
        return of(Bukkit.getServer());
    }

    public static boolean isMocked(@Nonnull Server server) {
        Validate.notNull(server, "The server cannot be null");

        String className = server.getClass().getName().toLowerCase(java.util.Locale.ROOT);
        String version = server.getVersion().toLowerCase(java.util.Locale.ROOT);
        return className.contains("mock") || version.contains("mockbukkit");
    }

    public static boolean isMocked() {
        return isMocked(Bukkit.getServer());
    }

    @Override
    public @Nonnull String getAsString() {
        return super.getAsString();
    }

    private static SemanticVersion tryParse(@Nonnull String versionString) {
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (!matcher.find()) {
            return null;
        }

        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(2));
        int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
        return new SemanticVersion(major, minor, patch);
    }
}

