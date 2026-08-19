# Modern F3

[![Join our discord!](https://media.forgecdn.net/attachments/description/1448257/description_c5e501e5-328a-46c7-a594-bb0c451254a5.png)](https://discord.gg/xTeHR2tdYh)

A modern replacement for Minecraft's debug overlay. Fully customizable, draggable, and profile-based — all without replacing the vanilla F3 data.

Supports Minecraft 1.21.1, 26.1.2, and 26.2 on Fabric and NeoForge.

## Features

1. Mirrors all vanilla F3 debug lines (left and right columns) with a clean, styled overlay.
2. Every module is **draggable** — open the edit screen with **F8** and place things where you want.
3. **Right-click any module** to customize text color, background color, opacity, scale, and text shadow.
4. **Profiles** — save and switch between different layouts instantly.
5. **Snap-to-grid** — hold Shift while dragging for pixel-perfect alignment.
6. **Copy & paste styles** across modules or apply a style to all modules at once.
7. Full **developer API** for adding custom modules from other mods.

## Controls

| Key | Action |
|-----|--------|
| **F3** | Toggle the debug overlay |
| **F8** | Open the edit screen |
| **Left-click + drag** | Move a module (edit screen) |
| **Shift + drag** | Snap to grid |
| **Right-click** | Edit module settings |

## Official Partners

We are partnered with [Kinetic Host](https://billing.kinetichosting.com/aff.php?aff=128) who help support the continuous development of this mod. Truly, a great host with really good prices and support.

[![image](https://media.forgecdn.net/attachments/description/1448257/description_9c39c92c-3a05-48d8-bf69-e0576dc4c2e5.png)](https://billing.kinetichosting.com/aff.php?aff=1285)

## Pack Developers

Modern F3 stores its config per-profile. Profiles are saved in the game directory under `.minecraft/config/modern_f3/`. You can distribute preset profiles with your modpack by including these files.

## Development

See [developer.md](developer.md) for the full API guide on adding custom overlay modules from your own mod.

The project uses Gradle Stonecutter. Build a target with `./gradlew :<minecraft>-<loader>:build -x copyjar`, such as `./gradlew :1.21.1-fabric:build -x copyjar`.

As a solo developer, updates may take time sometimes. I wish you will be patient with me and my work. For any questions, feature requests, or bug reports — join the [Discord](https://discord.gg/xTeHR2tdYh).
