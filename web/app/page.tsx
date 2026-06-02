import type { Metadata } from 'next';
import DashboardClient from '@/components/DashboardClient';

export const metadata: Metadata = {
  title: 'FileFlip — Free Online File Editor & Viewer',
  description:
    'Edit, preview, and export Markdown, JSON, YAML, XML, HTML, CSV and plain text files online. Free, offline-capable, no sign-up required. Live preview, syntax highlighting, and PDF export.',
  alternates: {
    canonical: '/',
  },
};

export default function HomePage() {
  return (
    <>
      {/* Server-rendered SEO content — visible to both users and crawlers */}
      <section className="sr-only-seo" aria-label="About FileFlip">
        <h1>FileFlip — Free Online File Editor, Viewer &amp; Converter</h1>
        <p>
          FileFlip is a high-performance, offline-capable web tool for editing, previewing, and
          exporting various file formats. Edit Markdown, JSON, YAML, XML, HTML, CSV, and plain text
          files directly in your browser — no sign-up, no installation, completely free.
        </p>

        <h2>Key Features</h2>
        <ul>
          <li>Offline-first — all editing, previewing, and export happens locally in your browser</li>
          <li>Live preview — see rendered output as you type, including Markdown, HTML, and more</li>
          <li>Syntax highlighting — code editor with line numbers, bracket matching, and auto-complete</li>
          <li>PDF export — professional GitHub-styled PDF export for documents</li>
          <li>Drag and drop — import files by dragging them into the editor</li>
          <li>Open from URL — view files from any public URL</li>
          <li>Multiple file formats — Markdown, JSON, YAML, XML, HTML, CSV, TXT, and LOG files</li>
        </ul>

        <h2>Supported File Formats</h2>
        <ul>
          <li>Markdown (.md) — rich text editing with GitHub Flavored Markdown support</li>
          <li>JSON (.json) — interactive tree view with collapsible nodes</li>
          <li>YAML (.yaml, .yml) — structured data editing and tree view</li>
          <li>XML (.xml) — syntax-highlighted editing with tree view</li>
          <li>HTML (.html) — live rendered preview in an isolated sandbox</li>
          <li>CSV (.csv) — tabular data displayed in formatted tables</li>
          <li>Plain Text (.txt, .log) — simple text editing with line numbers</li>
        </ul>

        <h2>Why Choose FileFlip?</h2>
        <p>
          Unlike other online editors, FileFlip works entirely offline. Your files never leave your
          device — all processing happens locally in your browser using modern web APIs. FileFlip is
          open source and available on GitHub. It also has an Android app for editing files on mobile.
        </p>
      </section>

      {/* Interactive dashboard — client-rendered */}
      <DashboardClient />
    </>
  );
}
