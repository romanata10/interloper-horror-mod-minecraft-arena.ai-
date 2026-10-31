package net.anomidae.interloper.block;

import net.anomidae.interloper.world.Graffiti;
import net.anomidae.interloper.world.SkyboxState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A man with an umbrella, painted on a wall that nobody remembers painting.
 * While this block exists, the Interloper can find his way to you.
 */
public class GraffitiBlock extends Block {
    public GraffitiBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
                        BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel server) {
            SkyboxState.get(server).setGraffitiPos(pos);
            Graffiti.announceToAllNearby(server, pos);
        }
    }

    @Override
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
                         BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            SkyboxState.get(server).removeRavenCheck(pos);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
