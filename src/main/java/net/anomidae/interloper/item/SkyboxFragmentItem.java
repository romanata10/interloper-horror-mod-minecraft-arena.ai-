package net.anomidae.interloper.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A shard of something that should not have edges.
 */
public class SkyboxFragmentItem extends Item {
    public SkyboxFragmentItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.skybox_fragment.hint1").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.interloper.skybox_fragment.hint2").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
