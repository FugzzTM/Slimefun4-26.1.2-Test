package org.bukkit;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import io.papermc.paper.world.flag.FeatureDependant;

public abstract class GameRule<T> implements Translatable, FeatureDependant, Keyed {

    private static final Map<String, GameRule<?>> BY_NAME = new LinkedHashMap<>();

    public static final GameRule<Boolean> ANNOUNCE_ADVANCEMENTS = new SimpleGameRule<>("announce_advancements", Boolean.class);
    public static final GameRule<Boolean> COMMAND_BLOCK_OUTPUT = new SimpleGameRule<>("command_block_output", Boolean.class);
    public static final GameRule<Boolean> DISABLE_ELYTRA_MOVEMENT_CHECK = new SimpleGameRule<>("disable_elytra_movement_check", Boolean.class);
    public static final GameRule<Boolean> DO_DAYLIGHT_CYCLE = new SimpleGameRule<>("do_daylight_cycle", Boolean.class);
    public static final GameRule<Boolean> DO_ENTITY_DROPS = new SimpleGameRule<>("do_entity_drops", Boolean.class);
    public static final GameRule<Boolean> DO_FIRE_TICK = new SimpleGameRule<>("do_fire_tick", Boolean.class);
    public static final GameRule<Boolean> DO_LIMITED_CRAFTING = new SimpleGameRule<>("do_limited_crafting", Boolean.class);
    public static final GameRule<Boolean> DO_MOB_LOOT = new SimpleGameRule<>("do_mob_loot", Boolean.class);
    public static final GameRule<Boolean> DO_MOB_SPAWNING = new SimpleGameRule<>("do_mob_spawning", Boolean.class);
    public static final GameRule<Boolean> DO_TILE_DROPS = new SimpleGameRule<>("do_tile_drops", Boolean.class);
    public static final GameRule<Boolean> DO_WEATHER_CYCLE = new SimpleGameRule<>("do_weather_cycle", Boolean.class);
    public static final GameRule<Boolean> KEEP_INVENTORY = new SimpleGameRule<>("keep_inventory", Boolean.class);
    public static final GameRule<Boolean> LOG_ADMIN_COMMANDS = new SimpleGameRule<>("log_admin_commands", Boolean.class);
    public static final GameRule<Integer> MAX_COMMAND_CHAIN_LENGTH = new SimpleGameRule<>("max_command_chain_length", Integer.class);
    public static final GameRule<Integer> MAX_ENTITY_CRAMMING = new SimpleGameRule<>("max_entity_cramming", Integer.class);
    public static final GameRule<Boolean> MOB_GRIEFING = new SimpleGameRule<>("mob_griefing", Boolean.class);
    public static final GameRule<Boolean> NATURAL_REGENERATION = new SimpleGameRule<>("natural_regeneration", Boolean.class);
    public static final GameRule<Integer> RANDOM_TICK_SPEED = new SimpleGameRule<>("random_tick_speed", Integer.class);
    public static final GameRule<Boolean> REDUCED_DEBUG_INFO = new SimpleGameRule<>("reduced_debug_info", Boolean.class);
    public static final GameRule<Boolean> SEND_COMMAND_FEEDBACK = new SimpleGameRule<>("send_command_feedback", Boolean.class);
    public static final GameRule<Boolean> SHOW_DEATH_MESSAGES = new SimpleGameRule<>("show_death_messages", Boolean.class);
    public static final GameRule<Integer> SPAWN_RADIUS = new SimpleGameRule<>("spawn_radius", Integer.class);
    public static final GameRule<Boolean> SPECTATORS_GENERATE_CHUNKS = new SimpleGameRule<>("spectators_generate_chunks", Boolean.class);

    protected GameRule() {
        BY_NAME.putIfAbsent(getName(), this);
    }

    public abstract String getName();

    public abstract Class<T> getType();

    public abstract T getDefaultValue();

    public static <T> GameRule<T> getByName(String name) {
        @SuppressWarnings("unchecked")
        GameRule<T> rule = (GameRule<T>) BY_NAME.get(name);
        return rule;
    }

    public static GameRule<?>[] values() {
        Collection<GameRule<?>> values = BY_NAME.values();
        return values.toArray(new GameRule<?>[0]);
    }

    @Override
    public String getTranslationKey() {
        return "gamerule.minecraft." + getName();
    }


    @Override
    public String toString() {
        return "GameRule{" + getName() + '}';
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    private static final class SimpleGameRule<T> extends GameRule<T> {

        private final String name;
        private final Class<T> type;

        private SimpleGameRule(String name, Class<T> type) {
            this.name = name;
            this.type = type;
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
            return null;
        }

        @Override
        public String getTranslationKey() {
            return "gamerule.minecraft." + name;
        }

        @Override
        public NamespacedKey getKey() {
            return NamespacedKey.minecraft(name);
        }
    }
}

