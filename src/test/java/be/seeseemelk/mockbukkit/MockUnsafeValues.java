package be.seeseemelk.mockbukkit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import be.seeseemelk.mockbukkit.registry.RegistryAccessMock;
import be.seeseemelk.mockbukkit.registry.PaperItemStackFactory;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.RegionAccessor;
import org.bukkit.Registry;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Biome;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;

import com.destroystokyo.paper.util.VersionFetcher;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import io.papermc.paper.inventory.tooltip.TooltipContext;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class MockUnsafeValues implements UnsafeValues {

	private static final RegistryAccessMock REGISTRY_ACCESS = new RegistryAccessMock();

	@Override
	public ComponentFlattener componentFlattener() {
		return null;
	}

	@Override
	public PlainComponentSerializer plainComponentSerializer() {
		return null;
	}

	@Override
	public PlainTextComponentSerializer plainTextSerializer() {
		return null;
	}

	@Override
	public GsonComponentSerializer gsonComponentSerializer() {
		return null;
	}

	@Override
	public GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
		return null;
	}

	@Override
	public LegacyComponentSerializer legacyComponentSerializer() {
		return null;
	}

	@Override
	public Component resolveWithContext(Component component, CommandSender sender, Entity context, boolean hasPermissions) throws IOException {
		return component;
	}

	public void reportTimings() {
	}

	@Override
	public Material toLegacy(Material material) {
		return material;
	}

	@Override
	public Material fromLegacy(Material material) {
		return material;
	}

	@Override
	public Material fromLegacy(org.bukkit.material.MaterialData materialData) {
		return materialData.getItemType();
	}

	@Override
	public Material fromLegacy(org.bukkit.material.MaterialData materialData, boolean itemPriority) {
		return fromLegacy(materialData);
	}

	@Override
	public BlockData fromLegacy(Material material, byte data) {
		return null;
	}

	@Override
	public int getDataVersion() {
		return 0;
	}

	@Override
	public ItemStack modifyItemStack(ItemStack stack, String arguments) {
		return stack;
	}

	public void setMinimumApiVersion(String version) {
	}

	@Override
	public void checkSupported(PluginDescriptionFile description) throws InvalidPluginException {
	}

	@Override
	public byte[] processClass(PluginDescriptionFile description, String path, byte[] clazz) {
		return clazz;
	}

	@Override
	public Advancement loadAdvancement(NamespacedKey key, String advancement) {
		return null;
	}

	@Override
	public boolean removeAdvancement(NamespacedKey key) {
		return false;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(Material material, EquipmentSlot slot) {
		return ImmutableMultimap.of();
	}

	@Override
	public CreativeCategory getCreativeCategory(Material material) {
		return null;
	}

	public String getTimingsServerName() {
		return "MockBukkit";
	}

	@Override
	public VersionFetcher getVersionFetcher() {
		return null;
	}

	@Override
	public boolean isSupportedApiVersion(String apiVersion) {
		return true;
	}

	@Override
	public byte[] serializeItem(ItemStack stack) {
		return new byte[0];
	}

	@Override
	public ItemStack deserializeItem(byte[] data) {
		return null;
	}

	@Override
	public com.google.gson.JsonObject serializeItemAsJson(ItemStack stack) {
		return null;
	}

	@Override
	public ItemStack deserializeItemFromJson(com.google.gson.JsonObject data) throws IllegalArgumentException {
		return null;
	}

	public byte[] serializeEntity(Entity entity) {
		return new byte[0];
	}

	@Override
	public byte[] serializeEntity(Entity entity, io.papermc.paper.entity.EntitySerializationFlag... flags) {
		return new byte[0];
	}

	@Override
	public Entity deserializeEntity(byte[] data, org.bukkit.World world) {
		return null;
	}

	@Override
	public Entity deserializeEntity(byte[] data, org.bukkit.World world, boolean preserveUUID) {
		return null;
	}

	@Override
	public Entity deserializeEntity(byte[] data, org.bukkit.World world, boolean preserveUUID, boolean preservePassenger) {
		return null;
	}

	@Override
	public String getBlockTranslationKey(Material material) {
		return null;
	}

	@Override
	public String getItemTranslationKey(Material material) {
		return null;
	}

	@Override
	public String getTranslationKey(EntityType entityType) {
		return null;
	}

	@Override
	public String getTranslationKey(ItemStack itemStack) {
		return null;
	}

	@Override
	public String getTranslationKey(Attribute attribute) {
		return null;
	}

	@Override
	public PotionType.InternalPotionData getInternalPotionData(NamespacedKey namespacedKey) {
		return null;
	}

	public DamageEffect getDamageEffect(String key) {
		return null;
	}

	@Override
	public DamageSource.Builder createDamageSourceBuilder(DamageType damageType) {
		return DamageSource.builder(damageType);
	}

	@Override
	public String get(Class<?> aClass, String name) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <B extends org.bukkit.Keyed> B get(RegistryKey<B> registry, NamespacedKey key) {
		if (registry != null && "minecraft:sound_event".equals(registry.key().asString())) {
			return (B) createSyntheticSound(key);
		}

		return (B) REGISTRY_ACCESS.getRegistry(registry).get(key);
	}

	private static org.bukkit.Sound createSyntheticSound(NamespacedKey key) {
		return new SyntheticSound(key);
	}

	private static final class SyntheticSound implements org.bukkit.Sound {

		private final NamespacedKey key;

		private SyntheticSound(NamespacedKey key) {
			this.key = key;
		}

		@Override
		public NamespacedKey getKey() {
			return key;
		}

		@Override
		public net.kyori.adventure.key.Key key() {
			return net.kyori.adventure.key.Key.key(key.getNamespace(), key.getKey());
		}

		@Override
		public String name() {
			return key.getKey().toUpperCase(java.util.Locale.ROOT);
		}

		@Override
		public int ordinal() {
			return 0;
		}

		@Override
		public int compareTo(org.bukkit.Sound other) {
			return other == null ? 1 : key.toString().compareTo(other.getKey().toString());
		}

		public String translationKey() {
			return getTranslationKey();
		}

		public String getTranslationKey() {
			return "sound.minecraft." + key.getKey();
		}

		@Override
		public String toString() {
			return "Sound{" + key + '}';
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof org.bukkit.Keyed keyed)) {
				return false;
			}
			return key.equals(keyed.getKey());
		}
	}

	@Override
	public Map<String, Object> serializeStack(ItemStack stack) {
		return stack == null ? Map.of() : stack.serialize();
	}

	@Override
	public ItemStack deserializeStack(Map<String, Object> data) {
		return data == null ? createEmptyStack() : ItemStack.deserialize(data);
	}

	@Override
	public ItemStack deserializeItemHover(net.kyori.adventure.text.event.HoverEvent.ShowItem item) {
		return createEmptyStack();
	}

	@Override
	public int nextEntityId() {
		return 1;
	}

	@Override
	public String getMainLevelName() {
		return "world";
	}

	@Override
	public boolean isValidRepairItemStack(ItemStack itemStack, ItemStack itemStack1) {
		return true;
	}

	@Override
	public int getProtocolVersion() {
		return 0;
	}

	@Override
	public boolean hasDefaultEntityAttributes(NamespacedKey namespacedKey) {
		return false;
	}

	@Override
	public Attributable getDefaultEntityAttributes(NamespacedKey namespacedKey) {
		return null;
	}

	public Biome getCustomBiome() {
		return REGISTRY_ACCESS.getRegistry(RegistryKey.BIOME).get(NamespacedKey.minecraft("custom"));
	}

	@Override
	public NamespacedKey getBiomeKey(RegionAccessor regionAccessor, int x, int y, int z) {
		return NamespacedKey.minecraft("ocean");
	}

	@Override
	public void setBiomeKey(RegionAccessor regionAccessor, int x, int y, int z, NamespacedKey key) {
	}

	@Override
	public String getStatisticCriteriaKey(org.bukkit.Statistic statistic) {
		return null;
	}

	@Override
	public Color getSpawnEggLayerColor(EntityType entityType, int i) {
		return null;
	}

	@Override
	public LifecycleEventManager<org.bukkit.plugin.Plugin> createPluginLifecycleEventManager(org.bukkit.plugin.java.JavaPlugin plugin, java.util.function.BooleanSupplier bool) {
		return null;
	}

	@Override
	public List<Component> computeTooltipLines(ItemStack itemStack, TooltipContext tooltipContext, Player player) {
		return List.of();
	}

	public <A extends org.bukkit.Keyed, M> Tag<A> getTag(TagKey<A> tagKey) {
		return null;
	}

	@Override
	public ItemStack createEmptyStack() {
		return PaperItemStackFactory.createEmpty();
	}

	private static final class EmptyItemStack extends ItemStack {

		private Material type = Material.AIR;
		private int amount = 0;

		private EmptyItemStack() {
			super();
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
			EmptyItemStack copy = new EmptyItemStack();
			copy.setType(type);
			copy.setAmount(amount);
			return copy;
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
			return 64;
		}

		@Override
		public boolean isEmpty() {
			return type == Material.AIR || amount <= 0;
		}

		@Override
		public ItemStack clone() {
			EmptyItemStack copy = new EmptyItemStack();
			copy.type = type;
			copy.amount = amount;
			return copy;
		}

		@Override
		public boolean hasItemMeta() {
			return false;
		}

		@Override
		public org.bukkit.inventory.meta.ItemMeta getItemMeta() {
			return null;
		}

		@Override
		public boolean setItemMeta(org.bukkit.inventory.meta.ItemMeta itemMeta) {
			return false;
		}

		@Override
		public boolean editMeta(java.util.function.Consumer<? super org.bukkit.inventory.meta.ItemMeta> consumer) {
			return false;
		}

		@Override
		public String getTranslationKey() {
			return "item.minecraft.air";
		}

		@Override
		public String translationKey() {
			return getTranslationKey();
		}

		@Override
		public Component displayName() {
			return Component.empty();
		}

		@Override
		public Component effectiveName() {
			return Component.empty();
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
		public ItemStack asOne() {
			return withAmount(1);
		}

		@Override
		public ItemStack asQuantity(int qty) {
			return withAmount(qty);
		}

		@Override
		public ItemStack add() {
			return withAmount(amount + 1);
		}

		@Override
		public ItemStack add(int qty) {
			return withAmount(amount + qty);
		}

		@Override
		public ItemStack subtract() {
			return withAmount(amount - 1);
		}

		@Override
		public ItemStack subtract(int qty) {
			return withAmount(amount - qty);
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
			return java.util.Objects.hash(type, amount);
		}

		@Override
		public String toString() {
			return "ItemStack{" + type + " x" + amount + '}';
		}

		private ItemStack withAmount(int qty) {
			EmptyItemStack copy = (EmptyItemStack) clone();
			copy.setAmount(qty);
			return copy;
		}
	}

	@Override
	public Material getMaterial(String name, int version) {
		return Material.matchMaterial(name);
	}
}

