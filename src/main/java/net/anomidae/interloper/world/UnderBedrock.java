package net.anomidae.interloper.world;

import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * What happens under the bedrock.
 *
 * Bedrock is breakable, so a player can dig through the bottom of the overworld
 * and fall out of the map. The fall itself does not kill: on the way down there
 * is one coin flip. Either the game pulls you into the Source Void, or an
 * Interloper is already falling next to you and that is how the fall ends.
 */
public final class UnderBedrock {
    private static final int ROLL_Y_OFFSET = -8;
    private static final Set<UUID> ROLLED = new HashSet<>();

    public static void reset() {
        ROLLED.clear();
    }

    /**
     * Anti damage: while a player is under the world we hold their fall and their
     * void damage, because the decision is made by the coin, not by the fall.
     */
    public static boolean holdsDamage(ServerPlayer player) {
        return player.level().getMinBuildHeight() - player.getY() > 0
                && !Dimensions.isVoidLevel(player);
    }

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return;
        }
        int floor = level.getMinBuildHeight();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isCreative()) {
                continue;
            }
            if (player.getY() > floor + ROLL_Y_OFFSET) {
                ROLLED.remove(player.getUUID());
                continue;
            }
            if (!ROLLED.add(player.getUUID())) {
                continue;
            }
            roll(level, player);
        }
    }

    private static void roll(ServerLevel level, ServerPlayer player) {
        player.resetFallDistance();
        boolean toVoid = level.getRandom().nextBoolean();

        if (toVoid) {
            ModNetwork.toPlayer(player, new ConsoleLinePacket("fall_shadow: moving to interloper:source_void",
                    0xFF8A8A8A, false));
            SourceVoid.enter(player);
            level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL,
                    SoundSource.PLAYERS, 0.6F, 0.5F);
            return;
        }

        player.resetFallDistance();
        launchHunter(level, player);
    }

    /**
     * The other half of the coin: he is already in the dark with you, and the fall
     * ends when he reaches you.
     */
    private static void launchHunter(ServerLevel level, ServerPlayer player) {
        InterloperEntity interloper = ModEntities.INTERLOPER.get().create(level);
        if (interloper == null) {
            SourceVoid.enter(player);
            return;
        }
        Vec3 under = new Vec3(player.getX(), level.getMinBuildHeight() - 24.0D, player.getZ());
        interloper.moveTo(under.x, under.y, under.z, 0.0F, 0.0F);
        interloper.bindPlayer(player.getUUID());
        interloper.setTarget(player);
        interloper.setHunting(true);
        level.addFreshEntity(interloper);

        ModNetwork.toPlayer(player, new ConsoleLinePacket("you are not alone down here", 0xFFFF0000, true));
        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN,
                SoundSource.HOSTILE, 0.7F, 0.4F);
    }

    /**
     * Called from the block break hook: bedrock is not supposed to be permanent.
     */
    public static boolean isBedrock(BlockState state) {
        return state.is(Blocks.BEDROCK);
    }

    public static void noteBroken(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.8F, 0.5F);
    }

    private UnderBedrock() {
    }
}
