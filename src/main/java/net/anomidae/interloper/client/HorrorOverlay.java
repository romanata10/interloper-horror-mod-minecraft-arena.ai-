package net.anomidae.interloper.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.anomidae.interloper.Interloper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * Draws the corruption. Everything here is cosmetic.
 */
public final class HorrorOverlay {
    private static final ResourceLocation NOISE = Interloper.id("textures/gui/noise.png");
    private static final ResourceLocation VIGNETTE = Interloper.id("textures/gui/vignette.png");

    public static void render(GuiGraphics gg, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.level == null) {
            return;
        }

        int width = gg.guiWidth();
        int height = gg.guiHeight();
        int time = mc.player.tickCount;
        boolean active = ClientEffects.glitchTicks > 0
                || ClientEffects.darkTicks > 0
                || ClientEffects.flashTicks > 0
                || ClientEffects.cubemapTicks > 0
                || ClientEffects.captureTicks > 0
                || ClientEffects.whisperTicks > 0;

        if (ClientEffects.proximity > 0.01F) {
            float alpha = ClientEffects.proximity * 0.45F;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            gg.blit(VIGNETTE, 0, 0, 0, 0, width, height, 256, 256);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (!active) {
            return;
        }

        if (ClientEffects.darkTicks > 0) {
            float alpha = ClientEffects.darkIntensity * 0.82F;
            gg.fill(0, 0, width, height, ((int) (alpha * 255) << 24));
        }

        if (ClientEffects.glitchTicks > 0) {
            float pulse = 0.35F + 0.25F * Mth.sin(time * 0.7F);
            float alpha = ClientEffects.glitchIntensity * pulse;
            drawNoise(gg, width, height, alpha, time);

            // horizontal tearing
            RandomSource random = RandomSource.create(time / 2L);
            int tears = 2 + random.nextInt(4);
            for (int i = 0; i < tears; i++) {
                int y = random.nextInt(height);
                int h = 1 + random.nextInt(4);
                gg.fill(0, y, width, y + h, 0x33FFFFFF);
            }
            for (int y = 0; y < height; y += 4) {
                gg.fill(0, y, width, y + 1, 0x14000000);
            }
        }

        if (ClientEffects.whisperTicks > 0) {
            float alpha = Math.min(1.0F, ClientEffects.whisperTicks / 20.0F);
            int color = ((int) (alpha * 200) << 24) | 0x707070;
            gg.drawCenteredString(mc.font, Component.literal("..."), width / 2, height / 2 + 40, color);
        }

        if (ClientEffects.cubemapTicks > 0) {
            drawCubemap(gg, mc, width, height, time);
        }

        if (ClientEffects.captureTicks > 0) {
            drawCapture(gg, mc, width, height, time);
        }

        if (ClientEffects.flashTicks > 0) {
            int alpha = (int) (255 * Math.min(1.0F, ClientEffects.flashTicks / 12.0F));
            gg.fill(0, 0, width, height, (alpha << 24) | 0xD8D8D8);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawNoise(GuiGraphics gg, int width, int height, float alpha, int seed) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Math.min(0.65F, alpha));
        RandomSource random = RandomSource.create(seed);
        int tile = 96;
        for (int x = 0; x < width; x += tile) {
            for (int y = 0; y < height; y += tile) {
                int u = random.nextInt(32);
                int v = random.nextInt(32);
                gg.blit(NOISE, x, y, u, v, tile, tile, 128, 128);
            }
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void drawCubemap(GuiGraphics gg, Minecraft mc, int width, int height, int time) {
        gg.fill(0, 0, width, height, 0xB0000000);

        int size = Math.min(width, height) / 4;
        int faces = 6;
        int totalWidth = size * faces;
        int startX = (width - totalWidth) / 2;
        int startY = height / 2 - size / 2;

        RandomSource random = RandomSource.create(time / 3L);
        for (int i = 0; i < faces; i++) {
            int shade = random.nextInt(3);
            int color = switch (shade) {
                case 0 -> 0xFF101010;
                case 1 -> 0xFF1C1C24;
                default -> 0xFF2A0A0A;
            };
            int x = startX + i * size;
            gg.fill(x + 2, startY + 2, x + size - 2, startY + size - 2, color);
            gg.fill(x + 6, startY + 6, x + size - 6, startY + size - 6, 0x33000000);
        }

        String label = "RENDERING CUBEMAP " + (time % 100) + "%";
        gg.drawCenteredString(mc.font, Component.literal(label).withStyle(ChatFormatting.GRAY),
                width / 2, startY - 16, 0xFFB0B0B0);
        gg.drawCenteredString(mc.font, Component.literal("DO NOT LOOK AT THE EDGES").withStyle(ChatFormatting.DARK_RED),
                width / 2, startY + size + 12, 0xFF8A1010);
    }

    private static void drawCapture(GuiGraphics gg, Minecraft mc, int width, int height, int time) {
        int alpha = (int) (200 + 40 * Mth.sin(time * 0.9F));
        gg.fill(0, 0, width, height, (alpha << 24));

        int shakeX = (int) (Mth.sin(time * 1.7F) * 4.0F);
        int shakeY = (int) (Mth.cos(time * 2.3F) * 3.0F);

        gg.drawCenteredString(mc.font, Component.literal("I N T E R L O P E"),
                width / 2 + shakeX, height / 2 - 20 + shakeY, 0xFF9B0D0D);
        gg.drawCenteredString(mc.font, Component.literal("s.interlope.pull:25565").withStyle(ChatFormatting.DARK_GRAY),
                width / 2, height / 2 + 4, 0xFF505050);
        gg.drawCenteredString(mc.font, Component.translatable("overlay.interloper.demo_end").withStyle(ChatFormatting.DARK_GRAY),
                width / 2, height / 2 + 24, 0xFF3A3A3A);

        String rec = "REC " + String.format("%02d:%02d", (time / 20) / 60, (time / 20) % 60);
        gg.drawString(mc.font, rec, 12, 12, 0xFFB01818);
        gg.fill(12 + mc.font.width(rec) + 6, 12, 20 + mc.font.width(rec) + 6, 20, 0xFFB01818);
    }

    private HorrorOverlay() {
    }
}
