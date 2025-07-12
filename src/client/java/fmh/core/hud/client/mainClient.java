package fmh.core.hud.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class mainClient implements ClientModInitializer {

    private static KeyBinding toggleGuiKey;

    @Override
    public void onInitializeClient() {
        toggleGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.gui.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.hud"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleGuiKey.wasPressed()) {
                client.setScreen(new ClickGuiScreen());
            }

            // Tick all modules
            ModuleManager.tick();
        });

        HudRenderer.register();
        ModuleManager.init();
    }
}