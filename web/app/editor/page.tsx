import type { Metadata } from 'next';
import EditorClient from './EditorClient';

export const metadata: Metadata = {
  title: 'Editor',
  description:
    'Edit files with syntax highlighting, live preview, and professional export. Supports Markdown, JSON, YAML, XML, HTML, CSV, and plain text — all offline in your browser.',
  alternates: {
    canonical: '/editor',
  },
};

export default function EditorPage() {
  return <EditorClient />;
}
