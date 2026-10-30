package net.anomidae.interloper.registry;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.util.DemoType;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Interloper.MODID);

    public static final RegistryObject<SoundEvent> WHISPER = register("whisper");
    public static final RegistryObject<SoundEvent> STATIC = register("static");
    public static final RegistryObject<SoundEvent> STINGER = register("stinger");
    public static final RegistryObject<SoundEvent> HEARTBEAT = register("heartbeat");
    public static final RegistryObject<SoundEvent> CUBEMAP = register("cubemap");
    public static final RegistryObject<SoundEvent> TERMINAL = register("terminal");
    public static final RegistryObject<SoundEvent> UMBRELLA = register("umbrella");
    public static final RegistryObject<SoundEvent> INTERLOPER_AMBIENT = register("interloper_ambient");
    public static final RegistryObject<SoundEvent> INTERLOPER_HURT = register("interloper_hurt");
    public static final RegistryObject<SoundEvent> INTERLOPER_DEATH = register("interloper_death");
    public static final RegistryObject<SoundEvent> CAPTURE = register("capture");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(Interloper.id(name)));
    }

    /** Convenience so callers do not have to think about demos. */
    public static SoundEvent demoStatic() {
        return STATIC.get();
    }

    private ModSounds() {
    }

    public static DemoType[] demoTab() {
        return DemoType.values();
    }
}
