package net.anomidae.interloper.world;

import net.anomidae.interloper.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;

/**
 * The graffiti of the man with the umbrella. It appears on the wall between two
 * houses, and every day the town decides whether it is there or not.
 *
 * Graffiti present: the Interloper can find you at night.
 * Wall clean: he does not exist, no matter how long you wait.
 */
public final class Graffiti {
    public static final int RADIUS = 48;

    public static boolean isActive(ServerLevel level) {
        return SkyboxState.get(level).graffitiPresent();
    }

    /**
     * The street is built once, the first time somebody is in the world.
     */
    public static void ensureStreet(ServerPlayer player) {
        if (player.level() instanceof ServerLevel level) {
            ensureStreet(player, level);
        }
    }

    /**
     * Whether today's wall lets the Interloper exist at all.
     */
    public static boolean allowsInterloper(ServerLevel level) {
        SkyboxState state = SkyboxState.get(level);
        return state.graffitiPresent() && level.getRandom().nextInt(100) < state.graffitiChance();
    }

    /**
     * The drawing is not decoration. Anything wearing the umbrella that walks past
     * an active wall has a good chance of not walking any further.
     */
    public static void tickInterlopers(ServerLevel level) {
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return;
        }
        if (level.getGameTime() % 20L != 0L) {
            return;
        }
        SkyboxState state = SkyboxState.get(level);
        BlockPos pos = state.graffitiPos();
        if (pos == null) {
            return;
        }
        boolean present = state.graffitiPresent();

        // Once a minute, the wall decides whether somebody is allowed to exist.
        if (level.getGameTime() % 1200L == 0L && present && allowsInterloper(level)) {
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > RADIUS * RADIUS) {
                    continue;
                }
                boolean already = !level.getEntitiesOfClass(
                        net.anomidae.interloper.entity.InterloperEntity.class,
                        player.getBoundingBox().inflate(96.0D)).isEmpty();
                if (already) {
                    continue;
                }
                net.anomidae.interloper.entity.InterloperEntity hunter =
                        net.anomidae.interloper.registry.ModEntities.INTERLOPER.get().create(level);
                if (hunter == null) {
                    continue;
                }
                hunter.moveTo(pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() - 5.5D, 0.0F, 0.0F);
                hunter.bindPlayer(player.getUUID());
                hunter.setTarget(player);
                hunter.setHunting(true);
                level.addFreshEntity(hunter);
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BELL_RESONATE,
                        net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 0.45F);
                player.sendSystemMessage(Component.literal("he came out of the wall")
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }

        for (net.anomidae.interloper.entity.InterloperEntity interloper
                : level.getEntitiesOfClass(net.anomidae.interloper.entity.InterloperEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(RADIUS))) {
            if (interloper.isRemoved() || interloper.isHunting() == false) {
                continue;
            }
            if (!present) {
                // the wall is clean, so he was never here
                interloper.discard();
                continue;
            }
            if (level.getRandom().nextInt(100) < 45) {
                for (ServerPlayer player : level.players()) {
                    player.sendSystemMessage(Component.literal("the man in the drawing looked at him")
                            .withStyle(ChatFormatting.DARK_RED));
                }
                level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BELL_RESONATE,
                        net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 0.5F);
                interloper.discard();
            }
        }
    }

    /**
     * Called once per world day from the overworld tick.
     */
    public static void tickDay(ServerLevel level) {
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return;
        }
        SkyboxState state = SkyboxState.get(level);
        long day = level.getDayTime() / 24000L;
        if (day == state.graffitiDay()) {
            return;
        }

        RandomSource random = level.getRandom();
        long chance = 35 + random.nextInt(46); // 35..80 %
        boolean present = random.nextInt(100) < chance;
        state.setGraffiti(day, present, chance);

        BlockPos pos = state.graffitiPos();
        if (pos != null && level.hasChunkAt(pos)) {
            level.setBlockAndUpdate(pos, present
                    ? ModBlocks.GRAFFITI.get().defaultBlockState()
                    : Blocks.AIR.defaultBlockState());
        }

        if (present) {
            for (ServerPlayer player : level.players()) {
                player.sendSystemMessage(Component.literal("the wall is not empty today")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        announceChecks(level, chance, present);
    }

    /**
     * Every RAVENCHECK.EXE on the wall reports today's number into the chat.
     */
    private static void announceChecks(ServerLevel level, long chance, boolean present) {
        SkyboxState state = SkyboxState.get(level);
        for (BlockPos pos : state.ravenChecks()) {
            if (!level.hasChunkAt(pos) || !(level.getBlockState(pos).getBlock() instanceof
                    net.anomidae.interloper.block.RavenCheckBlock)) {
                state.removeRavenCheck(pos);
                continue;
            }
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(),
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.7F, present ? 0.7F : 1.5F);
            for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(
                    pos.getX(), pos.getY(), pos.getZ()) < 64.0D * 64.0D)) {
                player.sendSystemMessage(Component.literal("ravencheck.exe: interloper chance "
                                + (present ? chance : 0) + "%")
                        .withStyle(present ? ChatFormatting.RED : ChatFormatting.GRAY));
            }
        }
    }

    public static void reset() {
        // nothing survives a server restart except SavedData, which reloads itself
    }

    public static void announceToAllNearby(ServerLevel level, net.minecraft.core.BlockPos pos) {
        for (ServerPlayer player : level.getPlayers(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < 64.0D * 64.0D)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component
                    .literal("something was painted on the wall").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    public static void announceTo(ServerPlayer player, ServerLevel level) {
        SkyboxState state = SkyboxState.get(level);
        player.sendSystemMessage(Component.literal("graffiti: " + (state.graffitiPresent() ? "PRESENT" : "clean")
                        + "  (interloper chance " + state.graffitiChance() + "%)")
                .withStyle(state.graffitiPresent() ? ChatFormatting.RED : ChatFormatting.GRAY));
    }

    /**
     * Makes sure the street exists somewhere near this player. Only one per world.
     */
    public static void ensureStreet(ServerPlayer player, ServerLevel level) {
        SkyboxState state = SkyboxState.get(level);
        if (state.streetBuilt()) {
            BlockPos center = state.streetCenter();
            if (level.hasChunkAt(center)) {
                return;
            }
        }
        BlockPos center = findSpot(level, player.blockPosition(), level.getRandom());
        StreetFabricator.build(level, center, level.getRandom());
        state.setStreet(center);
        state.setGraffitiPos(center.offset(0, 2, -7));
        if (state.graffitiPresent()) {
            level.setBlockAndUpdate(center.offset(0, 2, -7), ModBlocks.GRAFFITI.get().defaultBlockState());
        }
        player.sendSystemMessage(Component.literal("a street was not there yesterday")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static BlockPos findSpot(ServerLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < 24; attempt++) {
            int dx = 28 + random.nextInt(24);
            int dz = 28 + random.nextInt(24);
            if (random.nextBoolean()) {
                dx = -dx;
            }
            if (random.nextBoolean()) {
                dz = -dz;
            }
            BlockPos candidate = origin.offset(dx, 0, dz);
            level.getChunk(candidate);
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    candidate.getX(), candidate.getZ());
            if (y > level.getMinBuildHeight() + 5) {
                return new BlockPos(candidate.getX(), y, candidate.getZ());
            }
        }
        return origin;
    }

    private Graffiti() {
    }
}
