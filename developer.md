# Modern F3 â€” Developer Guide

Add custom debug overlay modules to Modern F3 from your own NeoForge mod.

## Setup

Modern F3 targets **Minecraft 26.1** on **NeoForge 26.1.x**. Add it as a compile-time dependency in your `build.gradle`:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    compileOnly "me.almana:modern_f3:1.0.0"
}
```

In your `neoforge.mods.toml`, declare the dependency as optional so your mod still loads without Modern F3:

```toml
[[dependencies.yourmodid]]
modId = "modern_f3"
type = "optional"
versionRange = "[1.0.0,)"
ordering = "AFTER"
side = "CLIENT"
```

## Creating a Module

Implement the `OverlayModule` interface. You are responsible for rendering, sizing, and ticking your module.

The example below focuses on core logic instead of listing every accessor override:

```java
import me.almana.modern_f3.debug.overlay.OverlayModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PingModule implements OverlayModule {
    private final int defX;
    private final int defY;
    private int x, y;
    private boolean enabled = true;
    private int textColor = 0xFFFFFFFF;
    private boolean background = true;
    private int backgroundColor = 0x101010;
    private int backgroundOpacity = 0xB0;
    private boolean textShadow = true;
    private float scale = 0.75F;
    private String text = "";
    private int cachedW, cachedH;

    public PingModule(int defX, int defY) {
        this.defX = defX;
        this.defY = defY;
        this.x = defX;
        this.y = defY;
    }

    @Override public String id() { return "yourmod_ping"; }
    @Override public String displayName() { return "Ping"; }

    @Override
    public void tick() {
        var connection = Minecraft.getInstance().getConnection();
        text = connection != null ? "Ping: " + connection.getAverageLatency() + "ms" : "Ping: N/A";
    }

    @Override
    public void updateSize() {
        Font font = Minecraft.getInstance().font;
        cachedW = Math.max(1, Math.round(font.width(text) * scale)) + 6;
        cachedH = Math.max(1, Math.round(font.lineHeight * scale)) + 4;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, float partialTick) {
        updateSize();
        if (text.isEmpty()) return;
        Font font = Minecraft.getInstance().font;

        if (background) {
            int argb = (backgroundOpacity << 24) | backgroundColor;
            graphics.fill(x, y, x + cachedW, y + cachedH, argb);
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 3, y + 2);
        graphics.pose().scale(scale, scale);
        graphics.text(font, text, 0, 0, textColor, textShadow);
        graphics.pose().popMatrix();
    }
}
```

`OverlayModule` still requires all interface methods. Keep them as direct field accessors unless you need extra logic.

## Registering

All registration goes through `me.almana.modern_f3.api.ModernF3Api`. Register early â€” ideally in your mod constructor. Modules registered before the overlay initializes are queued and flushed automatically.

### Direct registration

```java
ModernF3Api.registerModule(() -> new PingModule(4, 100));
```

### Builder

The builder lets you set initial properties without hardcoding style values in your constructor:

```java
ModernF3Api.module(() -> new PingModule(4, 100))
    .position(4, 100)
    .register();
```

Builder methods are all optional — only set what you want to change.

| Method              | Description                    |
|---------------------|--------------------------------|
| `position(x, y)`   | Override default position      |
| `enabled(bool)`    | Start enabled or disabled      |
| `textColor(int)`   | ARGB text color                |
| `background(bool)` | Show/hide background           |
| `backgroundColor`  | RGB background color           |
| `backgroundOpacity`| 0-255 alpha                    |
| `textShadow(bool)` | Text drop shadow               |
| `scale(float)`     | 0.5-2.0 render scale           |

## Retrieving Modules at Runtime

```java
OverlayModule module = ModernF3Api.getModule("yourmod_ping");
if (module != null) {
    module.setEnabled(false);
}
```

## Module IDs

IDs must be unique across all mods. Prefix with your mod ID to avoid collisions: `yourmod_modulename`.

Built-in IDs follow the pattern `f3_left_N` / `f3_right_N` (0-47) for the vanilla F3 line mirrors.

## User Configuration

Registered modules are automatically:
- Saved/loaded from the overlay config
- Draggable in the edit screen (F8)
- Editable via right-click (colors, scale, background, enabled state)
- Included in profile snapshots

