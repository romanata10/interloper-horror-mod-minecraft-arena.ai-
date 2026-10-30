package net.anomidae.interloper.entity;

import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.ModDamage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

/**
 * "The Umbrella Man". It follows you. It does not like being looked at.
 */
public class InterloperEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DATA_HUNTING =
            SynchedEntityData.defineId(InterloperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STARE =
            SynchedEntityData.defineId(InterloperEntity.class, EntityDataSerializers.INT);

    private int repositionCooldown = 80;
    private int lifeTicks;
    private int soundCooldown;

    public InterloperEntity(EntityType<? extends InterloperEntity> type, Level level) {
        super(type, level);
        this.xpReward = 80;
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 120.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, true));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 64.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HUNTING, Boolean.FALSE);
        this.entityData.define(DATA_STARE, 0);
    }

    public boolean isHunting() {
        return this.entityData.get(DATA_HUNTING);
    }

    public void setHunting(boolean hunting) {
        this.entityData.set(DATA_HUNTING, hunting);
        if (hunting) {
            this.setPersistenceRequired();
        }
    }

    public int stareTicks() {
        return this.entityData.get(DATA_STARE);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.isOnFire()) {
            this.clearFire();
        }

        if (this.level().isClientSide) {
            return;
        }

        this.lifeTicks++;
        if (this.soundCooldown > 0) {
            this.soundCooldown--;
        }

        Player nearest = this.level().getNearestPlayer(this, 80.0D);
        if (this.getTarget() == null && nearest != null && nearest.isAlive() && !nearest.isCreative()) {
            this.setTarget(nearest);
        }
        this.setHunting(this.getTarget() != null);

        if (!(this.level() instanceof ServerLevel serverLevel) || nearest == null) {
            return;
        }

        if (--this.repositionCooldown <= 0) {
            this.repositionCooldown = 70 + this.random.nextInt(90);
            this.handleStalk(serverLevel, nearest);
        }
    }

    private void handleStalk(ServerLevel level, Player player) {
        double distance = this.distanceTo(player);
        boolean watched = isWatched(player, this);

        if (watched) {
            this.getNavigation().stop();
            this.entityData.set(DATA_STARE, this.stareTicks() + 1);

            if (this.soundCooldown <= 0) {
                this.soundCooldown = 60;
                level.playSound(null, this.blockPosition(), ModSounds.STATIC.get(), SoundSource.HOSTILE, 0.9F, 0.45F);
            }

            if (distance < 14.0D) {
                // it refuses to be observed for long
                if (player instanceof ServerPlayer serverPlayer) {
                    ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 50, 0.8F));
                    ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 40, 0.6F));
                }
                this.vanish(level, player);
            }
            return;
        }

        this.entityData.set(DATA_STARE, 0);

        if (!this.isHunting()) {
            return;
        }

        if (distance > 44.0D) {
            this.teleportBehind(level, player, 18.0D, 26.0D);
        } else if (distance > 16.0D && this.random.nextInt(3) == 0) {
            this.teleportBehind(level, player, 9.0D, 14.0D);
        }
    }

    private void vanish(ServerLevel level, Player player) {
        this.getNavigation().stop();
        this.teleportBehind(level, player, 26.0D, 36.0D);
        this.repositionCooldown = 200 + this.random.nextInt(120);
    }

    private void teleportBehind(ServerLevel level, Player player, double min, double max) {
        Vec3 look = player.getViewVector(1.0F);
        double distance = min + this.random.nextDouble() * (max - min);
        double x = player.getX() - look.x * distance;
        double z = player.getZ() - look.z * distance;
        double y = player.getY();

        if (this.randomTeleport(x, y, z, false)) {
            this.getNavigation().stop();
            level.playSound(null, this.blockPosition(), ModSounds.STATIC.get(), SoundSource.HOSTILE, 0.7F, 0.6F);
        }
    }

    public static boolean isWatched(Player player, Entity entity) {
        Vec3 eye = player.getEyePosition();
        Vec3 target = entity.position().add(0.0D, entity.getBbHeight() * 0.65D, 0.0D);
        Vec3 toEntity = target.subtract(eye);
        double length = toEntity.length();
        if (length < 0.5D || length > 72.0D) {
            return false;
        }
        return player.getViewVector(1.0F).dot(toEntity.scale(1.0D / length)) > 0.965D;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            LivingEntity target = this.getTarget();
            speed.setBaseValue(target != null ? 0.30D : 0.21D);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof ServerPlayer player && !player.isCreative()) {
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CAPTURE, 70, 1.0F));
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 120, 1.0F));
            this.level().playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(), SoundSource.HOSTILE, 1.0F, 0.75F);
            player.hurt(ModDamage.interloped(this.level()), 4.0F);
        }
        return hit;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.SKYBOX_FRAGMENT.get(), 1 + this.random.nextInt(2)));
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.level().playSound(null, this.blockPosition(), ModSounds.INTERLOPER_DEATH.get(),
                    SoundSource.HOSTILE, 1.0F, 0.6F);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.INTERLOPER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.INTERLOPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.INTERLOPER_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 1.1F;
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("InterloperLife", this.lifeTicks);
        tag.putBoolean("InterloperHunting", this.isHunting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lifeTicks = tag.getInt("InterloperLife");
        this.setHunting(tag.getBoolean("InterloperHunting"));
    }

    public static boolean checkSpawnRules(EntityType<InterloperEntity> type, ServerLevelAccessor level,
                                          MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (reason == MobSpawnType.SPAWN_EGG || reason == MobSpawnType.COMMAND || reason == MobSpawnType.EVENT) {
            return true;
        }
        return level.getLevel().isNight()
                && random.nextInt(10) == 0
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
