package net.anomidae.interloper.entity;

import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModBlocks;
import net.anomidae.interloper.util.Bindings;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.util.ServerScheduler;
import net.anomidae.interloper.world.Dimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

/**
 * The little white cube that SourceBox spawns. It follows you, it can be thrown,
 * and if you hit it the game does not forgive you.
 */
public class WhiteCubeEntity extends Monster {
    private static final String CUBE_ERROR = "Cube:error";

    @Nullable
    private UUID owner;
    private boolean thrown;
    private int thrownTicks;
    private int lifetime;

    public WhiteCubeEntity(EntityType<? extends WhiteCubeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    public void bindOwner(UUID uuid) {
        this.owner = uuid;
    }

    public boolean isBoundTo(UUID uuid) {
        return uuid != null && uuid.equals(this.owner);
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        this.lifetime++;
        if (this.lifetime > 20 * 60 * 5) {
            this.discard();
            return;
        }

        ServerPlayer owner = this.ownerPlayer();

        // thrown into the void: the cube pays for itself
        boolean voidLevel = this.level().dimension().equals(Dimensions.SOURCE_VOID)
                || this.level().dimension().equals(Dimensions.XBOX_DREAMS);
        if ((voidLevel && this.getY() < Dimensions.FLOOR_Y - 4)
                || this.getY() < this.level().getMinBuildHeight() - 16) {
            this.reward(owner);
            return;
        }

        if (this.thrown) {
            this.setPos(this.getX() + this.getDeltaMovement().x,
                    this.getY() + this.getDeltaMovement().y,
                    this.getZ() + this.getDeltaMovement().z);
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.04D, 0.0D));
            if (--this.thrownTicks <= 0) {
                this.thrown = false;
                this.setDeltaMovement(Vec3.ZERO);
            }
            return;
        }

        if (owner == null || owner.level() != this.level()) {
            if (this.lifetime > 100) {
                this.discard();
            }
            return;
        }

        Vec3 target = owner.position().add(0.0D, 2.2D, 0.0D);
        Vec3 here = this.position();
        Vec3 step = target.subtract(here).scale(0.25D);
        this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!this.isBoundTo(player.getUUID())) {
            return InteractionResult.PASS;
        }
        Vec3 look = player.getLookAngle();
        this.thrown = true;
        this.thrownTicks = 100;
        this.setDeltaMovement(look.x * 0.9D + 0.0D, 0.35D, look.z * 0.9D);
        this.level().playSound(null, this.blockPosition(), SoundEvents.SNOWBALL_THROW,
                SoundSource.PLAYERS, 0.7F, 1.6F);
        player.sendSystemMessage(Component.literal("cube thrown").withStyle(ChatFormatting.GRAY));
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.level().isClientSide) {
            return false;
        }
        if (source.getEntity() instanceof ServerPlayer player && this.isBoundTo(player.getUUID())) {
            ModNetwork.toPlayer(player, new ConsoleLinePacket(CUBE_ERROR, 0xFFFF0000, true));
            ServerScheduler.schedule(20, () -> {
                if (Hunters.isOnline(player) && player.connection != null) {
                    player.connection.disconnect(Component.literal(CUBE_ERROR));
                }
            });
            Hunters.forget(this);
            this.discard();
            return false;
        }
        return super.hurt(source, amount);
    }

    private void reward(@Nullable ServerPlayer owner) {
        if (owner != null && Hunters.isOnline(owner)) {
            List<net.minecraft.world.item.alchemy.Potion> potions = List.of(
                    Potions.HEALING, Potions.STRENGTH, Potions.SWIFTNESS, Potions.NIGHT_VISION,
                    Potions.REGENERATION, Potions.FIRE_RESISTANCE, Potions.WATER_BREATHING,
                    Potions.INVISIBILITY, Potions.LEAPING, Potions.SLOW_FALLING);
            for (int i = 0; i < 5; i++) {
                ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.POTION),
                        potions.get(this.random.nextInt(potions.size())));
                owner.getInventory().placeItemBackInInventory(potion);
            }
            List<ItemStack> loot = List.of(
                    new ItemStack(ModBlocks.MISSING_TEXTURE.get(), 8 + this.random.nextInt(9)),
                    new ItemStack(ModBlocks.SOURCE_FLOOR.get(), 8 + this.random.nextInt(9)),
                    new ItemStack(ModBlocks.PORTAL_PANEL.get(), 8 + this.random.nextInt(9)),
                    new ItemStack(ModBlocks.XBOX_GRASS.get(), 8 + this.random.nextInt(9)),
                    new ItemStack(ModBlocks.WHITE_WALL.get(), 8 + this.random.nextInt(9)),
                    new ItemStack(Items.DIAMOND, 1 + this.random.nextInt(3)),
                    new ItemStack(Items.IRON_INGOT, 3 + this.random.nextInt(6)),
                    new ItemStack(Items.ENDER_PEARL, 1 + this.random.nextInt(3))
            );
            for (int i = 0; i < 5; i++) {
                owner.getInventory().placeItemBackInInventory(loot.get(this.random.nextInt(loot.size())).copy());
            }
            owner.sendSystemMessage(Component.literal("the cube fell into the void and paid you back")
                    .withStyle(ChatFormatting.WHITE));
            owner.level().playSound(null, owner.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 1.0F, 1.4F);
        }
        this.discard();
    }

    @Nullable
    private ServerPlayer ownerPlayer() {
        return Bindings.online(this, this.owner);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.owner != null) {
            tag.putUUID("CubeOwner", this.owner);
        }
        tag.putBoolean("Thrown", this.thrown);
        tag.putInt("CubeLife", this.lifetime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("CubeOwner")) {
            this.owner = tag.getUUID("CubeOwner");
        }
        this.thrown = tag.getBoolean("Thrown");
        this.lifetime = tag.getInt("CubeLife");
    }
}
