# SkyOS Design Language

## Overview

SkyOS uses a premium dark futuristic visual identity designed for Hypixel SkyBlock. Every UI element should feel polished, modern, and consistent.

## Color Palette

All colors are defined in `SkyOSPalette`. Never hardcode color values in rendering code.

### Backgrounds

| Token | Hex | Usage |
|---|---|---|
| `BG_PRIMARY` | `#0A0E1A E5` | Main background surface |
| `BG_SECONDARY` | `#121829 E5` | Secondary surfaces |
| `BG_ELEVATED` | `#1A2035 E5` | Elevated cards, active panels |
| `BG_CARD` | `#0D1120 CC` | Default card background |
| `BG_OVERLAY` | `#080C15 B0` | Modal/overlay backdrop |

### Accents

| Token | Hex | Usage |
|---|---|---|
| `ACCENT_PRIMARY` | `#4A9EFF` | Primary interactive accent, borders |
| `ACCENT_SECONDARY` | `#7B4FFF` | Secondary accent, gradients |
| `ACCENT_GLOW` | `#4A9EFF 55` | Glow layers around active elements |
| `ACCENT_GLOW_SOFT` | `#4A9EFF 22` | Very soft ambient glow |

### Text

| Token | Usage |
|---|---|
| `TEXT_PRIMARY` | Titles, primary content |
| `TEXT_SECONDARY` | Labels, secondary info |
| `TEXT_MUTED` | Placeholders, disabled |
| `TEXT_ACCENT` | Highlighted values |

## Typography

- Use Minecraft's built-in text renderer (`client.textRenderer`)
- Titles: `TEXT_PRIMARY` color, shadow enabled
- Labels: `TEXT_SECONDARY`, shadow enabled
- Values: `TEXT_ACCENT` for important numbers
- Muted: `TEXT_MUTED` for secondary/disabled text
- No custom fonts (preserves vanilla compatibility)

## Spacing & Layout

- Panel padding: 8–12px
- Element gap: 4–6px
- Border radius: 4–6px for panels, 2–4px for buttons
- Consistent 1px border on all card elements

## Glow Effects

Glow is achieved by rendering 2–4 stacked transparent layers around an element, decreasing in opacity with increasing spread.

Use `RenderEngine.drawGlow()` for element glow and `RenderEngine.drawGlowBorder()` for glowing borders.

Do not overuse glow — apply only to active, focused, or highlighted elements.

## Animation

- Entry animations: `EASE_OUT_CUBIC` or `EASE_OUT_BACK` (overshoot effect)
- Exit animations: `EASE_IN_QUAD` (quick exit)
- State transitions: `EASE_OUT_QUAD`, 150–250ms duration
- Progress values: `EASE_OUT_EXPO` for snap-to-fill feel
- Spring/bounce: `EASE_OUT_ELASTIC` for celebratory moments only

Default animation duration: 200ms.

## Component Guidelines

### Panels / Cards
- Background: `BG_CARD` with `fillRounded` (radius 6)
- Border: `BORDER_SUBTLE` (1px)
- Soft ambient glow: `ACCENT_GLOW_SOFT` (3 layers)

### Accent Panels
- Background: `BG_ELEVATED`
- Border: `BORDER_ACTIVE`
- Stronger glow: `ACCENT_GLOW` (4 layers)

### Progress Bars
- Track: `BG_SECONDARY`
- Fill: gradient `ACCENT_PRIMARY` → `ACCENT_SECONDARY`
- Add glow on fill for bars > 4px wide

### Buttons
- Idle: `BG_SECONDARY`, `BORDER_SUBTLE`
- Hover: `BG_ELEVATED`, `BORDER_ACTIVE` + glow
- Pressed: `BG_PRIMARY`, slightly darker border

### Notifications
- Accent bar on left edge in type color (info/success/warning/error)
- Life remaining shown as progress bar at bottom
- Slide in from right edge, fade out

## What to Avoid

- Vanilla Minecraft GUI aesthetics (hard corners, gray backgrounds)
- Cluttered layouts — whitespace is intentional
- Inconsistent colors — always use `SkyOSPalette` tokens
- Static, unanimated state changes
- Bright, saturated backgrounds
- Forge-era UI design patterns
