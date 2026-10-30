package net.anomidae.interloper.block;

import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.network.ScreenEffectPacket;
import net.anomidae.interloper.registry.ModItems;
import net.anomidae.interloper.registry.ModSounds;
import net.anomidae.interloper.util.DemoType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A dead console. Feed it a skybox fragment and it will render a demo for you.
 */
public class EngineTerminalBlock extends Block {
    public EngineTerminalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack held = player.getItemInHand(hand);
        boolean creative = player.getAbilities().instabuild;

        if (held.is(ModItems.SKYBOX_FRAGMENT.get())) {
            if (!creative) {
                held.shrink(1);
            }
            DemoType type = DemoType.random(level.getRandom());
            ItemStack tape = ModItems.demoTape(type);
            if (!player.getInventory().add(tape)) {
                player.drop(tape, false);
            }

            level.playSound(null, pos, ModSounds.TERMINAL.get(), SoundSource.BLOCKS, 1.0F, 0.7F);

            if (player instanceof ServerPlayer serverPlayer) {
                ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.GLITCH, 80, 0.8F));
                serverPlayer.displayClientMessage(Component.translatable("block.interloper.engine_terminal.write",
                        type.displayName()).withStyle(ChatFormatting.GRAY), false);
            }
            return InteractionResult.CONSUME;
        }

        if (held.isEmpty()) {
            level.playSound(null, pos, ModSounds.STATIC.get(), SoundSource.BLOCKS, 0.8F, 0.6F);
            player.displayClientMessage(Component.translatable("block.interloper.engine_terminal.idle")
                    .withStyle(ChatFormatting.DARK_GRAY), false);

            if (player instanceof ServerPlayer serverPlayer && level.getRandom().nextInt(4) == 0) {
                ModNetwork.toPlayer(serverPlayer, new ScreenEffectPacket(ScreenEffectPacket.Effect.WHISPER, 100, 0.7F));
                serverPlayer.displayClientMessage(Component.translatable("block.interloper.engine_terminal.idle2")
                        .withStyle(ChatFormatting.DARK_PURPLE), false);
            }
            return InteractionResult.CONSUME;
        }

        player.displayClientMessage(Component.translatable("block.interloper.engine_terminal.reject")
                .withStyle(ChatFormatting.DARK_GRAY), false);
        level.playSound(null, pos, ModSounds.STATIC.get(), SoundSource.BLOCKS, 0.5F, 0.4F);
        return InteractionResult.CONSUME;
    }
}
