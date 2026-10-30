package net.anomidae.interloper.item;

import net.anomidae.interloper.config.InterloperConfig;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.entity.SkyghostEntity;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Reads the skybox. Reports how far away the nearest anomaly is.
 */
public class AnomalyScannerItem extends Item {
    private static final String[] DIRECTIONS = {"north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"};

    public AnomalyScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        Entity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity entity : level.getEntities(player, player.getBoundingBox().inflate(96.0D))) {
            if (entity instanceof InterloperEntity || entity instanceof SkyghostEntity) {
                double distance = entity.distanceToSqr(player);
                if (distance < best) {
                    best = distance;
                    nearest = entity;
                }
            }
        }

        int distance = nearest == null ? -1 : (int) Math.sqrt(best);
        if (nearest == null) {
            player.displayClientMessage(Component.translatable("message.interloper.scanner.empty")
                    .withStyle(ChatFormatting.GRAY), true);
            level.playSound(null, player.blockPosition(), ModSounds.STATIC.get(), SoundSource.PLAYERS, 0.5F, 1.6F);
        } else {
            String name = nearest instanceof InterloperEntity
                    ? "message.interloper.scanner.interloper"
                    : "message.interloper.scanner.skyghost";
            player.displayClientMessage(Component.translatable("message.interloper.scanner.ping",
                            Component.translatable(name),
                            distance,
                            Component.translatable("message.interloper.direction." + direction(player, nearest)))
                    .withStyle(ChatFormatting.DARK_RED), true);
            level.playSound(null, player.blockPosition(), ModSounds.HEARTBEAT.get(), SoundSource.PLAYERS,
                    distance < 24 ? 1.0F : 0.5F, 1.0F);

            if (distance < 24 && player instanceof ServerPlayer serverPlayer && InterloperConfig.SCANNER_GLITCH.get()) {
                ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 60, 0.9F));
                ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 40, 0.8F));
            }
        }

        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
        player.getCooldowns().addCooldown(this, 30);
        return InteractionResultHolder.success(stack);
    }

    private static String direction(Player player, Entity entity) {
        Vec3 diff = entity.position().subtract(player.position());
        double angle = Math.toDegrees(Math.atan2(diff.x, -diff.z));
        int index = (int) Math.round(angle / 45.0D) & 7;
        // atan2(x, -z) gives 0 = north, 90 = east
        return DIRECTIONS[(index + 8) % 8];
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.anomaly_scanner.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
