package net.anomidae.interloper.entity;

import net.anomidae.interloper.util.Bindings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * dm_npc. An enderman that learned how to walk properly, and is therefore
 * hated by everything in the level.
 */
public class DmNpcEntity extends Monster {
    private static final String FOLLOWING = "DmFollowing";

    private boolean following;
    private java.util.UUID owner;

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
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.2D, true));
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
        if (this.following) {
            this.followOwner();
        }
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

    /**
     * dm_editor: one right click and the exported npc works for you.
     * Returns whether it is following after the call.
     */
    public boolean toggleFollowing(ServerPlayer player) {
        if (this.following && player.getUUID().equals(this.owner)) {
            this.following = false;
            this.owner = null;
            return false;
        }
        this.following = true;
        this.owner = player.getUUID();
        this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.NEUTRAL, 0.6F, 1.4F);
        return true;
    }

    public boolean isFollowing() {
        return this.following;
    }

    /**
     * The npc walks to its owner and attacks whatever is attacking them.
     */
    private void followOwner() {
        ServerPlayer player = Bindings.online(this, this.owner);
        if (player == null || !player.isAlive()) {
            this.following = false;
            return;
        }

        // defend: anything with a target set to the owner gets hit first
        if (this.tickCount % 10 == 0) {
            LivingEntity attacker = null;
            for (Monster monster : this.level().getEntitiesOfClass(Monster.class,
                    this.getBoundingBox().inflate(24.0D))) {
                if (monster == this || !monster.isAlive()) {
                    continue;
                }
                if (monster.getTarget() == player) {
                    attacker = monster;
                    break;
                }
            }
            if (attacker != null) {
                this.setTarget(attacker);
                if (this.distanceTo(attacker) > 22.0D) {
                    this.randomTeleport(attacker.getX(), attacker.getY(), attacker.getZ(), false);
                    this.level().playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                            SoundSource.NEUTRAL, 0.7F, 1.2F);
                }
            }
        }

        // follow
        double distance = this.distanceTo(player);
        if (distance > 22.0D) {
            this.randomTeleport(player.getX(), player.getY(), player.getZ(), false);
        } else if (distance > 6.0D) {
            Vec3 step = player.position().subtract(this.position()).normalize().scale(0.22D);
            this.setPos(this.getX() + step.x, this.getY(), this.getZ() + step.z);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        if (this.following && target instanceof ServerPlayer) {
            return false;
        }
        return super.doHurtTarget(target);
    }

    /**
     * A followed npc does not run away from its owner.
     */
    public void feedBread() {
        this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EAT,
                SoundSource.NEUTRAL, 0.8F, 1.2F);
    }

    public boolean wantsBread() {
        return this.getMainHandItem().is(Items.BREAD);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DmLife", this.tickCount);
        tag.putBoolean(FOLLOWING, this.following);
        if (this.owner != null) {
            tag.putUUID("DmOwner", this.owner);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.following = tag.getBoolean(FOLLOWING);
        if (tag.hasUUID("DmOwner")) {
            this.owner = tag.getUUID("DmOwner");
        }
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
