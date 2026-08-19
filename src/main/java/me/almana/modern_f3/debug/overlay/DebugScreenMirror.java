package me.almana.modern_f3.debug.overlay;

import me.almana.modern_f3.client.Compat;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jspecify.annotations.Nullable;
//?} else {
/*import me.almana.modern_f3.mixin.DebugScreenOverlayAccessor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
*///?}

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DebugScreenMirror {
    private final Minecraft minecraft = Minecraft.getInstance();
    private long buildTick = Long.MIN_VALUE;
    private Snapshot cachedFull = Snapshot.EMPTY;
    private Snapshot cachedFiltered = Snapshot.EMPTY;

    //? if >=26.1 {
    private static final Comparator<Identifier> VANILLA_FIRST =
        Comparator.<Identifier, Boolean>comparing(id -> !id.getNamespace().equals("minecraft"))
            .thenComparing(Identifier::toString);

    private @Nullable ChunkPos lastPos;
    private @Nullable LevelChunk clientChunk;
    private @Nullable CompletableFuture<LevelChunk> serverChunk;
    private boolean editMode;
    private boolean currentEntryActive;
    //?}

    public void setEditMode(boolean editMode) {
        //? if >=26.1
        this.editMode = editMode;
    }

    public Snapshot snapshot() {
        //? if >=26.1 {
        rebuild();
        return editMode ? cachedFull : cachedFiltered;
        //?} else {
        /*rebuildLegacy();
        return cachedFull;
        *///?}
    }

    //? if <26.1 {
    /*private void rebuildLegacy() {
        long guiTick = Compat.guiTicks(minecraft);
        if (buildTick == guiTick) return;
        buildTick = guiTick;

        if (!minecraft.isGameLoadFinished()
            || minecraft.level == null
            || minecraft.getConnection() == null
            || minecraft.getCameraEntity() == null
            || Compat.hudHidden(minecraft) && Compat.screen(minecraft) == null) {
            cachedFull = Snapshot.EMPTY;
            return;
        }

        DebugScreenOverlay overlay = minecraft.gui.getDebugOverlay();
        DebugScreenOverlayAccessor accessor = (DebugScreenOverlayAccessor) overlay;
        accessor.modernF3$setBlock(minecraft.getCameraEntity().pick(20.0, 0.0F, false));
        accessor.modernF3$setLiquid(minecraft.getCameraEntity().pick(20.0, 0.0F, true));

        CompactResult left = compactLegacy(accessor.modernF3$getGameInformation());
        CompactResult right = compactLegacy(accessor.modernF3$getSystemInformation());
        cachedFull = new Snapshot(left.lines, right.lines, left.inactive, right.inactive);
    }
    *///?}

    //? if >=26.1 {
    private void rebuild() {
        long guiTick = Compat.guiTicks(minecraft);
        if (buildTick == guiTick) return;
        buildTick = guiTick;

        if (!minecraft.isGameLoadFinished() || Compat.hudHidden(minecraft) && Compat.screen(minecraft) == null) {
            cachedFull = Snapshot.EMPTY;
            cachedFiltered = Snapshot.EMPTY;
            return;
        }

        updateChunkCache();

        boolean reduced = minecraft.showOnlyReducedInfo();
        Level level = getLevel();

        List<Identifier> allIds = new ArrayList<>(DebugScreenEntries.allEntries().keySet());
        allIds.sort(VANILLA_FIRST);

        if (editMode) {
            cachedFull = buildSnapshot(allIds, reduced, level, true);
        } else {
            List<Identifier> enabledIds = new ArrayList<>();
            for (Identifier id : allIds) {
                DebugScreenEntryStatus status = minecraft.debugEntries.getStatus(id);
                if (status == DebugScreenEntryStatus.ALWAYS_ON || status == DebugScreenEntryStatus.IN_OVERLAY) {
                    enabledIds.add(id);
                }
            }
            cachedFiltered = buildSnapshot(enabledIds, reduced, level, false);
        }
    }

    private Snapshot buildSnapshot(Iterable<Identifier> entryIds, boolean reduced, @Nullable Level level, boolean trackActive) {
        List<String> leftLines = new ArrayList<>();
        List<Boolean> leftActive = new ArrayList<>();
        List<String> rightLines = new ArrayList<>();
        List<Boolean> rightActive = new ArrayList<>();
        Map<Identifier, Collection<String>> groups = new LinkedHashMap<>();
        Map<Identifier, List<Boolean>> groupsActive = new LinkedHashMap<>();
        List<String> regularLines = new ArrayList<>();
        List<Boolean> regularActive = new ArrayList<>();

        DebugScreenDisplayer displayer = new DebugScreenDisplayer() {
            @Override
            public void addPriorityLine(String line) {
                if (leftLines.size() > rightLines.size()) {
                    rightLines.add(line);
                    rightActive.add(currentEntryActive);
                } else {
                    leftLines.add(line);
                    leftActive.add(currentEntryActive);
                }
            }

            @Override
            public void addLine(String line) {
                regularLines.add(line);
                regularActive.add(currentEntryActive);
            }

            @Override
            public void addToGroup(Identifier group, Collection<String> lines) {
                groups.computeIfAbsent(group, ignored -> new ArrayList<>()).addAll(lines);
                List<Boolean> flags = groupsActive.computeIfAbsent(group, ignored -> new ArrayList<>());
                for (int i = 0; i < lines.size(); i++) {
                    flags.add(currentEntryActive);
                }
            }

            @Override
            public void addToGroup(Identifier group, String line) {
                groups.computeIfAbsent(group, ignored -> new ArrayList<>()).add(line);
                groupsActive.computeIfAbsent(group, ignored -> new ArrayList<>()).add(currentEntryActive);
            }
        };

        for (Identifier id : entryIds) {
            DebugScreenEntry entry = DebugScreenEntries.getEntry(id);
            if (entry == null || !entry.isAllowed(reduced)) continue;

            if (trackActive) {
                DebugScreenEntryStatus status = minecraft.debugEntries.getStatus(id);
                currentEntryActive = (status == DebugScreenEntryStatus.ALWAYS_ON || status == DebugScreenEntryStatus.IN_OVERLAY);
            } else {
                currentEntryActive = true;
            }
            entry.display(displayer, level, getClientChunk(), getServerChunk());
        }

        balanceLines(leftLines, leftActive, rightLines, rightActive,
            regularLines, regularActive, groups, groupsActive);

        CompactResult leftResult = compactLines(leftLines, leftActive);
        CompactResult rightResult = compactLines(rightLines, rightActive);
        return new Snapshot(leftResult.lines, rightResult.lines,
            leftResult.inactive, rightResult.inactive);
    }

    private void updateChunkCache() {
        ChunkPos chunkPos = null;
        if (minecraft.getCameraEntity() != null && minecraft.level != null) {
            BlockPos feetPos = minecraft.getCameraEntity().blockPosition();
            chunkPos = ChunkPos.containing(feetPos);
        }

        if (!Objects.equals(lastPos, chunkPos)) {
            lastPos = chunkPos;
            clientChunk = null;
            serverChunk = null;
        }
    }

    private void balanceLines(
        List<String> leftLines, List<Boolean> leftActive,
        List<String> rightLines, List<Boolean> rightActive,
        List<String> regularLines, List<Boolean> regularActive,
        Map<Identifier, Collection<String>> groups, Map<Identifier, List<Boolean>> groupsActive
    ) {
        if (!leftLines.isEmpty()) {
            leftLines.add("");
            leftActive.add(true);
        }
        if (!rightLines.isEmpty()) {
            rightLines.add("");
            rightActive.add(true);
        }

        if (!regularLines.isEmpty()) {
            int mid = (regularLines.size() + 1) / 2;
            leftLines.addAll(regularLines.subList(0, mid));
            leftActive.addAll(regularActive.subList(0, mid));
            rightLines.addAll(regularLines.subList(mid, regularLines.size()));
            rightActive.addAll(regularActive.subList(mid, regularActive.size()));
            leftLines.add("");
            leftActive.add(true);
            if (mid < regularLines.size()) {
                rightLines.add("");
                rightActive.add(true);
            }
        }

        List<Collection<String>> groupedLines = new ArrayList<>(groups.values());
        List<List<Boolean>> groupedFlags = new ArrayList<>(groupsActive.values());
        if (groupedLines.isEmpty()) {
            return;
        }

        int mid = (groupedLines.size() + 1) / 2;
        for (int i = 0; i < groupedLines.size(); i++) {
            Collection<String> lines = groupedLines.get(i);
            List<Boolean> flags = i < groupedFlags.size() ? groupedFlags.get(i) : List.of();
            if (lines.isEmpty()) {
                continue;
            }

            if (i < mid) {
                leftLines.addAll(lines);
                leftActive.addAll(flags);
                leftLines.add("");
                leftActive.add(true);
            } else {
                rightLines.addAll(lines);
                rightActive.addAll(flags);
                rightLines.add("");
                rightActive.add(true);
            }
        }
    }

    private @Nullable Level getLevel() {
        if (minecraft.level == null) {
            return null;
        }
        return Optional.ofNullable(minecraft.getSingleplayerServer())
            .map(server -> server.getLevel(minecraft.level.dimension()))
            .map(Level.class::cast)
            .orElse(minecraft.level);
    }

    private @Nullable ServerLevel getServerLevel() {
        if (minecraft.level == null) {
            return null;
        }

        return Optional.ofNullable(minecraft.getSingleplayerServer())
            .map(server -> server.getLevel(minecraft.level.dimension()))
            .orElse(null);
    }

    private @Nullable LevelChunk getServerChunk() {
        if (minecraft.level == null || lastPos == null) {
            return null;
        }

        if (serverChunk == null) {
            ServerLevel level = getServerLevel();
            if (level == null) {
                return null;
            }

            serverChunk = level.getChunkSource()
                .getChunkFuture(lastPos.x(), lastPos.z(), ChunkStatus.FULL, false)
                .thenApply(result -> (LevelChunk) result.orElse(null));
        }

        return serverChunk.getNow(null);
    }

    private @Nullable LevelChunk getClientChunk() {
        if (minecraft.level == null || lastPos == null) {
            return null;
        }

        if (clientChunk == null) {
            clientChunk = minecraft.level.getChunk(lastPos.x(), lastPos.z());
        }

        return clientChunk;
    }

    //?}

    private static CompactResult compactLines(List<String> lines, List<Boolean> active) {
        LinkedHashMap<String, Boolean> seen = new LinkedHashMap<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.isEmpty()) {
                seen.merge(line, active.get(i), (a, b) -> a || b);
            }
        }
        List<String> compacted = List.copyOf(seen.keySet());
        BitSet inactive = new BitSet(compacted.size());
        int idx = 0;
        for (Boolean isActive : seen.values()) {
            if (!isActive) inactive.set(idx);
            idx++;
        }
        return new CompactResult(compacted, inactive);
    }

    private static CompactResult compactLegacy(List<String> lines) {
        return compactLines(lines, lines.stream().map(line -> true).toList());
    }

    private record CompactResult(List<String> lines, BitSet inactive) {}

    public record Snapshot(List<String> leftLines, List<String> rightLines,
                           BitSet inactiveLeft, BitSet inactiveRight) {
        public static final Snapshot EMPTY = new Snapshot(List.of(), List.of(), new BitSet(), new BitSet());
    }
}
