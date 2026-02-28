import Link from 'next/link';

export default function NotFound() {
  return (
    <div className="h-screen flex flex-col items-center justify-center bg-md-sys-color-background text-md-sys-color-on-background">
      <h1 className="text-4xl font-bold mb-2">404</h1>
      <p className="text-md-sys-color-on-surface-variant mb-6">Page not found</p>
      <Link href="/" className="px-6 py-3 rounded-full bg-md-sys-color-primary text-md-sys-color-on-primary font-medium hover:bg-md-sys-color-primary/90 transition-colors">
        Go Home
      </Link>
    </div>
  );
}
