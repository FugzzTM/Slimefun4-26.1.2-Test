package be.seeseemelk.mockbukkit.registry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import io.papermc.paper.persistence.PersistentDataContainerView;

import net.kyori.adventure.text.Component;

public final class PaperItemStackFactory {

    private static final Constructor<ItemStack> BASE_CONSTRUCTOR;
    private static final Field CRAFT_DELEGATE;

    static {
        try {
            BASE_CONSTRUCTOR = ItemStack.class.getDeclaredConstructor();
            BASE_CONSTRUCTOR.setAccessible(true);

            CRAFT_DELEGATE = ItemStack.class.getDeclaredField("craftDelegate");
            CRAFT_DELEGATE.setAccessible(true);
        } catch (ReflectiveOperationException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private PaperItemStackFactory() {
    }

    public static ItemStack create(Material type, int amount) {
        return wrap(new DelegateItemStack(type, amount, false));
    }

    public static ItemStack createEmpty() {
        return wrap(new DelegateItemStack(Material.AIR, 0, true));
    }

    private static ItemStack wrap(DelegateItemStack delegate) {
        try {
            ItemStack stack = BASE_CONSTRUCTOR.newInstance();
            CRAFT_DELEGATE.set(stack, delegate);
            return stack;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to create Paper ItemStack test double", ex);
        }
    }

    private static final class DelegateItemStack extends ItemStack {

        private Material type;
        private int amount;
        private short durability;
        private MaterialData data;
        private ItemMeta meta;
        private final Map<Enchantment, Integer> enchantments = new HashMap<>();
        private final Set<ItemFlag> itemFlags = new HashSet<>();
        private final boolean empty;

        private DelegateItemStack(Material type, int amount, boolean empty) {
            super();
            this.empty = empty;
            this.type = type == null ? Material.AIR : type;
            this.amount = Math.max(0, amount);
        }

        private boolean isEffectivelyEmpty() {
            return empty || type.isAir() || amount <= 0;
        }

        private ItemMeta ensureMeta() {
            if (meta == null) {
                try {
                    meta = Bukkit.getItemFactory().getItemMeta(type);
                } catch (Exception ignored) {
                    meta = null;
                }
            }

            return meta;
        }

        @Override
        public Material getType() {
            return type;
        }

        @Override
        public void setType(Material type) {
            this.type = type == null ? Material.AIR : type;
        }

        @Override
        public ItemStack withType(Material type) {
            DelegateItemStack copy = copyDelegate();
            copy.setType(type);
            return wrap(copy);
        }

        @Override
        public int getAmount() {
            return amount;
        }

        @Override
        public void setAmount(int amount) {
            this.amount = Math.max(0, amount);
        }

        @Override
        public int getMaxStackSize() {
            return type == null ? 64 : Math.max(1, type.getMaxStackSize());
        }

        @Override
        public boolean isEmpty() {
            return isEffectivelyEmpty();
        }

        @Override
        public ItemStack clone() {
            return wrap(copyDelegate());
        }

        @Override
        public boolean hasItemMeta() {
            return meta != null;
        }

        @Override
        public ItemMeta getItemMeta() {
            ItemMeta current = ensureMeta();
            return current == null ? null : current.clone();
        }

        @Override
        public boolean setItemMeta(ItemMeta itemMeta) {
            meta = itemMeta == null ? null : itemMeta.clone();
            return true;
        }

        @Override
        public boolean editMeta(Consumer<? super ItemMeta> consumer) {
            if (consumer == null) {
                return false;
            }

            ItemMeta current = ensureMeta();
            if (current == null) {
                return false;
            }

            consumer.accept(current);
            meta = current;
            return true;
        }

        @Override
        public boolean isSimilar(ItemStack stack) {
            if (stack == null) {
                return false;
            }

            return type == stack.getType() && amount == stack.getAmount();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ItemStack other)) {
                return false;
            }
            return type == other.getType() && amount == other.getAmount();
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(type, amount, durability);
        }

        @Override
        public String toString() {
            return "ItemStack{" + type + " x" + amount + '}';
        }

        @Override
        public boolean containsEnchantment(Enchantment enchantment) {
            return enchantment != null && enchantments.containsKey(enchantment);
        }

        @Override
        public int getEnchantmentLevel(Enchantment enchantment) {
            return enchantments.getOrDefault(enchantment, 0);
        }

        @Override
        public Map<Enchantment, Integer> getEnchantments() {
            return new HashMap<>(enchantments);
        }

        @Override
        public void addEnchantments(Map<Enchantment, Integer> enchantments) {
            if (enchantments != null) {
                enchantments.forEach(this::addEnchantment);
            }
        }

        @Override
        public void addEnchantment(Enchantment enchantment, int level) {
            if (enchantment != null && level > 0) {
                enchantments.put(enchantment, level);
            }
        }

        @Override
        public void addUnsafeEnchantments(Map<Enchantment, Integer> enchantments) {
            addEnchantments(enchantments);
        }

        @Override
        public void addUnsafeEnchantment(Enchantment enchantment, int level) {
            addEnchantment(enchantment, level);
        }

        @Override
        public int removeEnchantment(Enchantment enchantment) {
            Integer removed = enchantments.remove(enchantment);
            return removed == null ? 0 : removed;
        }

        @Override
        public void removeEnchantments() {
            enchantments.clear();
        }

        @Override
        public void setDurability(short durability) {
            this.durability = durability;
        }

        @Override
        public short getDurability() {
            return durability;
        }

        @Override
        public MaterialData getData() {
            if (data == null) {
                data = new MaterialData(type, (byte) durability);
            }

            return data;
        }

        @Override
        public void setData(MaterialData data) {
            this.data = data == null ? null : data.clone();
            if (data != null) {
                this.type = data.getItemType();
                this.durability = data.getData();
            }
        }

        @Override
        public String getTranslationKey() {
            return "item.minecraft." + type.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public Component displayName() {
            return Component.text(type.name().toLowerCase(Locale.ROOT));
        }

        @Override
        public Component effectiveName() {
            return displayName();
        }

        @Override
        public ItemStack ensureServerConversions() {
            return this;
        }

        @Override
        public byte[] serializeAsBytes() {
            return new byte[0];
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> serialized = new HashMap<>();
            serialized.put("type", type.name());
            serialized.put("amount", amount);
            if (durability != 0) {
                serialized.put("damage", durability);
            }
            if (!enchantments.isEmpty()) {
                Map<String, Integer> serializedEnchantments = new HashMap<>();
                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    NamespacedKey key = entry.getKey().getKey();
                    serializedEnchantments.put(key == null ? entry.getKey().getName() : key.toString(), entry.getValue());
                }
                serialized.put("enchantments", serializedEnchantments);
            }
            return serialized;
        }

        @Override
        public String getI18NDisplayName() {
            return type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        }

        @Override
        public ItemStack asOne() {
            setAmount(1);
            return this;
        }

        @Override
        public ItemStack asQuantity(int qty) {
            setAmount(qty);
            return this;
        }

        @Override
        public ItemStack add() {
            return add(1);
        }

        @Override
        public ItemStack add(int qty) {
            setAmount(amount + qty);
            return this;
        }

        @Override
        public ItemStack subtract() {
            return subtract(1);
        }

        @Override
        public ItemStack subtract(int qty) {
            setAmount(amount - qty);
            return this;
        }

        @Override
        public PersistentDataContainerView getPersistentDataContainer() {
            ItemMeta current = ensureMeta();
            return current == null ? null : current.getPersistentDataContainer();
        }

        @Override
        public boolean editPersistentDataContainer(Consumer<org.bukkit.persistence.PersistentDataContainer> consumer) {
            ItemMeta current = ensureMeta();
            if (current == null || consumer == null) {
                return false;
            }

            consumer.accept(current.getPersistentDataContainer());
            meta = current;
            return true;
        }

        @Override
        public <T> T getData(io.papermc.paper.datacomponent.DataComponentType.Valued<T> type) {
            return null;
        }

        @Override
        public <T> T getDataOrDefault(io.papermc.paper.datacomponent.DataComponentType.Valued<? extends T> type, T defaultValue) {
            return defaultValue;
        }

        @Override
        public boolean hasData(io.papermc.paper.datacomponent.DataComponentType type) {
            return false;
        }

        @Override
        public Set<io.papermc.paper.datacomponent.DataComponentType> getDataTypes() {
            return Set.of();
        }

        @Override
        public <T> void setData(io.papermc.paper.datacomponent.DataComponentType.Valued<T> type, io.papermc.paper.datacomponent.DataComponentBuilder<T> builder) {
        }

        @Override
        public <T> void setData(io.papermc.paper.datacomponent.DataComponentType.Valued<T> type, T value) {
        }

        @Override
        public void setData(io.papermc.paper.datacomponent.DataComponentType.NonValued type) {
        }

        @Override
        public void unsetData(io.papermc.paper.datacomponent.DataComponentType type) {
        }

        @Override
        public void resetData(io.papermc.paper.datacomponent.DataComponentType type) {
        }

        @Override
        public void copyDataFrom(ItemStack other, java.util.function.Predicate<io.papermc.paper.datacomponent.DataComponentType> predicate) {
        }

        @Override
        public boolean isDataOverridden(io.papermc.paper.datacomponent.DataComponentType type) {
            return false;
        }

        @Override
        public boolean matchesWithoutData(ItemStack other, Set<io.papermc.paper.datacomponent.DataComponentType> ignored) {
            return isSimilar(other);
        }

        @Override
        public boolean matchesWithoutData(ItemStack other, Set<io.papermc.paper.datacomponent.DataComponentType> ignored, boolean ignoreAmount) {
            if (other == null) {
                return false;
            }
            return ignoreAmount ? type == other.getType() : isSimilar(other);
        }

        @Override
        public boolean isRepairableBy(ItemStack repairItem) {
            return true;
        }

        @Override
        public boolean canRepair(ItemStack repairItem) {
            return true;
        }

        @Override
        public int getMaxItemUseDuration() {
            return 0;
        }

        @Override
        public int getMaxItemUseDuration(org.bukkit.entity.LivingEntity entity) {
            return 0;
        }

        @Override
        public java.util.List<Component> lore() {
            return java.util.List.of();
        }

        @Override
        public java.util.List<String> getLore() {
            return java.util.List.of();
        }

        @Override
        public void setLore(java.util.List<String> lore) {
        }

        @Override
        public void lore(java.util.List<? extends Component> lore) {
        }

        @Override
        public void addItemFlags(ItemFlag... flags) {
            if (flags != null) {
                java.util.Collections.addAll(itemFlags, flags);
            }
        }

        @Override
        public void removeItemFlags(ItemFlag... flags) {
            if (flags != null) {
                for (ItemFlag flag : flags) {
                    itemFlags.remove(flag);
                }
            }
        }

        @Override
        public Set<ItemFlag> getItemFlags() {
            return Set.copyOf(itemFlags);
        }

        @Override
        public boolean hasItemFlag(ItemFlag flag) {
            return itemFlags.contains(flag);
        }

        @Override
        public io.papermc.paper.inventory.ItemRarity getRarity() {
            return null;
        }

        private DelegateItemStack copyDelegate() {
            DelegateItemStack copy = new DelegateItemStack(type, amount, empty);
            copy.durability = durability;
            copy.data = data == null ? null : data.clone();
            copy.meta = meta == null ? null : meta.clone();
            copy.enchantments.putAll(enchantments);
            copy.itemFlags.addAll(itemFlags);
            return copy;
        }
    }
}


