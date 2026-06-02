import type { Metadata } from 'next';
import SettingsClient from './SettingsClient';

export const metadata: Metadata = {
  title: 'Settings',
  description:
    'Customize your FileFlip experience. Configure theme, editor font size, storage options, and privacy settings for the offline file editor.',
  alternates: {
    canonical: '/settings',
  },
};

export default function SettingsPage() {
  return <SettingsClient />;
}
