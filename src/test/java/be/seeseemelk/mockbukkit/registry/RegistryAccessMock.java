package be.seeseemelk.mockbukkit.registry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;

import be.seeseemelk.mockbukkit.UnimplementedOperationException;

/**
 * Minimal test-side registry access implementation that avoids MockBukkit's
 * reflective registry discovery path, which breaks on newer Paper registry
 * additions such as {@code org.bukkit.Art}.
 */
public class RegistryAccessMock implements RegistryAccess {

    private static final Pattern MAPPING_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
    private static final Map<String, String> KEY_TO_CLASS = loadMappings();

    private final Map<String, Registry<?>> registries = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Registry<T> getRegistry(Class<T> type) {
        Objects.requireNonNull(type, "type");
        synchronized (registries) {
            Registry<?> registry = registries.get(type.getName());
            if (registry == null) {
                registry = createRegistry(type.getName(), type);
                registries.put(type.getName(), registry);
            }
            return (Registry<T>) registry;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Keyed> Registry<T> getRegistry(RegistryKey<T> key) {
        Objects.requireNonNull(key, "key");

    String registryKey = key.key().asString();
    if ("minecraft:damage_type".equals(registryKey)) {
      synchronized (registries) {
        Registry<?> registry = registries.get("org.bukkit.damage.DamageType");
        if (registry == null) {
          registry = createRegistry("org.bukkit.damage.DamageType", null);
          registries.put("org.bukkit.damage.DamageType", registry);
        }
        return (Registry<T>) registry;
      }
    }
    if ("minecraft:sound_event".equals(registryKey)) {
      return (Registry<T>) getRegistry(org.bukkit.Sound.class);
    }
    if ("minecraft:game_rule".equals(registryKey)) {
      synchronized (registries) {
        Registry<?> registry = registries.get("org.bukkit.GameRule");
        if (registry == null) {
          registry = createRegistry("org.bukkit.GameRule", null);
          registries.put("org.bukkit.GameRule", registry);
        }
        return (Registry<T>) registry;
      }
    }
    if ("minecraft:worldgen/biome".equals(registryKey)) {
      synchronized (registries) {
        Registry<?> registry = registries.get("org.bukkit.block.Biome");
        if (registry == null) {
          registry = createRegistry("org.bukkit.block.Biome", null);
          registries.put("org.bukkit.block.Biome", registry);
        }
        return (Registry<T>) registry;
      }
    }

        String className = KEY_TO_CLASS.get(registryKey);
        if (className == null) {
            throw new UnimplementedOperationException("Could not find registry for " + key);
        }

        try {
            if ("org.bukkit.attribute.Attribute".equals(className) || "org.bukkit.Art".equals(className)) {
                synchronized (registries) {
                    Registry<?> registry = registries.get(className);
                    if (registry == null) {
                        registry = createRegistry(className, null);
                        registries.put(className, registry);
                    }
                    return (Registry<T>) registry;
                }
            }

            Class<?> rawType = Class.forName(className, false, RegistryAccessMock.class.getClassLoader());
            if (!Keyed.class.isAssignableFrom(rawType)) {
                throw new UnimplementedOperationException("Registry type " + className + " does not implement Keyed");
            }
            return (Registry<T>) getRegistry((Class<? extends Keyed>) rawType);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Registry<?> createRegistry(String className, Class<?> type) {
        if ("org.bukkit.attribute.Attribute".equals(className)) {
            return new SimpleRegistry(SimpleRegistry.buildAttributeValues());
        }
        if ("org.bukkit.inventory.MenuType".equals(className)) {
            return new SimpleRegistry(SimpleRegistry.buildMenuTypeValues());
        }
        if ("org.bukkit.GameRule".equals(className)) {
            return new SimpleRegistry(SimpleRegistry.buildGameRuleValues());
        }
        if ("org.bukkit.Art".equals(className)) {
            return new SimpleRegistry(SimpleRegistry.buildArtValues());
        }
        if ("org.bukkit.block.BlockType".equals(className)) {
            return new SimpleRegistry<>((NamespacedKey key) -> SimpleRegistry.createSyntheticBlockType(key));
        }
        if ("org.bukkit.Sound".equals(className)) {
            return new SimpleRegistry<>((NamespacedKey key) -> SimpleRegistry.createSyntheticSound(key));
        }
        if ("org.bukkit.enchantments.Enchantment".equals(className)) {
            return new SimpleRegistry<>((NamespacedKey key) -> SimpleRegistry.createSyntheticEnchantment(key));
        }
        if ("org.bukkit.potion.PotionEffectType".equals(className)) {
            return new SimpleRegistry<>((NamespacedKey key) -> SimpleRegistry.createSyntheticPotionEffectType(key));
        }
        if ("org.bukkit.inventory.ItemType".equals(className)) {
            return new SimpleRegistry(SimpleRegistry.buildItemTypeValues());
        }
        if ("org.bukkit.damage.DamageType".equals(className)) {
            return new SimpleRegistry(SimpleRegistry.buildDamageTypeValues());
        }
        if ("org.bukkit.block.Biome".equals(className)) {
            return new SimpleRegistry(org.bukkit.block.Biome.class);
        }

        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null for non-synthetic registry " + className);
        }
        return new SimpleRegistry(type);
    }

    private static Map<String, String> loadMappings() {
        Map<String, String> mappings = new HashMap<>();

        try (var in = RegistryAccessMock.class.getResourceAsStream("/registries/registry_key_class_relation.json")) {
            if (in == null) {
                return mappings;
            }

            String content = new String(in.readAllBytes());
            Matcher matcher = MAPPING_PATTERN.matcher(content);
            while (matcher.find()) {
                mappings.put(matcher.group(1), matcher.group(2));
            }
        } catch (Exception ignored) {
            // Best-effort for tests; fall back to an empty mapping if the resource is unavailable.
        }

        return mappings;
    }

    private static final class SimpleRegistry<T extends Keyed> implements Registry<T> {

        private final Class<T> type;
        private final Map<NamespacedKey, T> keyedMap;
        private final Function<NamespacedKey, ? extends T> valueFactory;
        private boolean loaded;

        private SimpleRegistry(Class<T> type) {
            this.type = type;
            this.keyedMap = new LinkedHashMap<>();
            this.valueFactory = null;
            this.loaded = false;
        }

        private SimpleRegistry(Collection<? extends T> values) {
            this.type = null;
            this.keyedMap = new LinkedHashMap<>();
            this.valueFactory = null;
            for (T value : values) {
                keyedMap.put(value.getKey(), value);
            }
            this.loaded = true;
        }

        private SimpleRegistry(Function<NamespacedKey, ? extends T> valueFactory) {
            this.type = null;
            this.keyedMap = new LinkedHashMap<>();
            this.valueFactory = valueFactory;
            this.loaded = true;
        }

        @SuppressWarnings("unchecked")
        private static <T extends Keyed> List<T> loadValues(Class<T> type) {
            try {
                if ("org.bukkit.Sound".equals(type.getName())) {
                    return Collections.emptyList();
                }

                if ("org.bukkit.Art".equals(type.getName())) {
                    return (List<T>) buildArtValues();
                }

                if ("org.bukkit.inventory.ItemType".equals(type.getName())) {
                    return (List<T>) buildItemTypeValues();
                }

                if ("org.bukkit.block.Biome".equals(type.getName())) {
                    return (List<T>) buildBiomeValues();
                }

                List<T> values = new ArrayList<>();
                for (Field field : type.getFields()) {
                    if (Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                        Object value = field.get(null);
                        if (type.isInstance(value)) {
                            values.add((T) value);
                        }
                    }
                }

                if (!values.isEmpty()) {
                    return values;
                }

                if (type.isEnum()) {
                    T[] enumValues = type.getEnumConstants();
                    if (enumValues != null) {
                        return List.of(enumValues);
                    }
                }

                try {
                    Method valuesMethod = type.getMethod("values");
                    if (valuesMethod.getParameterCount() == 0 && valuesMethod.getReturnType().isArray()) {
                        Object array = valuesMethod.invoke(null);
                        if (array instanceof Object[] objects) {
                            values = new ArrayList<>(objects.length);
                            for (Object object : objects) {
                                if (type.isInstance(object)) {
                                    values.add((T) object);
                                }
                            }
                            return values;
                        }
                    }
                } catch (ReflectiveOperationException ignored) {
                    // Fall through to an empty registry if this type exposes no values method.
                }

                return values;
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }

        private void ensureLoaded() {
            if (loaded || type == null) {
                return;
            }

            for (T value : loadValues(type)) {
                keyedMap.put(value.getKey(), value);
            }
            loaded = true;
        }

        private static List<Keyed> buildArtValues() {
            String[] artNames = {
                "kebab", "aztec", "alban", "aztec2", "bomb", "plant", "wasteland", "pool", "courbet",
                "sea", "sunset", "creebet", "wanderer", "graham", "match", "bust", "stage", "void",
                "skull_and_roses", "wither", "fighters", "pointer", "pigscene", "burning_skull", "skeleton",
                "dennis", "donkey_kong", "earth", "wind", "water", "fire", "baroque", "humble", "meditative",
                "prairie_ride", "unpacked", "backyard", "bouquet", "cavebird", "changing", "cotan", "endboss",
                "fern", "finding", "lowmist", "orb", "owlemons", "passage", "pond", "sunflowers", "tides"
            };

            List<Keyed> values = new ArrayList<>(artNames.length);

            for (int i = 0; i < artNames.length; i++) {
                final String name = artNames[i];
                final int id = i;
                values.add(new SimpleArt(name, id));
            }

            return values;
        }

        private static List<Keyed> buildAttributeValues() {
            String[] attributeNames = {
                "max_health", "follow_range", "knockback_resistance", "movement_speed", "flying_speed",
                "attack_damage", "attack_knockback", "attack_speed", "armor", "armor_toughness",
                "fall_damage_multiplier", "luck", "max_absorption", "safe_fall_distance", "scale",
                "step_height", "gravity", "jump_strength", "burning_time", "explosion_knockback_resistance",
                "movement_efficiency", "oxygen_bonus", "water_movement_efficiency", "tempt_range",
                "block_interaction_range", "entity_interaction_range", "block_break_speed", "mining_efficiency",
                "sneaking_speed", "submerged_mining_speed", "sweeping_damage_ratio", "spawn_reinforcements"
            };

            List<Keyed> values = new ArrayList<>(attributeNames.length);

            for (int i = 0; i < attributeNames.length; i++) {
                final String name = attributeNames[i];
                final int id = i;
                values.add(new SimpleAttribute(name, id));
            }

            return values;
        }

        private static List<Keyed> buildMenuTypeValues() {
            String[] menuNames = {
                "generic_9x1", "generic_9x2", "generic_9x3", "generic_9x4", "generic_9x5", "generic_9x6",
                "generic_3x3", "crafter_3x3", "anvil", "beacon", "blast_furnace", "brewing_stand",
                "crafting", "enchantment", "furnace", "grindstone", "hopper", "lectern", "loom",
                "merchant", "shulker_box", "smithing", "smoker", "cartography_table", "stonecutter",
                "composter", "chiseled_bookshelf", "shelf", "jukebox", "decorated_pot", "crafter",
                "smithing_new"
            };

            List<Keyed> values = new ArrayList<>(menuNames.length);
            for (int i = 0; i < menuNames.length; i++) {
                values.add(new SimpleMenuType(menuNames[i], i));
            }
            return values;
        }

        private static List<Keyed> buildItemTypeValues() {
            Field[] fields = org.bukkit.inventory.ItemType.class.getFields();
            List<Keyed> values = new ArrayList<>(fields.length);

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (Modifier.isStatic(field.getModifiers()) && org.bukkit.inventory.ItemType.class.isAssignableFrom(field.getType())) {
                    String name = field.getName().toLowerCase();
                    values.add(new SimpleItemType(name));
                }
            }

            return values;
        }

        private static List<Keyed> buildBiomeValues() {
            String[] biomeNames = {
                "ocean", "plains", "desert", "windswept_hills", "forest", "taiga", "swamp", "mangrove_swamp",
                "river", "nether_wastes", "the_end", "frozen_ocean", "frozen_river", "snowy_plains",
                "mushroom_fields", "beach", "jungle", "sparse_jungle", "deep_ocean", "stony_shore",
                "snowy_beach", "birch_forest", "dark_forest", "pale_garden", "snowy_taiga", "old_growth_pine_taiga",
                "windswept_forest", "savanna", "savanna_plateau", "badlands", "wooded_badlands", "small_end_islands",
                "end_midlands", "end_highlands", "end_barrens", "warm_ocean", "lukewarm_ocean", "cold_ocean",
                "deep_lukewarm_ocean", "deep_cold_ocean", "deep_frozen_ocean", "the_void", "sunflower_plains",
                "windswept_gravelly_hills", "flower_forest", "ice_spikes", "old_growth_birch_forest",
                "old_growth_spruce_taiga", "windswept_savanna", "eroded_badlands", "bamboo_jungle",
                "soul_sand_valley", "crimson_forest", "warped_forest", "basalt_deltas", "dripstone_caves",
                "lush_caves", "deep_dark", "meadow", "grove", "snowy_slopes", "frozen_peaks", "jagged_peaks",
                "stony_peaks", "cherry_grove", "custom"
            };

            List<Keyed> values = new ArrayList<>(biomeNames.length);
            for (int i = 0; i < biomeNames.length; i++) {
                final String name = biomeNames[i];
                final int id = i;
                values.add(new SimpleBiome(name, id));
            }
            return values;
        }

        private static List<Keyed> buildGameRuleValues() {
            Object[][] rules = {
                { "advance_time", Boolean.class, Boolean.FALSE },
                { "announce_advancements", Boolean.class, Boolean.TRUE },
                { "command_block_output", Boolean.class, Boolean.TRUE },
                { "disable_player_movement_check", Boolean.class, Boolean.FALSE },
                { "disable_elytra_movement_check", Boolean.class, Boolean.FALSE },
                { "do_daylight_cycle", Boolean.class, Boolean.TRUE },
                { "do_entity_drops", Boolean.class, Boolean.TRUE },
                { "do_fire_tick", Boolean.class, Boolean.TRUE },
                { "do_limited_crafting", Boolean.class, Boolean.FALSE },
                { "projectiles_can_break_blocks", Boolean.class, Boolean.FALSE },
                { "do_mob_loot", Boolean.class, Boolean.TRUE },
                { "do_mob_spawning", Boolean.class, Boolean.TRUE },
                { "do_tile_drops", Boolean.class, Boolean.TRUE },
                { "do_weather_cycle", Boolean.class, Boolean.TRUE },
                { "keep_inventory", Boolean.class, Boolean.FALSE },
                { "log_admin_commands", Boolean.class, Boolean.TRUE },
                { "mob_griefing", Boolean.class, Boolean.TRUE },
                { "natural_regeneration", Boolean.class, Boolean.TRUE },
                { "reduced_debug_info", Boolean.class, Boolean.FALSE },
                { "send_command_feedback", Boolean.class, Boolean.TRUE },
                { "show_death_messages", Boolean.class, Boolean.TRUE },
                { "spectators_generate_chunks", Boolean.class, Boolean.TRUE },
                { "disable_raids", Boolean.class, Boolean.FALSE },
                { "do_insomnia", Boolean.class, Boolean.TRUE },
                { "do_immediate_respawn", Boolean.class, Boolean.FALSE },
                { "drowning_damage", Boolean.class, Boolean.TRUE },
                { "fall_damage", Boolean.class, Boolean.TRUE },
                { "fire_damage", Boolean.class, Boolean.TRUE },
                { "freeze_damage", Boolean.class, Boolean.TRUE },
                { "do_patrol_spawning", Boolean.class, Boolean.TRUE },
                { "do_trader_spawning", Boolean.class, Boolean.TRUE },
                { "do_warden_spawning", Boolean.class, Boolean.TRUE },
                { "forgive_dead_players", Boolean.class, Boolean.TRUE },
                { "universal_anger", Boolean.class, Boolean.FALSE },
                { "block_explosion_drop_decay", Boolean.class, Boolean.TRUE },
                { "mob_explosion_drop_decay", Boolean.class, Boolean.TRUE },
                { "tnt_explosion_drop_decay", Boolean.class, Boolean.TRUE },
                { "water_source_conversion", Boolean.class, Boolean.TRUE },
                { "lava_source_conversion", Boolean.class, Boolean.TRUE },
                { "global_sound_events", Boolean.class, Boolean.TRUE },
                { "do_vines_spread", Boolean.class, Boolean.TRUE },
                { "ender_pearls_vanish_on_death", Boolean.class, Boolean.TRUE },
                { "allow_fire_ticks_away_from_player", Boolean.class, Boolean.FALSE },
                { "tnt_explodes", Boolean.class, Boolean.TRUE },
                { "locator_bar", Boolean.class, Boolean.TRUE },
                { "pvp", Boolean.class, Boolean.TRUE },
                { "spawn_monsters", Boolean.class, Boolean.TRUE },
                { "allow_entering_nether_using_portals", Boolean.class, Boolean.TRUE },
                { "command_blocks_enabled", Boolean.class, Boolean.TRUE },
                { "spawner_blocks_enabled", Boolean.class, Boolean.TRUE },
                { "random_tick_speed", Integer.class, 3 },
                { "spawn_radius", Integer.class, 10 },
                { "max_entity_cramming", Integer.class, 24 },
                { "max_command_chain_length", Integer.class, 65536 },
                { "max_command_fork_count", Integer.class, 65536 },
                { "command_modification_block_limit", Integer.class, 32768 },
                { "players_sleeping_percentage", Integer.class, 100 },
                { "snow_accumulation_height", Integer.class, 1 },
                { "players_nether_portal_default_delay", Integer.class, 80 },
                { "players_nether_portal_creative_delay", Integer.class, 0 },
                { "minecart_max_speed", Integer.class, 8 }
            };

            List<Keyed> values = new ArrayList<>(rules.length);
            for (int i = 0; i < rules.length; i++) {
                String name = (String) rules[i][0];
                @SuppressWarnings("unchecked")
                Class<Object> type = (Class<Object>) rules[i][1];
                Object defaultValue = rules[i][2];
                values.add(new SyntheticGameRule<>(name, type, defaultValue));
            }
            return values;
        }

        private static List<Keyed> buildDamageTypeValues() {
              String[] damageTypeKeys = {
                "arrow",
                "bad_respawn_point",
                "cactus",
                "campfire",
                "cramming",
                "dragon_breath",
                "drown",
                "dry_out",
                "ender_pearl",
                "explosion",
                "fall",
                "falling_anvil",
                "falling_block",
                "falling_stalactite",
                "fireball",
                "fireworks",
                "fly_into_wall",
                "freeze",
                "generic",
                "generic_kill",
                "hot_floor",
                "in_fire",
                "in_wall",
                "indirect_magic",
                "lava",
                "lightning_bolt",
                "mace_smash",
                "magic",
                "mob_attack",
                "mob_attack_no_aggro",
                "mob_projectile",
                "on_fire",
                "out_of_world",
                "outside_border",
                "player_attack",
                "player_explosion",
                "sonic_boom",
                "spear",
                "spit",
                "stalagmite",
                "starve",
                "sting",
                "sweet_berry_bush",
                "thorns",
                "thrown",
                "trident",
                "unattributed_fireball",
                "wind_charge",
                "wither",
                "wither_skull"
              };

              List<Keyed> values = new ArrayList<>(damageTypeKeys.length);
              for (String damageTypeKey : damageTypeKeys) {
                values.add(createSyntheticDamageType(damageTypeKey));
              }
              return values;
        }

        private static org.bukkit.block.BlockType createSyntheticBlockType(NamespacedKey key) {
            ClassLoader classLoader = RegistryAccessMock.class.getClassLoader();
            Class<?>[] interfaces = { org.bukkit.block.BlockType.Typed.class };

            return (org.bukkit.block.BlockType) Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
                String name = method.getName();

                if (name.equals("getKey")) {
                    return key;
                }
                if (name.equals("translationKey") || name.equals("getTranslationKey")) {
                    return "block.minecraft." + key.getKey();
                }
                if (name.equals("typed")) {
                    return proxy;
                }
                if (name.equals("hasItemType")) {
                    return false;
                }
                if (name.equals("getItemType")) {
                    return null;
                }
                if (name.equals("getBlockDataClass")) {
                    return org.bukkit.block.data.BlockData.class;
                }
                if (name.equals("createBlockData") || name.equals("createBlockDataStates")) {
                    return null;
                }
                if (name.equals("isAir")) {
                    return key.getKey().endsWith("air");
                }
                if (name.equals("isEnabledByFeature")) {
                    return true;
                }
                if (name.equals("asMaterial")) {
                    return org.bukkit.Material.matchMaterial(key.getKey().toUpperCase(java.util.Locale.ROOT));
                }
                if (name.equals("isSolid") || name.equals("isFlammable") || name.equals("isBurnable") || name.equals("isOccluding")
                    || name.equals("hasGravity") || name.equals("isInteractable") || name.equals("hasCollision")) {
                    return !key.getKey().endsWith("air");
                }
                if (name.equals("getHardness") || name.equals("getBlastResistance") || name.equals("getSlipperiness")) {
                    return 0F;
                }
                if (name.equals("toString")) {
                    return "BlockType{" + key + '}';
                }
                if (name.equals("hashCode")) {
                    return key.hashCode();
                }
                if (name.equals("equals")) {
                    return proxy == args[0];
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                }
                if (returnType == int.class) {
                    return 0;
                }
                if (returnType == float.class) {
                    return 0F;
                }
                if (returnType == double.class) {
                    return 0D;
                }
                if (returnType == long.class) {
                    return 0L;
                }
                return null;
            });
        }

        private static org.bukkit.damage.DamageType createSyntheticDamageType(String keyName) {
            NamespacedKey key = NamespacedKey.minecraft(keyName);
            ClassLoader classLoader = RegistryAccessMock.class.getClassLoader();
            Class<?>[] interfaces = { org.bukkit.damage.DamageType.class };

            return (org.bukkit.damage.DamageType) Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, args) -> {
                String name = method.getName();

                if (name.equals("getKey")) {
                    return key;
                }
                if (name.equals("key")) {
                    return key;
                }
                if (name.equals("getTranslationKey") || name.equals("translationKey")) {
                    return "death.attack." + key.getKey();
                }
                if (name.equals("getDamageScaling")) {
                    return org.bukkit.damage.DamageScaling.ALWAYS;
                }
                if (name.equals("getDamageEffect")) {
                    return org.bukkit.damage.DamageEffect.HURT;
                }
                if (name.equals("getDeathMessageType")) {
                    return org.bukkit.damage.DeathMessageType.DEFAULT;
                }
                if (name.equals("getExhaustion")) {
                    return 0F;
                }
                if (name.equals("toString")) {
                    return "DamageType{" + key + '}';
                }
                if (name.equals("hashCode")) {
                    return key.hashCode();
                }
                if (name.equals("equals")) {
                    return proxy == args[0];
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                }
                if (returnType == int.class) {
                    return 0;
                }
                if (returnType == float.class) {
                    return 0F;
                }
                return null;
            });
        }

