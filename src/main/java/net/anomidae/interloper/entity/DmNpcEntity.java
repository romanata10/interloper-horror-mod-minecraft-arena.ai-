package net.anomidae.interloper.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * dm_npc. An enderman that learned how to walk properly, and is therefore
 * hated by everything in the level.
 */
public class DmNpcEntity extends Monster {
    public DmNpcEntity(EntityType<? extends DmNpcEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        this.checkLifetime();
        if (this.tickCount % 20 != 0) {
            return;
        }
        // Everything aggressive in the level wants it dead.
        for (Monster monster : this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(24.0D))) {
            if (monster == this || !monster.isAlive()) {
                continue;
            }
            LivingEntity target = monster.getTarget();
            if (target instanceof DmNpcEntity) {
                continue;
            }
            if (this.random.nextInt(3) == 0) {
                monster.setTarget(this);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DmLife", this.tickCount);
    }

    /**
     * It is not immortal: it goes away after five minutes or when nobody is around.
     */
    private void checkLifetime() {
        if (this.tickCount > 20 * 300) {
            this.discard();
        }
    }
}
