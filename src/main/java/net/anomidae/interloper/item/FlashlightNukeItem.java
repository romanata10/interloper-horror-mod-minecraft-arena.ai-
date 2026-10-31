package net.anomidae.interloper.item;

import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.util.Hunters;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Flashlight Nuke Glitch. A flashlight with a nuke in it, found in a chest that
 * was covered in broken blocks. It kills the things that cannot be killed.
 */
public class FlashlightNukeItem extends Item {
    public FlashlightNukeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        LivingEntity target = findTarget(serverPlayer);
        if (target == null) {
            serverPlayer.sendSystemMessage(Component.literal("flashlight_nuke: no valid target within 64 blocks")
                    .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        ServerLevel serverLevel = serverPlayer.serverLevel();
        LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (bolt != null) {
            bolt.moveTo(target.position());
            bolt.setVisualOnly(false);
            serverLevel.addFreshEntity(bolt);
        }

        if (target instanceof net.anomidae.interloper.entity.InterloperCorruptEntity corrupt) {
            corrupt.breakByNuke();
        } else if (target instanceof InterloperEntity interloper) {
            Hunters.forget(interloper);
            interloper.discard();
        } else {
            target.hurt(serverLevel.damageSources().magic(), 1000.0F);
            target.discard();
        }

        serverLevel.playSound(null, target.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 3.0F, 0.5F);
        ModNetwork.toPlayer(serverPlayer, new ConsoleLinePacket("nuke_glitch_fired", 0xFFFF7A00, true));
        serverPlayer.sendSystemMessage(Component.literal("it stopped existing").withStyle(ChatFormatting.DARK_RED));
        stack.shrink(1);
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Nullable
    private static LivingEntity findTarget(ServerPlayer player) {
        LivingEntity best = null;
        double bestDistance = 64.0D * 64.0D;
        for (Entity entity : player.level().getEntities(player, player.getBoundingBox().inflate(64.0D))) {
            if (!(entity instanceof InterloperEntity) && !(entity instanceof SkyghostEntity)) {
                continue;
            }
            double distance = entity.distanceToSqr(player);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = (LivingEntity) entity;
            }
        }
        return best;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.flashlight_nuke.hint"));
    }
}
