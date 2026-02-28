'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { LayoutGrid, FolderOpen, Clock, Settings } from 'lucide-react';

export default function BottomNav() {
  const pathname = usePathname();

  const navItems = [
    { name: 'HOME', href: '/', icon: LayoutGrid },
    { name: 'LIBRARY', href: '/library', icon: FolderOpen },
    { name: 'RECENT', href: '/recent', icon: Clock },
    { name: 'SETTINGS', href: '/settings', icon: Settings },
  ];

  return (
    <div className="md:hidden fixed bottom-0 left-0 right-0 h-16 bg-md-sys-color-surface border-t border-md-sys-color-outline-variant flex items-center justify-around px-2 z-50">
      {navItems.map((item) => {
        const isActive = pathname === item.href;
        return (
          <Link 
            key={item.name} 
            href={item.href}
            className={`flex flex-col items-center justify-center w-16 h-full gap-1 ${isActive ? 'text-primary-blue' : 'text-md-sys-color-on-surface-variant'}`}
          >
            <item.icon className="w-6 h-6" />
            <span className="text-[10px] font-bold tracking-wider">{item.name}</span>
          </Link>
        );
      })}
    </div>
  );
}
