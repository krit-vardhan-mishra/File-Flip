'use client';

import Link from 'next/link';
import { motion } from 'motion/react';
import { 
  FileText, 
  FolderOpen, 
  Star, 
  Clock, 
  Settings,
  Search,
  LayoutGrid,
  List,
  MoreVertical,
  Braces,
  Code,
  ChevronLeft
} from 'lucide-react';
import { useFiles } from '@/lib/FileContext';
import { formatDistanceToNow } from 'date-fns';
import BottomNav from '@/components/BottomNav';
import Sidebar from '@/components/Sidebar';

export default function Starred() {
  const { files, toggleStar, deleteFile } = useFiles();

  const starredFiles = files.filter(f => f.isStarred);

  const getFileIcon = (type: string) => {
    switch (type) {
      case 'json': return <Braces className="text-accent-orange w-6 h-6 md:w-8 md:h-8" />;
      case 'html': return <Code className="text-accent-purple w-6 h-6 md:w-8 md:h-8" />;
      case 'text': return <FileText className="text-gray-400 w-6 h-6 md:w-8 md:h-8" />;
      default: return <FileText className="text-primary-blue w-6 h-6 md:w-8 md:h-8" />;
    }
  };

  const getFileColorClass = (type: string) => {
    switch (type) {
      case 'json': return 'group-hover:border-accent-orange/30';
      case 'html': return 'group-hover:border-accent-purple/30';
      case 'text': return 'group-hover:border-gray-400/30';
      default: return 'group-hover:border-primary-blue/30';
    }
  };

  const getFileTextColorClass = (type: string) => {
    switch (type) {
      case 'json': return 'group-hover:text-accent-orange';
      case 'html': return 'group-hover:text-accent-purple';
      case 'text': return 'group-hover:text-white';
      default: return 'group-hover:text-primary-blue';
    }
  };

  return (
    <div className="h-screen flex overflow-hidden selection:bg-primary-blue selection:text-white bg-md-sys-color-background">
      <Sidebar />

      {/* Main Content */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden relative bg-surface-dark pb-16 md:pb-0">
        {/* Mobile Header */}
        <header className="md:hidden h-16 flex items-center justify-between px-4 flex-shrink-0 border-b border-surface-variant">
          <Link href="/" className="w-10 h-10 rounded-xl bg-surface-variant flex items-center justify-center text-text-secondary">
            <ChevronLeft className="w-6 h-6" />
          </Link>
          <h1 className="text-lg font-bold tracking-tight text-white">Starred</h1>
          <div className="w-10 h-10"></div>
        </header>

        {/* Desktop Header */}
        <header className="hidden md:flex h-24 items-center justify-between px-8 lg:px-12 flex-shrink-0">
          <div className="flex-1 max-w-2xl">
            <h1 className="text-3xl font-bold text-white flex items-center gap-3">
              <Star className="w-8 h-8 text-accent-orange fill-accent-orange" />
              Starred Files
            </h1>
          </div>
          <div className="flex items-center gap-4 ml-6">
            <div className="relative group w-64">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-secondary group-focus-within:text-primary-blue transition-colors" />
              <input 
                type="text" 
                placeholder="Search starred..." 
                className="w-full h-12 pl-12 pr-6 rounded-full bg-surface-variant border-none text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-primary-blue focus:bg-surface-dark transition-all outline-none"
              />
            </div>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto px-4 md:px-8 lg:px-12 pb-12 custom-scrollbar">
          <div className="flex items-center justify-between mb-4 md:mb-6 mt-4 md:mt-0">
            <h3 className="text-sm md:text-lg font-bold md:font-semibold text-text-secondary md:text-text-primary uppercase md:normal-case">Favorites ({starredFiles.length})</h3>
            <div className="flex gap-2">
              <button className="w-10 h-10 rounded-full flex items-center justify-center hover:bg-surface-variant text-text-secondary">
                <LayoutGrid className="w-5 h-5" />
              </button>
              <button className="w-10 h-10 rounded-full flex items-center justify-center hover:bg-surface-variant text-text-secondary">
                <List className="w-5 h-5" />
              </button>
            </div>
          </div>

          <motion.div 
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3 md:gap-6"
          >
            {starredFiles.length === 0 ? (
              <div className="col-span-full py-12 flex flex-col items-center justify-center text-text-secondary">
                <Star className="w-16 h-16 mb-4 opacity-20" />
                <p>No starred files yet.</p>
              </div>
            ) : (
              starredFiles.map((file) => (
                <motion.div key={file.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
                  <div className="m3-card bg-surface-variant p-4 md:p-6 flex flex-row md:flex-col justify-between items-center md:items-start h-auto md:h-64 group block relative">
                    <Link href={`/editor?id=${file.id}`} className="absolute inset-0 z-0"></Link>
                    <div className="flex items-center md:items-start md:justify-between w-full md:w-auto gap-4 md:gap-0 z-10">
                      <div className={`w-12 h-12 md:w-14 md:h-14 rounded-xl md:rounded-2xl bg-surface-dark flex items-center justify-center border border-white/5 transition-colors ${getFileColorClass(file.type)} shrink-0`}>
                        {getFileIcon(file.type)}
                      </div>
                      <div className="flex-1 md:hidden">
                        <h4 className={`text-base font-semibold text-text-primary mb-0.5 transition-colors truncate ${getFileTextColorClass(file.type)}`}>{file.name}</h4>
                        <p className="text-xs text-text-secondary uppercase tracking-wider">
                          {formatDistanceToNow(file.lastModified, { addSuffix: true })}
                        </p>
                      </div>
                      <div className="flex items-center gap-1">
                        <button 
                          onClick={(e) => { e.preventDefault(); e.stopPropagation(); toggleStar(file.id); }}
                          className={`w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center transition-colors ${file.isStarred ? 'text-accent-orange' : 'text-text-secondary'}`}
                        >
                          <Star className={`w-5 h-5 ${file.isStarred ? 'fill-accent-orange' : ''}`} />
                        </button>
                        <button 
                          onClick={(e) => { e.preventDefault(); e.stopPropagation(); deleteFile(file.id); }}
                          className="w-8 h-8 rounded-full hover:bg-red-500/20 hover:text-red-400 flex items-center justify-center text-text-secondary transition-colors"
                        >
                          <MoreVertical className="w-5 h-5" />
                        </button>
                      </div>
                    </div>
                    <div className="hidden md:block mt-4 w-full z-10 pointer-events-none">
                      <h4 className={`text-lg font-semibold text-text-primary mb-1 transition-colors truncate ${getFileTextColorClass(file.type)}`}>{file.name}</h4>
                      <p className="text-sm text-text-secondary mb-4 line-clamp-2">{file.content.substring(0, 100)}...</p>
                      <div className="flex items-center gap-2 text-xs text-text-secondary font-medium uppercase tracking-wider">
                        <span>{formatDistanceToNow(file.lastModified, { addSuffix: true })}</span>
                      </div>
                    </div>
                  </div>
                </motion.div>
              ))
            )}
          </motion.div>
        </div>
      </main>
      <BottomNav />
    </div>
  );
}
