'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  FolderOpen,
  Star,
  Clock,
  Settings,
  FileText,
  Menu,
  ChevronLeft
} from 'lucide-react';
import { useSidebar } from '@/lib/SidebarContext';

export default function Sidebar() {
  const { isSidebarOpen, toggleSidebar } = useSidebar();
  const pathname = usePathname();

  const getLinkClasses = (href: string) => {
    const isActive = pathname === href;
    return `flex items-center gap-4 ${isSidebarOpen ? 'px-4 py-4' : 'p-3 justify-center'} rounded-full transition-all ${
      isActive
        ? 'bg-primary-container text-primary-blue font-medium'
        : 'hover:bg-surface-variant text-text-secondary hover:text-text-primary'
    }`;
  };

  return (
    <aside className={`hidden md:flex flex-shrink-0 flex-col border-r border-surface-variant bg-surface-dark transition-all duration-300 ${isSidebarOpen ? 'w-72' : 'w-20'}`}>
      <div className={`h-20 flex items-center ${isSidebarOpen ? 'px-6 justify-between' : 'justify-center'}`}>
        {isSidebarOpen && (
          <div className="flex items-center gap-3 overflow-hidden">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-primary-blue to-accent-emerald flex items-center justify-center shadow-lg shadow-primary-blue/20 shrink-0">
              <FileText className="text-white w-6 h-6" />
            </div>
            <span className="text-xl font-bold tracking-tight whitespace-nowrap">FileFlip</span>
          </div>
        )}
        <button onClick={toggleSidebar} className="w-10 h-10 rounded-xl hover:bg-surface-variant flex items-center justify-center text-text-secondary shrink-0 transition-colors">
          {isSidebarOpen ? <ChevronLeft className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
        </button>
      </div>

      <nav className="flex-1 px-4 py-6 space-y-2">
        <Link href="/" className={getLinkClasses('/')}>
          <LayoutDashboard className="w-6 h-6 shrink-0" />
          {isSidebarOpen && <span className="whitespace-nowrap">Dashboard</span>}
        </Link>
        <Link href="/library" className={getLinkClasses('/library')}>
          <FolderOpen className="w-6 h-6 shrink-0" />
          {isSidebarOpen && <span className="whitespace-nowrap">My Files</span>}
        </Link>
        <Link href="/starred" className={getLinkClasses('/starred')}>
          <Star className="w-6 h-6 shrink-0" />
          {isSidebarOpen && <span className="whitespace-nowrap">Starred</span>}
        </Link>
        <Link href="/recent" className={getLinkClasses('/recent')}>
          <Clock className="w-6 h-6 shrink-0" />
          {isSidebarOpen && <span className="whitespace-nowrap">Recent</span>}
        </Link>

        <div className="pt-4 mt-4 border-t border-surface-variant">
          <Link href="/settings" className={getLinkClasses('/settings')}>
            <Settings className="w-6 h-6 shrink-0" />
            {isSidebarOpen && <span className="whitespace-nowrap">Settings</span>}
          </Link>
        </div>
      </nav>
    </aside>
  );
}