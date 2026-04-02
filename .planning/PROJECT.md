# Modern F3 — Charts & Graphs

## What This Is

A NeoForge client mod that replaces Minecraft's vanilla F3 debug screen with a modular, customizable overlay. Players can reposition, style, and toggle individual debug info lines. Other mod developers can register custom modules via a public API. This milestone adds real-time chart and graph modules for performance metrics, with full API support for third-party chart creation.

## Core Value

Live, glanceable performance charts (FPS, RAM, TPS, Ping) rendered inside the existing overlay system, with a builder API that lets other devs create their own charts just as easily.

## Requirements

### Validated

- ✓ Modular F3 overlay replaces vanilla debug screen — existing
- ✓ Drag-and-drop module positioning via edit mode (F8) — existing
- ✓ Per-module styling (color, scale, shadow, background) — existing
- ✓ Profile system for saving/loading overlay layouts — existing
- ✓ Public API for registering custom modules (ModernF3Api) — existing
- ✓ Configuration persistence via JSON — existing
- ✓ Mixin-based F3 key interception — existing

### Active

- [ ] Line graph module type for time-series metrics (rolling window)
- [ ] Pie chart module type for breakdown visualization
- [ ] Built-in FPS chart with configurable time window
- [ ] Built-in RAM chart (used/allocated/max)
- [ ] Built-in TPS chart (client-integrated server or packet-based)
- [ ] Built-in Ping chart (network latency)
- [ ] Chart edit screen with time window, data range, and per-series color config
- [ ] Builder API for devs to create line graph modules
- [ ] Builder API for devs to create pie chart modules
- [ ] Data source abstraction for feeding values into charts

### Out of Scope

- Server-side TPS reporting protocol — rely on client-side integrated server data or existing packets
- Bar charts or histogram types — line + pie covers the use cases for v1
- Chart data export or logging to file — visualization only
- Custom chart rendering shaders — use standard GuiGraphics drawing

## Context

- Brownfield project: overlay system, module interface, config, profiles, and API already exist
- Current `OverlayModule` interface handles text-line modules; charts need different rendering but same positioning/styling contract
- `ModuleBuilder` already uses builder pattern for custom modules — chart builder should follow same conventions
- NeoForge 26.1 alpha on Minecraft 1.21.11, Java 25
- Rendering via Blaze3D / GuiGraphics — charts drawn with fill, line, and text primitives

## Constraints

- **Tech stack**: NeoForge 26.1 / Minecraft 1.21.11 / Java 25 — no external charting libraries
- **Performance**: Charts render every frame in the GUI layer — must be lightweight, no allocations in hot path
- **API compatibility**: New chart API must not break existing `ModernF3Api.module()` / `ModuleBuilder` usage
- **Rendering**: All drawing through Minecraft's `GuiGraphics` (or equivalent extractor) — no raw OpenGL

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Line graphs + pie charts for v1 | Covers time-series monitoring and breakdown visualization without overcomplicating | — Pending |
| Builder API over interface-only | Matches existing ModuleBuilder pattern, lower friction for devs | — Pending |
| Full chart editing in F8 screen | Users expect same level of control as text modules | — Pending |
| No external libraries | Must run in Minecraft's classloader, keep mod lightweight | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd:transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd:complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-02 after initialization*