        private static org.bukkit.enchantments.Enchantment createSyntheticEnchantment(NamespacedKey key) {
            return new org.bukkit.enchantments.Enchantment() {

                @Override
                public NamespacedKey getKey() {
                    return key;
                }

                @Override
                public String getName() {
                    return key.getKey().toUpperCase(java.util.Locale.ROOT);
                }

                @Override
                public int getMaxLevel() {
                    return 1;
                }

                @Override
                public int getStartLevel() {
                    return 1;
                }

                @Override
                public org.bukkit.enchantments.EnchantmentTarget getItemTarget() {
                    return org.bukkit.enchantments.EnchantmentTarget.ALL;
                }

                @Override
                public boolean isTreasure() {
                    return false;
                }

                @Override
                public boolean isCursed() {
                    return false;
                }

                @Override
                public boolean conflictsWith(org.bukkit.enchantments.Enchantment other) {
                    return false;
                }

                @Override
                public boolean canEnchantItem(org.bukkit.inventory.ItemStack item) {
                    return true;
                }

                @Override
                public Component displayName(int level) {
                    return Component.text(getName());
                }

                @Override
                public boolean isTradeable() {
                    return true;
                }

                @Override
                public boolean isDiscoverable() {
                    return true;
                }

                @Override
                public int getMinModifiedCost(int level) {
                    return 0;
                }

                @Override
                public int getMaxModifiedCost(int level) {
                    return 0;
                }

                @Override
                public int getAnvilCost() {
                    return 0;
                }

                @Override
                public io.papermc.paper.enchantments.EnchantmentRarity getRarity() {
                    return io.papermc.paper.enchantments.EnchantmentRarity.COMMON;
                }

                @Override
                public float getDamageIncrease(int level, org.bukkit.entity.EntityCategory entityCategory) {
                    return 0F;
                }

                @Override
                public float getDamageIncrease(int level, org.bukkit.entity.EntityType entityType) {
                    return 0F;
                }

                @Override
                public java.util.Set<org.bukkit.inventory.EquipmentSlotGroup> getActiveSlotGroups() {
                    return java.util.Collections.emptySet();
                }

                @Override
                public Component description() {
                    return Component.empty();
                }

                @Override
                public io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.inventory.ItemType> getSupportedItems() {
                    return null;
                }

                @Override
                public io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.inventory.ItemType> getPrimaryItems() {
                    return null;
                }

                @Override
                public int getWeight() {
                    return 1;
                }

                @Override
                public io.papermc.paper.registry.set.RegistryKeySet<org.bukkit.enchantments.Enchantment> getExclusiveWith() {
                    return null;
                }

                @Override
                public String translationKey() {
                    return "enchantment.minecraft." + key.getKey();
                }

                @Override
                public String getTranslationKey() {
                    return translationKey();
                }

                @Override
                public String toString() {
                    return "Enchantment{" + key + '}';
                }

                @Override
                public int hashCode() {
                    return key.hashCode();
                }

                @Override
                public boolean equals(Object obj) {
                    return this == obj;
                }
            };
        }

