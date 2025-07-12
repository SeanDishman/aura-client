package fmh.core.hud.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import java.awt.Color;
import java.util.List;

public class HudRenderer {

    public static void register() {
        HudRenderCallback.EVENT.register(HudRenderer::onHudRender);
    }

    private static void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        TextRenderer textRenderer = client.textRenderer;

        Module watermarkModule = ModuleManager.getModule("Watermark");
        if (watermarkModule != null && watermarkModule.isEnabled()) {
            drawRainbowWatermark(drawContext, textRenderer);
        }

        Module coordsModule = ModuleManager.getModule("Coords");
        if (coordsModule != null && coordsModule.isEnabled()) {
            drawCoordinates(drawContext, textRenderer, screenWidth, client.player.getBlockPos());
        }

        Module fpsModule = ModuleManager.getModule("Show FPS");
        if (fpsModule != null && fpsModule.isEnabled()) {
            drawFPS(drawContext, textRenderer, screenWidth, client);
        }

        Module showEnabledModule = ModuleManager.getModule("Show Enabled");
        if (showEnabledModule != null && showEnabledModule.isEnabled()) {
            drawEnabledModules(drawContext, textRenderer, screenWidth, client);
        }
    }

    private static void drawRainbowWatermark(DrawContext drawContext, TextRenderer textRenderer) {
        String watermark = "AURA CLIENT V1.0.0";
        int x = 10;
        int y = 10;

        WatermarkModule watermarkModule = (WatermarkModule) ModuleManager.getModule("Watermark");
        String colorMode = WatermarkModule.getColorMode();

        if (colorMode.equals("Rainbow")) {
            long time = System.currentTimeMillis();
            int currentX = x;
            for (int i = 0; i < watermark.length(); i++) {
                char c = watermark.charAt(i);
                String character = String.valueOf(c);

                float hue = (float) ((time * 0.002 + i * 0.1) % 1.0);
                Color color = Color.getHSBColor(hue, 0.8f, 1.0f);
                int rgbColor = color.getRGB();

                drawContext.drawText(textRenderer, Text.literal(character), currentX, y, rgbColor, true);
                currentX += textRenderer.getWidth(character);
            }
        } else {
            drawContext.drawText(textRenderer, Text.literal(watermark), x, y, 0xFFFFFF, true);
        }
    }

    private static void drawCoordinates(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, BlockPos playerPos) {
        MinecraftClient client = MinecraftClient.getInstance();

        String coordText = String.format("X: %d, Y: %d, Z: %d",
                playerPos.getX(),
                playerPos.getY(),
                playerPos.getZ());

        int textWidth = textRenderer.getWidth(coordText);
        int x = screenWidth - textWidth - 10;
        int y = 60; // Moved down from 25 to 60 to avoid potion effects

        if (CoordsModule.isShowPitchYaw() && client.player != null) {
            String pitchYawText = String.format("Pitch: %.1f, Yaw: %.1f",
                    client.player.getPitch(),
                    client.player.getYaw());

            int pitchYawWidth = textRenderer.getWidth(pitchYawText);
            int pitchYawX = screenWidth - pitchYawWidth - 10;
            int pitchYawY = y - 12;

            drawContext.fill(pitchYawX - 5, pitchYawY - 2, pitchYawX + pitchYawWidth + 5, pitchYawY + textRenderer.fontHeight + 2, 0x80000000);
            drawContext.drawText(textRenderer, Text.literal(pitchYawText), pitchYawX, pitchYawY, 0xFFFFFF, true);
        }

        drawContext.fill(x - 5, y - 2, x + textWidth + 5, y + textRenderer.fontHeight + 2, 0x80000000);
        drawContext.drawText(textRenderer, Text.literal(coordText), x, y, 0xFFFFFF, true);
    }

    private static void drawFPS(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, MinecraftClient client) {
        int fps = client.getCurrentFps();
        String fpsText = String.format("FPS: %d", fps);

        int textWidth = textRenderer.getWidth(fpsText);
        int x = screenWidth - textWidth - 10;

        int coordsY = 60; // Updated to match new coords position
        boolean coordsEnabled = ModuleManager.getModule("Coords") != null && ModuleManager.getModule("Coords").isEnabled();

        int y;
        if (coordsEnabled) {
            boolean showPitchYaw = CoordsModule.isShowPitchYaw();
            if (showPitchYaw) {
                y = coordsY + textRenderer.fontHeight + 7;
            } else {
                y = coordsY + textRenderer.fontHeight + 5;
            }
        } else {
            y = 60; // Changed from 25 to 60
        }

        int fpsColor;
        if (fps >= 60) {
            fpsColor = 0xFF00FF00;
        } else if (fps >= 30) {
            fpsColor = 0xFFFFFF00;
        } else {
            fpsColor = 0xFFFF0000;
        }

        drawContext.fill(x - 5, y - 2, x + textWidth + 5, y + textRenderer.fontHeight + 2, 0x80000000);
        drawContext.drawText(textRenderer, Text.literal(fpsText), x, y, fpsColor, true);
    }

    private static void drawEnabledModules(DrawContext drawContext, TextRenderer textRenderer, int screenWidth, MinecraftClient client) {
        List<Module> enabledModules = ModuleManager.getEnabledModules();

        if (enabledModules.isEmpty()) {
            return;
        }

        boolean coordsEnabled = ModuleManager.getModule("Coords") != null && ModuleManager.getModule("Coords").isEnabled();
        boolean fpsEnabled = ModuleManager.getModule("Show FPS") != null && ModuleManager.getModule("Show FPS").isEnabled();

        int startY = 60; // Changed from 25 to 60
        if (coordsEnabled) {
            boolean showPitchYaw = CoordsModule.isShowPitchYaw();
            if (showPitchYaw) {
                startY += textRenderer.fontHeight + 7;
            } else {
                startY += textRenderer.fontHeight + 5;
            }
        }

        if (fpsEnabled) {
            startY += textRenderer.fontHeight + 5;
        }

        int currentY = startY + 5;

        for (Module module : enabledModules) {
            String moduleText = module.getName();
            int textWidth = textRenderer.getWidth(moduleText);
            int x = screenWidth - textWidth - 10;

            drawContext.fill(x - 5, currentY - 2, x + textWidth + 5, currentY + textRenderer.fontHeight + 2, 0x80000000);
            drawContext.drawText(textRenderer, Text.literal(moduleText), x, currentY, 0xFF00FF00, true);

            currentY += textRenderer.fontHeight + 3;
        }
    }
}