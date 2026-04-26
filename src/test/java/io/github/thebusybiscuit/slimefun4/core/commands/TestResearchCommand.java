package io.github.thebusybiscuit.slimefun4.core.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.bakedlibs.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.test.TestUtilities;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;

class TestResearchCommand {

    private static ServerMock server;
    private static Research research;
    private static Research research2;

    @BeforeAll
    public static void load() {
        server = MockBukkit.mock();

        Slimefun plugin = MockBukkit.load(Slimefun.class);
        research = new Research(new NamespacedKey(plugin, "command_test"), 999, "Test", 10);
        SlimefunItem item = TestUtilities.mockSlimefunItem(plugin, "COMMAND_RESEARCH_TEST", CustomItemStack.create(Material.TORCH, "&bCommand Research Test"));
        research.addItems(item);
        research.register();

        research2 = new Research(new NamespacedKey(plugin, "command_test_two"), 1000, "Test Two", 10);
        SlimefunItem item2 = TestUtilities.mockSlimefunItem(plugin, "COMMAND_RESEARCH_TEST_TWO", CustomItemStack.create(Material.TORCH, "&bCommand Research Test Two"));
        research2.addItems(item2);
        research2.register();
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test /sf research all")
    void testResearchAll() throws InterruptedException {
        Slimefun.getRegistry().setResearchingEnabled(true);
        Player player = server.addPlayer();
        PlayerProfile profile = TestUtilities.awaitProfile(player);

        server.executeConsole("slimefun", "research", player.getName(), "all").assertSucceeded();

        Assertions.assertTrue(profile.hasUnlocked(research));
        Assertions.assertTrue(profile.hasUnlocked(research2));
    }

    @Test
    @DisplayName("Test /sf research <research id>")
    void testResearchSpecific() throws InterruptedException {
        Slimefun.getRegistry().setResearchingEnabled(true);
        Player player = server.addPlayer();
        PlayerProfile profile = TestUtilities.awaitProfile(player);

        server.executeConsole("slimefun", "research", player.getName(), research.getKey().toString()).assertSucceeded();

        Assertions.assertTrue(profile.hasUnlocked(research));
        Assertions.assertFalse(profile.hasUnlocked(research2));
    }

    @Test
    @DisplayName("Test /sf research reset")
    void testResearchReset() throws InterruptedException {
        Slimefun.getRegistry().setResearchingEnabled(true);
        Player player = server.addPlayer();
        PlayerProfile profile = TestUtilities.awaitProfile(player);

        server.executeConsole("slimefun", "research", player.getName(), "all").assertSucceeded();

        Assertions.assertTrue(profile.hasUnlocked(research));
        Assertions.assertTrue(profile.hasUnlocked(research2));

        server.executeConsole("slimefun", "research", player.getName(), "reset").assertSucceeded();

        Assertions.assertFalse(profile.hasUnlocked(research));
        Assertions.assertFalse(profile.hasUnlocked(research2));
    }
}
