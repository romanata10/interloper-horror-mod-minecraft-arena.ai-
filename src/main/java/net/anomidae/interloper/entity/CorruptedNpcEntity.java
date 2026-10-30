package net.anomidae.interloper.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * A zombie that was never given a pose or a direction. It slides through walls
 * without ever turning around.
 */
public class CorruptedNpcEntity extends SourceEntity {
    private double dirX;
    private double dirZ;
    private int directionTimer;

    public CorruptedNpcEntity(EntityType<? extends CorruptedNpcEntity> type, Level level) {
        super(type, level);
        RandomSource random = level.getRandom();
        this.dirX = random.nextDouble() - 0.5D;
        this.dirZ = random.nextDouble() - 0.5D;
        this.directionTimer = 40 + random.nextInt(100);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MonsterAttributes.base()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.12D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void serverTick() {
        if (--this.directionTimer <= 0) {
            this.directionTimer = 40 + this.random.nextInt(140);
            this.dirX = this.random.nextDouble() - 0.5D;
            this.dirZ = this.random.nextDouble() - 0.5D;
        }

        double length = Math.sqrt(this.dirX * this.dirX + this.dirZ * this.dirZ);
        if (length < 0.05D) {
            this.dirX = 0.0D;
            this.dirZ = 0.0D;
        } else {
            this.dirX /= length;
            this.dirZ /= length;
            this.setPos(this.getX() + this.dirX * 0.115D, this.getY(), this.getZ() + this.dirZ * 0.115D);
        }

        this.snapToGround(12);
        // The body never turns: no yaw updates, no head tracking, no animation.
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return true;
    }
}
