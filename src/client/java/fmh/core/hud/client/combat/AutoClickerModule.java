package fmh.core.hud.client.combat;

import fmh.core.hud.client.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.Random;

public class AutoClickerModule extends Module {
    private static boolean dropdownOpen = false;
    private static double minCps = 8.0;
    private static double maxCps = 12.0;
    private static boolean requireMouseDown = true;
    private static boolean leftClick = true;
    private static boolean draggingMinCps = false;
    private static boolean draggingMaxCps = false;

    private static boolean editingMinCps = false;
    private static boolean editingMaxCps = false;
    private static String minCpsInput = "";
    private static String maxCpsInput = "";

    private long lastClickTime = 0;
    private long nextClickDelay = 0;
    private Random random = new Random();

    public AutoClickerModule() {
        super("AutoClicker", "Combat");
        setEnabled(false);
    }

    @Override
    public void onEnable() {
        lastClickTime = System.currentTimeMillis();
        calculateNextDelay();
    }

    @Override
    public void onDisable() {
        // Reset any ongoing operations
    }

    public void tick() {
        if (!isEnabled()) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.currentScreen != null) return;

        if (requireMouseDown) {
            boolean mousePressed = leftClick ?
                    GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS :
                    GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

            if (!mousePressed) return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= nextClickDelay) {
            performClick();
            lastClickTime = currentTime;
            calculateNextDelay();
        }
    }

    private void performClick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (leftClick) {
            if (client.crosshairTarget != null) {
                switch (client.crosshairTarget.getType()) {
                    case ENTITY:
                        net.minecraft.util.hit.EntityHitResult entityHit = (net.minecraft.util.hit.EntityHitResult) client.crosshairTarget;
                        client.interactionManager.attackEntity(client.player, entityHit.getEntity());
                        client.player.swingHand(Hand.MAIN_HAND);
                        break;
                    case BLOCK:
                        net.minecraft.util.hit.BlockHitResult blockHit = (net.minecraft.util.hit.BlockHitResult) client.crosshairTarget;
                        client.interactionManager.attackBlock(blockHit.getBlockPos(), blockHit.getSide());
                        client.player.swingHand(Hand.MAIN_HAND);
                        break;
                    default:
                        client.player.swingHand(Hand.MAIN_HAND);
                        break;
                }
            } else {
                client.player.swingHand(Hand.MAIN_HAND);
            }
        } else {
            if (client.crosshairTarget != null && client.crosshairTarget.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
                net.minecraft.util.hit.BlockHitResult blockHit = (net.minecraft.util.hit.BlockHitResult) client.crosshairTarget;
                client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, blockHit);
            } else {
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            }
        }
    }

    private void calculateNextDelay() {
        double randomCps = minCps + (maxCps - minCps) * random.nextDouble();
        nextClickDelay = (long) (1000.0 / randomCps);
    }

    public static boolean isDropdownOpen() {
        return dropdownOpen;
    }

    public static void toggleDropdown() {
        dropdownOpen = !dropdownOpen;
    }

    public static void setDropdownOpen(boolean open) {
        dropdownOpen = open;
    }

    public static double getMinCps() {
        return minCps;
    }

    public static void setMinCps(double cps) {
        minCps = Math.max(0.1, cps);
        if (minCps > maxCps) {
            maxCps = minCps;
        }
    }

    public static double getMaxCps() {
        return maxCps;
    }

    public static void setMaxCps(double cps) {
        maxCps = Math.max(0.1, cps);
        if (maxCps < minCps) {
            minCps = maxCps;
        }
    }

    public static boolean isRequireMouseDown() {
        return requireMouseDown;
    }

    public static void setRequireMouseDown(boolean require) {
        requireMouseDown = require;
    }

    public static boolean isLeftClick() {
        return leftClick;
    }

    public static void setLeftClick(boolean left) {
        leftClick = left;
    }

    public static boolean isDraggingMinCps() {
        return draggingMinCps;
    }

    public static void setDraggingMinCps(boolean dragging) {
        draggingMinCps = dragging;
    }

    public static boolean isDraggingMaxCps() {
        return draggingMaxCps;
    }

    public static void setDraggingMaxCps(boolean dragging) {
        draggingMaxCps = dragging;
    }

    public static boolean isEditingMinCps() {
        return editingMinCps;
    }

    public static boolean isEditingMaxCps() {
        return editingMaxCps;
    }

    public static String getMinCpsInput() {
        return minCpsInput;
    }

    public static String getMaxCpsInput() {
        return maxCpsInput;
    }

    public static void startEditingMinCps() {
        editingMinCps = true;
        editingMaxCps = false;
        minCpsInput = "Min CPS: ";
    }

    public static void startEditingMaxCps() {
        editingMaxCps = true;
        editingMinCps = false;
        maxCpsInput = "Max CPS: ";
    }

    public static boolean handleMinCpsKeyPress(int keyCode) {
        if (keyCode == 257) { // Enter key
            try {
                String numberPart = minCpsInput.replace("Min CPS: ", "").trim();
                if (!numberPart.isEmpty()) {
                    double value = Double.parseDouble(numberPart);
                    setMinCps(value);
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
            editingMinCps = false;
            minCpsInput = "";
            return true;
        } else if (keyCode == 256) { // Escape key
            editingMinCps = false;
            minCpsInput = "";
            return true;
        } else if (keyCode == 259) { // Backspace
            if (minCpsInput.length() > "Min CPS: ".length()) {
                minCpsInput = minCpsInput.substring(0, minCpsInput.length() - 1);
            }
            return true;
        }
        return false;
    }

    public static boolean handleMaxCpsKeyPress(int keyCode) {
        if (keyCode == 257) { // Enter key
            try {
                String numberPart = maxCpsInput.replace("Max CPS: ", "").trim();
                if (!numberPart.isEmpty()) {
                    double value = Double.parseDouble(numberPart);
                    setMaxCps(value);
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
            editingMaxCps = false;
            maxCpsInput = "";
            return true;
        } else if (keyCode == 256) { // Escape key
            editingMaxCps = false;
            maxCpsInput = "";
            return true;
        } else if (keyCode == 259) { // Backspace
            if (maxCpsInput.length() > "Max CPS: ".length()) {
                maxCpsInput = maxCpsInput.substring(0, maxCpsInput.length() - 1);
            }
            return true;
        }
        return false;
    }

    public static boolean handleMinCpsCharTyped(char chr) {
        if (Character.isDigit(chr) || chr == '.') {
            minCpsInput += chr;
            return true;
        }
        return false;
    }

    public static boolean handleMaxCpsCharTyped(char chr) {
        if (Character.isDigit(chr) || chr == '.') {
            maxCpsInput += chr;
            return true;
        }
        return false;
    }
}