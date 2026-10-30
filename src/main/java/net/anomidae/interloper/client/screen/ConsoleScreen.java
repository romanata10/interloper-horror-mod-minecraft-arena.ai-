package net.anomidae.interloper.client.screen;

import net.anomidae.interloper.client.ConsoleLog;
import net.anomidae.interloper.network.ConsoleCommandPacket;
import net.anomidae.interloper.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * A Source engine console. It is not supposed to be open on this map.
 */
public class ConsoleScreen extends Screen {
    private static final int WHITE = 0xFFE8E8E8;
    private static final int GRAY = 0xFF9A9A9A;
    private static final int DIM = 0xFF606060;
    private static final int LINE_HEIGHT = 10;

    private final StringBuilder input = new StringBuilder();
    private final List<String> history = new ArrayList<>();
    private int historyIndex;
    private int scroll;
    private boolean prepared;

    public ConsoleScreen() {
        super(Component.literal("Console"));
    }

    @Override
    protected void init() {
        if (!this.prepared) {
            this.prepared = true;
            if (ConsoleLog.isEmpty()) {
                this.input.append("play demo interloper_1");
            }
        }
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        int width = gg.guiWidth();
        int height = gg.guiHeight();

        gg.fill(0, 0, width, height, 0xE6070808);
        gg.fill(0, 0, width, 14, 0xFF1B1F21);
        gg.fill(0, height - 14, width, height, 0xFF1B1F21);
        gg.fill(0, 14, width, 15, 0xFF3A4145);
        gg.fill(0, height - 15, width, height - 14, 0xFF3A4145);

        gg.drawString(this.font, "Console", 6, 3, WHITE);
        gg.drawString(this.font, "Source Engine 2006  |  build 4044  |  interloper", 60, 3, DIM);
        gg.drawString(this.font, "ESC close   ENTER execute   UP/DOWN history", 6, height - 11, DIM);

        int top = 20;
        int bottom = height - 26;
        int rows = Math.max(1, (bottom - top) / LINE_HEIGHT);
        List<ConsoleLog.Line> lines = ConsoleLog.lines();
        int start = Math.max(0, lines.size() - rows - this.scroll);
        int y = top;
        for (int i = start; i < lines.size() && i < start + rows; i++) {
            ConsoleLog.Line line = lines.get(i);
            gg.drawString(this.font, line.text(), 6, y, line.color() == 0 ? WHITE : line.color());
            y += LINE_HEIGHT;
        }

        String prompt = "] " + this.input;
        gg.drawString(this.font, prompt, 6, height - 22, WHITE);
        boolean cursor = (System.currentTimeMillis() / 450L) % 2L == 0L;
        if (cursor) {
            int cursorX = 6 + this.font.width(prompt);
            gg.fill(cursorX, height - 22, cursorX + 5, height - 13, 0xFFE8E8E8);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                submit();
                return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (this.input.length() > 0) {
                    this.input.setLength(this.input.length() - 1);
                }
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                navigateHistory(-1);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                navigateHistory(1);
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_UP -> {
                this.scroll = Math.min(this.scroll + 5, Math.max(0, ConsoleLog.lines().size() - 1));
                return true;
            }
            case GLFW.GLFW_KEY_PAGE_DOWN -> {
                this.scroll = Math.max(0, this.scroll - 5);
                return true;
            }
            default -> {
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (codePoint >= 32 && codePoint != 127 && this.input.length() < 200) {
            this.input.append(codePoint);
            return true;
        }
        return false;
    }

    private void navigateHistory(int direction) {
        if (this.history.isEmpty()) {
            return;
        }
        this.historyIndex = Mth.clamp(this.historyIndex + direction, 0, this.history.size());
        this.input.setLength(0);
        if (this.historyIndex < this.history.size()) {
            this.input.append(this.history.get(this.historyIndex));
        }
    }

    private void submit() {
        String command = this.input.toString().trim();
        if (command.isEmpty()) {
            return;
        }
        this.history.add(command);
        this.historyIndex = this.history.size();
        this.scroll = 0;
        ModNetwork.sendToServer(new ConsoleCommandPacket(command));
        this.input.setLength(0);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
