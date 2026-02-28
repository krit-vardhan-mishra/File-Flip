# Android App: MD to PDF Converter - Project Initiation Documents

## 1. Project Overview
**Project Name:** "FlipFile: Android Markdown to PDF Utility"
**Type:** Mobile Application (Android Native)
**Goal:** Create a blazing fast, offline-first utility that brings the "VS Code Markdown PDF" experience to mobile.

---

## 2. Business Case

### **Problem Statement**
Users (Developers, Technical Writers, Students) often have Markdown (`.md`) notes on their phones but lack a quick way to share them as professional-looking PDFs.
*   Existing editors are bloated or locked behind subscriptions.
*   Online converters require internet and raise privacy concerns for sensitive notes.
*   Current "Converter" apps often have outdated UIs (Holo/Material 1) or lack styling options.

### **Solution Value**
*   **Privacy:** 100% Local processing. No file ever uploads to a server.
*   **Speed:** usage of native compilation allows conversion in milliseconds.
*   **Consistency:** "What you see is what you get" using standard GitHub-flavored styles.

---

## 3. Market Research & Competitors

| App Name | Links | PDF? | Other Formats? | Key Pros | Key Cons |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Markor** | [F-Droid](https://f-droid.org/packages/net.gsantner.markor/), [GitHub](https://github.com/gsantner/markor) | ✅ Yes | HTML, Zip, Image | Free, Open Source, Todo.txt support. | UI is outdated (Material 1). PDF export hidden in menus. |
| **Obsidian** | [Play Store](https://play.google.com/store/apps/details?id=md.obsidian) | ⚠️ Plugin | Image (Plugin) | Powerful, huge ecosystem. | No native PDF export (needs plugins). Heavyweight. |
| **Joplin** | [Play Store](https://play.google.com/store/apps/details?id=net.cozic.joplin_mobile) | ✅ Yes | HTML, JEX | Syncs with Cloud, E2E encryption. | "Evernote replacement" focus, not quick utility. |
| **MD to PDF** | [Play Store](https://play.google.com/store/apps/details?id=com.shimul.markdown_to_pdf) | ✅ Yes | None | Specialized for this task. | Contains Ads, limited styling. |

### **Our Niche Strategy**
While tools like **Markor** are great *editors*, they are not optimized for *publishing*.
Our app **"FlipFile"** will focus on:
1.  **One-Tap Conversion:** No deep menus. Open File -> Click Convert.
2.  **Modern UI:** fully distinct Material 3 aesthetics (Dynamic Colors).
3.  **Visual Styling:** Pre-packaged themes (GitHub, Dracula, Solarized) for the PDF output.
4.  **Multi-Format:** (Phase 2) Support for **Source Code (Syntax Highlighted)** and **JSON/CSV**.



---

## 4. Project Charter
*   **Stakeholders:**
    *   **Product Owner:** User (You)
    *   **Developer:** AI Agent + User
*   **Success Metrics:**
    *   App Launch time < 2 seconds.
    *   PDF Generation time < 3 seconds for 50-page document.
    *   4.5+ Star rating capability (Crash-free users > 99%).
