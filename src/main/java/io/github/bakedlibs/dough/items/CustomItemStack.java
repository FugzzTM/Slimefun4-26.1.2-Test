package io.github.bakedlibs.dough.items;

import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Backward-compatible implementation that supports legacy addon bytecode
 * expecting this class to be an ItemStack subtype while retaining the modern
 * static factory API.
 * <p>
 * On Purpur 26.1.2+ (and potentially future Paper builds),
 * {@code CraftItemStack.asNMSCopy()} casts ItemStack instances to
 * {@code CraftItemStack}, which fails for custom subclasses.
 * All static {@code create()} factory methods therefore return plain
 * {@link ItemStack} objects rather than {@code CustomItemStack} instances.
 */
@ParametersAreNonnullByDefault
public class CustomItemStack extends ItemStack {

    public CustomItemStack(ItemStack itemStack, @Nullable String name, String... lore) {
        super(itemStack);
        applyMeta(this, name, lore);
    }

    public CustomItemStack(ItemStack itemStack, Consumer<ItemMeta> consumer) {
        super(itemStack);

        ItemMeta meta = getItemMeta();
        if (meta != null) {
            consumer.accept(meta);
            setItemMeta(meta);
        }
    }

    public CustomItemStack(Material material, @Nullable String name, String... lore) {
        this(new ItemStack(material), name, lore);
    }

    public CustomItemStack(Material material, Consumer<ItemMeta> consumer) {
        this(new ItemStack(material), consumer);
    }

    public CustomItemStack(Material material, int amount, @Nullable String name, String... lore) {
        this(material, name, lore);
        setAmount(amount);
    }

    public CustomItemStack(ItemStack itemStack, int amount) {
        super(itemStack);
        setAmount(amount);
    }

    public CustomItemStack(String texture, @Nullable String name, String... lore) {
        this(SlimefunUtils.getCustomHead(texture), name, lore);
    }

    // ---- Static factory methods returning plain ItemStack ----
    // These build a plain ItemStack so CraftBukkit can handle them safely.

    public static ItemStack create(ItemStack itemStack, Consumer<ItemMeta> metaConsumer) {
        ItemStack safe = toPlainStack(itemStack);
        ItemMeta meta = safe.getItemMeta();
        if (meta != null) {
            metaConsumer.accept(meta);
            safe.setItemMeta(meta);
        }
        return safe;
    }

    public static ItemStack create(Material material, Consumer<ItemMeta> metaConsumer) {
        return create(new ItemStack(material), metaConsumer);
    }

    public static ItemStack create(ItemStack item, @Nullable String name, String... lore) {
        ItemStack safe = toPlainStack(item);
        applyMeta(safe, name, lore);
        return safe;
    }

    public static ItemStack create(Material material, @Nullable String name, String... lore) {
        return create(new ItemStack(material), name, lore);
    }

    public static ItemStack create(Material type, @Nullable String name, List<String> lore) {
        return create(new ItemStack(type), name, lore.toArray(String[]::new));
    }

    public static ItemStack create(ItemStack item, List<String> list) {
        return create(new ItemStack(item), list.get(0), list.subList(1, list.size()).toArray(String[]::new));
    }

    public static ItemStack create(Material type, List<String> list) {
        return create(new ItemStack(type), list);
    }

    public static ItemStack create(ItemStack item, int amount) {
        ItemStack safe = toPlainStack(item);
        safe.setAmount(amount);
        return safe;
    }

    @Deprecated(forRemoval = true)
    public static ItemStack create(ItemStack itemStack, Material type) {
        return new ItemStackEditor(itemStack).andStackConsumer(item -> item.setType(type)).create();
    }

    /**
     * Translates {@code &} color codes and applies display name + lore to the given stack.
     */
    private static void applyMeta(ItemStack itemStack, @Nullable String name, String... lore) {
        itemStack.editMeta(meta -> {
            if (name != null) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            } else {
                meta.setDisplayName(null);
            }

            if (lore.length > 0) {
                List<String> translatedLore = new ArrayList<>(lore.length);
                for (String line : lore) {
                    translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(translatedLore);
            }
        });
    }

    /**
     * Converts any {@link ItemStack} subclass (e.g. SlimefunItemStack, CustomItemStack)
     * into a plain {@link ItemStack} that CraftBukkit can safely process.
     */
    private static ItemStack toPlainStack(ItemStack source) {
        if (source == null || source.getType() == Material.AIR) {
            return source;
        }

        // If it's already an exact ItemStack (not a subclass), return a copy
        if (source.getClass() == ItemStack.class) {
            return new ItemStack(source);
        }

        // Rebuild from scratch to shed any subclass
        ItemStack plain = new ItemStack(source.getType(), source.getAmount());
        if (source.hasItemMeta()) {
            plain.setItemMeta(source.getItemMeta());
        }
        return plain;
    }
}
