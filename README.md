# FileFlip

FileFlip is an offline-first Android app for editing, previewing and exporting text-based files. It provides a polished editor with syntax highlighting for Markdown, JSON, CSV, XML and other formats, an in-app preview powered by WebView, and professional GitHub-styled PDF export.

**Short description**
- **One-liner**: FileFlip — Offline Android editor & converter for Markdown, JSON, CSV and more with live preview and styled PDF export.

**Key features**
- **Offline first**: All editing, previewing and export happens locally — no internet required.
- **Multi-format support**: .md, .json, .yaml, .xml, .txt, .html, .log, .csv (rendered appropriately: HTML/WebView, tables for CSV, tree view for JSON/YAML, etc.).
- **Editor**: Syntax highlighting, line numbers, tabbed file UI, selection-aware formatting tools and cursor-aware insertions.
- **Preview**: Dedicated preview screen accessible from the FAB; live sync with editor for Markdown and other formats.
- **Export**: Professional GitHub-styled PDF export via Android PrintManager / PdfDocument.
- **File management**: Create, open, save, rename, delete files using Android system picker and app storage.
- **Architecture**: MVVM + Clean Architecture, Hilt DI, Kotlin Coroutines.

**How it works (functioning & workflow)**
- Open or create a text file from the dashboard or top toolbar.
- Edit content in the main editor which provides syntax highlighting and formatting helpers specialized by file type.
- Tap the FAB (preview) to view the rendered output in the Preview screen. Previews use a WebView for HTML/Markdown and format-specific renderers (CSV table, JSON tree, etc.).
- Export the preview as a styled PDF using the share/export menu or PrintManager. The exported PDF uses a GitHub-like CSS for Markdown rendering to keep output professional and readable.
- All operations (parsing, rendering, export) run locally on the device — no external services required.

**Supported file formats**
- Markdown (.md)
- JSON (.json)
- YAML (.yaml / .yml)
- XML (.xml)
- HTML (.html)
- CSV (.csv)
- Plain text (.txt, .log)

**Build & run (developer)**
- Open the project in Android Studio or use the Gradle wrapper on Windows:

```powershell
# Build debug APK (Windows)
.
\gradlew.bat assembleDebug
```

- Or open the project in Android Studio and run the `app` module on a device/emulator.

**Project structure & architecture**
- Clean Architecture with `domain`, `data` and `presentation` layers.
- `ViewModel` + `LiveData` / StateFlow for UI state handling.
- Hilt for dependency injection and Kotlin Coroutines for async work.
- UI follows Material Design 3 guidelines with dark theme support.

**Developer notes & TODOs**
- Implement undo/redo and advanced validation (JSON/YAML/XML) — TODOs referenced in `project_status.md` and `app/src/main/java/...`.
- Unit and UI tests are not yet complete; add ViewModel and repository tests before major refactors.
- Consider adding licensing (e.g., MIT) by creating a `LICENSE` file.

**Contributing**
- Open issues for bugs or feature requests.
- Fork, work on a branch, then open a pull request against `main`.

**References & docs**
- Initial idea and design notes: [md_app/initial_idea.md](md_app/initial_idea.md)
- Feature / planning docs: [md_app/what_the_will_have.md](md_app/what_the_will_have.md)
- Project status: [project_status.md](project_status.md)

---