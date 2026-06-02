import type { Metadata } from 'next';
import LibraryClient from './LibraryClient';

export const metadata: Metadata = {
  title: 'My Files',
  description:
    'Browse and manage all your files in the FileFlip library. View, edit, rename, delete, and star your Markdown, JSON, YAML, XML, HTML, CSV, and text files.',
  alternates: {
    canonical: '/library',
  },
};

export default function LibraryPage() {
  return <LibraryClient />;
}
