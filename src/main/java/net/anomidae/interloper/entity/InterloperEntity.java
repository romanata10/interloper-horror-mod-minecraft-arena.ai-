package net.anomidae.interloper.entity;

import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.world.SourceVoid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * The man with the umbrella. He is tall, he is black, and he has decided that
 * walls are a suggestion.
 *
 * He flies at exactly one player, and he always removes himself afterwards:
 * either he took you to the void, or he crashed you out. He never stays behind
 * to do it again the moment you reconnect.
 */
public class InterloperEntity extends SourceEntity {
    public static final String VOID_FLAG = "interloper.void_entered";

    private static final EntityDataAccessor<Boolean> DATA_HUNTING =
            SynchedEntityData.defineId(InterloperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_STARE =
            SynchedEntityData.defineId(InterloperEntity.class, EntityDataSerializers.INT);

    /** He gives up after three minutes even if he never reaches you. */
    private static final int MAX_LIFE_TICKS = 20 * 180;

    private int lifeTicks;
    private int soundCooldown;
    private boolean finished;

    public InterloperEntity(EntityType<? extends InterloperEntity> type, Level level) {
        super(type, level);
        this.xpReward = 80;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 20.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_HUNTING, Boolean.TRUE);
        this.entityData.define(DATA_STARE, 0);
    }

    public boolean isHunting() {
        return this.entityData.get(DATA_HUNTING);
    }

    public void setHunting(boolean hunting) {
        this.entityData.set(DATA_HUNTING, hunting);
    }

    public int stareTicks() {
        return this.entityData.get(DATA_STARE);
    }

    protected double flightSpeed() {
        return 0.26D;
    }

    @Override
    protected void serverTick() {
        if (this.finished) {
            return;
        }

        this.lifeTicks++;
        if (this.soundCooldown > 0) {
            this.soundCooldown--;
        }
        if (this.lifeTicks > MAX_LIFE_TICKS) {
            this.abandonHunt();
            return;
        }

        ServerPlayer player = this.resolveTarget(160.0D);
        if (player == null) {
            // target is dead, offline or left the dimension - handled by resolveTarget
            return;
        }

        double distance = this.distanceTo(player);
        this.setHunting(true);
        this.getLookControl().setLookAt(player, 90.0F, 90.0F);
        this.entityData.set(DATA_STARE, isWatched(player, this) ? 1 : 0);

        if (this.soundCooldown <= 0 && distance < 40.0D) {
            this.soundCooldown = 60 + this.random.nextInt(60);
            this.level().playSound(null, this.blockPosition(), ModSounds.STATIC.get(),
                    SoundSource.HOSTILE, 1.0F, 0.35F);
        }

        Vec3 target = player.getEyePosition().add(0.0D, -0.4D, 0.0D);
        Vec3 here = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 direction = target.subtract(here);
        if (direction.length() > 0.6D) {
            Vec3 step = direction.normalize().scale(flightSpeed());
            this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);
        }
        this.setDeltaMovement(Vec3.ZERO);

        if (this.tickCount % 40 == 0) {
            this.entityData.set(DATA_STARE, 0);
        }

        if (touching(player)) {
            this.onTouch(player);
        }
    }

    @Override
    protected boolean touching(ServerPlayer player) {
        return this.getBoundingBox().inflate(0.5D).intersects(player.getBoundingBox());
    }

    protected void onTouch(ServerPlayer player) {
        if (this.finished) {
            return;
        }
        if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
            this.finish();
            return;
        }

        boolean inVoid = this.level().dimension().equals(SourceVoid.LEVEL);
        boolean wasInVoid = player.getPersistentData().getBoolean(VOID_FLAG);

        if (!inVoid && !wasInVoid) {
            // the first meeting: he takes you somewhere else
            player.getPersistentData().putBoolean(VOID_FLAG, true);
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CORRUPT, 100, 1.0F));
            this.level().playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(),
                    SoundSource.HOSTILE, 1.0F, 0.4F);
            SourceVoid.enter(player);
            this.finish();
            return;
        }

        if (Hunters.isOnCrashCooldown(player)) {
            // you were already crashed out recently - he loses interest and is gone
            ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 40, 0.8F));
            this.level().playSound(null, player.blockPosition(), ModSounds.STATIC.get(),
                    SoundSource.AMBIENT, 0.8F, 0.4F);
            this.finish();
            return;
        }

        crashPlayer(player, "error:crash_by-error");
    }

    /**
     * Ends the session the way the ARG ends it - and takes the Interloper with it.
     */
    protected void crashPlayer(ServerPlayer player, String message) {
        Hunters.startCrashCooldown(player);
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CRASH_TEXT, 80, 1.0F));
        this.level().playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(),
                SoundSource.HOSTILE, 1.0F, 0.3F);

        // he disappears first, then the game throws the player out
        this.finish();
        Hunters.discardBoundTo(player);

        ServerScheduler.schedule(25, () -> {
            if (Hunters.isOnline(player) && player.connection != null) {
                player.connection.disconnect(Component.literal(message));
            }
        });
    }

    protected void finish() {
        this.finished = true;
        this.setHunting(false);
        Hunters.forget(this);
        this.discard();
    }

    /**
     * Removes every Interloper that is currently hunting this player.
     */
    public static void vanishFrom(ServerPlayer player) {
        Hunters.discardBoundTo(player);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (target instanceof ServerPlayer player) {
            this.onTouch(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // It cannot be killed, only avoided.
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(new ItemStack(ModItems.SKYBOX_FRAGMENT.get(), 1));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Hunting", this.isHunting());
        tag.putInt("Life", this.lifeTicks);
        tag.putBoolean("Finished", this.finished);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setHunting(tag.getBoolean("Hunting"));
        this.lifeTicks = tag.getInt("Life");
        this.finished = tag.getBoolean("Finished");
    }

    public static boolean checkSpawnRules(EntityType<InterloperEntity> type,
                                          net.minecraft.world.level.ServerLevelAccessor level,
                                          net.minecraft.world.entity.MobSpawnType reason,
                                          net.minecraft.core.BlockPos pos,
                                          net.minecraft.util.RandomSource random) {
        return true;
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

    public static boolean isWatched(net.minecraft.world.entity.player.Player player, Entity entity) {
        Vec3 eye = player.getEyePosition();
        Vec3 target = entity.position().add(0.0D, entity.getBbHeight() * 0.65D, 0.0D);
        Vec3 toEntity = target.subtract(eye);
        double length = toEntity.length();
        if (length < 0.5D || length > 90.0D) {
            return false;
        }
        return player.getViewVector(1.0F).dot(toEntity.scale(1.0D / length)) > 0.965D;
    }
}
