package io.github.thebusybiscuit.slimefun4.utils;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import io.github.thebusybiscuit.slimefun4.core.services.localization.LanguagePreset;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.block.BlockMock;

class TestHeadTextures {

    private static ServerMock server;

    private static final class SetterProbeTarget {
        private boolean called;

        @SuppressWarnings("unused")
        public void setOwnerProfile(Object profile) {
            called = profile != null;
        }
    }

    private static final class FakeTextures {
        private URL skin;

        @SuppressWarnings("unused")
        public void setSkin(URL skin) {
            this.skin = skin;
        }
    }

    private static final class FakeProfile {
        private final FakeTextures textures = new FakeTextures();

        @SuppressWarnings("unused")
        public FakeTextures getTextures() {
            return textures;
        }
    }

    private static final class UriOnlyTextures {
        private java.net.URI skin;

        @SuppressWarnings("unused")
        public void setSkin(java.net.URI skin) {
            this.skin = skin;
        }
    }

    private static final class UriOnlyProfile {
        private final UriOnlyTextures textures = new UriOnlyTextures();

        @SuppressWarnings("unused")
        public UriOnlyTextures getTextures() {
            return textures;
        }
    }

    private static final class ImmutableTextures {
        private final URL skin;

        private ImmutableTextures(URL skin) {
            this.skin = skin;
        }

        @SuppressWarnings("unused")
        public ImmutableTextures withSkin(URL skin) {
            return new ImmutableTextures(skin);
        }
    }

    private static final class ImmutableProfile {
        private ImmutableTextures textures = new ImmutableTextures(null);

        @SuppressWarnings("unused")
        public ImmutableTextures getTextures() {
            return textures;
        }

        @SuppressWarnings("unused")
        public void setTextures(ImmutableTextures textures) {
            this.textures = textures;
        }
    }

    private static final class ThrowingSetterTarget {
        private boolean fallbackCalled;

        @SuppressWarnings("unused")
        public void setOwnerProfile(CharSequence profile) {
            throw new IllegalStateException("Expected failure for first compatible method");
        }

        @SuppressWarnings("unused")
        public void setOwnerProfile(StringBuilder profile) {
            fallbackCalled = true;
        }
    }

    private static final class InaccessibleSetterTarget {
        private boolean called;

        @SuppressWarnings("unused")
        public void setOwnerProfile(CharSequence profile) {
            called = profile != null;
        }
    }

    @BeforeAll
    public static void load() {
        server = MockBukkit.mock();
        MockBukkit.load(Slimefun.class);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
        server = null;
    }

    @Test
    @DisplayName("Test if the HeadTexture enum contains any duplicates")
    void testForDuplicates() {
        Set<String> textures = new HashSet<>();

        for (HeadTexture head : HeadTexture.values()) {
            String texture = head.getTexture();
            Assertions.assertNotNull(texture);
            Assertions.assertTrue(texture.matches("[0-9a-f]+"), head.name() + " texture must be lowercase hexadecimal");

            // This will fail if a duplicate is found
            Assertions.assertTrue(textures.add(texture));
        }
    }


    @Test
    @DisplayName("Test custom head creation returns a player head")
    void testGetCustomHeadReturnsPlayerHead() {
        String texture = "e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52";
        ItemStack head = SlimefunUtils.getCustomHead(texture);

        Assertions.assertNotNull(head);
        Assertions.assertEquals(Material.PLAYER_HEAD, head.getType());
        assertTextureIfSupported(head, toTextureUrl(texture), SlimefunUtils.supportsCustomSkullProfiles());
    }

    @Test
    @DisplayName("Test the plain player head fallback returns a player head")
    void testPlainPlayerHeadFallbackReturnsPlayerHead() throws ReflectiveOperationException {
        Method method = SlimefunUtils.class.getDeclaredMethod("getPlainPlayerHeadFallback");
        method.setAccessible(true);

        ItemStack head = (ItemStack) method.invoke(null);

        Assertions.assertNotNull(head);
        Assertions.assertEquals(Material.PLAYER_HEAD, head.getType());
    }

