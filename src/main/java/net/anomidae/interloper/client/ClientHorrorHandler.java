package net.anomidae.interloper.client;

import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.entity.InterloperEntity;
import net.anomidae.interloper.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Interloper.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientHorrorHandler {
    private static int heartbeatTimer;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ClientEffects.tick();

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level == null || player == null) {
            ClientEffects.proximity = 0.0F;
            return;
        }

        float closest = 0.0F;
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof InterloperEntity) {
                double distance = entity.distanceTo(player);
                if (distance < 48.0D) {
                    closest = Math.max(closest, (float) (1.0D - distance / 48.0D));
                }
            }
        }
        ClientEffects.proximity = closest;

        if (closest > 0.62F) {
            if (--heartbeatTimer <= 0) {
                heartbeatTimer = 26;
                player.playSound(ModSounds.HEARTBEAT.get(), 0.9F,
                        0.9F + level.getRandom().nextFloat() * 0.2F);
            }
        } else {
            heartbeatTimer = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        HorrorOverlay.render(event.getGuiGraphics(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float strength = ClientEffects.shakeFactor(event.getPartialTick());
        if (strength <= 0.0F) {
            return;
        }
        float time = event.getPartialTick();
        double amount = 0.9D * strength;
        event.setRoll(event.getRoll() + Math.sin(time * 2.3D) * amount);
        event.setYaw(event.getYaw() + Math.cos(time * 3.1D) * amount * 0.6D);
        event.setPitch(event.getPitch() + Math.sin(time * 2.7D) * amount * 0.4D);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float distortion = ClientEffects.fovDistortion();
        if (distortion > 0.0F) {
            event.setFOV(event.getFOV() * (1.0D - 0.10D * distortion));
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientEffects.reset();
    }

    private ClientHorrorHandler() {
    }
}
