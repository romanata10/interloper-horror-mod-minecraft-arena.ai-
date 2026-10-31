package net.anomidae.interloper.block;

import net.anomidae.interloper.world.WhiteDoor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A hole in a floor that is not a hole. Falling into it puts you in white_door_bsp.
 */
public class FallHoleBlock extends Block {
    private static final VoxelShape HOLE = Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D);

    public FallHoleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level,
                                        BlockPos pos, CollisionContext context) {
        return HOLE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (player.getY() > pos.getY() + 1.1D) {
            return;
        }
        WhiteDoor.enter(player);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return true;
    }
}
