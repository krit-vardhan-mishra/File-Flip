import type { Metadata } from 'next';
import StarredClient from './StarredClient';

export const metadata: Metadata = {
  title: 'Starred Files',
  description:
    'Access your favorite starred files in FileFlip. Bookmark important documents for quick access — works offline with local browser storage.',
  alternates: {
    canonical: '/starred',
  },
};

export default function StarredPage() {
  return <StarredClient />;
}
