package net.anomidae.interloper.world;

import net.anomidae.interloper.entity.ConeEntity;
import net.anomidae.interloper.entity.CorruptedNpcEntity;
import net.anomidae.interloper.entity.CrasherEntity;
import net.anomidae.interloper.entity.DmNpcEntity;
import net.anomidae.interloper.entity.InterloperCorruptEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.entity.VradEntity;
import net.anomidae.interloper.network.ChatGhostPacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.util.SourceText;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Populates the void. Very few things live here and none of them stay.
 */
public final class VoidDirector {
    private static final EntityType<?>[] VOID_MOBS = {
            ModEntities.CORRUPTED_NPC.get(),
            ModEntities.VRAD.get(),
            ModEntities.CRASHER.get(),
            ModEntities.CONE.get(),
            ModEntities.DM_NPC.get(),
            ModEntities.SKYGHOST.get(),
            ModEntities.INTERLOPER_CORRUPT.get()
    };

    public static void tick(ServerLevel level) {
        if (!level.dimension().equals(SourceVoid.LEVEL)) {
            return;
        }
        if (level.getGameTime() % 40L != 0L) {
            return;
        }
        var players = level.players();
        if (players.isEmpty()) {
            return;
        }

        for (var player : players) {
            if (level.getRandom().nextInt(3) != 0) {
                continue;
            }
            long nearby = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48.0D),
                    mob -> mob.getType() != ModEntities.CONE.get()).size();
            if (nearby > 3) {
                continue;
            }
            spawnOne(level, player, level);
        }

        if (level.getGameTime() % 200L == 0L) {
            for (var player : players) {
                if (level.getRandom().nextInt(4) == 0) {
                    ModNetwork.toPlayer(player, new ChatGhostPacket(
                            Component.literal(SourceText.randomLetters(level.getRandom()))
                                    .withStyle(ChatFormatting.DARK_GRAY)));
                }
                if (level.getRandom().nextInt(6) == 0) {
                    ModNetwork.toPlayer(player, new ChatGhostPacket(
                            Component.literal(SourceText.randomError(level.getRandom()))
                                    .withStyle(ChatFormatting.GOLD)));
                }
            }
        }
    }

    private static void spawnOne(ServerLevel level, net.minecraft.server.level.ServerPlayer player,
                                 ServerLevel unused) {
        EntityType<?> type = VOID_MOBS[level.getRandom().nextInt(VOID_MOBS.length)];
        Mob mob = (Mob) type.create(level);
        if (mob == null) {
            return;
        }

        double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = 16.0D + level.getRandom().nextDouble() * 22.0D;
        int x = (int) (player.getX() + Math.cos(angle) * distance);
        int z = (int) (player.getZ() + Math.sin(angle) * distance);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        if (y <= level.getMinBuildHeight() + 2) {
            y = SourceVoid.FLOOR_Y;
        }

        Vec3Helper.place(mob, x + 0.5D, y, z + 0.5D, level.getRandom().nextFloat() * 360.0F);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);

        ServerScheduler.schedule(20 * (30 + level.getRandom().nextInt(45)), () -> {
            if (mob.isAlive()) {
                mob.discard();
            }
        });
    }

    private static final class Vec3Helper {
        static void place(Mob mob, double x, double y, double z, float yaw) {
            mob.moveTo(x, y, z, yaw, 0.0F);
        }
    }

    private VoidDirector() {
    }
}
