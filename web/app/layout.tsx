import type {Metadata} from 'next';
import { Inter, Roboto_Mono } from 'next/font/google';
import './globals.css'; // Global styles
import { FileProvider } from '@/lib/FileContext';

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-sans',
});

const robotoMono = Roboto_Mono({
  subsets: ['latin'],
  variable: '--font-mono',
});

export const metadata: Metadata = {
  title: 'FileFlip Web',
  description: 'A high-performance, offline-capable web tool for editing, previewing, and exporting various file formats.',
};

export default function RootLayout({children}: {children: React.ReactNode}) {
  return (
    <html lang="en" className={`${inter.variable} ${robotoMono.variable}`} suppressHydrationWarning>
      <body className="font-sans" suppressHydrationWarning>
        <FileProvider>
          {children}
        </FileProvider>
      </body>
    </html>
  );
}
