package com.anomidae.interloper;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(InterloperMod.MOD_ID)
public class InterloperMod {
    public static final String MOD_ID = "interloper";
    public static final Logger LOGGER = LogUtils.getLogger();

    public InterloperMod() {
        LOGGER.info("Interloper: Source Void initialized");
    }
}
