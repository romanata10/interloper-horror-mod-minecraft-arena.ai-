package net.anomidae.interloper.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * A character from a Source game that was exported with the wrong skeleton.
 * It stands where it was put down and never moves, never looks at you and
 * never does anything at all.
 */
public class BrokenCharacterEntity extends Monster {
    public static final int VARIANTS = 4;

    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(BrokenCharacterEntity.class, EntityDataSerializers.INT);

    public BrokenCharacterEntity(EntityType<? extends BrokenCharacterEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 0.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT, 0);
    }

    public int variant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, Math.max(0, variant % VARIANTS));
    }

    @Override
    protected void registerGoals() {
        // It stands.
    }

    @Override
    public void tick() {
        // Deliberately not calling super: no movement, no looking, no AI.
        if (this.tickCount == 0 && !this.level().isClientSide) {
            this.setVariant(this.random.nextInt(VARIANTS));
        }
        this.tickCount++;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BrokenVariant", this.variant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setVariant(tag.getInt("BrokenVariant"));
    }
}
