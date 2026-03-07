# FileFlip Web

FileFlip Web is a high-performance, offline-capable progressive web application for editing, previewing, and exporting various file formats directly in your browser. It serves as the web counterpart to the FileFlip Android app, providing a seamless file management and editing experience across devices.

## Features

- **Local First & Privacy Focused**: Edits and saves happen directly in your browser using IndexedDB and the File System Access API. Your files never leave your device unless you choose to use cloud features.
- **Multi-format Support**: Edit and preview Markdown (.md), JSON, YAML, XML, HTML, CSV, Logs, and Plain Text.
- **Rich Editor**: Syntax highlighting, auto-completion, and format-specific features powered by CodeMirror.
- **Live Preview**: See your Markdown rendered in real-time.
- **AI Integration**: AI assistant powered by Gemini to help you generate, edit, or format text contents easily.
- **Offline Capable**: Works without an internet connection once loaded.
- **Export to PDF**: Professional GitHub-styled PDF exports.

## Tech Stack

- **Framework**: Next.js 15 (App Router)
- **UI & Styling**: Tailwind CSS, Framer Motion, Lucide Icons
- **Editor**: CodeMirror 6 with syntax specific language support
- **Storage**: `idb-keyval` for IndexedDB, File System Access API for local files
- **AI Integration**: `@google/genai` (Gemini API)

## Getting Started

### Prerequisites

- Node.js (v18 or higher recommended)
- npm or pnpm or yarn

### Installation

1. Clone the repository and navigate to the web directory:
   ```bash
   cd FileFlip/web
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Setup environment variables:
   Copy the example environment file:
   ```bash
   cp .env.example .env.local
   ```
   Add your Gemini API key to `.env.local` to enable AI features:
   ```env
   GEMINI_API_KEY="your_api_key_here"
   ```

4. Run the development server:
   ```bash
   npm run dev
   ```

5. Open [http://localhost:3000](http://localhost:3000) in your browser to see the application.

## Development

The project is structured around Next.js App Router:
- `app/` - Next.js pages, layouts, and API routes.
- `components/` - Reusable React components (Sidebar, BottomNav, ExportModal).
- `lib/` - Context providers (FileContext, SidebarContext) and utility functions.
- `hooks/` - Custom React hooks.

## License

This project is part of FileFlip. Refer to the directory root for licensing information.
