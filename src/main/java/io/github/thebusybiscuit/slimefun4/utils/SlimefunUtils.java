package io.github.thebusybiscuit.slimefun4.utils;

import java.nio.charset.StandardCharsets;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import io.github.bakedlibs.dough.common.CommonPatterns;
import io.github.bakedlibs.dough.items.ItemMetaSnapshot;
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion;
import io.github.thebusybiscuit.slimefun4.api.events.SlimefunItemSpawnEvent;
import io.github.thebusybiscuit.slimefun4.api.exceptions.PrematureCodeException;
import io.github.thebusybiscuit.slimefun4.api.items.ItemSpawnReason;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.DistinctiveItem;
import io.github.thebusybiscuit.slimefun4.core.attributes.Radioactive;
import io.github.thebusybiscuit.slimefun4.core.attributes.Soulbound;
import io.github.thebusybiscuit.slimefun4.core.debug.Debug;
import io.github.thebusybiscuit.slimefun4.core.debug.TestCase;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun4.implementation.items.altar.AncientPedestal;
import io.github.thebusybiscuit.slimefun4.implementation.tasks.CapacitorTextureUpdateTask;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;

/**
 * This utility class holds method that are directly linked to Slimefun.
 * It provides a very crucial method for {@link ItemStack} comparison, as well as a simple method
 * to check if an {@link ItemStack} is {@link Soulbound} or not.
 *
 * @author TheBusyBiscuit
 * @author Walshy
 * @author Sfiguz7
 */
public final class SlimefunUtils {

    private static final AtomicBoolean CUSTOM_HEAD_FALLBACK_LOGGED = new AtomicBoolean(false);
    private static final AtomicBoolean CUSTOM_SKULL_PROFILE_SUPPORT_CHECKED = new AtomicBoolean(false);
    private static final AtomicBoolean CUSTOM_SKULL_PROFILE_SUPPORTED = new AtomicBoolean(false);
    private static final AtomicBoolean CUSTOM_HEAD_TEXTURE_DEBUG_LOGGED = new AtomicBoolean(false);

    private static final String NO_PICKUP_METADATA = "no_pickup";
    private static final String SOULBOUND_LORE = ChatColor.GRAY + "Soulbound";

    private SlimefunUtils() {}

    /**
     * This method quickly returns whether an {@link Item} was marked as "no_pickup" by
     * a Slimefun device.
     *
     * @param item
     *            The {@link Item} to query
     * @return Whether the {@link Item} is excluded from being picked up
     */
    public static boolean hasNoPickupFlag(@Nonnull Item item) {
        return item.hasMetadata(NO_PICKUP_METADATA);
    }

    /**
     * This will prevent the given {@link Item} from being picked up.
     * This is useful for display items which the {@link AncientPedestal} uses.
     *
     * @param item
     *            The {@link Item} to prevent from being picked up
     * @param context
     *            The context in which this {@link Item} was flagged
     */
    public static void markAsNoPickup(@Nonnull Item item, @Nonnull String context) {
        item.setMetadata(NO_PICKUP_METADATA, new FixedMetadataValue(Slimefun.instance(), context));
        /*
         * Max the pickup delay - This makes it so no Player can pick up items ever without need for an event.
         * It is also an indication used by third-party plugins to know if it's a custom item.
         * Fixes #3203
         */
        item.setPickupDelay(Short.MAX_VALUE);
    }

    /**
     * This method checks whether the given {@link ItemStack} is considered {@link Soulbound}.
     *
     * @param item
     *            The {@link ItemStack} to check for
     * @return Whether the given item is soulbound
     */
    public static boolean isSoulbound(@Nullable ItemStack item) {
        return isSoulbound(item, null);
    }

    /**
     * This method checks whether the given {@link ItemStack} is considered {@link Soulbound}.
     * If the provided item is a {@link SlimefunItem} then this method will also check that the item
     * is enabled in the provided {@link World}.
     * If the provided item is {@link Soulbound} through the {@link SlimefunItems#SOULBOUND_RUNE}, then this
     * method will also check that the {@link SlimefunItems#SOULBOUND_RUNE} is enabled in the provided {@link World}
     *
     * @param item
     *            The {@link ItemStack} to check for
     * @param world
     *            The {@link World} to check if the {@link SlimefunItem} is enabled in if applicable.
     *            If {@code null} then this will not do a world check.
     * @return Whether the given item is soulbound
     */
    public static boolean isSoulbound(@Nullable ItemStack item, @Nullable World world) {
        if (item != null && item.getType() != Material.AIR) {
            ItemMeta meta = item.hasItemMeta() ? item.getItemMeta() : null;

            SlimefunItem rune = SlimefunItems.SOULBOUND_RUNE.getItem();
            if (rune != null && !rune.isDisabled() && (world == null || !rune.isDisabledIn(world)) && hasSoulboundFlag(meta)) {
                return true;
            }

            SlimefunItem sfItem = SlimefunItem.getByItem(item);

            if (sfItem instanceof Soulbound) {
                if (world != null) {
                    return !sfItem.isDisabledIn(world);
                } else {
                    return !sfItem.isDisabled();
                }
            } else if (meta != null) {
                return meta.hasLore() && meta.getLore().contains(SOULBOUND_LORE);
            }

        }
        return false;
    }

    private static boolean hasSoulboundFlag(@Nullable ItemMeta meta) {
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey key = Slimefun.getRegistry().getSoulboundDataKey();

            return container.has(key, PersistentDataType.BYTE);
        }

