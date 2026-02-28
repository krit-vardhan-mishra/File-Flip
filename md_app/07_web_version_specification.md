# FileFlip Web - Project Documentation & Strategy

## 1. Project Overview
**FileFlip Web** is the browser-based extension of the FileFlip ecosystem. It is designed for users who prefer a desktop workflow or wish to quickly edit and convert files without installing a native application. The platform serves as a high-performance, offline-capable web tool for editing, previewing, and exporting various file formats, primarily focusing on Markdown to PDF conversion.

### **Core Vision**
To provide a seamless, platform-agnostic experience where the transition between the Android app and the Web version feels invisible, maintained through strict adherence to the **Material 3 Expressive Design** system.

---

## 2. Design Foundation (CRITICAL)
> **IMPORTANT:** The website MUST mirror the mobile app's "Expressive" personality. This involves bold typography, dynamic color palettes (Deep Navy/Primary Blue), and fluid, spring-based animations.

### **2.1 Visual Language**
- **Design System:** Material Design 3 (M3).
- **Color Palette:** 
    - *Primary:* Blue (#0DA6F2)
    - *Surface:* Deep Navy / Background Dark (#101C22)
    - *Accent:* Icon Orange (#FFFF9F1C) / Icon Emerald (#10B981)
- **Typography:** Modern Sans-serif (Inter/Roboto) with strict adherence to M3 Type Scale (Display, Headline, Title, Body, Label).
- **Shapes:** Extra-large corner radius (24dp+) for cards and containers to match the mobile "Expressive" style.

### **2.2 Animation Patterns**
- **Transitions:** Layout transitions must use "Shared Element" logic where possible.
- **Feedback:** Use ripple effects on all clickable elements.
- **Entry:** Elements should cascade in with a slight "overshoot" spring easing (0.4s duration).

---

## 3. Requirements

### **3.1 Functional Requirements**
- **File Upload:** Drag-and-drop or file picker support for .md, .json, .yaml, .xml, .txt, .html, .log, .csv.
- **Multi-Tab Editor:** High-performance code editor with syntax highlighting.
- **Live Preview:** Side-by-side (desktop) or toggleable (mobile) live rendering.
- **Local Persistence:** Automatic saving to browser `IndexedDB` or `localStorage` to prevent data loss.
- **Professional PDF Export Engine (CRITICAL):**
    - **NO Browser Print API:** We will avoid `window.print()` as it often scales poorly and clips content.
    - **Replicate "MD to PDF Blazing" (VS Code):** Use a dedicated client-side PDF generation library (like `jsPDF` + `html2canvas` or `Puppeteer-core` in a serverless function) to ensure pixel-perfect reproduction of the GitHub-style Markdown theme.
    - **Pagination Logic:** Implement custom CSS `@page` break handling to prevent text from being cut in the middle of a line.
    - **Styles:** Hard-coded injection of `github-markdown.css` and `highlight.js` themes specifically for the PDF renderer.

### **3.2 Non-Functional Requirements**
- **Offline-First:** Progressive Web App (PWA) capabilities to work without internet.
- **Responsiveness:** Single codebase handling 320px (Mobile) to 2560px+ (Ultrawide).
- **Performance:** Instant typing feedback even in large (1MB+) files.

---

## 4. Implementation Plan

### **Phase 1: Scaffolding (Week 1)**
- Initialize React/Next.js with TypeScript.
- Configure Tailwind CSS with the FileFlip Material 3 color tokens.
- Implement the M3 component library (Buttons, Cards, Bottom Sheets).

### **Phase 2: Core Editor & Engine (Week 2)**
- Integrate CodeMirror 6 (optimized for mobile/web).
- Implement the "Converter Engine" (Markdown to HTML parser).
- Build the File System Access API integration for browser-based file management.

### **Phase 3: Responsive UI (Week 3)**
- Build the "Adaptive Layout" system.
- Implement the Desktop Sidebar vs. Mobile Drawer navigation.
- Implement the Mobile Bottom Sheet for "More Options."

---

## 5. Screen Descriptions & Behavior

### **5.1 Dashboard (The Launchpad)**
- **Desktop:** 
    - Large Hero section with "Unlock Premium" card (matching mobile Pro screen style).
    - Grid layout of "Recent Files" with large M3 cards.
    - Left Sidebar for Navigation (Files, Templates, Settings).
- **Mobile:**
    - Top bar with Hamburger menu.
    - Vertical list of recent files with "More" icon for actions.
    - Floating Action Button (FAB) at bottom-right for "New File."

### **5.2 Editor (The Workspace)**
- **Desktop (Split View):**
    - **Left:** Folder structure/Tab bar.
    - **Center:** Editor with line numbers and gutter.
    - **Right:** Live Preview pane (Markdown rendered).
    - **Header:** Quick actions (Undo, Redo, Save, Export).
- **Mobile (Toggle View):**
    - **Editor Mode:** Full-screen typing area with context-aware toolbar above the keyboard.
    - **Preview Mode:** View accessed via a FAB or Top-Bar toggle. Uses an M3 "Switch" animation to slide the preview over the editor.
- **Behavior:** Auto-saves every 3 seconds of inactivity.

### **5.3 File Explorer (The Library)**
- **Desktop:** Large table or grid view with multi-select capabilities. Sort/Filter dropdowns in the top right.
- **Mobile:** Search bar at the top that expands on tap. Bottom navigation for "Files, Starred, Recent."
- **Common:** Files use the `FileIconHelper` logic—distinct icons and colors for MD, JSON, etc.

### **5.4 Export/Save Dialog**
- **Common UI:** An M3 Modal (Desktop) or Bottom Sheet (Mobile).
- **Behavior:** Users select format (.pdf, .md, .txt) and template style. A "Generating..." circular progress indicator (M3 style) appears before the browser download is triggered.

### **5.5 Settings Screen**
- **Common UI:** Grouped sections (Appearance, Editor, About).
- **Features:** Theme toggle (Dark/GitHub Dark), Font family selection, and Text size sliders that provide instant live updates to the UI.

---

## 6. Technical Stack Recommendation
- **Framework:** Next.js (for SEO and fast initial load).
- **Styling:** Tailwind CSS + Framer Motion (for M3 Spring animations).
- **Editor:** CodeMirror 6 (Modular and Mobile-friendly).
- **PDF Logic:** Flexmark (for MD parsing) + Browser Print Engine.
- **Icons:** Material Symbols Rounded.
