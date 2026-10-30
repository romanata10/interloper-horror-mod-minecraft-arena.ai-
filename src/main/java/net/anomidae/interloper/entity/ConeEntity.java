package net.anomidae.interloper.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Just a grey cone. You can hit it. Something falls out.
 */
public class ConeEntity extends SourceEntity {
    private static final ItemStack[] LOOT = {
            new ItemStack(Items.DIRT),
            new ItemStack(Items.STICK),
            new ItemStack(Items.COAL),
            new ItemStack(Items.IRON_INGOT),
            new ItemStack(Items.APPLE),
            new ItemStack(Items.BREAD),
            new ItemStack(Items.PAPER),
            new ItemStack(Items.BONE),
            new ItemStack(Items.GLASS_BOTTLE),
            new ItemStack(Items.DIAMOND),
            new ItemStack(Items.ROTTEN_FLESH),
            new ItemStack(Items.REDSTONE),
            new ItemStack(Items.BRICK),
            new ItemStack(Items.STRING),
            new ItemStack(Items.FEATHER)
    };

    public ConeEntity(EntityType<? extends ConeEntity> type, Level level) {
        super(type, level);
        this.xpReward = 1;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return MonsterAttributes.base()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE, 8.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D);
    }

    @Override
    protected void serverTick() {
        // A cone does not do things.
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        ItemStack stack = LOOT[this.random.nextInt(LOOT.length)].copy();
        stack.setCount(1 + this.random.nextInt(stack.getMaxStackSize()));
        this.spawnAtLocation(stack);
    }

    @Override
    protected void registerGoals() {
    }
}
