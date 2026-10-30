package net.anomidae.interloper.item;

import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.DemoType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A recovered .dem file burnt onto something the game can read.
 */
public class DemoTapeItem extends Item {
    public DemoTapeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        DemoType type = DemoType.read(stack);

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            type.play(serverPlayer);
            level.playSound(null, player.blockPosition(), ModSounds.TERMINAL.get(), SoundSource.PLAYERS, 0.9F, 0.8F);
            player.getCooldowns().addCooldown(this, 40);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Component getName(ItemStack stack) {
        return DemoType.read(stack).displayName();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        DemoType type = DemoType.read(stack);
        tooltip.add(type.description().withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.interloper.demo_tape.hint").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
