package net.anomidae.interloper.world;

import net.anomidae.interloper.entity.ConeEntity;
import net.anomidae.interloper.entity.CorruptedNpcEntity;
import net.anomidae.interloper.entity.CrasherEntity;
import net.anomidae.interloper.entity.DmNpcEntity;
import net.anomidae.interloper.entity.InterloperCorruptEntity;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.entity.VradEntity;
import net.anomidae.interloper.network.ChatGhostPacket;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.util.SourceText;
import net.anomidae.interloper.util.TargetBound;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Populates the void levels: rare single spawns that walk around for a while and
 * then despawn, one Skyghost at a time, and the occasional Interloper.
 */
public final class VoidDirector {
    private static final EntityType<?>[] VOID_MOBS = {
            ModEntities.CORRUPTED_NPC.get(),
            ModEntities.VRAD.get(),
            ModEntities.CRASHER.get(),
            ModEntities.CONE.get(),
            ModEntities.DM_NPC.get()
    };

    private static final EntityType<?>[] DREAM_MOBS = {
            ModEntities.CORRUPTED_NPC.get(),
            ModEntities.CRASHER.get(),
            ModEntities.CONE.get(),
            ModEntities.VRAD.get()
    };

    public static void tick(ServerLevel level) {
        if (level.dimension().equals(Dimensions.SOURCE_VOID)) {
            tickSourceVoid(level);
        } else if (level.dimension().equals(Dimensions.XBOX_DREAMS)) {
            tickDreams(level);
        }
    }

    private static void tickSourceVoid(ServerLevel level) {
        var players = level.players();
        if (players.isEmpty()) {
            return;
        }

        // everyone who falls through the hole in the floor wakes up in the dashboard
        for (var player : players) {
            if (player.getY() < SourceVoid.FLOOR_Y - 4) {
                XboxDreams.enter(player);
                return;
            }
        }

        if (level.getGameTime() % 40L != 0L) {
            return;
        }

        for (var player : players) {
            if (Hunters.isOnCrashCooldown(player)) {
                continue;
            }

            // rare Interloper hunt, straight out of the void
            if (level.getGameTime() % 600L == 0L
                    && level.getRandom().nextInt(100) < 12
                    && countNear(level, player, ModEntities.INTERLOPER.get(), 96.0D) == 0) {
                spawnHunter(level, player);
            }

            // exactly one Skyghost at a time, and it has a 40% chance to come back
            if (level.getGameTime() % 400L == 0L
                    && countNear(level, player, ModEntities.SKYGHOST.get(), 128.0D) == 0
                    && level.getRandom().nextFloat() < 0.4F) {
                HorrorDirector.spawnSkyghost(level, player, level.getRandom());
            }

            if (level.getRandom().nextInt(4) != 0) {
                continue;
            }
            long nearby = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48.0D),
                    mob -> mob.getType() != ModEntities.CONE.get()).size();
            if (nearby > 3) {
                continue;
            }
            spawnOne(level, player, VOID_MOBS);
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

    private static void tickDreams(ServerLevel level) {
        var players = level.players();
        if (players.isEmpty()) {
            return;
        }
        // falling out of the dream drops you back into the void
        for (var player : players) {
            if (player.getY() < XboxDreams.FLOOR_Y - 4) {
                SourceVoid.enter(player);
                return;
            }
        }
        if (level.getGameTime() % 100L != 0L) {
            return;
        }
        for (var player : players) {
            // fewer things live here
            if (level.getRandom().nextInt(5) != 0) {
                continue;
            }
            long nearby = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48.0D),
                    mob -> mob.getType() != ModEntities.CONE.get()).size();
            if (nearby > 2) {
                continue;
            }
            spawnOne(level, player, DREAM_MOBS);
        }
    }

    private static int countNear(ServerLevel level, ServerPlayer player, EntityType<?> type, double range) {
        return level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(range),
                mob -> mob.getType() == type).size();
    }

    /**
     * A void Interloper. Rare, bound to one player, and it goes away with them.
     */
    public static void spawnHunter(ServerLevel level, ServerPlayer player) {
        InterloperEntity interloper = ModEntities.INTERLOPER.get().create(level);
        if (interloper == null) {
            return;
        }
        double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
        double distance = 34.0D + level.getRandom().nextDouble() * 16.0D;
        double x = player.getX() + Math.cos(angle) * distance;
        double z = player.getZ() + Math.sin(angle) * distance;
        interloper.moveTo(x, player.getY() + 6.0D, z, level.getRandom().nextFloat() * 360.0F, 0.0F);
        interloper.bindPlayer(player.getUUID());
        interloper.setTarget(player);
        interloper.setHunting(true);
        level.addFreshEntity(interloper);

        level.playSound(null, player.blockPosition(), net.minecraft.sounds.SoundEvents.BELL_RESONATE,
                net.minecraft.sounds.SoundSource.AMBIENT, 1.0F, 0.6F);
        ModNetwork.toPlayer(player, new ConsoleLinePacket("ent_create interloper", 0xFFCFCFCF, false));
    }

    private static void spawnOne(ServerLevel level, ServerPlayer player, EntityType<?>[] pool) {
        EntityType<?> type = pool[level.getRandom().nextInt(pool.length)];
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

        mob.moveTo(x + 0.5D, y, z + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        mob.setPersistenceRequired();
        if (mob instanceof TargetBound bound) {
            bound.bindPlayer(player.getUUID());
        }
        level.addFreshEntity(mob);

        ServerScheduler.schedule(20 * (30 + level.getRandom().nextInt(45)), () -> {
            if (mob.isAlive()) {
                mob.discard();
            }
        });
    }

    private VoidDirector() {
    }
}
