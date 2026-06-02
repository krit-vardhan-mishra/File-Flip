import type {Metadata} from 'next';
import { Inter, Roboto_Mono } from 'next/font/google';
import './globals.css'; // Global styles
import { FileProvider } from '@/lib/FileContext';
import { SidebarProvider } from '@/lib/SidebarContext';
import JsonLd from '@/components/JsonLd';

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-sans',
});

const robotoMono = Roboto_Mono({
  subsets: ['latin'],
  variable: '--font-mono',
});

export const metadata: Metadata = {
  metadataBase: new URL('https://file-flip-fawn.vercel.app'),
  title: {
    default: 'FileFlip — Free Online File Editor & Viewer',
    template: '%s | FileFlip',
  },
  description:
    'Edit, preview, and export Markdown, JSON, YAML, XML, HTML, CSV and plain text files online — completely free and offline-capable. No sign-up required.',
  keywords: [
    'file flip',
    'fileflip',
    'online file editor',
    'markdown editor',
    'json editor',
    'yaml editor',
    'csv viewer',
    'xml editor',
    'html editor',
    'offline file editor',
    'file viewer',
    'file converter',
    'text editor online',
    'code editor',
    'file preview',
    'pdf export',
    'free file editor',
  ],
  applicationName: 'FileFlip',
  authors: [{ name: 'Krit Vardhan Mishra', url: 'https://github.com/krit-vardhan-mishra' }],
  creator: 'Krit Vardhan Mishra',
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large',
      'max-snippet': -1,
    },
  },
  openGraph: {
    title: 'FileFlip — Free Online File Editor & Viewer',
    description:
      'Edit, preview, and export Markdown, JSON, YAML, XML, HTML, CSV and plain text files online — completely free and offline-capable.',
    url: 'https://file-flip-fawn.vercel.app',
    siteName: 'FileFlip',
    type: 'website',
    locale: 'en_US',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'FileFlip — Free Online File Editor & Viewer',
    description:
      'Edit, preview, and export Markdown, JSON, YAML, XML, HTML, CSV and plain text files online — completely free and offline-capable.',
  },
  alternates: {
    canonical: 'https://file-flip-fawn.vercel.app',
  },
  verification: {
    google: 'google903e04f2c9023684',
  },
};

export default function RootLayout({children}: {children: React.ReactNode}) {
  return (
    <html lang="en" className={`${inter.variable} ${robotoMono.variable}`} suppressHydrationWarning>
      <body className="font-sans" suppressHydrationWarning>
        <JsonLd />
        <SidebarProvider>
          <FileProvider>
            {children}
          </FileProvider>
        </SidebarProvider>
      </body>
    </html>
  );
}
