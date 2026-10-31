package net.anomidae.interloper.block;

import net.anomidae.interloper.world.SkyboxState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * RAVENCHECK.EXE - a little terminal that watches one wall and tells the chat
 * whether the Interloper has a reason to visit today.
 */
public class RavenCheckBlock extends Block {
    public RavenCheckBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (level instanceof ServerLevel server) {
            SkyboxState.get(server).addRavenCheck(pos);
            Player nearest = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 16.0D, false);
            if (nearest instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.literal("ravencheck.exe: registered wall monitor")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel server) {
            SkyboxState.get(server).removeRavenCheck(pos);
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
