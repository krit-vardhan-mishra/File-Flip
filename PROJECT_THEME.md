# FileFlip: Project Description & Theming Guide

## 🚀 Why FileFlip Was Created?
**FileFlip** was born out of the need for a **privacy-focused, offline-first** productivity tool that doesn't compromise on aesthetics. 

While many editors exist, most are either too bloated or lack professional export capabilities. FileFlip fills this gap by providing:
- **Local-Only Processing**: Your data never leaves your device. Perfect for developers and writers handling sensitive logs, config files, or notes.
- **Polished Experience**: A "developer-first" interface that feels like a desktop IDE but optimized for mobile and web.
- **Professional Export**: Transforming raw text/markdown into clean, GitHub-styled PDFs ready for documentation or sharing.

---

## 🎨 Visual Identity & Theming

### 1. Color Palette (Oceanic Dark)
The app uses a curated "Oceanic Dark" theme designed to reduce eye strain while maintaining high contrast for code editing.

| Element | Hex Code | Purpose |
| :--- | :--- | :--- |
| **Primary Blue** | `#0DA6F2` | Buttons, Active States, Brand Identity |
| **Surface Dark** | `#101C22` | App Background, Main UI Shell |
| **Surface Variant** | `#1A2830` | Cards, Sidebar, Input Fields |
| **Text Primary** | `#F1F5F9` | High-emphasis headings and body text |
| **Text Secondary** | `#94A3B8` | Subtitles, Gutter numbers, Descriptions |
| **Accent Orange** | `#FF9F1C` | Warning icons, Highlighted features |
| **Accent Emerald** | `#10B981` | Success states, Validated file status |

> **Note:** The Android app also includes a **GitHub Dark Gray** mode (`#0D1117`) for users who prefer the classic repository aesthetic.

### 2. Typography
A mix of modern sans-serif for UI and high-legibility monospace for code.

- **UI Font**: **Inter** (Next.js) / **System Sans-Serif** (Android)
  - *Vibe*: Clean, neutral, and highly readable at small sizes.
- **Code Font**: **Roboto Mono** / **JetBrains Mono**
  - *Vibe*: Precise, distinct characters, optimized for long reading sessions.

### 3. Design Patterns
- **Material Design 3 (M3)**: Utilizing the latest Google design language with large corner radii (24dp/px) and adaptive color schemes.
- **Polished Minimalism**: No unnecessary clutter. The focus is entirely on the content, with subtle micro-animations (0.2s cubic-bezier) and hover transforms to make the interface feel "alive".
- **Glassmorphism Hints**: Used in headers and overlays to provide depth without distracting from the editor.

---

## 📸 For GitHub & Play Store Screenshots
When capturing screenshots, ensure:
1. **The Editor**: Showcases syntax highlighting with the **Oceanic Dark** background.
2. **The Preview**: Displays a rendered Markdown file with **GitHub-style** formatting.
3. **The Dashboard**: Highlights the **Surface Variant** cards and the **Primary Blue** floating action button (FAB).
4. **Theme Toggling**: If possible, show a side-by-side of Oceanic vs. GitHub themes.
