'use client';

import Link from 'next/link';
import { motion } from 'motion/react';
import { 
  FileText, 
  FolderOpen, 
  Star, 
  Clock, 
  Settings,
  LayoutGrid,
  ChevronLeft,
  Moon,
  Sun,
  Monitor,
  HardDrive,
  Cloud,
  Shield,
  Palette
} from 'lucide-react';
import BottomNav from '@/components/BottomNav';

export default function SettingsPage() {

  return (
    <div className="h-screen flex overflow-hidden selection:bg-primary-blue selection:text-white bg-md-sys-color-background">
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex w-20 lg:w-72 flex-shrink-0 flex-col border-r border-surface-variant bg-surface-dark transition-all duration-300">
        <div className="h-20 flex items-center px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-primary-blue to-accent-emerald flex items-center justify-center shadow-lg shadow-primary-blue/20">
              <FileText className="text-white w-6 h-6" />
            </div>
            <span className="text-xl font-bold tracking-tight hidden lg:block">MarkPDF</span>
          </div>
        </div>
        
        <nav className="flex-1 px-4 py-6 space-y-2">
          <Link href="/" className="flex items-center gap-4 px-4 py-4 rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-colors">
            <LayoutGrid className="w-6 h-6" />
            <span className="hidden lg:block">Dashboard</span>
          </Link>
          <Link href="/library" className="flex items-center gap-4 px-4 py-4 rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-colors">
            <FolderOpen className="w-6 h-6" />
            <span className="hidden lg:block">My Files</span>
          </Link>
          <Link href="/starred" className="flex items-center gap-4 px-4 py-4 rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-colors">
            <Star className="w-6 h-6" />
            <span className="hidden lg:block">Starred</span>
          </Link>
          <Link href="/recent" className="flex items-center gap-4 px-4 py-4 rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-colors">
            <Clock className="w-6 h-6" />
            <span className="hidden lg:block">Recent</span>
          </Link>
          
          <div className="pt-4 mt-4 border-t border-surface-variant">
            <Link href="/settings" className="flex items-center gap-4 px-4 py-4 rounded-full bg-primary-container text-primary-blue font-medium">
              <Settings className="w-6 h-6" />
              <span className="hidden lg:block">Settings</span>
            </Link>
          </div>
        </nav>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden relative bg-surface-dark pb-16 md:pb-0">
        {/* Mobile Header */}
        <header className="md:hidden h-16 flex items-center justify-between px-4 flex-shrink-0 border-b border-surface-variant">
          <Link href="/" className="w-10 h-10 rounded-xl bg-surface-variant flex items-center justify-center text-text-secondary">
            <ChevronLeft className="w-6 h-6" />
          </Link>
          <h1 className="text-lg font-bold tracking-tight text-white">Settings</h1>
          <div className="w-10 h-10"></div>
        </header>

        {/* Desktop Header */}
        <header className="hidden md:flex h-24 items-center justify-between px-8 lg:px-12 flex-shrink-0">
          <div className="flex-1 max-w-2xl">
            <h1 className="text-3xl font-bold text-white flex items-center gap-3">
              <Settings className="w-8 h-8 text-text-secondary" />
              Settings
            </h1>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto px-4 md:px-8 lg:px-12 pb-12 custom-scrollbar">
          
          <div className="max-w-3xl mx-auto mt-6 space-y-8">
            
            {/* Appearance */}
            <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
              <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                <Palette className="w-5 h-5 text-primary-blue" />
                Appearance
              </h2>
              <div className="bg-surface-variant rounded-3xl p-2">
                <div className="flex items-center justify-between p-4 border-b border-white/5">
                  <div>
                    <h3 className="text-text-primary font-medium">Theme</h3>
                    <p className="text-sm text-text-secondary">Select your preferred interface theme</p>
                  </div>
                  <div className="flex bg-surface-dark rounded-xl p-1">
                    <button className="px-4 py-2 rounded-lg bg-surface-variant text-white font-medium text-sm flex items-center gap-2">
                      <Moon className="w-4 h-4" /> Dark
                    </button>
                    <button className="px-4 py-2 rounded-lg text-text-secondary hover:text-white font-medium text-sm flex items-center gap-2 transition-colors">
                      <Sun className="w-4 h-4" /> Light
                    </button>
                    <button className="px-4 py-2 rounded-lg text-text-secondary hover:text-white font-medium text-sm flex items-center gap-2 transition-colors hidden md:flex">
                      <Monitor className="w-4 h-4" /> System
                    </button>
                  </div>
                </div>
                <div className="flex items-center justify-between p-4">
                  <div>
                    <h3 className="text-text-primary font-medium">Editor Font Size</h3>
                    <p className="text-sm text-text-secondary">Adjust the text size in the code editor</p>
                  </div>
                  <select defaultValue="16px" className="bg-surface-dark border border-white/10 rounded-xl px-4 py-2 text-white outline-none focus:border-primary-blue">
                    <option value="12px">12px</option>
                    <option value="14px">14px</option>
                    <option value="16px">16px</option>
                    <option value="18px">18px</option>
                    <option value="20px">20px</option>
                  </select>
                </div>
              </div>
            </motion.section>

            {/* Storage */}
            <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
              <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                <HardDrive className="w-5 h-5 text-accent-emerald" />
                Storage & Sync
              </h2>
              <div className="bg-surface-variant rounded-3xl p-2">
                <div className="flex items-center justify-between p-4 border-b border-white/5">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-full bg-surface-dark flex items-center justify-center">
                      <HardDrive className="w-6 h-6 text-text-secondary" />
                    </div>
                    <div>
                      <h3 className="text-text-primary font-medium">Local Storage</h3>
                      <p className="text-sm text-text-secondary">Files are saved securely in your browser</p>
                    </div>
                  </div>
                  <button className="px-4 py-2 rounded-full border border-white/10 text-white text-sm font-medium hover:bg-white/5 transition-colors">
                    Clear Data
                  </button>
                </div>
                <div className="flex items-center justify-between p-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-full bg-primary-blue/10 flex items-center justify-center">
                      <Cloud className="w-6 h-6 text-primary-blue" />
                    </div>
                    <div>
                      <h3 className="text-text-primary font-medium">Cloud Sync</h3>
                      <p className="text-sm text-text-secondary">Sync files across devices (Coming Soon)</p>
                    </div>
                  </div>
                  <button className="px-4 py-2 rounded-full bg-primary-blue/20 text-primary-blue text-sm font-medium opacity-50 cursor-not-allowed">
                    Connect
                  </button>
                </div>
              </div>
            </motion.section>

            {/* Privacy */}
            <motion.section initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 }}>
              <h2 className="text-lg font-semibold text-white mb-4 flex items-center gap-2">
                <Shield className="w-5 h-5 text-accent-orange" />
                Privacy & Security
              </h2>
              <div className="bg-surface-variant rounded-3xl p-2">
                <div className="flex items-center justify-between p-4">
                  <div>
                    <h3 className="text-text-primary font-medium">Offline Mode</h3>
                    <p className="text-sm text-text-secondary">All processing happens locally on your device</p>
                  </div>
                  <div className="w-12 h-6 bg-primary-blue rounded-full relative cursor-pointer">
                    <div className="absolute right-1 top-1 w-4 h-4 bg-white rounded-full"></div>
                  </div>
                </div>
              </div>
            </motion.section>

            <div className="text-center pt-8 pb-4">
              <p className="text-sm text-text-secondary">MarkPDF v1.0.0</p>
              <p className="text-xs text-text-secondary/50 mt-1">Built with Next.js & Tailwind CSS</p>
            </div>

          </div>
        </div>
      </main>
      <BottomNav />
    </div>
  );
}
