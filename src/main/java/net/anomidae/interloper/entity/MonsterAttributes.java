package net.anomidae.interloper.entity;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;

/**
 * Small shim so every Source Void mob gets the same sane defaults.
 */
final class MonsterAttributes {
    static AttributeSupplier.Builder base() {
        return Monster.createMonsterAttributes();
    }

    private MonsterAttributes() {
    }
}
