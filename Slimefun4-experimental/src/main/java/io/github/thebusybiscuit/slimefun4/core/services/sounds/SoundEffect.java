package io.github.thebusybiscuit.slimefun4.core.services.sounds;

import java.util.Locale;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;

import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.google.common.base.Preconditions;

/**
 * This enum holds references to all our sounds.
 * 
 * @author TheBusyBiscuit
 * @author J3fftw1
 *
 * @see SoundService
 * @see SoundConfiguration
 *
 */
public enum SoundEffect {

    ANCIENT_ALTAR_ITEM_CHECK_SOUND("entity.enderman.teleport", 1F, 2F),
    ANCIENT_ALTAR_ITEM_DROP_SOUND("entity.zombie.attack.iron.door", 1F, 1F),
    ANCIENT_ALTAR_ITEM_PICK_UP_SOUND("entity.item.pickup", 1F, 1F),
    ANCIENT_ALTAR_FINISH_SOUND("entity.zombie.villager.cure", 1F, 1F),
    ANCIENT_ALTAR_START_SOUND("entity.illusioner.prepare.mirror", 1F, 1F),
    ANCIENT_PEDESTAL_ITEM_PLACE_SOUND("entity.item.pickup", 0.5F, 0.5F),
    ARMOR_FORGE_FINISH_SOUND("block.anvil.use", 1F, 1F),
    ARMOR_FORGE_WORKING_SOUND("entity.arrow.hit.player", 1F, 1F),
    AUTO_CRAFTER_GUI_CLICK_SOUND("ui.button.click", 1F, 1F),
    AUTO_CRAFTER_UPDATE_RECIPE("ui.button.click", 1F, 1F),
    AUTOMATED_PANNING_MACHINE_FAIL_SOUND("entity.armor.stand.break", 1F, 1F),
    AUTOMATED_PANNING_MACHINE_SUCCESS_SOUND("entity.arrow.hit.player", 1F, 1F),
    BEE_BOOTS_FALL_SOUND("block.honey.block.fall", 1F, 1F),
    BACKPACK_CLOSE_SOUND("entity.horse.armor", 1F, 1F),
    BACKPACK_OPEN_SOUND("entity.horse.armor", 1F, 1F),
    COMPOSTER_COMPOST_SOUND("entity.arrow.hit.player", 1F, 1F),
    COMPRESSOR_CRAFT_SOUND("entity.arrow.hit.player", 1F, 1F),
    COMPRESSOR_CRAFT_CONTRACT_SOUND("block.piston.contract", 1F, 1F),
    COMPRESSOR_CRAFT_EXTEND_SOUND("block.piston.extend", 1F, 1F),
    COOLER_CONSUME_SOUND("entity.generic.drink", 1F, 1F),
    CRUCIBLE_ADD_WATER_SOUND("entity.player.splash", 1F, 1F),
    CRUCIBLE_ADD_LAVA_SOUND("block.lava.pop", 1F , 1F),
    CRUCIBLE_BLOCK_BREAK_SOUND("block.metal.break", 1F, 1F),
    CRUCIBLE_GENERATE_LIQUID_SOUND("block.lava.extinguish", 1F, 1F),
    CRUCIBLE_INTERACT_SOUND("entity.arrow.hit.player", 1F, 1F),
    CRUCIBLE_PLACE_LAVA_SOUND("block.lava.pop", 1F , 1F),
    CRUCIBLE_PLACE_WATER_SOUND("entity.player.splash", 1F, 1F),
    DEBUG_FISH_CLICK_SOUND("block.bamboo.place", 1F, 1F),
    DIET_COOKIE_CONSUME_SOUND("entity.generic.eat", 1F, 1F),
    ENCHANTMENT_RUNE_ADD_ENCHANT_SOUND("entity.zombie.villager.cure", 1F, 1F),
    ENDER_BACKPACK_OPEN_SOUND("entity.enderman.teleport", 1F, 1F),
    ENHANCED_CRAFTING_TABLE_CRAFT_SOUND("block.wooden.button.click.on", 1F, 1F),
    ELYTRA_CAP_IMPACT_SOUND("block.stone.hit", 1F, 1F),
    EXPLOSIVE_BOW_HIT_SOUND("entity.generic.explode", 1F, 1F),
    EXPLOSIVE_TOOL_EXPLODE_SOUND("entity.generic.explode", 0.2F, 1F),
    FISHERMAN_ANDROID_FISHING_SOUND("entity.player.splash", 0.3F, 0.7F),
    FLASK_OF_KNOWLEDGE_FILLUP_SOUND("entity.experience.orb.pickup", 1F, 0.5F),
    GUIDE_BUTTON_CLICK_SOUND("item.book.page.turn", 1F, 1F),
    GUIDE_CONTRIBUTORS_OPEN_SOUND("block.note.block.harp", 0.7F, 0.7F),
    GUIDE_LANGUAGE_OPEN_SOUND("block.note.block.harp", 0.7F, 0.7F),
    GUIDE_OPEN_SETTING_SOUND("block.note.block.harp", 0.7F, 0.7F),
    GRIND_STONE_INTERACT_SOUND("block.wooden.button.click.on", 1F, 1F),
    IGNITION_CHAMBER_USE_FLINT_AND_STEEL_SOUND("entity.item.break", 1F, 1F),
    INFUSED_HOPPER_TELEPORT_SOUND("entity.enderman.teleport", 0.5F, 2F),
    INFUSED_MAGNET_TELEPORT_SOUND("entity.enderman.teleport", 0.25F, 0.9F),
    IRON_GOLEM_ASSEMBLER_ASSEMBLE_SOUND("entity.iron.golem.repair", 0.5F, 1F),
    JETBOOTS_THRUST_SOUND("entity.tnt.primed", 0.25F, 1F),
    JETPACK_THRUST_SOUND("entity.generic.explode", 0.25F, 1F),
    JUICER_USE_SOUND("entity.player.splash", 1F, 1F),
    LIMITED_USE_ITEM_BREAK_SOUND("entity.item.break", 1F, 1F),
    MAGICAL_EYE_OF_ENDER_USE_SOUND("entity.enderman.teleport", 1F, 1F),
    MAGIC_SUGAR_CONSUME_SOUND("entity.generic.eat", 1F, 1F),
    MAGIC_WORKBENCH_FINISH_SOUND("entity.arrow.hit.player", 1F, 1F),
    MAGIC_WORKBENCH_START_ANIMATION_SOUND("block.wooden.button.click.on", 1F, 1F),
    MINER_ANDROID_BLOCK_GENERATION_SOUND("block.fire.extinguish", 0.075F, 0.8F),
    MINING_TASK_SOUND("entity.arrow.hit.player", 0.2F, 1F),
    ORE_WASHER_WASH_SOUND("entity.player.splash", 1F, 1F),
    PLAYER_RESEARCHING_SOUND("entity.bat.takeoff", 0.7F, 1F),
    PORTABLE_DUSTBIN_OPEN_SOUND("block.anvil.land", 1F, 1F),
    PORTABLE_CRAFTER_OPEN_SOUND("block.wooden.button.click.on", 1F, 1F),
    PRESSURE_CHAMBER_FINISH_SOUND("entity.arrow.hit.player", 1F, 1F),
    PRESSURE_CHAMBER_WORKING_SOUND("entity.tnt.primed", 1F, 1F),
    PROGRAMMABLE_ANDROID_SCRIPT_DOWNLOAD_SOUND("block.note.block.hat", 0.7F, 0.7F),
    SLIME_BOOTS_FALL_SOUND("block.slime.block.fall", 1F, 1F),
    TELEPORTATION_MANAGER_OPEN_GUI("ui.button.click", 1F, 1F),
    GPS_NETWORK_ADD_WAYPOINT("block.note.block.pling", 1F, 1F),
    GPS_NETWORK_CREATE_WAYPOINT("block.note.block.pling", 0.5F, 1F),
    GPS_NETWORK_OPEN_PANEL_SOUND("ui.button.click", 1F, 1F),
    SMELTERY_CRAFT_SOUND("block.lava.pop", 1F, 1F),
    SOULBOUND_RUNE_RITUAL_SOUND("entity.generic.explode", 0.3F, 1F),
    SPLINT_CONSUME_SOUND("entity.skeleton.hurt", 1F, 1F),
    STOMPER_BOOTS_STOMP_SOUND("entity.zombie.break.wooden.door", 1F, 2F),
    TAPE_MEASURE_MEASURE_SOUND("item.book.put", 1, 0.7F),
    TOME_OF_KNOWLEDGE_USE_SOUND("entity.player.levelup", 1F, 1F),
    TELEPORT_UPDATE_SOUND("block.beacon.ambient", 1F, 0.6F),
    TELEPORT_SOUND("block.beacon.activate", 1F, 1F),
    VAMPIRE_BLADE_HEALING_SOUND("entity.arrow.hit.player", 0.7F, 0.7F),
    VANILLA_AUTO_CRAFTER_UPDATE_RECIPE_SOUND("ui.button.click", 1F, 1F),
    VILLAGER_RUNE_TRANSFORM_SOUND("entity.villager.celebrate", 1F, 1.4F),
    VITAMINS_CONSUME_SOUND("entity.generic.eat", 1F, 1F),
    WIND_STAFF_USE_SOUND("entity.tnt.primed", 1F, 1F);

