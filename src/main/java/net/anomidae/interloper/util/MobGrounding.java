package net.anomidae.interloper.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Entities that ignore walls still have to believe there is a floor.
 */
public final class MobGrounding {
    /**
     * Returns the y coordinate of the surface below the entity, or the entity y if nothing was found.
     */
    public static double floorY(Level level, Entity entity, int maxDrop) {
        BlockPos.MutableBlockPos pos = BlockPos.MutableBlockPos.containing(entity.getX(), entity.getY(), entity.getZ());
        for (int i = 0; i < maxDrop; i++) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.isCollisionShapeFullBlock(level, pos)) {
                return pos.getY() + 1.0D;
            }
            pos.move(0, -1, 0);
        }
        return entity.getY();
    }

    public static void snap(Entity entity, int maxDrop) {
        double y = floorY(entity.level(), entity, maxDrop);
        if (Math.abs(y - entity.getY()) > 0.05D) {
            entity.setPos(entity.getX(), y, entity.getZ());
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, 0.0D, motion.z);
        }
    }

    private MobGrounding() {
    }
}
