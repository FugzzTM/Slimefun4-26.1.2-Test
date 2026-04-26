package org.bukkit.attribute;

/**
 * Paper 26 changed {@code Attribute} from an enum-like API to an interface,
 * but MockBukkit 3.133.2 still relies on the legacy enum constants.
 *
 * This test-scope shim restores the legacy enum shape so MockBukkit's
 * {@code EnumMap}-based entity setup keeps working on Java 25 / Paper 26.
 */
public enum Attribute {

    MAX_HEALTH,
    FOLLOW_RANGE,
    KNOCKBACK_RESISTANCE,
    MOVEMENT_SPEED,
    FLYING_SPEED,
    ATTACK_DAMAGE,
    ATTACK_KNOCKBACK,
    ATTACK_SPEED,
    ARMOR,
    ARMOR_TOUGHNESS,
    FALL_DAMAGE_MULTIPLIER,
    LUCK,
    MAX_ABSORPTION,
    SAFE_FALL_DISTANCE,
    SCALE,
    STEP_HEIGHT,
    GRAVITY,
    JUMP_STRENGTH,
    BURNING_TIME,
    CAMERA_DISTANCE,
    EXPLOSION_KNOCKBACK_RESISTANCE,
    MOVEMENT_EFFICIENCY,
    OXYGEN_BONUS,
    WATER_MOVEMENT_EFFICIENCY,
    TEMPT_RANGE,
    BLOCK_INTERACTION_RANGE,
    ENTITY_INTERACTION_RANGE,
    BLOCK_BREAK_SPEED,
    MINING_EFFICIENCY,
    SNEAKING_SPEED,
    SUBMERGED_MINING_SPEED,
    SWEEPING_DAMAGE_RATIO,
    SPAWN_REINFORCEMENTS,
    WAYPOINT_TRANSMIT_RANGE,
    WAYPOINT_RECEIVE_RANGE;

    // Legacy MockBukkit / pre-Paper-26 aliases.
    public static final Attribute GENERIC_MAX_HEALTH = MAX_HEALTH;
    public static final Attribute GENERIC_FOLLOW_RANGE = FOLLOW_RANGE;
    public static final Attribute GENERIC_KNOCKBACK_RESISTANCE = KNOCKBACK_RESISTANCE;
    public static final Attribute GENERIC_MOVEMENT_SPEED = MOVEMENT_SPEED;
    public static final Attribute GENERIC_FLYING_SPEED = FLYING_SPEED;
    public static final Attribute GENERIC_ATTACK_DAMAGE = ATTACK_DAMAGE;
    public static final Attribute GENERIC_ATTACK_KNOCKBACK = ATTACK_KNOCKBACK;
    public static final Attribute GENERIC_ATTACK_SPEED = ATTACK_SPEED;
    public static final Attribute GENERIC_ARMOR = ARMOR;
    public static final Attribute GENERIC_ARMOR_TOUGHNESS = ARMOR_TOUGHNESS;
    public static final Attribute GENERIC_LUCK = LUCK;
    public static final Attribute GENERIC_JUMP_STRENGTH = JUMP_STRENGTH;
    public static final Attribute ZOMBIE_SPAWN_REINFORCEMENTS = SPAWN_REINFORCEMENTS;
}

