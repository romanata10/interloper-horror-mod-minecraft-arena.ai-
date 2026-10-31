package net.anomidae.interloper.world;

import net.anomidae.interloper.network.ConsoleLinePacket;
import net.anomidae.interloper.network.ModNetwork;
import net.anomidae.interloper.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/**
 * Decides when the player is "out", keeps SKYBOX.exe in their inventory forever,
 * and hands out skybox_active.
 *
 * The exe is not an item you can lose: it is a place you now carry.
 */
public final class SkyboxWatcher {
    private static final String WAS_IN_VOID = "interloper.was_in_void";
    private static final String GOT_SKYBOX_ACTIVE = "interloper.skybox_active_given";
    private static final ResourceLocation ADVANCEMENT =
            new ResourceLocation("interloper", "skybox_active");

    public static void onLogin(ServerPlayer player) {
        ensureExe(player);
        checkEscape(player);
    }

    public static void onRespawn(ServerPlayer player) {
        ensureExe(player);
    }

    /**
     * Called from the overworld tick: the moment a player who was in the void is
     * standing in the normal world, they get the achievement.
     */
    public static void tick(ServerPlayer player) {
        if (player.level() instanceof net.minecraft.server.level.ServerLevel level
                && level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            checkEscape(player);
        }
    }

    /**
     * Remember that this player has been under the map, so coming back up counts.
     */
    public static void markVoid(ServerPlayer player) {
        player.getPersistentData().putBoolean(WAS_IN_VOID, true);
    }

    private static void checkEscape(ServerPlayer player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel level)
                || !level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) {
            return;
        }
        if (!player.getPersistentData().getBoolean(WAS_IN_VOID)) {
            return;
        }
        if (player.getPersistentData().getBoolean(GOT_SKYBOX_ACTIVE)) {
            return;
        }
        player.getPersistentData().putBoolean(GOT_SKYBOX_ACTIVE, true);
        unlock(player);
    }

    private static void unlock(ServerPlayer player) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(ADVANCEMENT);
        if (advancement != null) {
            for (String criterion : advancement.getCriteria().keySet()) {
                player.getAdvancements().award(advancement, criterion);
            }
        }
        SkyboxDimension.unlock(player);
        ensureExe(player);
        player.sendSystemMessage(Component.literal("skybox_active").withStyle(ChatFormatting.AQUA));
        ModNetwork.toPlayer(player, new ConsoleLinePacket("skybox_unlocked", 0xFFFFFFFF, true));
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void ensureExe(ServerPlayer player) {
        if (!SkyboxDimension.isUnlocked(player) && !SkyboxState.get(player.serverLevel()).skyboxUnlocked()) {
            return;
        }
        if (!SkyboxDimension.isUnlocked(player)) {
            player.getPersistentData().putBoolean(SkyboxDimension.UNLOCKED, true);
        }
        ItemStack exe = new ItemStack(ModItems.SKYBOX_EXE.get());
        if (!player.getInventory().contains(exe)) {
            player.getInventory().add(exe);
        }
    }

    private SkyboxWatcher() {
    }
}