    private final String defaultSound;
    private final float defaultVolume;
    private final float defaultPitch;

    SoundEffect(@Nonnull String sound, float volume, float pitch) {
        Preconditions.checkNotNull(sound, "The Sound id cannot be null!");
        Preconditions.checkArgument(volume >= 0, "The volume cannot be a negative number.");
        Preconditions.checkArgument(pitch >= 0.5, "A pitch below 0.5 has no effect on the sound.");

        this.defaultSound = sound;
        this.defaultVolume = volume;
        this.defaultPitch = pitch;
    }


    private @Nullable SoundConfiguration getConfiguration() {
        SoundConfiguration config = Slimefun.getSoundService().getConfiguration(this);

        if (config == null) {
            // This should not happen. But if it does... send a warning
            Slimefun.logger().log(Level.WARNING, "Could not find any sound configuration for: {0}", name());
        }

        return config;
    }

    /**
     * This method will play this {@link SoundEffect} only to the given {@link Player} using the
     * eye {@link Location} of the {@link Player} and the {@link SoundCategory} {@code PLAYERS}.
     *
     * @param player The {@link Player} which to play the {@link Sound} to.
     */
    public void playFor(@Nonnull Player player) {
        Preconditions.checkNotNull(player, "Cannot play sounds to a Player that is null!");
        SoundConfiguration config = getConfiguration();

        if (config != null) {
            Location loc = player.getEyeLocation();
            player.playSound(loc, config.getSoundId(), SoundCategory.PLAYERS, config.getVolume(), config.getPitch());
        }
    }

