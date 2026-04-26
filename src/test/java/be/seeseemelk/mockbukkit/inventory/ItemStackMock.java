package be.seeseemelk.mockbukkit.inventory;

import java.util.Map;

import javax.annotation.Nonnull;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Paper 26.1.2 compatible replacement for MockBukkit's ItemStackMock.
 * <p>
 * MockBukkit's bundled implementation still tries to instantiate the now-interface
 * {@code ItemMeta} directly, which breaks on modern Paper. This shadow class keeps
 * the same public surface used by the tests, but delegates to Paper's own ItemStack
 * implementation so the real server-side item delegate is initialized correctly.
 */
public class ItemStackMock extends ItemStack {

    protected ItemStackMock() {
        super(Material.AIR, 0);
    }

    public ItemStackMock(@Nonnull Material type) {
        super(type);
    }

    public ItemStackMock(@Nonnull Material type, int amount) {
        super(type, amount);
    }

    public ItemStackMock(@Nonnull ItemStack stack) throws IllegalArgumentException {
        super(stack);
    }

    private ItemStackMock(Void ignored) {
        super(Material.AIR, 0);
    }

    public static ItemStackMock empty() {
        return new ItemStackMock((Void) null);
    }

    public static ItemStackMock deserialize(Map<String, Object> data) {
        ItemStack stack = ItemStack.deserialize(data);
        return stack == null ? empty() : new ItemStackMock(stack);
    }

    @Override
    public ItemStack clone() {
        return new ItemStackMock(this);
    }
}

