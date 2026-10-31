package net.anomidae.interloper.item;

import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.world.SkyboxDimension;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
 * SKYBOX.exe. Restored from shards, never leaves the inventory, and it is a door
 * to a very small, very quiet place.
 */
public class SkyboxExeItem extends Item {
    public SkyboxExeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (serverPlayer.isShiftKeyDown()) {
            player.sendSystemMessage(Component.literal("SKYBOX.exe: running"));
            SkyboxDimension.enter(serverPlayer);
        } else {
            ModNetwork.toPlayer(serverPlayer, new ConsoleLinePacket(
                    "skybox.exe: shift + use to open the pocket dimension", 0xFFCFCFCF, true));
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.skybox_exe.hint"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