        private static org.bukkit.Sound createSyntheticSound(NamespacedKey key) {
            ClassLoader classLoader = RegistryAccessMock.class.getClassLoader();

            return (org.bukkit.Sound) Proxy.newProxyInstance(classLoader, new Class<?>[] { org.bukkit.Sound.class }, (proxy, method, args) -> {
                String name = method.getName();

                if (name.equals("getKey")) {
                    return key;
                }
                if (name.equals("key")) {
                    return Key.key(key.getNamespace(), key.getKey());
                }
                if (name.equals("name")) {
                    return key.getKey().toUpperCase(java.util.Locale.ROOT).replace('.', '_').replace('/', '_');
                }
                if (name.equals("ordinal")) {
                    return key.hashCode() & Integer.MAX_VALUE;
                }
                if (name.equals("compareTo")) {
                    Object other = args != null && args.length > 0 ? args[0] : null;

                    if (other instanceof org.bukkit.Keyed keyed) {
                        return key.toString().compareTo(keyed.getKey().toString());
                    }

                    return 0;
                }
                if (name.equals("toString")) {
                    return "Sound{" + key + '}';
                }
                if (name.equals("hashCode")) {
                    return key.hashCode();
                }
                if (name.equals("equals")) {
                    if (args == null || args.length == 0 || args[0] == null) {
                        return false;
                    }
                    if (proxy == args[0]) {
                        return true;
                    }
                    if (args[0] instanceof org.bukkit.Keyed keyed) {
                        return key.equals(keyed.getKey());
                    }
                    return false;
                }

                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                }
                if (returnType == int.class) {
                    return 0;
                }
                if (returnType == float.class) {
                    return 0F;
                }
                if (returnType == double.class) {
                    return 0D;
                }
                if (returnType == long.class) {
                    return 0L;
                }
                return null;
            });
        }

        private static final class SimpleItemType implements org.bukkit.inventory.ItemType.Typed<org.bukkit.inventory.meta.ItemMeta> {

            private final String name;
            private final NamespacedKey key;

            private SimpleItemType(String name) {
                this.name = name;
                this.key = NamespacedKey.minecraft(name);
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            public org.bukkit.inventory.ItemType.Typed<org.bukkit.inventory.meta.ItemMeta> typed() {
                return this;
            }

            @Override
            @SuppressWarnings("unchecked")
            public <M extends org.bukkit.inventory.meta.ItemMeta> org.bukkit.inventory.ItemType.Typed<M> typed(Class<M> metaClass) {
                return (org.bukkit.inventory.ItemType.Typed<M>) this;
            }

            @Override
            public Class<org.bukkit.inventory.meta.ItemMeta> getItemMetaClass() {
                return org.bukkit.inventory.meta.ItemMeta.class;
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public int getBurnDuration() {
                return 0;
            }

            @Override
            public org.bukkit.block.BlockType getBlockType() {
                return null;
            }

            @Override
            public boolean hasBlockType() {
                return false;
            }

            @Override
            public org.bukkit.inventory.ItemStack createItemStack() {
                return createItemStack(1);
            }

            @Override
            public org.bukkit.inventory.ItemStack createItemStack(int amount) {
                return PaperItemStackFactory.create(material(), amount);
            }

            @Override
            public org.bukkit.inventory.ItemStack createItemStack(java.util.function.Consumer<? super org.bukkit.inventory.meta.ItemMeta> consumer) {
                return createItemStack(1, consumer);
            }

            @Override
            public org.bukkit.inventory.ItemStack createItemStack(int amount, java.util.function.Consumer<? super org.bukkit.inventory.meta.ItemMeta> consumer) {
                org.bukkit.inventory.ItemStack stack = createItemStack(amount);
                if (consumer != null) {
                    stack.editMeta(consumer);
                }
                return stack;
            }

            @Override
            public short getMaxDurability() {
                return 0;
            }

            @Override
            public boolean isEdible() {
                return false;
            }

            @Override
            public boolean isRecord() {
                return false;
            }

            @Override
            public boolean isFuel() {
                return false;
            }

            @Override
            public boolean isCompostable() {
                return false;
            }

            @Override
            public float getCompostChance() {
                return 0F;
            }

            @Override
            public org.bukkit.inventory.ItemType getCraftingRemainingItem() {
                return null;
            }

            @Override
            public com.google.common.collect.Multimap<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> getDefaultAttributeModifiers() {
                return com.google.common.collect.ImmutableMultimap.of();
            }

            @Override
            public com.google.common.collect.Multimap<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> getDefaultAttributeModifiers(org.bukkit.inventory.EquipmentSlot slot) {
                return com.google.common.collect.ImmutableMultimap.of();
            }

            @Override
            public org.bukkit.inventory.CreativeCategory getCreativeCategory() {
                return null;
            }

            @Override
            public boolean isEnabledByFeature(org.bukkit.World world) {
                return true;
            }

            @Override
            public org.bukkit.Material asMaterial() {
                return material();
            }

            @Override
            public org.bukkit.inventory.ItemRarity getItemRarity() {
                return null;
            }

            @Override
            public <T> T getDefaultData(io.papermc.paper.datacomponent.DataComponentType.Valued<T> type) {
                return null;
            }

            @Override
            public boolean hasDefaultData(io.papermc.paper.datacomponent.DataComponentType type) {
                return false;
            }

            @Override
            public java.util.Set<io.papermc.paper.datacomponent.DataComponentType> getDefaultDataTypes() {
                return java.util.Collections.emptySet();
            }

            @Override
            public String getTranslationKey() {
                return "item.minecraft." + name;
            }

            @Override
            public String translationKey() {
                return getTranslationKey();
            }

            @Override
            public String toString() {
                return "ItemType{" + name + '}';
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }

            private org.bukkit.Material material() {
                org.bukkit.Material material = org.bukkit.Material.matchMaterial(name.toUpperCase(java.util.Locale.ROOT));
                return material != null ? material : org.bukkit.Material.STONE;
            }
        }

        private static final class SyntheticItemStack extends org.bukkit.inventory.ItemStack {

            private org.bukkit.Material type;
            private int amount;
            private org.bukkit.inventory.meta.ItemMeta meta;

            private SyntheticItemStack(org.bukkit.Material type, int amount) {
                super();
                this.type = type;
                this.amount = amount;
                this.meta = createMeta(type);
            }

            private static org.bukkit.inventory.meta.ItemMeta createMeta(org.bukkit.Material type) {
                try {
                    return org.bukkit.Bukkit.getItemFactory().getItemMeta(type);
                } catch (Exception ignored) {
                    return null;
                }
            }

            @Override
            public org.bukkit.Material getType() {
                return type;
            }

            @Override
            public void setType(org.bukkit.Material type) {
                this.type = type;
                if (meta == null) {
                    meta = createMeta(type);
                }
            }

            @Override
            public int getAmount() {
                return amount;
            }

            @Override
            public void setAmount(int amount) {
                this.amount = amount;
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }

            @Override
            public boolean hasItemMeta() {
                return meta != null;
            }

            @Override
            public org.bukkit.inventory.meta.ItemMeta getItemMeta() {
                if (meta == null) {
                    meta = createMeta(type);
                }
                return meta == null ? null : meta.clone();
            }

            @Override
            public boolean setItemMeta(org.bukkit.inventory.meta.ItemMeta itemMeta) {
                this.meta = itemMeta == null ? null : itemMeta.clone();
                return true;
            }

            @Override
            public boolean editMeta(java.util.function.Consumer<? super org.bukkit.inventory.meta.ItemMeta> consumer) {
                if (consumer == null) {
                    return false;
                }
                org.bukkit.inventory.meta.ItemMeta current = meta;
                if (current == null) {
                    current = createMeta(type);
                }
                if (current == null) {
                    return false;
                }
                consumer.accept(current);
                meta = current;
                return true;
            }

            @Override
            public org.bukkit.inventory.ItemStack clone() {
                SyntheticItemStack copy = new SyntheticItemStack(type, amount);
                copy.meta = meta == null ? null : meta.clone();
                return copy;
            }

            @Override
            public boolean isSimilar(org.bukkit.inventory.ItemStack stack) {
                if (stack == null) {
                    return false;
                }
                return type == stack.getType() && amount == stack.getAmount();
            }

            @Override
            public String getTranslationKey() {
                return "item.minecraft." + type.name().toLowerCase(java.util.Locale.ROOT);
            }

            @Override
            public String toString() {
                return "SyntheticItemStack{" + type + " x" + amount + '}';
            }

            @Override
            public int hashCode() {
                return java.util.Objects.hash(type, amount);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof org.bukkit.inventory.ItemStack other)) {
                    return false;
                }
                return type == other.getType() && amount == other.getAmount();
            }
        }


        private static final class SimpleAttribute implements org.bukkit.Keyed, org.bukkit.Translatable {

            private final String name;
            private final int id;
            private final NamespacedKey key;

            private SimpleAttribute(String name, int id) {
                this.name = name;
                this.id = id;
                this.key = NamespacedKey.minecraft(name);
            }

            public NamespacedKey getKey() {
                return key;
            }

    @Override
    public String getTranslationKey() {
 			return "attribute.name." + name;
 		}

 		public String translationKey() {
 			return getTranslationKey();
 		}

 		public String name() {
 			return name.toUpperCase();
 		}

 		public int ordinal() {
 			return id;
 		}

    public int compareTo(org.bukkit.attribute.Attribute other) {
 			return Integer.compare(id, other.ordinal());
 		}

 		public String toString() {
 			return "Attribute{" + name + '}';
 		}

 		public int hashCode() {
 			return name.hashCode();
 		}

 		public boolean equals(Object obj) {
 			return this == obj;
 		}
        }

        private static final class SimpleMenuType implements org.bukkit.inventory.MenuType.Typed<org.bukkit.inventory.InventoryView, org.bukkit.inventory.view.builder.InventoryViewBuilder<org.bukkit.inventory.InventoryView>> {

            private final String name;
            private final int id;
            private final NamespacedKey key;

            private SimpleMenuType(String name, int id) {
                this.name = name;
                this.id = id;
                this.key = NamespacedKey.minecraft(name);
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            public org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity viewer, Component title) {
                return null;
            }

            @Override
            public org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity viewer, String title) {
                return null;
            }

            @Override
            public org.bukkit.inventory.MenuType.Typed<org.bukkit.inventory.InventoryView, org.bukkit.inventory.view.builder.InventoryViewBuilder<org.bukkit.inventory.InventoryView>> typed() {
                return this;
            }

            @Override
            public <V extends org.bukkit.inventory.InventoryView, B extends org.bukkit.inventory.view.builder.InventoryViewBuilder<V>> org.bukkit.inventory.MenuType.Typed<V, B> typed(Class<V> viewClass) {
                return (org.bukkit.inventory.MenuType.Typed<V, B>) this;
            }

            @Override
            public Class<? extends org.bukkit.inventory.InventoryView> getInventoryViewClass() {
                return org.bukkit.inventory.InventoryView.class;
            }

            @Override
            public org.bukkit.inventory.view.builder.InventoryViewBuilder<org.bukkit.inventory.InventoryView> builder() {
                return null;
            }

            @Override
            public String toString() {
                return "MenuType{" + name + '}';
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }
        }

        private static final class SimpleBiome implements org.bukkit.block.Biome {

            private final String name;
            private final int id;
            private final NamespacedKey key;

            private SimpleBiome(String name, int id) {
                this.name = name;
                this.id = id;
                this.key = NamespacedKey.minecraft(name);
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            public String translationKey() {
                return "biome.minecraft." + name;
            }

            @Override
            public String name() {
                return name.toUpperCase(java.util.Locale.ROOT);
            }

            @Override
            public int ordinal() {
                return id;
            }

            @Override
            public int compareTo(org.bukkit.block.Biome other) {
                return Integer.compare(id, other.ordinal());
            }

            @Override
            public String toString() {
                return "Biome{" + name + '}';
            }
        }

        private static final class SimplePotionEffectType extends org.bukkit.potion.PotionEffectType {

            private final String name;
            private final int id;
            private final NamespacedKey key;

            private SimplePotionEffectType(String name, int id) {
                this.name = name;
                this.id = id;
                this.key = NamespacedKey.minecraft(name);
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            public org.bukkit.potion.PotionEffect createEffect(int duration, int amplifier) {
                return new org.bukkit.potion.PotionEffect(this, duration, amplifier);
            }

            @Override
            public boolean isInstant() {
                return name.startsWith("instant_");
            }

            @Override
            public org.bukkit.potion.PotionEffectTypeCategory getCategory() {
                return org.bukkit.potion.PotionEffectTypeCategory.BENEFICIAL;
            }

            @Override
            public org.bukkit.Color getColor() {
                return org.bukkit.Color.WHITE;
            }

            @Override
            public double getDurationModifier() {
                return 1.0D;
            }

            @Override
            public int getId() {
                return id;
            }

            @Override
            public String getName() {
                return name.toUpperCase(java.util.Locale.ROOT);
            }

            @Override
            public java.util.Map<org.bukkit.attribute.Attribute, org.bukkit.attribute.AttributeModifier> getEffectAttributes() {
                return java.util.Collections.emptyMap();
            }

            @Override
            public double getAttributeModifierAmount(org.bukkit.attribute.Attribute attribute, int amplifier) {
                return 0D;
            }

            @Override
            public org.bukkit.potion.PotionEffectType.Category getEffectCategory() {
                return org.bukkit.potion.PotionEffectType.Category.BENEFICIAL;
            }

            @Override
            public String translationKey() {
                return "effect.minecraft." + name;
            }

            @Override
            public String getTranslationKey() {
                return translationKey();
            }

            @Override
            public String toString() {
                return "PotionEffectType{" + name + '}';
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }
        }

        private static org.bukkit.potion.PotionEffectType createSyntheticPotionEffectType(NamespacedKey key) {
            String name = key.getKey();
            int id = Math.abs(name.hashCode());
            return new SimplePotionEffectType(name, id);
        }

        private static final class SimpleArt implements org.bukkit.Art {

            private final String name;
            private final int id;
            private final NamespacedKey key;
            private final Key adventureKey;

            private SimpleArt(String name, int id) {
                this.name = name;
                this.id = id;
                this.key = NamespacedKey.minecraft(name);
                this.adventureKey = Key.key(key.getNamespace(), key.getKey());
            }

            @Override
            public int getBlockWidth() {
                return 1;
            }

            @Override
            public int getBlockHeight() {
                return 1;
            }

            @Override
            public int getId() {
                return id;
            }

            @Override
            public int ordinal() {
                return id;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public int compareTo(org.bukkit.Art other) {
                return Integer.compare(id, other.ordinal());
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            public Component title() {
                return Component.text(name);
            }

            @Override
            public Component author() {
                return Component.empty();
            }

            @Override
            public Key assetId() {
                return adventureKey;
            }

            @Override
            public String toString() {
                return "Art{" + name + '}';
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }
        }

        private static final class SyntheticGameRule<T> extends org.bukkit.GameRule<T> {

            private final String name;
            private final Class<T> type;
            private final T defaultValue;
            private final NamespacedKey key;

            private SyntheticGameRule(String name, Class<T> type, Object defaultValue) {
                this.name = name;
                this.type = type;
                this.defaultValue = type.cast(defaultValue);
                this.key = NamespacedKey.minecraft(name);
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public Class<T> getType() {
                return type;
            }

            @Override
            public T getDefaultValue() {
                return defaultValue;
            }

            @Override
            public String getTranslationKey() {
                return "gamerule.minecraft." + name;
            }

            @Override
            public NamespacedKey getKey() {
                return key;
            }

            @Override
            public String toString() {
                return "GameRule{" + name + '}';
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj;
            }
        }

        @Override
        public T get(NamespacedKey key) {
            ensureLoaded();
            T value = keyedMap.get(key);
            if (value == null && valueFactory != null) {
                value = valueFactory.apply(key);
                if (value != null) {
                    keyedMap.put(key, value);
                }
            }
            if (value == null && type != null && "org.bukkit.Sound".equals(type.getName())) {
                value = (T) createSyntheticSound(key);
                keyedMap.put(key, value);
            }
            return value;
        }

        @Override
        public NamespacedKey getKey(T value) {
            ensureLoaded();
            return value.getKey();
        }

        @Override
        public boolean hasTag(TagKey<T> tag) {
            return false;
        }

        @Override
        public Tag<T> getTag(TagKey<T> tag) {
            return null;
        }

        @Override
        public Collection<Tag<T>> getTags() {
            return Collections.emptyList();
        }

        @Override
        public Iterator<T> iterator() {
            ensureLoaded();
            return keyedMap.values().iterator();
        }

        @Override
        public java.util.stream.Stream<T> stream() {
            ensureLoaded();
            return keyedMap.values().stream();
        }

        @Override
        public java.util.stream.Stream<NamespacedKey> keyStream() {
            ensureLoaded();
            return keyedMap.keySet().stream();
        }

        @Override
        public int size() {
            ensureLoaded();
            return keyedMap.size();
        }
    }
}

