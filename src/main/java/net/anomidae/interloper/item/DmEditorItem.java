package net.anomidae.interloper.item;

import net.anomidae.interloper.entity.DmNpcEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * dm_editor. Written by whoever exported dm_npc, and it still works.
 * Right click a dm_npc to make it follow you and fight for you.
 */
public class DmEditorItem extends Item {
    public DmEditorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof DmNpcEntity npc)) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            boolean following = npc.toggleFollowing(serverPlayer);
            serverPlayer.sendSystemMessage(Component.literal(following
                            ? "dm_npc: follow_player = true, defend = true"
                            : "dm_npc: follow_player = false")
                    .withStyle(ChatFormatting.GREEN));
            player.level().playSound(null, npc.blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.PLAYERS, 0.6F, following ? 1.6F : 0.8F);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.dm_editor.hint"));
    }
}