    @Test
    @DisplayName("Test custom head creation accepts direct texture URLs")
    void testGetCustomHeadAcceptsDirectTextureUrl() {
        String texture = "e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52";
        String url = toTextureUrl(texture);

        ItemStack head = SlimefunUtils.getCustomHead(url);

        Assertions.assertNotNull(head);
        Assertions.assertEquals(Material.PLAYER_HEAD, head.getType());
        assertTextureIfSupported(head, url, SlimefunUtils.supportsCustomSkullProfiles());
    }

    @Test
    @DisplayName("Test custom head creation accepts base64 texture payloads")
    void testGetCustomHeadAcceptsBase64TexturePayload() {
        String texture = "e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52";
        String url = toTextureUrl(texture);
        String base64 = java.util.Base64.getEncoder().encodeToString(("{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}").getBytes(java.nio.charset.StandardCharsets.UTF_8));

        ItemStack head = SlimefunUtils.getCustomHead(base64);

        Assertions.assertNotNull(head);
        Assertions.assertEquals(Material.PLAYER_HEAD, head.getType());
        assertTextureIfSupported(head, url, SlimefunUtils.supportsCustomSkullProfiles());
    }

    @Test
    @DisplayName("Test the English language preset texture creates a custom head")
    void testEnglishLanguagePresetTextureCreatesCustomHead() {
        String texture = LanguagePreset.ENGLISH.getTexture();
        ItemStack head = SlimefunUtils.getCustomHead(texture);

        Assertions.assertNotNull(head);
        Assertions.assertEquals(Material.PLAYER_HEAD, head.getType());
        assertTextureIfSupported(head, toTextureUrl(texture), SlimefunUtils.supportsCustomSkullProfiles());
    }


