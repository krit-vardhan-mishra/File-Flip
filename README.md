# FileFlip

FileFlip is an offline-first Android app for editing, previewing and exporting text-based files. It provides a polished editor with syntax highlighting for Markdown, JSON, CSV, XML and other formats, an in-app preview powered by WebView, and professional GitHub-styled PDF export.

## 🚀 Product Showcase

<p align="center">
  <img src="screenshots/launchpad/privacy-focused.png" width="100%" alt="Privacy Focused & Offline First" />
</p>

<p align="center">
  <img src="screenshots/launchpad/professional-writing.png" width="100%" alt="Professional Writing Environment" />
</p>

<p align="center">
  <img src="screenshots/launchpad/minimalism.png" width="100%" alt="Minimalist Workspace" />
</p>

<p align="center">
  <img src="screenshots/launchpad/export-to-professional-formats.png" width="100%" alt="Export to Professional Formats" />
</p>


> **Download the app**: Get the latest APK from the [Releases](https://github.com/krit-vardhan-mishra/File-Flip/releases) section.

> **Live web app**: [https://file-flip-fawn.vercel.app/](https://file-flip-fawn.vercel.app/)

**Short description**
- **One-liner**: FileFlip — Offline Android editor & converter for Markdown, JSON, CSV and more with live preview and styled PDF export.

**Key features**
- **Offline first**: All editing, previewing and export happens locally — no internet required.
- **AI Agent Integration**: Chat and co-write files with an AI assistant that automatically leverages active file contents, selection highlights, and local workspace context (via an offline ONNX RAG indexer).
- **Secure Credentials**: All API credentials entered for Gemini, Groq, and OpenRouter are fully encrypted using hardware-backed Android KeyStore AES encryption.
- **Robust Provider Integrations**: Full support for Google Gemini, Groq, and OpenRouter with automatic failover/fallback routing on rate limits.
- **Multi-format support**: .md, .json, .yaml, .xml, .txt, .html, .log, .csv (rendered appropriately: HTML/WebView, tables for CSV, tree view for JSON/YAML, etc.).
- **Editor**: Syntax highlighting, line numbers, tabbed file UI, selection-aware formatting tools and cursor-aware insertions.
- **Preview**: Dedicated preview screen accessible from the FAB; live sync with editor for Markdown and other formats.
- **Export**: Professional GitHub-styled PDF export via Android PrintManager / PdfDocument.
- **File management**: Create, open, save, rename, delete files using Android system picker and app storage.
- **Architecture**: MVVM + Clean Architecture, Room SQLite Database, Dagger Hilt DI, Kotlin Coroutines & Flows.

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
