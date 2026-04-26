package io.github.thebusybiscuit.slimefun4.utils.compatibility;

import java.lang.reflect.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.potion.PotionEffectType;

// https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/legacy/FieldRename.java?until=2a6207fe150b6165722fce94c83cc1f206620ab5&untilPath=src%2Fmain%2Fjava%2Forg%2Fbukkit%2Fcraftbukkit%2Flegacy%2FFieldRename.java#216-228
public class VersionedPotionEffectType {

    public static final PotionEffectType SLOWNESS;
    public static final PotionEffectType HASTE;
    public static final PotionEffectType MINING_FATIGUE;
    public static final PotionEffectType STRENGTH;
    public static final PotionEffectType INSTANT_HEALTH;
    public static final PotionEffectType INSTANT_DAMAGE;
    public static final PotionEffectType JUMP_BOOST;
    public static final PotionEffectType NAUSEA;
    public static final PotionEffectType RESISTANCE;

    static {
        SLOWNESS = getKey("SLOWNESS", "SLOW");
        HASTE = getKey("HASTE", "FAST_DIGGING");
        MINING_FATIGUE = getKey("MINING_FATIGUE", "SLOW_DIGGING");
        STRENGTH = getKey("STRENGTH", "INCREASE_DAMAGE");
        INSTANT_HEALTH = getKey("INSTANT_HEALTH", "HEAL");
        INSTANT_DAMAGE = getKey("INSTANT_DAMAGE", "HARM");
        JUMP_BOOST = getKey("JUMP_BOOST", "JUMP");
        NAUSEA = getKey("NAUSEA", "CONFUSION");
        RESISTANCE = getKey("RESISTANCE", "DAMAGE_RESISTANCE");
    }

    @Nullable
    private static PotionEffectType getKey(@Nonnull String... keys) {
        for (String key : keys) {
            try {
                Field field = PotionEffectType.class.getDeclaredField(key);
                return (PotionEffectType) field.get(null);
            } catch (Exception ignored) {
                // Try the next key.
            }
        }

        return null;
    }
}
