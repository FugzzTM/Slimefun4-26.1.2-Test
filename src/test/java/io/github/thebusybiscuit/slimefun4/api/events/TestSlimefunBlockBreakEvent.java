package io.github.thebusybiscuit.slimefun4.api.events;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.listeners.BlockListener;
import io.github.thebusybiscuit.slimefun4.test.TestUtilities;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

class TestSlimefunBlockBreakEvent {

    private static ServerMock server;
    private static Slimefun plugin;
    private static SlimefunItem slimefunItem;

    @BeforeAll
    public static void load() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(Slimefun.class);

        new BlockListener(plugin);

        slimefunItem = TestUtilities.mockSlimefunItem(plugin, "FOOD_COMPOSTER", new ItemStack(Material.GREEN_TERRACOTTA));
        slimefunItem.register(plugin);
    }

    @AfterAll
    public static void unload() {
        MockBukkit.unmock();
    }

    @BeforeEach
    public void beforeEach() {
        server.getPluginManager().clearEvents();
    }

    @Test
    @DisplayName("Test that SlimefunBlockBreakEvent is fired when a SlimefunItem is broken")
    void testEventIsFired() {
        Player player = mockPlayer("SomePlayer", new ItemStack(Material.IRON_PICKAXE));

        World world = mockWorld();
        Block block = mockBlock(world, Material.GREEN_TERRACOTTA);

        Slimefun.getRegistry().getWorlds().put("my_world", new BlockStorage(world));
        BlockStorage.addBlockInfo(block, "id", "FOOD_COMPOSTER");

        server.getPluginManager().callEvent(new BlockBreakEvent(block, player));
        server.getPluginManager().assertEventFired(SlimefunBlockBreakEvent.class, e -> true);
    }

    @Test
    @DisplayName("Test the getters are set to the right values")
    void testGetters() {
        Player player = mockPlayer("SomePlayer", new ItemStack(Material.IRON_PICKAXE));
        ItemStack itemStack = new ItemStack(Material.IRON_PICKAXE);

        World world = mockWorld();
        Block block = mockBlock(world, Material.GREEN_TERRACOTTA);

        Slimefun.getRegistry().getWorlds().put("my_world", new BlockStorage(world));
        BlockStorage.addBlockInfo(block, "id", "FOOD_COMPOSTER");

        server.getPluginManager().callEvent(new BlockBreakEvent(block, player));
        server.getPluginManager().assertEventFired(SlimefunBlockBreakEvent.class, e -> {
            Assertions.assertEquals(block, e.getBlockBroken());
            Assertions.assertEquals(slimefunItem, e.getSlimefunItem());
            Assertions.assertEquals(itemStack, e.getHeldItem());
            Assertions.assertEquals(player, e.getPlayer());
            Assertions.assertFalse(e.isCancelled());
            return true;
        });
    }

    @Test
    @DisplayName("Test that the SlimefunBlockBreakEvent & BlockBreakEvent events are cancelled correctly")
    void testIsCancelled() {
        server.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onBlockBreak(SlimefunBlockBreakEvent event) {
                event.setCancelled(true);
            }
        }, plugin);

        Player player = mockPlayer("SomePlayer", new ItemStack(Material.IRON_PICKAXE));
        ItemStack itemStack = new ItemStack(Material.IRON_PICKAXE);

        World world = mockWorld();
        Block block = mockBlock(world, Material.GREEN_TERRACOTTA);

        Slimefun.getRegistry().getWorlds().put("my_world", new BlockStorage(world));
        BlockStorage.addBlockInfo(block, "id", "FOOD_COMPOSTER");

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        server.getPluginManager().callEvent(blockBreakEvent);
        server.getPluginManager().assertEventFired(SlimefunBlockBreakEvent.class, e -> {
            Assertions.assertTrue(e.isCancelled());
            Assertions.assertTrue(blockBreakEvent.isCancelled());
            return true;
        });
    }

    private static Player mockPlayer(String name, ItemStack mainHand) {
        Player player = Mockito.mock(Player.class);
        PlayerInventory inventory = Mockito.mock(PlayerInventory.class);
        Mockito.when(player.getName()).thenReturn(name);
        Mockito.when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        Mockito.when(player.getInventory()).thenReturn(inventory);
        Mockito.when(inventory.getItemInMainHand()).thenReturn(mainHand);
        return player;
    }

    private static World mockWorld() {
        World world = Mockito.mock(World.class);
        Mockito.when(world.getName()).thenReturn("my_world");
        return world;
    }

    private static Block mockBlock(World world, Material type) {
        Block block = Mockito.mock(Block.class);
        BlockState state = Mockito.mock(BlockState.class);
        Block blockAbove = Mockito.mock(Block.class);
        Location location = new Location(world, TestUtilities.randomInt(), 100, TestUtilities.randomInt());
        Location locationAbove = new Location(world, location.getBlockX(), location.getBlockY() + 1, location.getBlockZ());

        Mockito.when(block.getType()).thenReturn(type);
        Mockito.when(block.getLocation()).thenReturn(location);
        Mockito.when(block.getState()).thenReturn(state);
        Mockito.when(block.getWorld()).thenReturn(world);
        Mockito.when(block.getX()).thenReturn(location.getBlockX());
        Mockito.when(block.getY()).thenReturn(location.getBlockY());
        Mockito.when(block.getZ()).thenReturn(location.getBlockZ());
        Mockito.when(block.getRelative(org.bukkit.block.BlockFace.UP)).thenReturn(blockAbove);

        Mockito.when(blockAbove.getType()).thenReturn(Material.AIR);
        Mockito.when(blockAbove.getLocation()).thenReturn(locationAbove);
        Mockito.when(blockAbove.getWorld()).thenReturn(world);
        return block;
    }

    @Test
    @DisplayName("Test that breaking a Slimefun block gets queued for deletion")
    void testBlockBreaksGetQueuedForDeletion() {
        Player player = mockPlayer("SomePlayer", new ItemStack(Material.IRON_PICKAXE));
        ItemStack itemStack = new ItemStack(Material.IRON_PICKAXE);

        World world = mockWorld();
        Block block = mockBlock(world, Material.GREEN_TERRACOTTA);

        Slimefun.getRegistry().getWorlds().put("my_world", new BlockStorage(world));
        BlockStorage.addBlockInfo(block, "id", "FOOD_COMPOSTER");

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(block, player);
        server.getPluginManager().callEvent(blockBreakEvent);
        server.getPluginManager().assertEventFired(SlimefunBlockBreakEvent.class, e -> true);

        Assertions.assertTrue(Slimefun.getTickerTask().isDeletedSoon(block.getLocation()));
    }
}
