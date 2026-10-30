package net.anomidae.interloper.entity;

import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.util.SourceText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A red error that is looking for a place to happen. It sees through everything.
 * One hit and it is gone - it does not linger and it does not follow you
 * to the next session.
 */
public class VradEntity extends SourceEntity {
    private int beepTimer;
    private boolean spent;

    public VradEntity(EntityType<? extends VradEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MonsterAttributes.base()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 96.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void serverTick() {
        if (this.spent) {
            return;
        }
        ServerPlayer player = this.resolveTarget(96.0D);
        if (player == null) {
            return;
        }

        Vec3 target = player.position().add(0.0D, player.getBbHeight() * 0.6D, 0.0D);
        Vec3 here = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        Vec3 direction = target.subtract(here);
        double distance = direction.length();
        if (distance > 0.4D) {
            Vec3 step = direction.normalize().scale(0.215D);
            this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);
        }

        if (--this.beepTimer <= 0) {
            this.beepTimer = 40 + this.random.nextInt(50);
            this.level().playSound(null, this.blockPosition(), ModSounds.STATIC.get(), SoundSource.HOSTILE, 0.5F, 1.8F);
            if (distance < 32.0D) {
                ModNetwork.toPlayer(player, new ConsoleLinePacket(SourceText.randomError(this.random), 0xFFFF7A00, false));
            }
        }

        if (distance < 1.6D) {
            this.hit(player);
        }
    }

    private void hit(ServerPlayer player) {
        this.spent = true;
        DamageSource source = this.damageSources().mobAttack(this);

        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.VRAD_ERROR, 120, 1.0F));
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 120, 1.0F));
        for (int i = 0; i < 5; i++) {
            ServerScheduler.schedule(6 + i * 8, () -> {
                if (Hunters.isOnline(player)) {
                    ModNetwork.toPlayer(player, new ConsoleLinePacket(
                            SourceText.randomError(player.getRandom()), 0xFFFF7A00, true));
                }
            });
        }
        ServerScheduler.schedule(70, () -> {
            if (player.isAlive()) {
                player.hurt(source, 1000.0F);
            }
        });
        player.sendSystemMessage(Component.literal("error: vrad_collision").withStyle(ChatFormatting.RED));

        // the error deletes itself with the player
        Hunters.forget(this);
        this.discard();
    }
}
