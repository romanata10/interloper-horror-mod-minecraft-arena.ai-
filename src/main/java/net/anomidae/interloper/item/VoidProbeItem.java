package net.anomidae.interloper.item;

import net.anomidae.interloper.entity.VoidProbeEntity;
import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModEntities;
import net.anomidae.interloper.world.Dimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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
 * Void Probe. Launched from anywhere, it drops into the Source Void and flies
 * around on its own, reporting what it finds.
 */
public class VoidProbeItem extends Item {
    public VoidProbeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        ServerLevel voidLevel = Dimensions.level(serverPlayer, Dimensions.SOURCE_VOID);
        if (voidLevel == null) {
            serverPlayer.sendSystemMessage(Component.literal("void probe: source_void is not loaded"));
            return InteractionResultHolder.fail(stack);
        }

        BlockPos origin = serverPlayer.blockPosition();
        int x = (origin.getX() / 4) * 4 + serverPlayer.getRandom().nextInt(64) - 32;
        int z = (origin.getZ() / 4) * 4 + serverPlayer.getRandom().nextInt(64) - 32;
        voidLevel.getChunk(x >> 4, z >> 4);

        VoidProbeEntity probe = ModEntities.VOID_PROBE.get().create(voidLevel);
        if (probe == null) {
            return InteractionResultHolder.fail(stack);
        }
        probe.moveTo(x + 0.5D, Dimensions.FLOOR_Y + 6.0D, z + 0.5D, 0.0F, 0.0F);
        probe.bindOwner(serverPlayer.getUUID());
        voidLevel.addFreshEntity(probe);

        stack.shrink(1);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_LAUNCH, SoundSource.PLAYERS, 1.0F, 1.4F);
        ModNetwork.toPlayer(serverPlayer, new ConsoleLinePacket("void_probe.launch -> source_void", 0xFF8CFF00, true));
        serverPlayer.sendSystemMessage(Component.literal("probe launched into the source void")
                .withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.void_probe.hint"));
    }
}
