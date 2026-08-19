package me.almana.modern_f3.debug.overlay;

import com.mojang.blaze3d.platform.InputConstants;
import me.almana.modern_f3.ModernF3;
import me.almana.modern_f3.client.Compat;
import me.almana.modern_f3.api.ModernF3Api;
import me.almana.modern_f3.debug.config.OverlayConfig;
import me.almana.modern_f3.debug.config.OverlayProfiles;
import me.almana.modern_f3.debug.overlay.modules.DebugLineModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
//? if neoforge
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugOverlay {
    public static final int DEFAULT_X = 4;
    public static final int DEFAULT_Y = 4;
    public static final int DEFAULT_ROW_STEP = 12;
    public static final int SNAP_X_STEP = 8;
    private static final int LEGACY_ROW_STEP = 17;
    private static final int LINE_MODULE_COUNT = 48;

    private static DebugOverlay instance;

    //? if >=26.1 {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
        Identifier.fromNamespaceAndPath(ModernF3.MODID, "modern_f3")
    );
    //?} else {
    /*public static final String CATEGORY = "key.category.modern_f3.modern_f3";
    *///?}

    //? if neoforge {
    public static final KeyMapping TOGGLE_KEY = new KeyMapping(
        "key.modern_f3.toggle_overlay",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_F3),
        CATEGORY
    );
    //?} else {
    /*public static final KeyMapping TOGGLE_KEY = new KeyMapping(
        "key.modern_f3.toggle_overlay",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F3,
        CATEGORY
    );
    *///?}

    public static final KeyMapping EDIT_KEY = new KeyMapping(
        "key.modern_f3.edit_overlay",
        InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8,
        CATEGORY
    );

    private final DebugScreenMirror screenMirror = new DebugScreenMirror();
    private final List<OverlayModule> modules = new ArrayList<>();
    private boolean visible;
    private boolean editMode;

    private DebugOverlay() {
        for (int i = 0; i < LINE_MODULE_COUNT; i++) {
            modules.add(new DebugLineModule("f3_left_" + i, "F3 Left " + i, screenMirror, false, i, DEFAULT_X, DEFAULT_Y + i * DEFAULT_ROW_STEP));
        }
        for (int i = 0; i < LINE_MODULE_COUNT; i++) {
            modules.add(new DebugLineModule("f3_right_" + i, "F3 Right " + i, screenMirror, true, i, 0, DEFAULT_Y + i * DEFAULT_ROW_STEP));
        }
        ModernF3Api.flushPending(this);
        applyDefaultPreset();
        OverlayConfig.load(this);
        OverlayProfiles.applyStoredProfile(this);
        tightenLegacySpacing();
    }

    public static DebugOverlay get() {
        if (instance == null) instance = new DebugOverlay();
        return instance;
    }

    public void toggle() {
        visible = !visible;
        OverlayConfig.save(this);
    }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { visible = v; }
    public boolean isEditMode() { return editMode; }
    public void setEditMode(boolean m) { editMode = m; screenMirror.setEditMode(m); }
    public List<OverlayModule> getModules() { return Collections.unmodifiableList(modules); }

    public void addModule(OverlayModule module) {
        if (findModule(module.id()) != null) return;
        modules.add(module);
    }

    public OverlayModule findModule(String id) {
        for (OverlayModule m : modules) {
            if (m.id().equals(id)) return m;
        }
        return null;
    }

    public void tick() {
        if (!visible) return;
        for (OverlayModule m : modules) {
            m.tick();
        }
    }

    public void renderLayer(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!visible || editMode || Compat.screen(Minecraft.getInstance()) != null) return;
        float partial = deltaTracker.getGameTimeDeltaPartialTick(true);
        renderModules(graphics, partial);
    }

    public void renderOnScreen(GuiGraphicsExtractor graphics, float partial) {
        if (!visible || editMode) return;
        renderModules(graphics, partial);
    }

    private void renderModules(GuiGraphicsExtractor graphics, float partial) {
        for (OverlayModule m : modules) {
            if (m.isActive()) m.render(graphics, partial);
        }
    }

    public void resetAllModules() {
        for (OverlayModule module : modules) {
            module.setPosition(module.defaultX(), module.defaultY());
            module.setEnabled(true);
            module.setTextColor(0xFFFFFFFF);
            module.setShowBackground(true);
            module.setBackgroundColor(0x101010);
            module.setBackgroundOpacity(0xB0);
            module.setTextShadow(true);
            module.setScale(module.defaultScale());
        }
    }

    public void applyVanillaPreset() {
        resetAllModules();
        for (OverlayModule module : modules) {
            module.setEnabled(isLineModule(module));
        }
    }

    public void applyDefaultPreset() {
        applyVanillaPreset();
    }

    private boolean isLineModule(OverlayModule module) {
        return module.id().startsWith("f3_left_") || module.id().startsWith("f3_right_");
    }

    private void tightenLegacySpacing() {
        for (OverlayModule module : modules) {
            String id = module.id();
            int lastUnderscore = id.lastIndexOf('_');
            if (lastUnderscore < 0) {
                continue;
            }

            int index;
            try {
                index = Integer.parseInt(id.substring(lastUnderscore + 1));
            } catch (NumberFormatException ignored) {
                continue;
            }

            int legacyY = DEFAULT_Y + index * LEGACY_ROW_STEP;
            if (module.getConfigY() != legacyY) {
                continue;
            }

            module.setPosition(module.getConfigX(), DEFAULT_Y + index * DEFAULT_ROW_STEP);
        }
    }
}
