package fmh.core.hud.client;

public abstract class Module {
    private String name;
    private String category;
    private boolean enabled;

    public Module(String name, String category) {
        this.name = name;
        this.category = category;
        this.enabled = false;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onEnable() {}
    public void onDisable() {}

    public String getName() { return name; }
    public String getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }
}

class WatermarkModule extends Module {
    public static String colorMode = "Rainbow";
    public static boolean dropdownOpen = false;

    public WatermarkModule() {
        super("Watermark", "HUD");
        setEnabled(true);
    }

    public static void setColorMode(String mode) {
        colorMode = mode;
        dropdownOpen = false;
    }

    public static String getColorMode() {
        return colorMode;
    }

    public static boolean isDropdownOpen() {
        return dropdownOpen;
    }

    public static void toggleDropdown() {
        dropdownOpen = !dropdownOpen;
    }
}

class CoordsModule extends Module {
    public static boolean showPitchYaw = false;
    public static boolean dropdownOpen = false;

    public CoordsModule() {
        super("Coords", "HUD");
        setEnabled(false);
    }

    public static void setShowPitchYaw(boolean show) {
        showPitchYaw = show;
        dropdownOpen = false;
    }

    public static boolean isShowPitchYaw() {
        return showPitchYaw;
    }

    public static boolean isDropdownOpen() {
        return dropdownOpen;
    }

    public static void toggleDropdown() {
        dropdownOpen = !dropdownOpen;
    }
}

class FpsModule extends Module {
    public FpsModule() {
        super("Show FPS", "HUD");
        setEnabled(false);
    }
}

class ShowEnabledModule extends Module {
    public ShowEnabledModule() {
        super("Show Enabled", "HUD");
        setEnabled(true);
    }
}

class FigModule extends Module {
    public FigModule() {
        super("Fig", "Macros");
        setEnabled(false);
    }
}

class MacroModule extends Module {
    public MacroModule() {
        super("Mangrove", "Macros");
        setEnabled(false);
    }
}

class SeaLumisModule extends Module {
    public SeaLumisModule() {
        super("Sea Lumis", "Macros");
        setEnabled(false);
    }
}