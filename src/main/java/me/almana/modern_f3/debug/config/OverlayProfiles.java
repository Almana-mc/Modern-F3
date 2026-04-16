package me.almana.modern_f3.debug.config;

import com.mojang.logging.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.almana.modern_f3.debug.overlay.DebugOverlay;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OverlayProfiles {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_ID = "builtin:default";
    private static final String LEGACY_VANILLA_ID = "builtin:vanilla";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final List<StoredProfile> customProfiles = new ArrayList<>();

    private static Path configPath;
    private static boolean loaded;
    private static boolean hasStoredSelection;
    private static String activeProfileId = DEFAULT_ID;

    private static Path path() {
        if (configPath == null) {
            configPath = FMLPaths.CONFIGDIR.get().resolve("modern_f3").resolve("profiles.json");
        }
        return configPath;
    }

    public static List<ProfileOption> options() {
        load();
        List<ProfileOption> options = new ArrayList<>();
        options.add(new ProfileOption(DEFAULT_ID, Component.translatable("profile.modern_f3.default")));
        for (StoredProfile profile : customProfiles) {
            options.add(new ProfileOption(profile.id, Component.literal(profile.name)));
        }
        return options;
    }

    public static String activeProfileId() {
        load();
        return activeProfileId;
    }

    public static void applyStoredProfile(DebugOverlay overlay) {
        load();
        if (!hasStoredSelection) {
            return;
        }
        applyProfile(overlay, activeProfileId);
    }

    public static void setActiveProfile(DebugOverlay overlay, String profileId) {
        load();
        activeProfileId = knownProfileId(profileId);
        applyProfile(overlay, activeProfileId);
        saveStorage();
        OverlayConfig.save(overlay);
    }

    public static void createProfile(DebugOverlay overlay, String name) {
        load();
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            return;
        }

        customProfiles.add(new StoredProfile("custom:" + UUID.randomUUID(), trimmedName, profileSnapshot(overlay)));
        activeProfileId = customProfiles.get(customProfiles.size() - 1).id;
        saveStorage();
        OverlayConfig.save(overlay);
    }

    public static void saveActiveCustomSnapshot(DebugOverlay overlay) {
        load();
        StoredProfile activeProfile = findCustomProfile(activeProfileId);
        if (activeProfile == null) {
            return;
        }

        activeProfile.snapshot = profileSnapshot(overlay);
        saveStorage();
    }

    private static void applyProfile(DebugOverlay overlay, String profileId) {
        if (DEFAULT_ID.equals(profileId) || LEGACY_VANILLA_ID.equals(profileId)) {
            overlay.applyDefaultPreset();
            return;
        }

        StoredProfile customProfile = findCustomProfile(profileId);
        overlay.resetAllModules();
        if (customProfile != null) {
            OverlayConfig.apply(overlay, customProfile.snapshot.deepCopy());
        }
    }

    private static JsonObject profileSnapshot(DebugOverlay overlay) {
        JsonObject snapshot = OverlayConfig.snapshot(overlay);
        snapshot.remove("visible");
        return snapshot;
    }

    private static void load() {
        if (loaded) {
            return;
        }

        loaded = true;
        Path p = path();
        if (!Files.exists(p)) {
            return;
        }

        hasStoredSelection = true;

        try (Reader reader = Files.newBufferedReader(p)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                return;
            }

            if (root.has("active_profile")) {
                activeProfileId = root.get("active_profile").getAsString();
            }

            JsonArray profiles = root.getAsJsonArray("custom_profiles");
            if (profiles == null) {
                validateActiveProfile();
                return;
            }

            for (JsonElement element : profiles) {
                JsonObject profile = element.getAsJsonObject();
                if (profile == null || !profile.has("id") || !profile.has("name")) {
                    continue;
                }

                JsonObject snapshot = profile.getAsJsonObject("snapshot");
                customProfiles.add(new StoredProfile(
                    profile.get("id").getAsString(),
                    profile.get("name").getAsString(),
                    snapshot == null ? new JsonObject() : snapshot.deepCopy()
                ));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load profiles", e);
        }

        validateActiveProfile();
    }

    private static void saveStorage() {
        validateActiveProfile();

        JsonObject root = new JsonObject();
        root.addProperty("active_profile", activeProfileId);

        JsonArray profiles = new JsonArray();
        for (StoredProfile profile : customProfiles) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", profile.id);
            entry.addProperty("name", profile.name);
            entry.add("snapshot", profile.snapshot.deepCopy());
            profiles.add(entry);
        }
        root.add("custom_profiles", profiles);

        Path p = path();
        try {
            Files.createDirectories(p.getParent());
            try (Writer writer = Files.newBufferedWriter(p)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save profiles", e);
        }
    }

    private static void validateActiveProfile() {
        activeProfileId = knownProfileId(activeProfileId);
    }

    private static String knownProfileId(String profileId) {
        if (DEFAULT_ID.equals(profileId) || LEGACY_VANILLA_ID.equals(profileId)) {
            return DEFAULT_ID;
        }
        return findCustomProfile(profileId) != null ? profileId : DEFAULT_ID;
    }

    private static StoredProfile findCustomProfile(String profileId) {
        for (StoredProfile profile : customProfiles) {
            if (profile.id.equals(profileId)) {
                return profile;
            }
        }
        return null;
    }

    public static class ProfileOption {
        private final String id;
        private final Component label;

        public ProfileOption(String id, Component label) {
            this.id = id;
            this.label = label;
        }

        public String id() {
            return id;
        }

        public Component label() {
            return label;
        }
    }

    private static class StoredProfile {
        private final String id;
        private final String name;
        private JsonObject snapshot;

        private StoredProfile(String id, String name, JsonObject snapshot) {
            this.id = id;
            this.name = name;
            this.snapshot = snapshot;
        }
    }
}
