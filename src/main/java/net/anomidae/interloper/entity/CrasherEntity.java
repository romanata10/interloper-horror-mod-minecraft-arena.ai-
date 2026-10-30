package net.anomidae.interloper.entity;

import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * A villager whose rig was never finished. It follows you through everything.
 */
public class CrasherEntity extends SourceEntity {
    public CrasherEntity(EntityType<? extends CrasherEntity> type, Level level) {
        super(type, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MonsterAttributes.base()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.12D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void serverTick() {
        var player = this.nearestPlayer(64.0D);
        if (player == null) {
            return;
        }
        double dx = player.getX() - this.getX();
        double dz = player.getZ() - this.getZ();
        double dy = player.getY() + 0.2D - this.getY();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal > 0.2D) {
            double speed = this.getAttributeValue(Attributes.MOVEMENT_SPEED);
            this.setPos(this.getX() + dx / horizontal * speed,
                    this.getY() + Math.max(-0.2D, Math.min(0.2D, dy * 0.2D)),
                    this.getZ() + dz / horizontal * speed);
        }
        if (this.random.nextInt(200) == 0) {
            this.level().playSound(null, this.blockPosition(), ModSounds.WHISPER.get(), SoundSource.HOSTILE, 0.6F, 0.35F);
        }
        if (this.touching(player)) {
            hit(player);
        }
    }

    private void hit(ServerPlayer player) {
        ModNetwork.toPlayer(player, new ScreenEffectPacket(ScreenEffectPacket.Effect.CRASH_TEXT, 100, 1.0F));
        player.level().playSound(null, player.blockPosition(), ModSounds.CAPTURE.get(), SoundSource.HOSTILE, 1.0F, 0.4F);
        player.hurt(player.level().damageSources().genericKill(), 1000.0F);
        this.discard();
    }
}
