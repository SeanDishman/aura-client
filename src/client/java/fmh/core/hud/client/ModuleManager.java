package fmh.core.hud.client;

import fmh.core.hud.client.combat.AutoClickerModule;
import fmh.core.hud.client.macros.FigModule;
import fmh.core.hud.client.render.OceanEspModule; // Updated import
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleManager {
    private static Map<String, List<Module>> categories = new HashMap<>();

    public static void init() {
        categories.put("HUD", new ArrayList<>());
        categories.put("Combat", new ArrayList<>());
        categories.put("Macros", new ArrayList<>());
        categories.put("Render", new ArrayList<>());

        addModule(new WatermarkModule());
        addModule(new CoordsModule());
        addModule(new FpsModule());
        addModule(new ShowEnabledModule());
        addModule(new AutoClickerModule());
        addModule(new FigModule());
        addModule(new MacroModule());
        addModule(new SeaLumisModule());
        addModule(new OceanEspModule()); // Updated to use OceanEspModule
    }

    private static void addModule(Module module) {
        categories.get(module.getCategory()).add(module);
    }

    public static Map<String, List<Module>> getCategories() {
        return categories;
    }

    public static Module getModule(String name) {
        for (List<Module> modules : categories.values()) {
            for (Module module : modules) {
                if (module.getName().equals(name)) {
                    return module;
                }
            }
        }
        return null;
    }

    public static List<Module> getEnabledModules() {
        List<Module> enabledModules = new ArrayList<>();
        for (List<Module> modules : categories.values()) {
            for (Module module : modules) {
                if (module.isEnabled()) {
                    enabledModules.add(module);
                }
            }
        }
        return enabledModules.stream()
                .filter(module -> !module.getCategory().equals("HUD"))
                .collect(Collectors.toList());
    }

    public static void tick() {
        for (List<Module> modules : categories.values()) {
            for (Module module : modules) {
                if (module instanceof AutoClickerModule) {
                    ((AutoClickerModule) module).tick();
                } else if (module instanceof FigModule) {
                    ((FigModule) module).tick();
                }
            }
        }
    }
}