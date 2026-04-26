package io.github.thebusybiscuit.slimefun4.implementation.items;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.test.TestUtilities;
import io.github.thebusybiscuit.slimefun4.test.mocks.MockDamageable;
import io.github.thebusybiscuit.slimefun4.utils.compatibility.VersionedEnchantment;
import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

class TestDamageableItem {

    private static Slimefun plugin;
    private static ServerMock server;

    @BeforeAll
    public static void load() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Slimefun.class);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    public static MockDamageable getDummyItem(String id, boolean damageable, @Nullable Enchantment enchantment, @Nullable Integer enchantmentLevel) {
        ItemGroup itemGroup = TestUtilities.getItemGroup(plugin, "damageable_item_test");
        SlimefunItemStack stack = new SlimefunItemStack("DAMAGEABLE_PICKAXE_" + id, Material.DIAMOND_PICKAXE, "&4This pickaxe can break", "&6It appears, it breaks, but most importantly, it tests.");
        if (enchantment != null && enchantmentLevel != null) {
            ItemMeta im = stack.getItemMeta();
            im.addEnchant(enchantment, enchantmentLevel, true);
            stack.setItemMeta(im);
        }
        MockDamageable item = new MockDamageable(itemGroup, stack, RecipeType.NULL, new ItemStack[9], damageable);
        item.register(plugin);
        return item;
    }

    @Test
    @DisplayName("Test DamageableItem actually damages")
    void testDamageableItemDamagesItem() {
        MockDamageable unDamageableItem = getDummyItem("UDM", false, null, null);
        // Check if damageable is successfully registered
        Assertions.assertFalse(unDamageableItem.isDamageable());

        Player player = server.addPlayer();
        ItemStack unDamageableDummy = unDamageableItem.getItem().clone();
        unDamageableItem.damageItem(player, unDamageableDummy);
        Assertions.assertTrue(unDamageableDummy.hasItemMeta());
        // Check if the item is not damaged as we intended it to not be
        Assertions.assertEquals(((Damageable) unDamageableDummy.getItemMeta()).getDamage(), 0);

        MockDamageable damageableItem = getDummyItem("DM", true, null, null);
        // Check if damageable is successfully registered
        Assertions.assertTrue(damageableItem.isDamageable());

        ItemStack damageableDummy = damageableItem.getItem().clone();
        damageableItem.damageItem(player, damageableDummy);
        Assertions.assertTrue(damageableDummy.hasItemMeta());
        // Check if the item is damaged as we intended it to not be
        Assertions.assertEquals(((Damageable) damageableDummy.getItemMeta()).getDamage(), 1);
    }

    @Test
    @DisplayName("Test DamageableItem breaks items at max durability")
    void testDamageableItemBreaksAtMaxDurability() {
        MockDamageable damageableItem = getDummyItem("BREAK", true, null, null);

        ItemStack breakingDummy = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = breakingDummy.getItemMeta();
        Assertions.assertNotNull(meta);

        int maxDurability = 2;
        Damageable damageableMeta = (Damageable) meta;
        damageableMeta.setMaxDamage(maxDurability);
        damageableMeta.setDamage(0);
        breakingDummy.setItemMeta(meta);

        Player player = server.addPlayer();
        damageableItem.damageItem(player, breakingDummy);

        Assertions.assertEquals(1, breakingDummy.getAmount());
        Assertions.assertEquals(1, ((Damageable) breakingDummy.getItemMeta()).getDamage());

        ItemMeta breakingMeta = breakingDummy.getItemMeta();
        Assertions.assertNotNull(breakingMeta);
        ((Damageable) breakingMeta).setDamage(maxDurability - 1);
        breakingDummy.setItemMeta(breakingMeta);
        damageableItem.damageItem(player, breakingDummy);

        Assertions.assertEquals(0, breakingDummy.getAmount());
    }

    @Test
    @DisplayName("Test if DamageableItem cares about unbreaking levels")
    void testDamageableItemCaresUnbreaking() {
        MockDamageable noUnbreakingItem = getDummyItem("NU", true, null, null);
        MockDamageable iiiUnbreakingItem = getDummyItem("IIIU", true, VersionedEnchantment.UNBREAKING, 3);
        MockDamageable xUnbreakingItem = getDummyItem("XU", true, VersionedEnchantment.UNBREAKING, 10);

        int noUnbreakingSaved = 0;
        int iiiUnbreakingSaved = 0;
        int xUnbreakingSaved = 0;

        for (int i = 0; i < 500; ++i) {
            if (noUnbreakingItem.evaluateUnbreakingEnchantment(0)) {
                noUnbreakingSaved++;
            }

            if (iiiUnbreakingItem.evaluateUnbreakingEnchantment(3)) {
                iiiUnbreakingSaved++;
            }

            if (xUnbreakingItem.evaluateUnbreakingEnchantment(10)) {
                xUnbreakingSaved++;
            }
        }

        Assertions.assertEquals(0, noUnbreakingSaved);
        Assertions.assertTrue(xUnbreakingSaved > iiiUnbreakingSaved);
        Assertions.assertTrue(iiiUnbreakingSaved > noUnbreakingSaved);
    }

}