    /**
     * This method will play this {@link SoundEffect} at the given {@link Location} using the
     * provided {@link SoundCategory}.
     *
     * @param loc      The {@link Location} at which to play the {@link SoundEffect}.
     * @param category The {@link SoundCategory} that should be used.
     */
    public void playAt(@Nonnull Location loc, @Nonnull SoundCategory category) {
        Preconditions.checkNotNull(loc, "The location should not be null.");
        SoundConfiguration config = getConfiguration();

        if (config != null && loc.getWorld() != null) {
            loc.getWorld().playSound(loc, config.getSoundId(), category, config.getVolume(), config.getPitch());
        }
    }

    /**
     * This method will play this {@link SoundEffect} at the {@link Location} of the given {@link Block},
     * the used {@link SoundCategory} will be {@code BLOCKS}.
     *
     * @param block The {@link Block} at which to play the {@link SoundEffect}
     */
    public void playAt(@Nonnull Block block) {
        Preconditions.checkNotNull(block, "The block cannot be null.");
        playAt(block.getLocation(), SoundCategory.BLOCKS);
    }

    /**
     * This returns the default sound id.
     *
     * @return The default sound id.
     */
    public @Nonnull String getDefaultSoundId() {
        return defaultSound;
    }

    /**
     * This returns the default volume.
     *
     * @return The default volume.
     */
    public float getDefaultVolume() {
        return defaultVolume;
    }

    /**
     * This returns the default pitch.
     *
     * @return The default pitch.
     */
    public float getDefaultPitch() {
        return defaultPitch;
    }
}