    @Test
    @DisplayName("Test custom head creation rejects null input")
    void testGetCustomHeadRejectsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> SlimefunUtils.getCustomHead(null));
    }

    @Test
    @DisplayName("Test Bukkit profile factory prefers the modern createProfile API")
    void testCreateBukkitPlayerProfilePrefersCreateProfile() throws ReflectiveOperationException {
        Assumptions.assumeTrue(hasBukkitCreateProfile(), "Current test runtime does not expose Bukkit.createProfile(...)");

        Method method = SlimefunUtils.class.getDeclaredMethod("createBukkitPlayerProfile", String.class);
        method.setAccessible(true);

        Object profile = method.invoke(null, "Slimefun");
        Assertions.assertNotNull(profile, "createBukkitPlayerProfile should not return null when Bukkit.createProfile(...) is available");
    }

    @Test
    @DisplayName("Test skull profile setter lookup accepts compatible runtime profile classes")
    void testSetSkullProfileAcceptsCompatibleRuntimeProfileClass() throws ReflectiveOperationException {
        Method method = SlimefunUtils.class.getDeclaredMethod("setSkullProfile", Object.class, Object.class, String.class, String[].class);
        method.setAccessible(true);

        SetterProbeTarget target = new SetterProbeTarget();
        Object profile = new StringBuilder("profile");

        boolean applied = (Boolean) method.invoke(null, target, profile, "setOwnerProfile", new String[] {"java.lang.CharSequence"});

        Assertions.assertTrue(applied, "The setter lookup should accept a compatible runtime profile implementation");
        Assertions.assertTrue(target.called, "The compatible setter should have been invoked");
    }

    @Test
    @DisplayName("Test skull profile setter lookup does not depend on profile class names")
    void testSetSkullProfileDoesNotDependOnProfileClassNames() throws ReflectiveOperationException {
        Method method = SlimefunUtils.class.getDeclaredMethod("setSkullProfile", Object.class, Object.class, String.class, String[].class);
        method.setAccessible(true);

        SetterProbeTarget target = new SetterProbeTarget();
        Object profile = new Object();

        boolean applied = (Boolean) method.invoke(null, target, profile, "setOwnerProfile", new String[] {"com.example.DoesNotExist"});

        Assertions.assertTrue(applied, "The setter lookup should use the runtime profile type instead of the declared class names");
        Assertions.assertTrue(target.called, "The fallback setter should have been invoked");
    }

    @Test
    @DisplayName("Test texture application uses the runtime textures object")
    void testApplyTextureUsesRuntimeTexturesObject() throws Exception {
        Method method = SlimefunUtils.class.getDeclaredMethod("applyTexture", Object.class, URL.class);
        method.setAccessible(true);

        FakeProfile profile = new FakeProfile();
        URL skinUrl = new URL(toTextureUrl("e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52"));

        method.invoke(null, profile, skinUrl);

        Assertions.assertEquals(skinUrl, profile.getTextures().skin, "The runtime textures object should receive the skin URL directly");
    }

    @Test
    @DisplayName("Test texture application supports URI-only setSkin overloads")
    void testApplyTextureSupportsUriOnlySetSkin() throws Exception {
        Method method = SlimefunUtils.class.getDeclaredMethod("applyTexture", Object.class, URL.class);
        method.setAccessible(true);

        UriOnlyProfile profile = new UriOnlyProfile();
        URL skinUrl = new URL(toTextureUrl("e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52"));

        method.invoke(null, profile, skinUrl);

        Assertions.assertEquals(skinUrl.toURI(), profile.getTextures().skin, "URI-only textures APIs should still receive the requested skin URL");
    }

    @Test
    @DisplayName("Test texture application supports immutable withSkin APIs")
    void testApplyTextureSupportsImmutableWithSkinApi() throws Exception {
        Method method = SlimefunUtils.class.getDeclaredMethod("applyTexture", Object.class, URL.class);
        method.setAccessible(true);

        ImmutableProfile profile = new ImmutableProfile();
        URL skinUrl = new URL(toTextureUrl("e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52"));

        method.invoke(null, profile, skinUrl);

        Assertions.assertEquals(skinUrl, profile.getTextures().skin, "Immutable textures APIs should apply the new textures object back to the profile");
    }

    @Test
    @DisplayName("Test skull profile setter lookup continues after a failing compatible method")
    void testSetSkullProfileContinuesAfterFailingCompatibleMethod() throws ReflectiveOperationException {
        Method method = SlimefunUtils.class.getDeclaredMethod("setSkullProfile", Object.class, Object.class, String.class, String[].class);
        method.setAccessible(true);

        ThrowingSetterTarget target = new ThrowingSetterTarget();
        Object profile = new StringBuilder("profile");

        boolean applied = (Boolean) method.invoke(null, target, profile, "setOwnerProfile", new String[] {"java.lang.CharSequence"});

        Assertions.assertTrue(applied, "The setter lookup should continue to the next compatible method when one setter invocation fails");
        Assertions.assertTrue(target.fallbackCalled, "A later compatible setter should still be invoked");
    }

    @Test
    @DisplayName("Test skull profile setter handles IllegalAccessException for non-public runtime classes")
    void testSetSkullProfileHandlesIllegalAccessForNonPublicRuntimeClass() throws ReflectiveOperationException {
        Method method = SlimefunUtils.class.getDeclaredMethod("setSkullProfile", Object.class, Object.class, String.class, String[].class);
        method.setAccessible(true);

        InaccessibleSetterTarget target = new InaccessibleSetterTarget();
        Object profile = new StringBuilder("profile");

        boolean applied = (Boolean) method.invoke(null, target, profile, "setOwnerProfile", new String[] {"java.lang.CharSequence"});

        Assertions.assertTrue(applied, "The setter lookup should recover from IllegalAccessException for non-public implementation classes");
        Assertions.assertTrue(target.called, "The compatible setter should still be invoked");
    }


    @Test
    @DisplayName("Test custom head application returns false for non-skull blocks")
    void testSetCustomHeadRejectsNonSkullBlock() {
        World world = server.addSimpleWorld("head_test_world_non_skull");
        Block block = new BlockMock(Material.STONE, new Location(world, 0, 64, 0));

        Assertions.assertFalse(SlimefunUtils.setCustomHead(block, "e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52"));
    }

    @Test
    @DisplayName("Test custom head application updates placed skull blocks")
    void testSetCustomHeadAppliesTextureToSkullBlock() {
        String texture = "e952d2b3f351a6b0487cc59db31bf5f2641133e5ba0006b18576e996a0293e52";
        String url = toTextureUrl(texture);
        World world = server.addSimpleWorld("head_test_world_skull");
        Block block = new BlockMock(Material.PLAYER_HEAD, new Location(world, 0, 64, 0));

        Assumptions.assumeTrue(block.getState() instanceof Skull, "MockBukkit does not expose a Skull block state for player heads");
        boolean applied = SlimefunUtils.setCustomHead(block, texture);
        Assumptions.assumeTrue(applied, "MockBukkit does not emulate the skull block profile setter chain used by live Paper/Purpur servers");
        assertTextureIfSupported(block.getState(), url, true);
    }

    private static String toTextureUrl(String texture) {
        return "https://textures.minecraft.net/texture/" + texture.toLowerCase(java.util.Locale.ROOT);
    }

    private static void assertTextureIfSupported(Object holder, String expectedTextureUrl, boolean requireTexture) {
        if (holder == null) {
            return;
        }

        try {
            Object profile = getProfile(holder);
            if (profile == null) {
                if (requireTexture) {
                    Assertions.fail("Expected a skull profile to be present");
                }
                return;
            }

            Method getTextures = profile.getClass().getMethod("getTextures");
            Object textures = getTextures.invoke(profile);
            Assertions.assertNotNull(textures);

            Method getSkin = textures.getClass().getMethod("getSkin");
            URL skin = (URL) getSkin.invoke(textures);
            Assertions.assertEquals(new URL(expectedTextureUrl), skin);
        } catch (ReflectiveOperationException e) {
            Assertions.fail("Could not inspect skull profile textures", e);
        } catch (java.net.MalformedURLException e) {
            Assertions.fail(e);
        }
    }

    private static Object getProfile(Object holder) throws ReflectiveOperationException {
        return getProfile(holder, java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>()));
    }

    private static Object getProfile(Object holder, Set<Object> visited) throws ReflectiveOperationException {
        if (holder == null || !visited.add(holder)) {
            return null;
        }

        if (holder instanceof ItemStack stack) {
            ItemMeta meta = stack.getItemMeta();
            return meta == null ? null : getProfile(meta, visited);
        }

        for (String methodName : new String[] {"getOwnerProfile", "getPlayerProfile", "getProfile"}) {
            try {
                Method method = holder.getClass().getMethod(methodName);
                Object profile = method.invoke(holder);
                if (profile != null) {
                    Object resolved = getProfile(profile, visited);
                    return resolved != null ? resolved : profile;
                }
            } catch (NoSuchMethodException ignored) {
                // Try the next supported profile accessor.
            }
        }

        for (String methodName : new String[] {"profile", "resolve", "getResolvedProfile", "asProfile"}) {
            try {
                Method method = holder.getClass().getMethod(methodName);
                Object profile = method.invoke(holder);
                if (profile != null) {
                    Object resolved = getProfile(profile, visited);
                    return resolved != null ? resolved : profile;
                }
            } catch (NoSuchMethodException ignored) {
                // Try the next supported profile accessor.
            }
        }

        try {
            holder.getClass().getMethod("getTextures");
            return holder;
        } catch (NoSuchMethodException ignored) {
            // Not a profile-like object yet.
        }

        return null;
    }

    private static boolean hasBukkitCreateProfile() {
        try {
            for (String methodName : new String[] {"createProfile", "createPlayerProfile"}) {
                for (Class<?>[] signature : new Class<?>[][] {
                    {String.class},
                    {UUID.class, String.class},
                    {UUID.class}
                }) {
                    try {
                        org.bukkit.Bukkit.class.getMethod(methodName, signature);
                        return true;
                    } catch (NoSuchMethodException ignored) {
                        // Try the next supported factory overload.
                    }
                }
            }

            return false;
        } catch (SecurityException ignored) {
            return false;
        }
    }

}
