package net.anomidae.interloper.entity;

import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.util.Bindings;
import net.anomidae.interloper.util.Hunters;
import net.anomidae.interloper.world.Dimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Void Probe. A drone that explores the Source Void on its own and talks to the
 * player who launched it.
 */
public class VoidProbeEntity extends Monster {
    private static final int LIFETIME = 20 * 75;
    private static final int REPORT_INTERVAL = 60;

    @Nullable
    private UUID owner;
    private int age;
    private int reportTimer;
    private int found;

    public VoidProbeEntity(EntityType<? extends VoidProbeEntity> type, Level level) {
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
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    public void bindOwner(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    protected void registerGoals() {
        // No AI. It flies where it wants.
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            return;
        }

        this.age++;
        if (this.age > LIFETIME) {
            this.finishReport("probe returned to sender");
            return;
        }

        // drift around the void
        if (this.age % 20 == 0) {
            double angle = this.random.nextDouble() * Math.PI * 2.0D;
            this.setPos(this.getX() + Math.cos(angle) * 3.0D,
                    Dimensions.FLOOR_Y + 5.0D + this.random.nextDouble() * 3.0D,
                    this.getZ() + Math.sin(angle) * 3.0D);
            this.setDeltaMovement(Vec3.ZERO);
        }

        if (--this.reportTimer <= 0) {
            this.reportTimer = REPORT_INTERVAL;
            this.scan();
        }
    }

    private void scan() {
        if (!(this.level() instanceof ServerLevel level)) {
            return;
        }
        ServerPlayer player = this.ownerPlayer();
        if (player == null) {
            return;
        }

        BlockPos center = this.blockPosition();
        List<String> findings = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-24, -8, -24), center.offset(24, 8, 24))) {
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            if (level.getBlockEntity(pos) instanceof Container container) {
                List<String> contents = new ArrayList<>();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (!stack.isEmpty()) {
                        contents.add(stack.getCount() + "x " + stack.getHoverName().getString());
                    }
                }
                findings.add("chest at " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                        + (contents.isEmpty() ? " (empty)" : " -> " + String.join(", ", contents)));
                if (findings.size() >= 3) {
                    break;
                }
            }
        }

        for (Mob mob : level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(24.0D))) {
            if (mob.getType() == net.anomidae.interloper.registry.ModEntities.VOID_PROBE.get()) {
                continue;
            }
            findings.add("entity: " + net.minecraft.world.entity.EntityType.getKey(mob.getType()).getPath()
                    + " at " + mob.blockPosition().getX() + " " + mob.blockPosition().getY()
                    + " " + mob.blockPosition().getZ());
            if (findings.size() >= 4) {
                break;
            }
        }

        if (findings.isEmpty()) {
            ModNetwork.toPlayer(player, new ConsoleLinePacket("probe: sector "
                    + (center.getX() >> 4) + "," + (center.getZ() >> 4) + " empty", 0xFF8A8A8A, false));
            return;
        }

        this.found += findings.size();
        for (String finding : findings) {
            player.sendSystemMessage(Component.literal("[probe] " + finding)
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            ModNetwork.toPlayer(player, new ConsoleLinePacket("probe: " + finding, 0xFFB0B0B0, false));
        }
        level.playSound(null, this.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.AMBIENT, 0.5F, 1.8F);
    }

    private void finishReport(String reason) {
        ServerPlayer player = this.ownerPlayer();
        if (player != null) {
            player.sendSystemMessage(Component.literal("[probe] " + reason + ", " + this.found + " findings")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            ModNetwork.toPlayer(player, new ConsoleLinePacket("probe: done, " + this.found + " findings",
                    0xFFCFCFCF, true));
        }
        Hunters.forget(this);
        this.discard();
    }

    @Nullable
    private ServerPlayer ownerPlayer() {
        ServerPlayer player = Bindings.online(this, this.owner);
        return player;
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
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        this.finishReport("probe destroyed");
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.owner != null) {
            tag.putUUID("ProbeOwner", this.owner);
        }
        tag.putInt("ProbeAge", this.age);
        tag.putInt("ProbeFound", this.found);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("ProbeOwner")) {
            this.owner = tag.getUUID("ProbeOwner");
        }
        this.age = tag.getInt("ProbeAge");
        this.found = tag.getInt("ProbeFound");
    }
}
