package be.seeseemelk.mockbukkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.papermc.paper.registry.RegistryKey;

class MockUnsafeValuesTest {

	private final MockUnsafeValues unsafeValues = new MockUnsafeValues();

	@BeforeEach
	void setUp() {
		MockBukkit.mock();
	}

	@AfterEach
	void tearDown() {
		MockBukkit.unmock();
	}

	@Test
	void createEmptyStackProducesEmptyStackWithoutRecursion() {
		ItemStack stack = unsafeValues.createEmptyStack();

		assertNotNull(stack);
		assertTrue(stack.isEmpty());
		assertEquals(Material.AIR, stack.getType());
		assertEquals(0, stack.getAmount());
		assertFalse(stack.toString().isBlank());
	}

	@Test
	void paperRegistryLookupResolvesSyntheticSound() {
		NamespacedKey key = NamespacedKey.minecraft("slimefun_test_sound");

		Object sound = unsafeValues.get(RegistryKey.SOUND_EVENT, key);

		assertNotNull(sound);
		assertTrue(sound instanceof org.bukkit.Sound);
		assertEquals(key, ((org.bukkit.Sound) sound).getKey());
	}
}

