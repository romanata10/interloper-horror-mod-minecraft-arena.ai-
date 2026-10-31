package net.anomidae.interloper.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.anomidae.interloper.Interloper;
import net.anomidae.interloper.util.SourceText;
import net.anomidae.interloper.world.Dimensions;
import net.anomidae.interloper.world.SourceVoid;
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

        if (mc.level.dimension().equals(Dimensions.SOURCE_VOID)) {
            renderVoidLabel(gg, mc, width, height);
        } else if (mc.level.dimension().equals(Dimensions.XBOX_DREAMS)) {
            renderXbox(gg, mc, width, height, time);
        } else if (mc.level.dimension().equals(Dimensions.WHITE_DOOR)) {
            renderWhiteRoom(gg, mc, width, height);
        }

        if (ClientEffects.proximity > 0.01F) {
            float alpha = ClientEffects.proximity * 0.45F;
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
            gg.blit(VIGNETTE, 0, 0, 0, 0, width, height, 256, 256);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (ClientEffects.darkTicks > 0) {
            float alpha = ClientEffects.darkIntensity * 0.82F;
            gg.fill(0, 0, width, height, ((int) (alpha * 255) << 24));
        }

        if (ClientEffects.corruptTicks > 0) {
            drawCorruption(gg, mc, width, height, time);
        }

        if (ClientEffects.vradTicks > 0) {
            drawVrad(gg, mc, width, height, time);
        }

        if (ClientEffects.glitchTicks > 0) {
            float pulse = 0.35F + 0.25F * Mth.sin(time * 0.7F);
            float alpha = ClientEffects.glitchIntensity * pulse;
            drawNoise(gg, width, height, alpha, time);

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

        if (ClientEffects.crashTicks > 0) {
            drawCrash(gg, mc, width, height);
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderVoidLabel(GuiGraphics gg, Minecraft mc, int width, int height) {
        gg.drawString(mc.font, "source void: minecraft", 6, 6, 0xFFBEBEBE);
        gg.drawString(mc.font, "maps/source_void.bsp", 6, 17, 0xFF5A5A5A);
        gg.drawString(mc.font, "elevation: " + (int) mc.player.getY(), 6, 28, 0xFF4A4A4A);
        gg.drawString(mc.font, "entities: 0", width - 6 - mc.font.width("entities: 0"), 6, 0xFF4A4A4A);
    }

    /**
     * The dashboard. Green, curved, and missing the menu it used to have.
     */
    private static void renderXbox(GuiGraphics gg, Minecraft mc, int width, int height, int time) {
        // green gradient, like the old blades
        for (int y = 0; y < height; y += 2) {
            float t = (float) y / height;
            int alpha = (int) (18 + 30 * t);
            int color = (alpha << 24) | (0x2A << 16) | (0x6B << 8) | 0x2A;
            gg.fill(0, y, width, y + 2, color);
        }
        // vignette-ish corners, dashboard chrome
        gg.fill(0, 0, width, 3, 0xCC8CFF00);
        gg.fill(0, height - 3, width, height, 0xCC8CFF00);

        if ((time / 90) % 12 < 8) {
            gg.drawString(mc.font, "XBOX 360 DREAMS", 8, 8, 0xFFB6FF62);
            gg.drawString(mc.font, "achievement unlocked: you are still here", 8, 20, 0xFF8CFF00);
        }
        gg.drawString(mc.font, "gamerscore: " + (mc.player == null ? 0 : mc.player.getXpLevel() * 100), 8, 32, 0xFF7ADB4A);
        gg.drawString(mc.font, "players: 1", width - 8 - mc.font.width("players: 1"), 8, 0xFF7ADB4A);
        gg.drawString(mc.font, "disk 1 / source engine", width - 8 - mc.font.width("disk 1 / source engine"),
                height - 14, 0xFF5FA83C);
    }

    private static void renderWhiteRoom(GuiGraphics gg, Minecraft mc, int width, int height) {
        gg.fill(0, 0, width, height, 0x14FFFFFF);
        gg.drawString(mc.font, "white_door_bsp", 6, 6, 0xFFFFFFFF);
        gg.drawString(mc.font, "the exit is white door and key", 6, 17, 0xFFDADADA);
        gg.drawString(mc.font, "sv_cheats: 0", width - 6 - mc.font.width("sv_cheats: 0"), 6, 0xFFB0B0B0);
    }

    private static void drawCorruption(GuiGraphics gg, Minecraft mc, int width, int height, int time) {
        RandomSource random = RandomSource.create(time / 3L);
        for (int i = 0; i < 14; i++) {
            int w = 8 + random.nextInt(70);
            int h = 4 + random.nextInt(26);
            int x = random.nextInt(Math.max(1, width - w));
            int y = random.nextInt(Math.max(1, height - h));
            gg.fill(x, y, x + w, y + h, random.nextBoolean() ? 0x33000000 : 0x22FFFFFF);
        }
        if (time % 9 == 0) {
            gg.fill(0, 0, width, height, 0x22000000);
        }
    }

    /**
     * The VRAD death: the corners of the screen stop having textures and the
     * engine starts complaining in orange.
     */
    private static void drawVrad(GuiGraphics gg, Minecraft mc, int width, int height, int time) {
        RandomSource random = RandomSource.create(time / 2L);
        float alpha = Math.min(1.0F, ClientEffects.vradIntensity + 0.2F);

        int patch = 28;
        for (int corner = 0; corner < 4; corner++) {
            int baseX = (corner % 2 == 0) ? 0 : width - patch * 2;
            int baseY = (corner < 2) ? 0 : height - patch * 2;
            for (int i = 0; i < 5; i++) {
                int x = baseX + random.nextInt(patch * 2);
                int y = baseY + random.nextInt(patch * 2);
                int size = 4 + random.nextInt(12);
                boolean red = random.nextBoolean();
                int color = red ? 0xFFFF0000 : 0xFF000000;
                if (random.nextInt(3) == 0) {
                    color = 0xFFFF5A00;
                }
                gg.fill(x, y, Math.min(width, x + size), Math.min(height, y + size), color);
            }
        }

        gg.fill(0, 0, width, height, ((int) (55 * alpha) << 24) | 0x400000);

        int lineCount = 16;
        for (int i = 0; i < lineCount; i++) {
            String text = SourceText.randomError(RandomSource.create(time / 3L + i * 31L));
            int y = (int) ((time * 3 + i * 22) % (height + 80)) - 40;
            int x = 12 + (i % 3) * 4;
            int color = 0xFFFF7A00;
            gg.drawString(mc.font, text, x, y, color);
            if (i % 4 == 0) {
                gg.drawString(mc.font, "  at vrad.cpp:" + (100 + i * 7), x + 10, y + 11, 0xFFB35A00);
            }
        }

        gg.drawString(mc.font, "ERROR", 8, height - 30, 0xFFFF0000);
        gg.drawString(mc.font, "material 'vrad/error' has no vtex", 8, height - 19, 0xFFFF9A5A);
    }

    private static void drawCrash(GuiGraphics gg, Minecraft mc, int width, int height) {
        gg.fill(0, 0, width, height, 0xF0000000);
        String title = "error:crash_by-error";
        gg.drawCenteredString(mc.font, Component.literal(title).withStyle(ChatFormatting.DARK_RED),
                width / 2, height / 2 - 12, 0xFFAA0000);
        gg.drawCenteredString(mc.font, Component.literal("the demo could not be written")
                .withStyle(ChatFormatting.DARK_GRAY), width / 2, height / 2 + 4, 0xFF606060);
        gg.drawCenteredString(mc.font, Component.literal("s.interlope.pull:25565")
                .withStyle(ChatFormatting.DARK_GRAY), width / 2, height / 2 + 18, 0xFF4A4A4A);
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