        return false;
    }

    /**
     * Toggles an {@link ItemStack} to be Soulbound.<br>
     * If true is passed, this will add the {@link #SOULBOUND_LORE} and
     * add a {@link NamespacedKey} to the item so it can be quickly identified
     * by {@link #isSoulbound(ItemStack)}.<br>
     * If false is passed, this property will be removed.
     *
     * @param item
     *            The {@link ItemStack} you want to add/remove Soulbound from.
     * @param makeSoulbound
     *            If the item should be soulbound.
     *
     * @see #isSoulbound(ItemStack)
     */
    public static void setSoulbound(@Nullable ItemStack item, boolean makeSoulbound) {
        if (item == null || item.getType() == Material.AIR) {
            throw new IllegalArgumentException("A soulbound item cannot be null or air!");
        }

        boolean isSoulbound = isSoulbound(item);
        ItemMeta meta = item.getItemMeta();

        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey key = Slimefun.getRegistry().getSoulboundDataKey();

        if (makeSoulbound && !isSoulbound) {
            container.set(key, PersistentDataType.BYTE, (byte) 1);
        }

        if (!makeSoulbound && isSoulbound) {
            container.remove(key);
        }

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        if (makeSoulbound && !isSoulbound) {
            lore.add(SOULBOUND_LORE);
        }

        if (!makeSoulbound && isSoulbound) {
            lore.remove(SOULBOUND_LORE);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * This method checks whether the given {@link ItemStack} is radioactive.
     *
     * @param item
     *            The {@link ItemStack} to check
     *
     * @return Whether this {@link ItemStack} is radioactive or not
     */
    public static boolean isRadioactive(@Nullable ItemStack item) {
        return SlimefunItem.getByItem(item) instanceof Radioactive;
    }

    /**
     * This method returns an {@link ItemStack} for the given texture.
     * The result will be a Player Head with this texture.
     *
     * @param texture
     *            The texture for this head (base64 or hash)
     *
     * @return An {@link ItemStack} with this Head texture
     */
    public static @Nonnull ItemStack getCustomHead(@Nonnull String texture) {
        Validate.notNull(texture, "The provided texture is null");

        if (Slimefun.instance() == null) {
            throw new PrematureCodeException("You cannot instantiate a custom head before Slimefun was loaded.");
        }

        if (Slimefun.getMinecraftVersion() == MinecraftVersion.UNIT_TEST) {
            // com.mojang.authlib.GameProfile does not exist in a Test Environment
            return new ItemStack(Material.PLAYER_HEAD);
        }

        URL skinUrl;

        try {
            skinUrl = resolveTextureUrl(texture);
        } catch (Exception e) {
            logCustomHeadDebug("Failed to resolve a custom head texture URL.", e);
            return getPlainPlayerHeadFallback();
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta == null) {
            CUSTOM_SKULL_PROFILE_SUPPORTED.set(false);
            return getPlainPlayerHeadFallback();
        }

        try {
            if (!applyTexture(meta, skinUrl)) {
                CUSTOM_SKULL_PROFILE_SUPPORTED.set(false);
                return getPlainPlayerHeadFallback();
            }

            head.setItemMeta(meta);
            return head;
        } catch (LinkageError e) {
            CUSTOM_SKULL_PROFILE_SUPPORTED.set(false);
            return getPlainPlayerHeadFallback();
        }
    }

    /**
     * Applies the provided head texture to a placed head block.
     *
     * @param block
     *            The head block to update
     * @param texture
     *            The texture hash or base64 payload
     * @return Whether the texture could be applied successfully
     */
    public static boolean setCustomHead(@Nonnull Block block, @Nonnull String texture) {
        Validate.notNull(block, "The provided block is null");
        Validate.notNull(texture, "The provided texture is null");

        if (!(block.getState() instanceof Skull skull)) {
            return false;
        }

        try {
            URL skinUrl = resolveTextureUrl(texture);
            return skinUrl != null && applyTexture(skull, skinUrl);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean supportsCustomSkullProfiles() {
        if (Slimefun.getMinecraftVersion() == MinecraftVersion.UNIT_TEST) {
            return false;
        }

        if (CUSTOM_SKULL_PROFILE_SUPPORT_CHECKED.compareAndSet(false, true)) {
            CUSTOM_SKULL_PROFILE_SUPPORTED.set(canUseModernSkullProfiles());
        }

        return CUSTOM_SKULL_PROFILE_SUPPORTED.get();
    }

    public static @Nonnull String getItemName(@Nonnull ItemStack item) {
        Validate.notNull(item, "The provided item is null");

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                return meta.getDisplayName();
            }
        }

        String name = item.getType().name().toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        return ChatColor.translateAlternateColorCodes('&', Character.toUpperCase(name.charAt(0)) + name.substring(1));
    }

    private static boolean canUseModernSkullProfiles() {
        try {
            Class.forName("org.bukkit.profile.PlayerTextures");
            Class.forName("org.bukkit.profile.PlayerProfile");

            Class<?> bukkit = Class.forName("org.bukkit.Bukkit");
            try {
                bukkit.getMethod("createProfile", java.util.UUID.class, String.class);
            } catch (NoSuchMethodException ignored) {
                bukkit.getMethod("createPlayerProfile", java.util.UUID.class, String.class);
            }

            return hasSkullProfileMethod("setOwnerProfile")
                || hasSkullProfileMethod("setPlayerProfile")
                || hasSkullProfileMethod("setProfile");
        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError ignored) {
            return false;
        }
    }


    private static @Nonnull URL resolveTextureUrl(@Nonnull String texture) throws Exception {
        if (texture.startsWith("http://") || texture.startsWith("https://")) {
            return new URL(texture);
        }

        if (CommonPatterns.HEXADECIMAL.matcher(texture).matches()) {
            return new URL("https://textures.minecraft.net/texture/" + texture.toLowerCase(java.util.Locale.ROOT));
        }

        String decoded = new String(Base64.getDecoder().decode(texture), StandardCharsets.UTF_8);
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("https?://textures\\.minecraft\\.net/texture/[A-Za-z0-9]+", java.util.regex.Pattern.CASE_INSENSITIVE)
            .matcher(decoded);

        if (!matcher.find()) {
            throw new IllegalArgumentException("The provided texture does not contain a valid textures.minecraft.net URL");
        }

        return new URL(matcher.group());
    }

    private static boolean applyTexture(@Nonnull SkullMeta meta, @Nonnull URL skinUrl) {
        Validate.notNull(meta, "The skull meta cannot be null");
        Validate.notNull(skinUrl, "The skin URL cannot be null");

        boolean bukkit = applyTextureWithBukkitProfile(meta, skinUrl);
        boolean paper = bukkit || applyTextureWithPaperProfile(meta, skinUrl);
        logCustomHeadDebug("Skull meta texture result: bukkit=" + bukkit + ", paper=" + paper + ", meta=" + meta.getClass().getName(), null);
        return paper;
    }

    public static boolean applyCustomHeadTexture(@Nonnull SkullMeta meta, @Nonnull URL skinUrl) {
        return applyTexture(meta, skinUrl);
    }

    private static boolean applyTexture(@Nonnull Skull skull, @Nonnull URL skinUrl) {
        Validate.notNull(skull, "The skull block state cannot be null");
        Validate.notNull(skinUrl, "The skin URL cannot be null");

        boolean bukkit = applyTextureWithBukkitProfile(skull, skinUrl);
        boolean paper = bukkit || applyTextureWithPaperProfile(skull, skinUrl);
        logCustomHeadDebug("Skull block texture result: bukkit=" + bukkit + ", paper=" + paper + ", skull=" + skull.getClass().getName(), null);
        return paper;
    }

    private static boolean applyTextureWithBukkitProfile(@Nonnull Skull skull, @Nonnull URL skinUrl) {
        try {
            Object profile = createBukkitPlayerProfile("Slimefun");
            if (profile == null) {
                logCustomHeadDebug("Bukkit skull block profile creation returned null.", null);
                return false;
            }

            applyTexture(profile, skinUrl);

            if (!setSkullProfile(skull, profile, "setPlayerProfile", "org.bukkit.profile.PlayerProfile", "com.destroystokyo.paper.profile.PlayerProfile")
                && !setSkullProfile(skull, profile, "setOwnerProfile", "org.bukkit.profile.PlayerProfile", "com.destroystokyo.paper.profile.PlayerProfile")) {
                return false;
            }

            return skull.update(true, false);
        } catch (ReflectiveOperationException | LinkageError e) {
            logCustomHeadDebug("Bukkit skull block profile application failed.", e);
            return false;
        }
    }

    private static boolean applyTextureWithBukkitProfile(@Nonnull SkullMeta meta, @Nonnull URL skinUrl) {
        try {
            Object profile = createBukkitPlayerProfile("Slimefun");
            if (profile == null) {
                logCustomHeadDebug("Bukkit skull meta profile creation returned null.", null);
                return false;
            }


            applyTexture(profile, skinUrl);

            return setSkullProfile(meta, profile, "setPlayerProfile", "org.bukkit.profile.PlayerProfile", "com.destroystokyo.paper.profile.PlayerProfile")
                || setSkullProfile(meta, profile, "setOwnerProfile", "org.bukkit.profile.PlayerProfile", "com.destroystokyo.paper.profile.PlayerProfile")
                || applyTextureWithResolvableProfile(meta, profile);
        } catch (ReflectiveOperationException | LinkageError e) {
            logCustomHeadDebug("Bukkit skull meta profile application failed.", e);
            return false;
        }
    }

    private static boolean applyTextureWithPaperProfile(@Nonnull SkullMeta meta, @Nonnull URL skinUrl) {
        try {
            Object profile = createPaperPlayerProfile("Slimefun");
            if (profile == null) {
                logCustomHeadDebug("Paper skull meta profile creation returned null.", null);
                return false;
            }

            applyTexture(profile, skinUrl);

            return setSkullProfile(meta, profile, "setPlayerProfile") || setSkullProfile(meta, profile, "setOwnerProfile");
        } catch (ReflectiveOperationException | LinkageError e) {
            logCustomHeadDebug("Paper skull meta profile application failed.", e);
            return false;
        }
    }

    private static boolean applyTextureWithPaperProfile(@Nonnull Skull skull, @Nonnull URL skinUrl) {
        try {
            Object profile = createPaperPlayerProfile("Slimefun");
            if (profile == null) {
                logCustomHeadDebug("Paper skull block profile creation returned null.", null);
                return false;
            }

            applyTexture(profile, skinUrl);

            Class<?> resolvableProfileClass = Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");
            Object resolvableProfile = createResolvableProfile(resolvableProfileClass, profile);
            if (resolvableProfile == null) {
                return false;
            }

            if (setSkullProfile(skull, resolvableProfile, "setProfile")) {
                return skull.update(true, false);
            }

            return false;
        } catch (ReflectiveOperationException | LinkageError e) {
            logCustomHeadDebug("Paper skull block profile application failed.", e);
            return false;
        }
    }

    private static @Nullable Object createBukkitPlayerProfile(@Nonnull String name) throws ReflectiveOperationException {
        try {
            return Bukkit.class.getMethod("createProfile", java.lang.String.class).invoke(null, name);
        } catch (NoSuchMethodException ignored) {
            try {
                return Bukkit.class.getMethod("createPlayerProfile", java.lang.String.class).invoke(null, name);
            } catch (NoSuchMethodException ignoredToo) {
                try {
                    java.util.UUID uuid = java.util.UUID.randomUUID();
                    return Bukkit.class.getMethod("createProfile", java.util.UUID.class, String.class).invoke(null, uuid, name);
                } catch (NoSuchMethodException ignoredThrice) {
                    try {
                        java.util.UUID uuid = java.util.UUID.randomUUID();
                        return Bukkit.class.getMethod("createPlayerProfile", java.util.UUID.class, String.class).invoke(null, uuid, name);
                    } catch (NoSuchMethodException ignoredFour) {
                        try {
                            return Bukkit.class.getMethod("createProfile", java.util.UUID.class).invoke(null, java.util.UUID.randomUUID());
                        } catch (NoSuchMethodException ignoredFive) {
                            try {
                                return Bukkit.class.getMethod("createPlayerProfile", java.util.UUID.class).invoke(null, java.util.UUID.randomUUID());
                            } catch (NoSuchMethodException ignoredSix) {
                                return null;
                            }
                        }
                    }
                }
            }
        }
    }

    private static @Nullable Object createPaperPlayerProfile(@Nonnull String name) throws ReflectiveOperationException {
        try {
            return Bukkit.class.getMethod("createProfile", java.lang.String.class).invoke(null, name);
        } catch (NoSuchMethodException ignored) {
            try {
                java.util.UUID uuid = java.util.UUID.randomUUID();
                return Bukkit.class.getMethod("createProfile", java.util.UUID.class, String.class).invoke(null, uuid, name);
            } catch (NoSuchMethodException ignoredToo) {
                try {
                    return Bukkit.class.getMethod("createProfile", java.util.UUID.class).invoke(null, java.util.UUID.randomUUID());
                } catch (NoSuchMethodException ignoredThrice) {
                    try {
                        return Bukkit.class.getMethod("createProfile", java.lang.String.class).invoke(null, name);
                    } catch (NoSuchMethodException ignoredFour) {
                        return null;
                    }
                }
            }
        }
    }

    private static void applyTexture(@Nonnull Object profile, @Nonnull URL skinUrl) throws ReflectiveOperationException {
        Class<?> profileClass = profile.getClass();

        Object textures = profileClass.getMethod("getTextures").invoke(profile);
        Object updatedTextures = setSkinTexture(textures, skinUrl);
        if (updatedTextures == null) {
            throw new NoSuchMethodException("Could not find a compatible setSkin(...) method for " + textures.getClass().getName());
        }

        if (!applyTextures(profile, updatedTextures)) {
            // Some API variants only expose mutable textures via getTextures().
        }
    }

    private static @Nullable Object setSkinTexture(@Nonnull Object textures, @Nonnull URL skinUrl) throws ReflectiveOperationException {
        Class<?> texturesClass = textures.getClass();

        for (java.lang.reflect.Method method : texturesClass.getMethods()) {
            if ((!method.getName().equals("setSkin") && !method.getName().equals("withSkin")) || method.getParameterCount() == 0) {
                continue;
            }

            try {
                Object[] args = buildSetSkinArguments(method.getParameterTypes(), skinUrl);
                if (args != null) {
                    Object result = method.invoke(textures, args);

                    if (result != null) {
                        return result;
                    }

                    return textures;
                }
            } catch (IllegalArgumentException ignored) {
                // Try the next compatible overload.
            }
        }

        return null;
    }

    private static @Nullable Object[] buildSetSkinArguments(@Nonnull Class<?>[] parameterTypes, @Nonnull URL skinUrl) throws ReflectiveOperationException {
        if (parameterTypes.length == 0) {
            return null;
        }

        Object[] args = new Object[parameterTypes.length];
        Object firstArgument = convertSkinUrlArgument(parameterTypes[0], skinUrl);

        if (firstArgument == null) {
            return null;
        }

        args[0] = firstArgument;

        for (int i = 1; i < parameterTypes.length; i++) {
            Object defaultValue = getDefaultSkinArgument(parameterTypes[i]);
            if (defaultValue == null && parameterTypes[i].isPrimitive()) {
                return null;
            }

            args[i] = defaultValue;
        }

        return args;
    }

    private static @Nullable Object convertSkinUrlArgument(@Nonnull Class<?> type, @Nonnull URL skinUrl) {
        if (type.isAssignableFrom(URL.class)) {
            return skinUrl;
        }

        if (type == java.net.URI.class) {
            return java.net.URI.create(skinUrl.toString());
        }

        if (type == String.class || type == CharSequence.class) {
            return skinUrl.toString();
        }

        return null;
    }

    private static @Nullable Object getDefaultSkinArgument(@Nonnull Class<?> type) throws ReflectiveOperationException {
        if (!type.isPrimitive()) {
            if (type.isEnum()) {
                Object[] constants = type.getEnumConstants();
                if (constants != null && constants.length > 0) {
                    return constants[0];
                }
            }

            return null;
        }

        if (type == boolean.class) {
            return false;
        }

        if (type == byte.class) {
            return (byte) 0;
        }

        if (type == short.class) {
            return (short) 0;
        }

        if (type == int.class) {
            return 0;
        }

        if (type == long.class) {
            return 0L;
        }

        if (type == float.class) {
            return 0F;
        }

        if (type == double.class) {
            return 0D;
        }

        if (type == char.class) {
            return '\0';
        }

        return null;
    }

    private static boolean applyTextures(@Nonnull Object profile, @Nonnull Object textures) throws ReflectiveOperationException {
        Class<?> profileClass = profile.getClass();
        Class<?> runtimeTexturesClass = textures.getClass();

        for (java.lang.reflect.Method method : profileClass.getMethods()) {
            if (!method.getName().equals("setTextures") || method.getParameterCount() != 1) {
                continue;
            }

            if (method.getParameterTypes()[0].isAssignableFrom(runtimeTexturesClass)) {
                method.invoke(profile, textures);
                return true;
            }
        }

        return false;
    }


    private static boolean applyTextureWithResolvableProfile(@Nonnull SkullMeta meta, @Nonnull Object profile) throws ReflectiveOperationException {
        Class<?> resolvableProfileClass = Class.forName("io.papermc.paper.datacomponent.item.ResolvableProfile");

        Object resolvableProfile = createResolvableProfile(resolvableProfileClass, profile);
        if (resolvableProfile == null) {
            return false;
        }

        try {
            meta.getClass().getMethod("setProfile", resolvableProfileClass).invoke(meta, resolvableProfile);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static @Nullable Object createResolvableProfile(@Nonnull Class<?> resolvableProfileClass, @Nonnull Object profile) {
        Class<?> runtimeProfileClass = profile.getClass();

        for (java.lang.reflect.Method method : resolvableProfileClass.getMethods()) {
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())
                || method.getParameterCount() != 1
                || !resolvableProfileClass.isAssignableFrom(method.getReturnType())) {
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[0];
            if (!parameterType.isAssignableFrom(runtimeProfileClass)) {
                continue;
            }

            try {
                return method.invoke(null, profile);
            } catch (ReflectiveOperationException | IllegalArgumentException | LinkageError ignored) {
                // Try the next factory method.
            }
        }

        return null;
    }

    private static boolean setSkullProfile(@Nonnull Object target, @Nonnull Object profile, @Nonnull String methodName, @Nonnull String... profileClassNames) throws ReflectiveOperationException {
        Class<?> runtimeProfileClass = profile.getClass();
        Throwable firstFailure = null;
        java.lang.reflect.Method failedMethod = null;

        for (java.lang.reflect.Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom(runtimeProfileClass)) {
                try {
                    method.invoke(target, profile);
                    return true;
                } catch (IllegalAccessException e) {
                    if (tryInvokeWithAccessibleOverride(method, target, profile)) {
                        return true;
                    }

                    java.lang.reflect.Method publicApiMethod = findPublicCompatibleSkullSetter(target.getClass(), methodName, runtimeProfileClass, method);
                    if (publicApiMethod != null) {
                        try {
                            publicApiMethod.invoke(target, profile);
                            return true;
                        } catch (ReflectiveOperationException | IllegalArgumentException | LinkageError ignored) {
                            // Keep the original failure for logging.
                        }
                    }

                    if (firstFailure == null) {
                        firstFailure = e;
                        failedMethod = method;
                    }
                } catch (ReflectiveOperationException | IllegalArgumentException | LinkageError e) {
                    if (firstFailure == null) {
                        firstFailure = e;
                        failedMethod = method;
                    }
                }
            }
        }

        if (firstFailure != null && failedMethod != null) {
            Throwable cause = firstFailure instanceof java.lang.reflect.InvocationTargetException invocation && invocation.getCause() != null
                ? invocation.getCause()
                : firstFailure;

            logCustomHeadDebug(
                "Skull profile setter failed: "
                    + target.getClass().getName()
                    + "#"
                    + failedMethod.getName()
                    + "("
                    + failedMethod.getParameterTypes()[0].getName()
                    + ") -> "
                    + cause.getClass().getName()
                    + ": "
                    + String.valueOf(cause.getMessage()),
                null
            );
        }

        return false;
    }

    private static boolean tryInvokeWithAccessibleOverride(@Nonnull java.lang.reflect.Method method, @Nonnull Object target, @Nonnull Object profile) {
        try {
            if (!method.canAccess(target) && !method.trySetAccessible()) {
                return false;
            }

            method.invoke(target, profile);
            return true;
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException | LinkageError ignored) {
            return false;
        }
    }

    private static @Nullable java.lang.reflect.Method findPublicCompatibleSkullSetter(@Nonnull Class<?> targetClass, @Nonnull String methodName, @Nonnull Class<?> runtimeProfileClass, @Nonnull java.lang.reflect.Method ignoredMethod) {
        java.util.ArrayDeque<Class<?>> queue = new java.util.ArrayDeque<>();
        java.util.HashSet<Class<?>> visited = new java.util.HashSet<>();
        queue.add(targetClass);

        while (!queue.isEmpty()) {
            Class<?> current = queue.removeFirst();
            if (!visited.add(current)) {
                continue;
            }

            for (java.lang.reflect.Method method : current.getMethods()) {
                if (method.equals(ignoredMethod)
                    || !method.getName().equals(methodName)
                    || method.getParameterCount() != 1
                    || !method.getParameterTypes()[0].isAssignableFrom(runtimeProfileClass)) {
                    continue;
                }

                Class<?> declaringClass = method.getDeclaringClass();
                if (java.lang.reflect.Modifier.isPublic(declaringClass.getModifiers()) || declaringClass.isInterface()) {
                    return method;
                }
            }

            Class<?> superClass = current.getSuperclass();
            if (superClass != null) {
                queue.add(superClass);
            }

            for (Class<?> interfaceClass : current.getInterfaces()) {
                queue.add(interfaceClass);
            }
        }

        return null;
    }

    private static boolean hasSkullProfileMethod(@Nonnull String methodName) {
        for (java.lang.reflect.Method method : SkullMeta.class.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                return true;
            }
        }

        return false;
    }

    private static @Nonnull ItemStack getPlainPlayerHeadFallback() {
        if (CUSTOM_HEAD_FALLBACK_LOGGED.compareAndSet(false, true)) {
            Slimefun.logger().log(Level.WARNING, "Falling back to a plain player head because the runtime no longer supports custom skull profiles.");
        }

        return new ItemStack(Material.PLAYER_HEAD);
    }

    private static void logCustomHeadDebug(@Nonnull String message, @Nullable Throwable throwable) {
        if (CUSTOM_HEAD_TEXTURE_DEBUG_LOGGED.compareAndSet(false, true)) {
            if (throwable == null) {
                Slimefun.logger().log(Level.WARNING, message);
            } else {
                Slimefun.logger().log(Level.WARNING, message, throwable);
            }
        }
    }

    public static boolean containsSimilarItem(Inventory inventory, ItemStack item, boolean checkLore) {
        if (inventory == null || item == null) {
            return false;
        }

        // Performance optimization
        SlimefunItem slimefunItem = SlimefunItem.getByItem(item);
        if (slimefunItem != null) {
            item = ItemStackWrapper.wrap(item);
        }

        for (ItemStack stack : inventory.getStorageContents()) {
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }

            if (isItemSimilar(stack, item, checkLore, false, true)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Compares two {@link ItemStack}s and returns if they are similar or not.
     * Takes into account some shortcut checks specific to {@link SlimefunItem}s
     * for performance.
     * Will check for distintion of items by default and will also confirm the amount
     * is the same.
     * @see DistinctiveItem
     *
     * @param item
     *            The {@link ItemStack} being tested.
     * @param sfitem
     *            The {@link ItemStack} that {@param item} is being compared against.
     * @param checkLore
     *            Whether to include the current lore of either item in the comparison
     *
     * @return True if the given {@link ItemStack}s are similar under the given constraints
     */
    public static boolean isItemSimilar(@Nullable ItemStack item, @Nullable ItemStack sfitem, boolean checkLore) {
        return isItemSimilar(item, sfitem, checkLore, true, true);
    }

    /**
     * Compares two {@link ItemStack}s and returns if they are similar or not.
     * Takes into account some shortcut checks specific to {@link SlimefunItem}s
     * for performance.
     * Will check for distintion of items by default
     * @see DistinctiveItem
     *
     * @param item
     *            The {@link ItemStack} being tested.
     * @param sfitem
     *            The {@link ItemStack} that {@param item} is being compared against.
     * @param checkLore
     *            Whether to include the current lore of either item in the comparison
     * @param checkAmount
     *            Whether to include the item's amount(s) in the comparison
     *
     * @return True if the given {@link ItemStack}s are similar under the given constraints
     */
    public static boolean isItemSimilar(@Nullable ItemStack item, @Nullable ItemStack sfitem, boolean checkLore, boolean checkAmount) {
        return isItemSimilar(item, sfitem, checkLore, checkAmount, true);
    }

    /**
     * Compares two {@link ItemStack}s and returns if they are similar or not.
     * Takes into account some shortcut checks specific to {@link SlimefunItem}s
     * for performance.
     *
     * @param item
     *            The {@link ItemStack} being tested.
     * @param sfitem
     *            The {@link ItemStack} that {@param item} is being compared against.
     * @param checkLore
     *            Whether to include the current lore of either item in the comparison
     * @param checkAmount
     *            Whether to include the item's amount(s) in the comparison
     * @param checkDistinction
     *            Whether to check for special distinctive properties of the items.
     *            @see DistinctiveItem
     *
     * @return True if the given {@link ItemStack}s are similar under the given constraints
     */
    public static boolean isItemSimilar(@Nullable ItemStack item, @Nullable ItemStack sfitem, boolean checkLore, boolean checkAmount, boolean checkDistinction) {
        if (item == null) {
            return sfitem == null;
        } else if (sfitem == null) {
            return false;
        } else if (item.getType() != sfitem.getType()) {
            return false;
        } else if (checkAmount && item.getAmount() < sfitem.getAmount()) {
            return false;
        }
        SlimefunItem sf_sfitem = SlimefunItem.getByItem(sfitem);
        SlimefunItem sf_item = SlimefunItem.getByItem(item);
   
        if (sf_sfitem != null && sf_item != null) {
            if (!sf_sfitem.getId().equals(sf_item.getId())) {
                return false;
            }
            /*
             * PR #3417
             *
             * Some items can't rely on just IDs matching and will implement {@link DistinctiveItem}
             * in which case we want to use the method provided to compare
             */
            if (checkDistinction && sf_sfitem instanceof DistinctiveItem distinctive && sf_item instanceof DistinctiveItem) {
                return distinctive.canStack(sf_sfitem.getItem().getItemMeta(), sf_item.getItem().getItemMeta());
            }
            return true;
        } else if (item.hasItemMeta()) {
            Debug.log(TestCase.CARGO_INPUT_TESTING, "SlimefunUtils#isItemSimilar - item.hasItemMeta()");
            ItemMeta itemMeta = item.getItemMeta();

            if (sf_sfitem != null) {
                String id = Slimefun.getItemDataService().getItemData(itemMeta).orElse(null);

                if (id != null) {
                    if (checkDistinction) {
                        /*
                         * PR #3417
                         *
                         * Some items can't rely on just IDs matching and will implement {@link DistinctiveItem}
                         * in which case we want to use the method provided to compare
                         */
                        Optional<DistinctiveItem> optionalDistinctive = getDistinctiveItem(id);
                        if (optionalDistinctive.isPresent()) {
                            ItemMeta sfItemMeta = sfitem.getItemMeta();
                            return optionalDistinctive.get().canStack(sfItemMeta, itemMeta);
                        }
                    }
                    return id.equals((sf_sfitem.getId()));
                }

                ItemMeta meta = sf_sfitem.getItem().getItemMeta();
                return equalsItemMeta(itemMeta, meta, checkLore);
            } else if (sfitem instanceof ItemStackWrapper && sfitem.hasItemMeta()) {
                Debug.log(TestCase.CARGO_INPUT_TESTING, "  is wrapper");
                /*
                 * Cargo optimization (PR #3258)
                 *
                 * Slimefun items may be ItemStackWrapper's in the context of cargo
                 * so let's try to do an ID comparison before meta comparison
                 */
                Debug.log(TestCase.CARGO_INPUT_TESTING, "  sfitem is ItemStackWrapper - possible SF Item: {}", sfitem);

                ItemMeta possibleSfItemMeta = sfitem.getItemMeta();
                String id = Slimefun.getItemDataService().getItemData(itemMeta).orElse(null);
                String possibleItemId = Slimefun.getItemDataService().getItemData(possibleSfItemMeta).orElse(null);
                // Prioritize SlimefunItem id comparison over ItemMeta comparison
                if (id != null && id.equals(possibleItemId)) {
                    Debug.log(TestCase.CARGO_INPUT_TESTING, "  Item IDs matched!");

                    /*
                     * PR #3417
                     *
                     * Some items can't rely on just IDs matching and will implement {@link DistinctiveItem}
                     * in which case we want to use the method provided to compare
                     */
                    Optional<DistinctiveItem> optionalDistinctive = getDistinctiveItem(id);
                    if (optionalDistinctive.isPresent()) {
                        return optionalDistinctive.get().canStack(possibleSfItemMeta, itemMeta);
                    }
                    return true;
                } else {
                    Debug.log(TestCase.CARGO_INPUT_TESTING, "  Item IDs don't match, checking meta {} == {} (lore: {})", itemMeta, possibleSfItemMeta, checkLore);
                    return equalsItemMeta(itemMeta, possibleSfItemMeta, checkLore);
                }
            } else if (sfitem.hasItemMeta()) {
                ItemMeta sfItemMeta = sfitem.getItemMeta();
                Debug.log(TestCase.CARGO_INPUT_TESTING, "  Comparing meta (vanilla items?) - {} == {} (lore: {})", itemMeta, sfItemMeta, checkLore);
                return equalsItemMeta(itemMeta, sfItemMeta, checkLore);
            } else {
                return false;
            }
        } else {
            return !sfitem.hasItemMeta();
        }
    }

    private static @Nonnull Optional<DistinctiveItem> getDistinctiveItem(@Nonnull String id) {
        SlimefunItem slimefunItem = SlimefunItem.getById(id);
        if (slimefunItem instanceof DistinctiveItem distinctive) {
            return Optional.of(distinctive);
        }
        return Optional.empty();
    }

    private static boolean equalsItemMeta(@Nonnull ItemMeta itemMeta, @Nonnull ItemMetaSnapshot itemMetaSnapshot, boolean checkLore) {
        Optional<String> displayName = itemMetaSnapshot.getDisplayName();

        if (itemMeta.hasDisplayName() != displayName.isPresent()) {
            return false;
        } else if (itemMeta.hasDisplayName() && displayName.isPresent() && !itemMeta.getDisplayName().equals(displayName.get())) {
            return false;
        } else if (checkLore) {
            Optional<List<String>> itemLore = itemMetaSnapshot.getLore();

            if (itemMeta.hasLore() && itemLore.isPresent() && !equalsLore(itemMeta.getLore(), itemLore.get())) {
                return false;
            } else if (itemMeta.hasLore() != itemLore.isPresent()) {
                return false;
            }
        }

        // Fixes #3133: name and lore are not enough
        OptionalInt itemCustomModelData = itemMetaSnapshot.getCustomModelData();
        if (itemMeta.hasCustomModelData() && itemCustomModelData.isPresent() && itemMeta.getCustomModelData() != itemCustomModelData.getAsInt()) {
            return false;
        } else {
            return itemMeta.hasCustomModelData() == itemCustomModelData.isPresent();
        }
    }

    private static boolean equalsItemMeta(@Nonnull ItemMeta itemMeta, @Nonnull ItemMeta sfitemMeta, boolean checkLore) {
        if (itemMeta.hasDisplayName() != sfitemMeta.hasDisplayName()) {
            return false;
        } else if (itemMeta.hasDisplayName() && sfitemMeta.hasDisplayName() && !itemMeta.getDisplayName().equals(sfitemMeta.getDisplayName())) {
            return false;
        } else if (checkLore) {
            boolean hasItemMetaLore = itemMeta.hasLore();
            boolean hasSfItemMetaLore = sfitemMeta.hasLore();

            if (hasItemMetaLore && hasSfItemMetaLore) {
                if (!equalsLore(itemMeta.getLore(), sfitemMeta.getLore())) {
                    return false;
                }
            } else if (hasItemMetaLore != hasSfItemMetaLore) {
                return false;
            }
        }

        // Fixes #3133: name and lore are not enough
        boolean hasItemMetaCustomModelData = itemMeta.hasCustomModelData();
        boolean hasSfItemMetaCustomModelData = sfitemMeta.hasCustomModelData();
        if (hasItemMetaCustomModelData && hasSfItemMetaCustomModelData && itemMeta.getCustomModelData() != sfitemMeta.getCustomModelData()) {
            return false;
        } else if (hasItemMetaCustomModelData != hasSfItemMetaCustomModelData) {
            return false;
        }

        if (!(itemMeta instanceof PotionMeta potionMeta) || !(sfitemMeta instanceof PotionMeta sfPotionMeta)) {
            return true;
        }
        MinecraftVersion current = Slimefun.getMinecraftVersion();

        if (current.isBefore(20, 2)) {
            // getBasePotionData pre 1.20.2
            return potionMeta.getBasePotionData().equals(sfPotionMeta.getBasePotionData());
        } else if (current.isBefore(20, 5)) {
            //  getBasePotionType without null check for 1.20.3 and 1.20.4
            return potionMeta.getBasePotionType() == sfPotionMeta.getBasePotionType();
        }
        // check if potionMetha has a basePotionType (acting a null check for getBasePotionType
        // on 1.20.5+
        if (potionMeta.hasBasePotionType() != sfPotionMeta.hasBasePotionType()) {
            return false;
        }
        return potionMeta.getBasePotionType() == sfPotionMeta.getBasePotionType();
    }

    /**
     * This checks if the two provided lores are equal.
     * This method will ignore any lines such as the soulbound one.
     *
     * @param lore1
     *            The first lore
     * @param lore2
     *            The second lore
     *
     * @return Whether the two lores are equal
     */
    public static boolean equalsLore(@Nonnull List<String> lore1, @Nonnull List<String> lore2) {
        Validate.notNull(lore1, "Cannot compare lore that is null!");
        Validate.notNull(lore2, "Cannot compare lore that is null!");

        List<String> longerList = lore1.size() > lore2.size() ? lore1 : lore2;
        List<String> shorterList = lore1.size() > lore2.size() ? lore2 : lore1;

        int a = 0;
        int b = 0;

        for (; a < longerList.size(); a++) {
            if (isLineIgnored(longerList.get(a))) {
                continue;
            }

            while (shorterList.size() > b && isLineIgnored(shorterList.get(b))) {
                b++;
            }

            if (b >= shorterList.size()) {
                return false;
            } else if (longerList.get(a).equals(shorterList.get(b))) {
                b++;
            } else {
                return false;
            }
        }

        while (shorterList.size() > b && isLineIgnored(shorterList.get(b))) {
            b++;
        }

        return b == shorterList.size();
    }

    private static boolean isLineIgnored(@Nonnull String line) {
        return line.equals(SOULBOUND_LORE);
    }

    public static void updateCapacitorTexture(@Nonnull Location l, int charge, int capacity) {
        Validate.notNull(l, "Cannot update a texture for null");
        Validate.isTrue(capacity > 0, "Capacity must be greater than zero!");

        Slimefun.runSync(new CapacitorTextureUpdateTask(l, charge, capacity));
    }

    /**
     * This checks whether the {@link Player} is able to use the given {@link ItemStack}.
     * It will always return <code>true</code> for non-Slimefun items.
     * <p>
     * If you already have an instance of {@link SlimefunItem}, please use {@link SlimefunItem#canUse(Player, boolean)}.
     *
     * @param p
     *            The {@link Player}
     * @param item
     *            The {@link ItemStack} to check
     * @param sendMessage
     *            Whether to send a message response to the {@link Player}
     *
     * @return Whether the {@link Player} is able to use that item.
     */
    public static boolean canPlayerUseItem(@Nonnull Player p, @Nullable ItemStack item, boolean sendMessage) {
        Validate.notNull(p, "The player cannot be null");

        SlimefunItem sfItem = SlimefunItem.getByItem(item);

        if (sfItem != null) {
            return sfItem.canUse(p, sendMessage);
        } else {
            return true;
        }
    }

    /**
     * Helper method to spawn an {@link ItemStack}.
     * This method automatically calls a {@link SlimefunItemSpawnEvent} to allow
     * other plugins to catch the item being dropped.
     *
     * @param loc
     *            The {@link Location} where to drop the item
     * @param item
     *            The {@link ItemStack} to drop
     * @param reason
     *            The {@link ItemSpawnReason} why the item is being dropped
     * @param addRandomOffset
     *            Whether a random offset should be added (see {@link World#dropItemNaturally(Location, ItemStack)})
     * @param player
     *            The player that caused this {@link SlimefunItemSpawnEvent}
     *
     * @return The dropped {@link Item} (or null if the {@link SlimefunItemSpawnEvent} was cancelled)
     */
    @ParametersAreNonnullByDefault
    public static @Nullable Item spawnItem(Location loc, ItemStack item, ItemSpawnReason reason, boolean addRandomOffset, @Nullable Player player) {
        SlimefunItemSpawnEvent event = new SlimefunItemSpawnEvent(player, loc, item, reason);
        Slimefun.instance().getServer().getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            World world = event.getLocation().getWorld();

            if (addRandomOffset) {
                return world.dropItemNaturally(event.getLocation(), event.getItemStack());
            } else {
                return world.dropItem(event.getLocation(), event.getItemStack());
            }
        } else {
            return null;
        }
    }

    /**
     * Helper method to spawn an {@link ItemStack}.
     * This method automatically calls a {@link SlimefunItemSpawnEvent} to allow
     * other plugins to catch the item being dropped.
     *
     * @param loc
     *            The {@link Location} where to drop the item
     * @param item
     *            The {@link ItemStack} to drop
     * @param reason
     *            The {@link ItemSpawnReason} why the item is being dropped
     * @param addRandomOffset
     *            Whether a random offset should be added (see {@link World#dropItemNaturally(Location, ItemStack)})
     *
     * @return The dropped {@link Item} (or null if the {@link SlimefunItemSpawnEvent} was cancelled)
     */
    @ParametersAreNonnullByDefault
    public static @Nullable Item spawnItem(Location loc, ItemStack item, ItemSpawnReason reason, boolean addRandomOffset) {
        return spawnItem(loc, item, reason, addRandomOffset, null);
    }

    /**
     * Helper method to spawn an {@link ItemStack}.
     * This method automatically calls a {@link SlimefunItemSpawnEvent} to allow
     * other plugins to catch the item being dropped.
     *
     * @param loc
     *            The {@link Location} where to drop the item
     * @param item
     *            The {@link ItemStack} to drop
     * @param reason
     *            The {@link ItemSpawnReason} why the item is being dropped
     *
     * @return The dropped {@link Item} (or null if the {@link SlimefunItemSpawnEvent} was cancelled)
     */
    @ParametersAreNonnullByDefault
    public static @Nullable Item spawnItem(Location loc, ItemStack item, ItemSpawnReason reason) {
        return spawnItem(loc, item, reason, false);
    }

    /**
     * Helper method to check if an Inventory is empty (has no items in "storage").
     * If the MC version is 1.16 or above
     * this will call {@link Inventory#isEmpty()} (Which calls MC code resulting in a faster method).
     *
     * @param inventory
     *            The {@link Inventory} to check.
     *
     * @return True if the inventory is empty and false otherwise
     */
    public static boolean isInventoryEmpty(@Nonnull Inventory inventory) {
        if (Slimefun.getMinecraftVersion().isAtLeast(MinecraftVersion.MINECRAFT_1_16)) {
            return inventory.isEmpty();
        } else {
            for (ItemStack is : inventory.getStorageContents()) {
                if (is != null && !is.getType().isAir()) {
                    return false;
                }
            }
            return true;
        }
    }
}
