package net.anomidae.interloper.item;

import net.anomidae.interloper.entity.WhiteCubeEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.world.Dimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * SourceBox. A block salvaged from a corrupt Interloper. It finds anomalies,
 * and its cube function has a mind of its own.
 */
public class SourceBoxItem extends Item {
    public static final String CUBE_COOLDOWN = "interloper.cube_cooldown";
    private static final int COOLDOWN_TICKS = 20 * 60 * 10;

    public SourceBoxItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (player.isShiftKeyDown()) {
            return cube(serverPlayer, stack);
        }

        // anomaly scan
        Mob nearest = null;
        double best = 128.0D * 128.0D;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(128.0D))) {
            if (!(mob instanceof net.anomidae.interloper.entity.SourceEntity)
                    && !(mob instanceof net.anomidae.interloper.entity.InterloperEntity)
                    && !(mob instanceof net.anomidae.interloper.entity.SkyghostEntity)) {
                continue;
            }
            double distance = mob.distanceToSqr(player);
            if (distance < best) {
                best = distance;
                nearest = mob;
            }
        }

        if (nearest == null) {
            serverPlayer.sendSystemMessage(Component.literal("sourcebox: no anomalies within 128 blocks")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            BlockPos pos = nearest.blockPosition();
            serverPlayer.sendSystemMessage(Component.literal("sourcebox: "
                            + net.minecraft.world.entity.EntityType.getKey(nearest.getType()).getPath()
                            + " at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                            + " (" + (int) Math.sqrt(best) + "m)")
                    .withStyle(ChatFormatting.RED));
            ModNetwork.toPlayer(serverPlayer, new ConsoleLinePacket("anomaly detected: "
                    + pos.getX() + " " + pos.getY() + " " + pos.getZ(), 0xFFFF7A00, false));
        }
        player.getCooldowns().addCooldown(this, 20);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    private InteractionResultHolder<ItemStack> cube(ServerPlayer player, ItemStack stack) {
        long now = player.serverLevel().getGameTime();
        long ready = player.getPersistentData().getLong(CUBE_COOLDOWN);
        if (now < ready) {
            int seconds = (int) ((ready - now) / 20L);
            player.sendSystemMessage(Component.literal("cube is recharging: " + seconds + "s")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        for (WhiteCubeEntity existing : player.level().getEntitiesOfClass(WhiteCubeEntity.class,
                player.getBoundingBox().inflate(64.0D))) {
            if (existing.isBoundTo(player.getUUID())) {
                existing.discard();
            }
        }

        WhiteCubeEntity cube = ModEntities.WHITE_CUBE.get().create(player.serverLevel());
        if (cube == null) {
            return InteractionResultHolder.fail(stack);
        }
        cube.bindOwner(player.getUUID());
        cube.moveTo(player.getX(), player.getY() + 2.0D, player.getZ(), 0.0F, 0.0F);
        player.serverLevel().addFreshEntity(cube);
        player.getPersistentData().putLong(CUBE_COOLDOWN, now + COOLDOWN_TICKS);
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 1.0F, 1.8F);
        player.sendSystemMessage(Component.literal("cube deployed. do not hit it.")
                .withStyle(ChatFormatting.WHITE));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static boolean isVoidLevel(ServerLevel level) {
        return level.dimension().equals(Dimensions.SOURCE_VOID)
                || level.dimension().equals(Dimensions.XBOX_DREAMS);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.source_box.hint"));
        tooltip.add(Component.translatable("item.interloper.source_box.hint2"));
    }
}
