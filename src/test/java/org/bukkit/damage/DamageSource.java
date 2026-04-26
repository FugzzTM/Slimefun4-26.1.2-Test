package org.bukkit.damage;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Objects;

public interface DamageSource {

    DamageType getDamageType();

    Entity getCausingEntity();

    Entity getDirectEntity();

    Location getDamageLocation();

    Location getSourceLocation();

    boolean isIndirect();

    float getFoodExhaustion();

    boolean scalesWithDifficulty();

    static Builder builder(DamageType type) {
        return new BuilderImpl(type);
    }

    interface Builder {
        Builder withCausingEntity(Entity entity);

        Builder withDirectEntity(Entity entity);

        Builder withDamageLocation(Location location);

        DamageSource build();
    }

    final class BuilderImpl implements Builder {
        private final DamageType type;
        private Entity causingEntity;
        private Entity directEntity;
        private Location damageLocation;

        BuilderImpl(DamageType type) {
            this.type = Objects.requireNonNull(type, "type");
        }

        @Override
        public Builder withCausingEntity(Entity entity) {
            this.causingEntity = entity;
            return this;
        }

        @Override
        public Builder withDirectEntity(Entity entity) {
            this.directEntity = entity;
            return this;
        }

        @Override
        public Builder withDamageLocation(Location location) {
            this.damageLocation = location;
            return this;
        }

        @Override
        public DamageSource build() {
            return new SimpleDamageSource(type, causingEntity, directEntity, damageLocation);
        }
    }

    final class SimpleDamageSource implements DamageSource {
        private final DamageType type;
        private final Entity causingEntity;
        private final Entity directEntity;
        private final Location damageLocation;

        SimpleDamageSource(DamageType type, Entity causingEntity, Entity directEntity, Location damageLocation) {
            this.type = type;
            this.causingEntity = causingEntity;
            this.directEntity = directEntity;
            this.damageLocation = damageLocation;
        }

        @Override
        public DamageType getDamageType() {
            return type;
        }

        @Override
        public Entity getCausingEntity() {
            return causingEntity;
        }

        @Override
        public Entity getDirectEntity() {
            return directEntity;
        }

        @Override
        public Location getDamageLocation() {
            return damageLocation;
        }

        @Override
        public Location getSourceLocation() {
            return damageLocation;
        }

        @Override
        public boolean isIndirect() {
            return causingEntity != null && causingEntity != directEntity;
        }

        @Override
        public float getFoodExhaustion() {
            return 0F;
        }

        @Override
        public boolean scalesWithDifficulty() {
            return false;
        }
    }
}
