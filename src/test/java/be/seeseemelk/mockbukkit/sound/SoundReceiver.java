package be.seeseemelk.mockbukkit.sound;

import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Sound;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public interface SoundReceiver {

    List<AudioExperience> getHeardSounds();

    default void addHeardSound(AudioExperience experience) {
        getHeardSounds().add(experience);
    }

    default void clearSounds() {
        getHeardSounds().clear();
    }

    default void assertSoundHeard(Sound sound) {
        assertSoundHeard(sound.getKey().asString());
    }

    default void assertSoundHeard(net.kyori.adventure.sound.Sound sound) {
        assertSoundHeard(sound.name().asString());
    }

    default void assertSoundHeard(String sound) {
        assertSoundHeard(sound, experience -> true);
    }

    default void assertSoundHeard(Sound sound, Predicate<AudioExperience> predicate) {
        assertSoundHeard(sound.getKey().asString(), predicate);
    }

    default void assertSoundHeard(net.kyori.adventure.sound.Sound sound, Predicate<AudioExperience> predicate) {
        assertSoundHeard(sound.name().asString(), predicate);
    }

    default void assertSoundHeard(String sound, Predicate<AudioExperience> predicate) {
        boolean heard = getHeardSounds().stream().anyMatch(experience -> matchesSound(experience, sound) && predicate.test(experience));
        assertTrue(heard, () -> "Expected sound '" + sound + "' to have been heard, but heard: " + getHeardSounds());
    }

    default void assertSoundHeard(String source, Sound sound) {
        assertSoundHeard(source, sound.getKey().asString());
    }

    default void assertSoundHeard(String source, net.kyori.adventure.sound.Sound sound) {
        assertSoundHeard(source, sound.name().asString());
    }

    default void assertSoundHeard(String source, String sound) {
        assertSoundHeard(source, sound, experience -> true);
    }

    default void assertSoundHeard(String source, Sound sound, Predicate<AudioExperience> predicate) {
        assertSoundHeard(source, sound.getKey().asString(), predicate);
    }

    default void assertSoundHeard(String source, net.kyori.adventure.sound.Sound sound, Predicate<AudioExperience> predicate) {
        assertSoundHeard(source, sound.name().asString(), predicate);
    }

    default void assertSoundHeard(String source, String sound, Predicate<AudioExperience> predicate) {
        boolean heard = getHeardSounds().stream().anyMatch(experience -> matchesSound(experience, sound)
            && matchesSource(experience, source)
            && predicate.test(experience));
        assertTrue(heard, () -> "Expected sound '" + sound + "' from source '" + source + "' to have been heard, but heard: " + getHeardSounds());
    }

    private static boolean matchesSound(AudioExperience experience, String expectedSound) {
        String actual = experience.getSound();
        if (actual == null) {
            return false;
        }

        if (actual.equals(expectedSound)) {
            return true;
        }

        if (actual.startsWith("minecraft:") && actual.substring("minecraft:".length()).equals(expectedSound)) {
            return true;
        }

        return expectedSound.startsWith("minecraft:") && expectedSound.substring("minecraft:".length()).equals(actual);
    }

    private static boolean matchesSource(AudioExperience experience, String source) {
        if (source == null) {
            return true;
        }

        String actualSource = experience.getSource() == null ? null : experience.getSource().toString();
        if (actualSource != null && source.equals(actualSource)) {
            return true;
        }

        return experience.getCategory() != null && source.equals(experience.getCategory().name());
    }
}
