package net.anomidae.interloper.util;

import net.anomidae.interloper.Interloper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public final class ModDamage {
    public static final ResourceKey<DamageType> INTERLOPED =
            ResourceKey.create(Registries.DAMAGE_TYPE, Interloper.id("interloped"));

    public static DamageSource interloped(Level level) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(INTERLOPED));
    }

    private ModDamage() {
    }
}
