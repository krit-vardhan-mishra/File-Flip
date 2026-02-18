# Android App: MD to PDF Converter - Planning Documents

## 1. Project Management Plan

### **Objective**
Develop a native Android application that allows users to select Markdown (`.md`) files from their device, preview them with standard styling, and export them as high-quality PDF documents.

### **Scope**
*   **In-Scope:**
    *   File selection from local storage.
    *   Markdown parsing (Headers, Lists, Code Blocks, Images, Tables).
    *   Live Preview of the rendered HTML/Markdown.
    *   PDF Generation settings (Page size, Margins).
    *   "Share PDF" functionality.
*   **Out-of-Scope:**
    *   Cloud synchronization.
    *   Collaborative editing.
    *   OCR features.

### **Tech Stack**
*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3)
*   **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture.
*   **Critical Libraries:**
    *   `flexmark-java` or `commonmark` (Markdown parsing).
    *   `Accompanist` (Permissions).
    *   `Android PrintManager` / `WebView` (PDF Generation).

---

## 2. Work Breakdown Structure (WBS)

### **Phase 1: Foundation & UI (Week 1)**
*   **1.1 Project Setup:**
    *   Initialize Android Studio Project.
    *   Setup Gradle dependencies (Hilt, Compose, Navigation).
*   **1.2 Home Screen UI:**
    *   Create `RecentFiles` list.
    *   Implement Floating Action Button (FAB) for "Pick File".
*   **1.3 File System Integration:**
    *   Implement Scoped Storage logic (ContentResolver).
    *   Handle `READ_EXTERNAL_STORAGE` permissions.

### **Phase 2: Core Logic (Week 2)**
*   **2.1 Markdown Parsing:**
    *   Integrate `flexmark-java`.
    *   Convert `.md` text to HTML string with inline CSS styles (GitHub flavor).
*   **2.2 Preview Screen:**
    *   Implement `WebView` inside Compose to render the generated HTML.
    *   Add "Edit" button to switch to raw text mode (optional simple editor).

### **Phase 3: PDF Generation (Week 3)**
*   **3.1 PDF Engine:**
    *   Implement `PrintDocumentAdapter` to convert WebView content to PDF.
    *   Handle page breaks (CSS `break-inside: avoid`).
*   **3.2 Export Options:**
    *   UI for selecting A4 / Letter size.
    *   Portrait / Landscape toggle.

### **Phase 4: Optimization & Polish (Week 4)**
*   **4.1 Performance:**
    *   Optimize rendering for large files (>5MB).
*   **4.2 Error Handling:**
    *   Handle corrupt files or unsupported encoding.
*   **4.3 Testing:**
    *   Unit tests for MarkdownParser.
    *   Instrumented tests for File Picking flow.

---

## 3. Schedule / Gantt Chart (Estimated)

| Task ID | Task Name            | Duration | Start Day | End Day  |
| :--- | :--- | :--- | :--- | :--- |
| **1.1** | Project Init         | 1 Day    | Day 1     | Day 1    |
| **1.3** | File System Logic    | 2 Days   | Day 2     | Day 3    |
| **2.1** | MD Parsing Logic     | 3 Days   | Day 4     | Day 6    |
| **2.2** | UI & Preview         | 3 Days   | Day 7     | Day 9    |
| **3.1** | PDF Engine           | 4 Days   | Day 10    | Day 13   |
| **4.3** | Testing & Fixes      | 2 Days   | Day 14    | Day 15   |

---

## 4. Risk Management Plan

| Risk ID | Description | Probability | Impact | Mitigation Strategy |
| :--- | :--- | :--- | :--- | :--- |
| **R-01** | **Scoped Storage Issues:** Android 11+ restrictions makes reading files complex. | High | Critical | Use `ACTION_OPEN_DOCUMENT` (System Picker) instead of scanning file system manually. |
| **R-02** | **Rendering Artifacts:** Tables or large code blocks cutting off in PDF. | Medium | High | Inject custom Media Query CSS during PDF generation to handle overflow. |
| **R-03** | **Performance:** App crashes on 20MB+ Markdown files. | Low | Medium | Run parsing on Background Thread (Coroutines). Implement pagination if needed. |

---

## 5. Resource Plan
*   **Developer:** 1x Android Developer (Full Stack Mobile).
*   **Designer:** 1x UI/UX (Can be same person using Material3 Guidelines).
*   **QA:** Self-testing + 1 Beta Tester device (Pixel recommended).

---

## 6. Communication Plan
Since this is likely a solo/small team project:
*   **Daily:** Commit code to GitHub with descriptive messages.
*   **Weekly:** Review `task.md` checklist and update progress.
*   **Milestone:** Create a "Release" APK at the end of every Phase.
