package io.papermc.paper;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Pose;

import be.seeseemelk.mockbukkit.registry.RegistryAccessMock;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import io.papermc.paper.world.damagesource.CombatEntry;
import io.papermc.paper.world.damagesource.FallLocationType;

import net.kyori.adventure.text.Component;

public final class MockInternalAPIBridge implements InternalAPIBridge {

    private static final RegistryAccessMock REGISTRY_ACCESS = new RegistryAccessMock();

    @Override
    public DamageEffect getDamageEffect(String key) {
        return null;
    }

    @Override
    public Biome constructLegacyCustomBiome() {
        return REGISTRY_ACCESS.getRegistry(io.papermc.paper.registry.RegistryKey.BIOME).get(NamespacedKey.minecraft("custom"));
    }

    @Override
    public CombatEntry createCombatEntry(LivingEntity livingEntity, DamageSource damageSource, float damage) {
        return null;
    }

    @Override
    public CombatEntry createCombatEntry(DamageSource damageSource, float damage, FallLocationType fallLocationType, float fallDamage) {
        return null;
    }

    @Override
    public Predicate<CommandSourceStack> restricted(Predicate<CommandSourceStack> predicate) {
        return predicate;
    }

    @Override
    public ResolvableProfile defaultMannequinProfile() {
        return null;
    }

    @Override
    public com.destroystokyo.paper.SkinParts.Mutable allSkinParts() {
        return null;
    }

    @Override
    public Component defaultMannequinDescription() {
        return Component.empty();
    }

    @Override
    public <MODERN, LEGACY> GameRule<LEGACY> legacyGameRuleBridge(GameRule<MODERN> gameRule, Function<LEGACY, MODERN> toModern, Function<MODERN, LEGACY> toLegacy, Class<LEGACY> legacyType) {
        return (GameRule<LEGACY>) gameRule;
    }

    @Override
    public Set<Pose> validMannequinPoses() {
        return Collections.emptySet();
    }
}

