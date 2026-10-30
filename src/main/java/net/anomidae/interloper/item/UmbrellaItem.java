package net.anomidae.interloper.item;

import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The umbrella that does not belong to you.
 * Open it and something opens its eyes.
 */
public class UmbrellaItem extends Item {
    public UmbrellaItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            level.playSound(null, player.blockPosition(), ModSounds.UMBRELLA.get(), SoundSource.PLAYERS, 1.0F, 0.7F);
            ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.SHAKE, 100, 0.7F));
            ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 140, 0.8F));
            serverPlayer.displayClientMessage(Component.translatable("message.interloper.umbrella.open")
                    .withStyle(ChatFormatting.DARK_PURPLE), false);

            for (int i = 0; i < 3; i++) {
                double x = player.getX() + (level.getRandom().nextDouble() - 0.5D) * 40.0D;
                double z = player.getZ() + (level.getRandom().nextDouble() - 0.5D) * 40.0D;
                InterloperEntity interloper = ModEntities.INTERLOPER.get().spawn(level,
                        net.minecraft.core.BlockPos.containing(x, player.getY(), z), MobSpawnType.EVENT);
                if (interloper != null) {
                    interloper.setTarget(player);
                    interloper.setHunting(true);
                    break;
                }
            }

            player.getCooldowns().addCooldown(this, 20 * 45);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.umbrella.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
