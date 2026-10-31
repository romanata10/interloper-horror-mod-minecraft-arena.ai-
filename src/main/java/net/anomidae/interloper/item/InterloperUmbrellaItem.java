package net.anomidae.interloper.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Interloper's umbrella. Whatever it touches stops existing, including things
 * that usually refuse to die.
 */
public class InterloperUmbrellaItem extends SwordItem {
    public InterloperUmbrellaItem(Properties properties) {
        super(Tiers.NETHERITE, 1000, -2.4F, properties);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (target instanceof LivingEntity living && !(target instanceof Player)) {
            erase(living);
            return true;
        }
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        erase(target);
        return true;
    }

    private static void erase(LivingEntity target) {
        target.setHealth(0.0F);
        target.die(target.damageSources().genericKill());
        target.discard();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.interloper.interloper_umbrella.hint"));
    }
}
