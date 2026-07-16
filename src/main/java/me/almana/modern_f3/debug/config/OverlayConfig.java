package me.almana.modern_f3.debug.config;

import com.mojang.logging.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import me.almana.modern_f3.debug.overlay.OverlayModule;
//? if neoforge
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class OverlayConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;

    private static Path path() {
        if (configPath == null) {
            //? if neoforge {
            configPath = FMLPaths.CONFIGDIR.get().resolve("modern_f3").resolve("overlay.json");
            //?} else {
            /*configPath = net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("modern_f3").resolve("overlay.json");
            *///?}
        }
        return configPath;
    }

    public static void load(DebugOverlay overlay) {
        Path p = path();
        if (!Files.exists(p)) return;

        try (Reader reader = Files.newBufferedReader(p)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            apply(overlay, root);
        } catch (Exception e) {
            LOGGER.error("Failed to load overlay config", e);
        }
    }

    public static void save(DebugOverlay overlay) {
        JsonObject root = snapshot(overlay);

        Path p = path();
        try {
            Files.createDirectories(p.getParent());
            try (Writer writer = Files.newBufferedWriter(p)) {
                GSON.toJson(root, writer);
            }
            OverlayProfiles.saveActiveCustomSnapshot(overlay);
        } catch (Exception e) {
            LOGGER.error("Failed to save overlay config", e);
        }
    }

    public static JsonObject snapshot(DebugOverlay overlay) {
        JsonObject root = new JsonObject();
        root.addProperty("visible", overlay.isVisible());

        JsonObject mods = new JsonObject();
        for (OverlayModule m : overlay.getModules()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("enabled", m.isEnabled());
            obj.addProperty("x", m.getConfigX());
            obj.addProperty("y", m.getConfigY());
            obj.addProperty("color", String.format("%06X", m.getTextColor() & 0x00FFFFFF));
            obj.addProperty("background", m.showBackground());
            obj.addProperty("background_color", String.format("%06X", m.getBackgroundColor() & 0x00FFFFFF));
            obj.addProperty("background_opacity", m.getBackgroundOpacity());
            obj.addProperty("shadow", m.useTextShadow());
            obj.addProperty("scale", m.getScale());
            mods.add(m.id(), obj);
        }
        root.add("modules", mods);
        return root;
    }

    public static void apply(DebugOverlay overlay, JsonObject root) {
        if (root == null) {
            return;
        }

        if (root.has("visible")) overlay.setVisible(root.get("visible").getAsBoolean());

        JsonObject mods = root.getAsJsonObject("modules");
        if (mods == null) {
            return;
        }

        for (var entry : mods.entrySet()) {
            OverlayModule module = overlay.findModule(entry.getKey());
            if (module == null) continue;
            JsonObject obj = entry.getValue().getAsJsonObject();

            if (obj.has("enabled")) module.setEnabled(obj.get("enabled").getAsBoolean());
            if (obj.has("x") && obj.has("y")) module.setPosition(obj.get("x").getAsInt(), obj.get("y").getAsInt());
            if (obj.has("color")) {
                module.setTextColor(0xFF000000 | Integer.parseInt(obj.get("color").getAsString(), 16));
            }
            if (obj.has("background")) module.setShowBackground(obj.get("background").getAsBoolean());
            if (obj.has("background_color")) {
                module.setBackgroundColor(Integer.parseInt(obj.get("background_color").getAsString(), 16));
            }
            if (obj.has("background_opacity")) {
                module.setBackgroundOpacity(obj.get("background_opacity").getAsInt());
            }
            if (obj.has("shadow")) module.setTextShadow(obj.get("shadow").getAsBoolean());
            if (obj.has("scale")) module.setScale(obj.get("scale").getAsFloat());
        }
    }
}
