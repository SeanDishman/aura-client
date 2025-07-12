package fmh.core.hud.client;

import fmh.core.hud.client.combat.AutoClickerModule;
import fmh.core.hud.client.render.OceanEspModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {
    private Map<String, CategoryPanel> panels = new HashMap<>();

    public ClickGuiScreen() {
        super(Text.literal("ClickGUI"));

        int x = 50;
        for (String category : ModuleManager.getCategories().keySet()) {
            panels.put(category, new CategoryPanel(category, x, 50, 120, 20));
            x += 140;
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (CategoryPanel panel : panels.values()) {
            panel.render(context, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels.values()) {
            if (panel.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (CategoryPanel panel : panels.values()) {
            panel.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryPanel panel : panels.values()) {
            panel.mouseReleased();
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (AutoClickerModule.isEditingMinCps()) {
            return AutoClickerModule.handleMinCpsKeyPress(keyCode);
        }

        if (AutoClickerModule.isEditingMaxCps()) {
            return AutoClickerModule.handleMaxCpsKeyPress(keyCode);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (AutoClickerModule.isEditingMinCps()) {
            return AutoClickerModule.handleMinCpsCharTyped(chr);
        }

        if (AutoClickerModule.isEditingMaxCps()) {
            return AutoClickerModule.handleMaxCpsCharTyped(chr);
        }

        return super.charTyped(chr, modifiers);
    }
}

class CategoryPanel {
    private String category;
    private int x, y, width, headerHeight;
    private boolean expanded = true;
    private boolean dragging = false;
    private int dragOffsetX, dragOffsetY;
    private boolean draggingOceanSlider = false;

    public CategoryPanel(String category, int x, int y, int width, int headerHeight) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.headerHeight = headerHeight;
    }

    public void render(DrawContext context, int mouseX, int mouseY) {
        long time = System.currentTimeMillis();
        List<Module> modules = ModuleManager.getCategories().get(category);

        int height = headerHeight;
        if (expanded) {
            height += modules.size() * 15;
        }

        context.fill(x, y, x + width, y + height, 0x90000000);
        context.drawBorder(x, y, width, height, getLavaColor(time, 0));

        context.fill(x + 1, y + 1, x + width - 1, y + headerHeight - 1, 0x60000000);
        context.drawBorder(x + 1, y + 1, width - 2, headerHeight - 2, 0xFFFFFFFF);

        drawLavaText(context, category, x + 5, y + 6, time);

        if (expanded) {
            int moduleY = y + headerHeight;
            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                int moduleColor = module.isEnabled() ? 0xFF00FF00 : 0xFFFFFFFF;

                if (mouseX >= x && mouseX <= x + width && mouseY >= moduleY && mouseY <= moduleY + 15) {
                    context.fill(x, moduleY, x + width, moduleY + 15, 0x40FFFFFF);
                }

                context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(module.getName()), x + 10, moduleY + 3, moduleColor, false);

                if (module instanceof WatermarkModule) {
                    int symbolX = x + width - 15;
                    String symbol = WatermarkModule.isDropdownOpen() ? "-" : "+";
                    int symbolColor = WatermarkModule.isDropdownOpen() ? 0xFFFF0000 : 0xFF00FF00;
                    context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(symbol), symbolX, moduleY + 3, symbolColor, false);

                    if (WatermarkModule.isDropdownOpen()) {
                        String[] options = {"Rainbow", "Default"};
                        int dropdownX = x + width + 5;
                        int dropdownY = moduleY;
                        int dropdownWidth = 80;

                        context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + (options.length * 12), 0x90000000);
                        context.drawBorder(dropdownX, dropdownY, dropdownWidth, options.length * 12, 0xFFFFFFFF);

                        for (int j = 0; j < options.length; j++) {
                            String option = options[j];
                            boolean selected = option.equals(WatermarkModule.getColorMode());
                            int optionColor = selected ? 0xFF00FF00 : 0xFFAAAAAA;
                            int optionY = dropdownY + (j * 12);

                            if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                                    mouseY >= optionY && mouseY <= optionY + 12) {
                                context.fill(dropdownX, optionY, dropdownX + dropdownWidth, optionY + 12, 0x40FFFFFF);
                            }

                            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(option), dropdownX + 5, optionY + 2, optionColor, false);
                        }
                    }
                }

                if (module instanceof CoordsModule) {
                    int symbolX = x + width - 15;
                    String symbol = CoordsModule.isDropdownOpen() ? "-" : "+";
                    int symbolColor = CoordsModule.isDropdownOpen() ? 0xFFFF0000 : 0xFF00FF00;
                    context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(symbol), symbolX, moduleY + 3, symbolColor, false);

                    if (CoordsModule.isDropdownOpen()) {
                        String option = "Show Pitch and Yaw";
                        boolean selected = CoordsModule.isShowPitchYaw();
                        int optionColor = selected ? 0xFF00FF00 : 0xFFAAAAAA;
                        int dropdownX = x + width + 5;
                        int dropdownY = moduleY;
                        int dropdownWidth = 120;

                        context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + 12, 0x90000000);
                        context.drawBorder(dropdownX, dropdownY, dropdownWidth, 12, 0xFFFFFFFF);

                        if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                                mouseY >= dropdownY && mouseY <= dropdownY + 12) {
                            context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + 12, 0x40FFFFFF);
                        }

                        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(option), dropdownX + 5, dropdownY + 2, optionColor, false);
                    }
                }

                if (module instanceof AutoClickerModule) {
                    int symbolX = x + width - 15;
                    String symbol = AutoClickerModule.isDropdownOpen() ? "-" : "+";
                    int symbolColor = AutoClickerModule.isDropdownOpen() ? 0xFFFF0000 : 0xFF00FF00;
                    context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(symbol), symbolX, moduleY + 3, symbolColor, false);

                    if (AutoClickerModule.isDropdownOpen()) {
                        int dropdownX = x + width + 5;
                        int dropdownY = moduleY;
                        int dropdownWidth = 150;
                        int dropdownHeight = 95;

                        context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + dropdownHeight, 0x90000000);
                        context.drawBorder(dropdownX, dropdownY, dropdownWidth, dropdownHeight, 0xFFFFFFFF);

                        int currentY = dropdownY + 5;

                        String minCpsText = AutoClickerModule.isEditingMinCps() ? AutoClickerModule.getMinCpsInput() : "Min CPS: " + String.format("%.1f", AutoClickerModule.getMinCps());
                        int minCpsColor = AutoClickerModule.isEditingMinCps() ? 0xFFFFFF00 : 0xFFFFFFFF;

                        if (mouseX >= dropdownX + 5 && mouseX <= dropdownX + 145 && mouseY >= currentY && mouseY <= currentY + 10) {
                            context.fill(dropdownX + 5, currentY, dropdownX + 145, currentY + 10, 0x40FFFFFF);
                        }

                        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(minCpsText), dropdownX + 5, currentY, minCpsColor, false);
                        currentY += 12;

                        drawSlider(context, dropdownX + 5, currentY, 140, AutoClickerModule.getMinCps(), 1.0, 20.0, 0xFF00FF00);
                        currentY += 15;

                        String maxCpsText = AutoClickerModule.isEditingMaxCps() ? AutoClickerModule.getMaxCpsInput() : "Max CPS: " + String.format("%.1f", AutoClickerModule.getMaxCps());
                        int maxCpsColor = AutoClickerModule.isEditingMaxCps() ? 0xFFFFFF00 : 0xFFFFFFFF;

                        if (mouseX >= dropdownX + 5 && mouseX <= dropdownX + 145 && mouseY >= currentY && mouseY <= currentY + 10) {
                            context.fill(dropdownX + 5, currentY, dropdownX + 145, currentY + 10, 0x40FFFFFF);
                        }

                        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(maxCpsText), dropdownX + 5, currentY, maxCpsColor, false);
                        currentY += 12;

                        drawSlider(context, dropdownX + 5, currentY, 140, AutoClickerModule.getMaxCps(), 1.0, 20.0, 0xFF0088FF);
                        currentY += 15;

                        String mouseDownText = "Mouse Down: " + (AutoClickerModule.isRequireMouseDown() ? "ON" : "OFF");
                        int mouseDownColor = AutoClickerModule.isRequireMouseDown() ? 0xFF00FF00 : 0xFFFF0000;
                        if (mouseX >= dropdownX + 5 && mouseX <= dropdownX + 145 && mouseY >= currentY && mouseY <= currentY + 10) {
                            context.fill(dropdownX + 5, currentY, dropdownX + 145, currentY + 10, 0x40FFFFFF);
                        }
                        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(mouseDownText), dropdownX + 5, currentY, mouseDownColor, false);
                        currentY += 15;

                        String clickTypeText = AutoClickerModule.isLeftClick() ? "Left Click" : "Right Click";
                        int clickTypeColor = 0xFFFFFFFF;
                        int buttonX = dropdownX + 5;
                        int buttonY = currentY;
                        int buttonWidth = 70;
                        int buttonHeight = 12;

                        context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0xFF333333);
                        context.drawBorder(buttonX, buttonY, buttonWidth, buttonHeight, 0xFFAAAAAA);

                        if (mouseX >= buttonX && mouseX <= buttonX + buttonWidth && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                            context.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, 0x40FFFFFF);
                        }

                        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(clickTypeText);
                        int textX = buttonX + (buttonWidth - textWidth) / 2;
                        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(clickTypeText), textX, buttonY + 2, clickTypeColor, false);
                    }
                }

                if (module instanceof OceanEspModule) {
                    int symbolX = x + width - 15;
                    String symbol = OceanEspModule.isDropdownOpen() ? "-" : "+";
                    int symbolColor = OceanEspModule.isDropdownOpen() ? 0xFFFF0000 : 0xFF00FF00;
                    context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(symbol), symbolX, moduleY + 3, symbolColor, false);

                    if (OceanEspModule.isDropdownOpen()) {
                        int dropdownX = x + width + 5;
                        int dropdownY = moduleY;
                        int dropdownWidth = 160;

                        // Calculate dynamic height based on what's open
                        int baseHeight = 85; // Distance slider + MOBS/BLOCKS headers + padding
                        if (OceanEspModule.isMobsDropdownOpen()) {
                            baseHeight += 84; // 7 mobs * 12 pixels each
                        }
                        if (OceanEspModule.isBlocksDropdownOpen()) {
                            baseHeight += 20; // 1 block option * 12 pixels + extra padding
                        }
                        int dropdownHeight = baseHeight;

                        context.fill(dropdownX, dropdownY, dropdownX + dropdownWidth, dropdownY + dropdownHeight, 0x90000000);
                        context.drawBorder(dropdownX, dropdownY, dropdownWidth, dropdownHeight, 0xFFFFFFFF);

                        int yPos = dropdownY + 8;

                        context.drawText(MinecraftClient.getInstance().textRenderer,
                                Text.literal("Distance: " + String.format("%.0f", OceanEspModule.getRenderDistance())),
                                dropdownX + 8, yPos, 0xFFFFFFFF, false);
                        yPos += 15;

                        drawSlider(context, dropdownX + 8, yPos, 144, OceanEspModule.getRenderDistance(), 0.0, 300.0, 0xFF00FF00);
                        yPos += 20;

                        // MOBS section
                        drawLavaText(context, "MOBS", dropdownX + 8, yPos, time);
                        String mobSymbol = OceanEspModule.isMobsDropdownOpen() ? "-" : "+";
                        int mobSymbolColor = OceanEspModule.isMobsDropdownOpen() ? 0xFFFF0000 : 0xFF00FF00;
                        int mobSymbolX = dropdownX + 8 + MinecraftClient.getInstance().textRenderer.getWidth("MOBS") + 3;
                        int mobSymbolY = yPos;

                        if (mouseX >= mobSymbolX && mouseX <= mobSymbolX + 10 && mouseY >= mobSymbolY && mouseY <= mobSymbolY + 12) {
                            context.fill(mobSymbolX, mobSymbolY, mobSymbolX + 10, mobSymbolY + 12, 0x40FFFFFF);
                        }
                        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(mobSymbol), mobSymbolX, mobSymbolY, mobSymbolColor, false);
                        yPos += 15;

                        if (OceanEspModule.isMobsDropdownOpen()) {
                            String[] mobs = {"Tropical Fish", "Pufferfish", "Cod", "Salmon", "Frogs", "Dolphins", "Sea Turtles"};
                            for (String mob : mobs) {
                                boolean selected = OceanEspModule.getMobSelection().getOrDefault(mob, false);
                                String text = (selected ? "[✓] " : "[ ] ") + mob;
                                int color = selected ? 0xFF00AAFF : 0xFFAAAAAA;
                                context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(text), dropdownX + 16, yPos, color, false);
                                yPos += 12;
                            }
                        }

                        // BLOCKS section
                        yPos += 8; // More spacing before BLOCKS
                        drawLavaText(context, "BLOCKS", dropdownX + 8, yPos, time);
                        String blockSymbol = OceanEspModule.isBlocksDropdownOpen() ? "-" : "+";
                        int blockSymbolColor = OceanEspModule.isBlocksDropdownOpen() ? 0xFFFF0000 : 0xFF00FF00;
                        int blockSymbolX = dropdownX + 8 + MinecraftClient.getInstance().textRenderer.getWidth("BLOCKS") + 3;
                        int blockSymbolY = yPos;

                        if (mouseX >= blockSymbolX && mouseX <= blockSymbolX + 10 && mouseY >= blockSymbolY && mouseY <= blockSymbolY + 12) {
                            context.fill(blockSymbolX, blockSymbolY, blockSymbolX + 10, blockSymbolY + 12, 0x40FFFFFF);
                        }
                        context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(blockSymbol), blockSymbolX, blockSymbolY, blockSymbolColor, false);
                        yPos += 15;

                        if (OceanEspModule.isBlocksDropdownOpen()) {
                            boolean seaPicklesSelected = OceanEspModule.getBlockSelection().getOrDefault("Sea Pickles", false);
                            String seaPicklesText = (seaPicklesSelected ? "[✓] " : "[ ] ") + "Sea Lumis";
                            int seaPicklesColor = seaPicklesSelected ? 0xFF80FF00 : 0xFFAAAAAA;
                            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(seaPicklesText), dropdownX + 16, yPos, seaPicklesColor, false);
                        }
                    }
                }

                moduleY += 15;
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + headerHeight) {
            if (button == 0) {
                dragging = true;
                dragOffsetX = (int) (mouseX - x);
                dragOffsetY = (int) (mouseY - y);
                return true;
            } else if (button == 1) {
                expanded = !expanded;
                return true;
            }
        }

        if (expanded && mouseX >= x && mouseX <= x + width) {
            List<Module> modules = ModuleManager.getCategories().get(category);
            int moduleY = y + headerHeight;
            for (Module module : modules) {
                if (mouseY >= moduleY && mouseY <= moduleY + 15) {
                    if (module instanceof WatermarkModule) {
                        int symbolX = x + width - 15;
                        if (mouseX >= symbolX && mouseX <= symbolX + 10) {
                            WatermarkModule.toggleDropdown();
                        } else if (!WatermarkModule.isDropdownOpen()) {
                            module.toggle();
                        }
                    } else if (module instanceof CoordsModule) {
                        int symbolX = x + width - 15;
                        if (mouseX >= symbolX && mouseX <= symbolX + 10) {
                            CoordsModule.toggleDropdown();
                        } else if (!CoordsModule.isDropdownOpen()) {
                            module.toggle();
                        }
                    } else if (module instanceof AutoClickerModule) {
                        int symbolX = x + width - 15;
                        if (mouseX >= symbolX && mouseX <= symbolX + 10) {
                            AutoClickerModule.toggleDropdown();
                        } else {
                            if (AutoClickerModule.isDropdownOpen()) {
                                int dropdownX = x + width + 5;
                                int dropdownY = moduleY;
                                int dropdownWidth = 150;
                                int dropdownHeight = 95;

                                if (!(mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                                        mouseY >= dropdownY && mouseY <= dropdownY + dropdownHeight)) {
                                    module.toggle();
                                }
                            } else {
                                module.toggle();
                            }
                        }
                    } else if (module instanceof OceanEspModule) {
                        int symbolX = x + width - 15;
                        if (mouseX >= symbolX && mouseX <= symbolX + 10) {
                            OceanEspModule.toggleDropdown();
                        } else {
                            if (OceanEspModule.isDropdownOpen()) {
                                int dropdownX = x + width + 5;
                                int dropdownY = moduleY;
                                int dropdownWidth = 160;

                                // Calculate dynamic height
                                int baseHeight = 70;
                                if (OceanEspModule.isMobsDropdownOpen()) {
                                    baseHeight += 84;
                                }
                                if (OceanEspModule.isBlocksDropdownOpen()) {
                                    baseHeight += 12;
                                }
                                int dropdownHeight = baseHeight;

                                if (!(mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                                        mouseY >= dropdownY && mouseY <= dropdownY + dropdownHeight)) {
                                    module.toggle();
                                }
                            } else {
                                module.toggle();
                            }
                        }
                    } else {
                        module.toggle();
                    }
                    return true;
                }
                moduleY += 15;
            }
        }

        if (expanded) {
            List<Module> modules = ModuleManager.getCategories().get(category);
            int moduleY = y + headerHeight;
            for (Module module : modules) {
                if (module instanceof WatermarkModule && WatermarkModule.isDropdownOpen()) {
                    String[] options = {"Rainbow", "Default"};
                    int dropdownX = x + width + 5;
                    int dropdownY = moduleY;
                    int dropdownWidth = 80;

                    for (int j = 0; j < options.length; j++) {
                        String option = options[j];
                        int optionY = dropdownY + (j * 12);
                        if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                                mouseY >= optionY && mouseY <= optionY + 12) {
                            WatermarkModule.setColorMode(option);
                            return true;
                        }
                    }
                }

                if (module instanceof CoordsModule && CoordsModule.isDropdownOpen()) {
                    int dropdownX = x + width + 5;
                    int dropdownY = moduleY;
                    int dropdownWidth = 120;
                    if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                            mouseY >= dropdownY && mouseY <= dropdownY + 12) {
                        CoordsModule.setShowPitchYaw(!CoordsModule.isShowPitchYaw());
                        return true;
                    }
                }

                if (module instanceof AutoClickerModule && AutoClickerModule.isDropdownOpen()) {
                    int dropdownX = x + width + 5;
                    int dropdownY = moduleY;
                    int dropdownWidth = 150;
                    int dropdownHeight = 95;

                    if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                            mouseY >= dropdownY && mouseY <= dropdownY + dropdownHeight) {

                        int relativeY = (int) (mouseY - dropdownY);

                        if (relativeY >= 5 && relativeY <= 15 && mouseX >= dropdownX + 5 && mouseX <= dropdownX + 145) {
                            AutoClickerModule.startEditingMinCps();
                            return true;
                        }

                        if (relativeY >= 32 && relativeY <= 42 && mouseX >= dropdownX + 5 && mouseX <= dropdownX + 145) {
                            AutoClickerModule.startEditingMaxCps();
                            return true;
                        }

                        if (relativeY >= 14 && relativeY <= 35) {
                            if (!AutoClickerModule.isEditingMinCps()) {
                                AutoClickerModule.setDraggingMinCps(true);
                                double sliderPos = Math.max(0, Math.min(1, (mouseX - dropdownX - 5) / 140.0));
                                AutoClickerModule.setMinCps(1.0 + sliderPos * 19.0);
                            }
                            return true;
                        }

                        if (relativeY >= 41 && relativeY <= 62) {
                            if (!AutoClickerModule.isEditingMaxCps()) {
                                AutoClickerModule.setDraggingMaxCps(true);
                                double sliderPos = Math.max(0, Math.min(1, (mouseX - dropdownX - 5) / 140.0));
                                AutoClickerModule.setMaxCps(1.0 + sliderPos * 19.0);
                            }
                            return true;
                        }

                        if (relativeY >= 61 && relativeY <= 76 && mouseX >= dropdownX + 5 && mouseX <= dropdownX + 145) {
                            AutoClickerModule.setRequireMouseDown(!AutoClickerModule.isRequireMouseDown());
                            return true;
                        }

                        if (relativeY >= 77 && relativeY <= 89 && mouseX >= dropdownX + 5 && mouseX <= dropdownX + 75) {
                            AutoClickerModule.setLeftClick(!AutoClickerModule.isLeftClick());
                            return true;
                        }

                        return true;
                    }
                }

                if (module instanceof OceanEspModule && OceanEspModule.isDropdownOpen()) {
                    int dropdownX = x + width + 5;
                    int dropdownY = moduleY;
                    int dropdownWidth = 160;

                    // Calculate dynamic height
                    int baseHeight = 70;
                    if (OceanEspModule.isMobsDropdownOpen()) {
                        baseHeight += 84;
                    }
                    if (OceanEspModule.isBlocksDropdownOpen()) {
                        baseHeight += 12;
                    }
                    int dropdownHeight = baseHeight;

                    if (mouseX >= dropdownX && mouseX <= dropdownX + dropdownWidth &&
                            mouseY >= dropdownY && mouseY <= dropdownY + dropdownHeight) {

                        int relativeY = (int) (mouseY - dropdownY);

                        // Distance slider
                        if (relativeY >= 23 && relativeY <= 43) {
                            draggingOceanSlider = true;
                            double sliderPos = Math.max(0, Math.min(1, (mouseX - dropdownX - 8) / 144.0));
                            OceanEspModule.setRenderDistance(sliderPos * 300.0);
                            return true;
                        }

                        // MOBS click area
                        int mobSymbolX = dropdownX + 8 + MinecraftClient.getInstance().textRenderer.getWidth("MOBS") + 3;
                        int mobSymbolY = dropdownY + 43;
                        if (mouseX >= mobSymbolX && mouseX <= mobSymbolX + 10 && mouseY >= mobSymbolY && mouseY <= mobSymbolY + 12) {
                            OceanEspModule.toggleMobsDropdown();
                            return true;
                        }

                        if (OceanEspModule.isMobsDropdownOpen()) {
                            String[] mobs = {"Tropical Fish", "Pufferfish", "Cod", "Salmon", "Frogs", "Dolphins", "Sea Turtles"};
                            int mobStartY = 58; // Starting Y for mob items
                            for (int i = 0; i < mobs.length; i++) {
                                int mobY = mobStartY + (i * 12);
                                if (relativeY >= mobY && relativeY <= mobY + 12 && mouseX >= dropdownX + 16 && mouseX <= dropdownX + 152) {
                                    OceanEspModule.toggleMobSelection(mobs[i]);
                                    return true;
                                }
                            }
                        }

                        // BLOCKS click area - calculate position based on whether mobs dropdown is open
                        int blocksYPos = OceanEspModule.isMobsDropdownOpen() ? 147 : 63; // Adjust Y position
                        int blockSymbolX = dropdownX + 8 + MinecraftClient.getInstance().textRenderer.getWidth("BLOCKS") + 3;
                        int blockSymbolY = dropdownY + blocksYPos;
                        if (mouseX >= blockSymbolX && mouseX <= blockSymbolX + 10 && mouseY >= blockSymbolY && mouseY <= blockSymbolY + 12) {
                            OceanEspModule.toggleBlocksDropdown();
                            return true;
                        }

                        if (OceanEspModule.isBlocksDropdownOpen()) {
                            int blockItemY = blocksYPos + 15;
                            if (relativeY >= blockItemY && relativeY <= blockItemY + 12 && mouseX >= dropdownX + 16 && mouseX <= dropdownX + 152) {
                                OceanEspModule.toggleBlockSelection("Sea Pickles");
                                return true;
                            }
                        }

                        return true;
                    }
                }

                moduleY += 15;
            }
        }

        return false;
    }

    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            x = (int) mouseX - dragOffsetX;
            y = (int) mouseY - dragOffsetY;
        }

        if (AutoClickerModule.isDraggingMinCps() || AutoClickerModule.isDraggingMaxCps()) {
            int dropdownX = x + width + 5;
            double sliderPos = Math.max(0, Math.min(1, (mouseX - dropdownX - 5) / 140.0));
            double value = 1.0 + sliderPos * 19.0;

            if (AutoClickerModule.isDraggingMinCps()) {
                AutoClickerModule.setMinCps(value);
            } else if (AutoClickerModule.isDraggingMaxCps()) {
                AutoClickerModule.setMaxCps(value);
            }
        }

        if (draggingOceanSlider) {
            int dropdownX = x + width + 5;
            double sliderPos = Math.max(0, Math.min(1, (mouseX - dropdownX - 8) / 144.0));
            OceanEspModule.setRenderDistance(sliderPos * 300.0);
        }
    }

    public void mouseReleased() {
        dragging = false;
        AutoClickerModule.setDraggingMinCps(false);
        AutoClickerModule.setDraggingMaxCps(false);
        draggingOceanSlider = false;
    }

    private void drawSlider(DrawContext context, int x, int y, int width, double value, double min, double max, int color) {
        context.fill(x, y + 2, x + width, y + 6, 0xFF444444);

        double progress = (value - min) / (max - min);
        int sliderPos = (int) (progress * width);

        context.fill(x + sliderPos - 3, y - 1, x + sliderPos + 3, y + 9, color);
        context.drawBorder(x + sliderPos - 3, y - 1, 6, 10, 0xFFFFFFFF);
    }

    private void drawLavaText(DrawContext context, String text, int x, int y, long time) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            String character = String.valueOf(c);

            float progress = (float) ((time * 0.001 + i * 0.2) % 2.0);
            if (progress > 1.0f) progress = 2.0f - progress;

            int red = 255;
            int green = (int) (165 * progress);
            int blue = 0;
            int lavaColor = (255 << 24) | (red << 16) | (green << 8) | blue;

            context.drawText(MinecraftClient.getInstance().textRenderer, Text.literal(character), x, y, lavaColor, false);
            x += MinecraftClient.getInstance().textRenderer.getWidth(character);
        }
    }

    private int getLavaColor(long time, int offset) {
        float progress = (float) ((time * 0.001 + offset * 0.2) % 2.0);
        if (progress > 1.0f) progress = 2.0f - progress;

        int red = 255;
        int green = (int) (165 * progress);
        int blue = 0;
        return (255 << 24) | (red << 16) | (green << 8) | blue;
    }
}