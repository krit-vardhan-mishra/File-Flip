import type { Metadata } from 'next';
import RecentClient from './RecentClient';

export const metadata: Metadata = {
  title: 'Recent Files',
  description:
    'Quickly access your recently edited files in FileFlip. Jump back into your Markdown documents, JSON data, YAML configs, and other text files.',
  alternates: {
    canonical: '/recent',
  },
};

export default function RecentPage() {
  return <RecentClient />;
}
