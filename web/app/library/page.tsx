'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'motion/react';
import { 
  FileText, 
  FolderOpen, 
  Star, 
  Clock, 
  Settings,
  Search,
  Bell,
  Plus,
  LayoutGrid,
  List,
  MoreVertical,
  Braces,
  Code,
  Menu,
  ChevronLeft,
  FileCode2,
  FileSpreadsheet,
  FileCog,
  ScrollText,
  Pencil,
  Trash2,
  ExternalLink
} from 'lucide-react';
import { useFiles } from '@/lib/FileContext';
import { formatDistanceToNow } from 'date-fns';
import BottomNav from '@/components/BottomNav';
import Sidebar from '@/components/Sidebar';

export default function Library() {
  const router = useRouter();
  const { files, toggleStar, deleteFile, renameFile, openFileInEditor } = useFiles();
  const [menuFileId, setMenuFileId] = useState<string | null>(null);
  const [renameFileId, setRenameFileId] = useState<string | null>(null);
  const [renameValue, setRenameValue] = useState('');
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);

  const getFileIcon = (type: string) => {
    switch (type) {
      case 'markdown': return <FileText className="text-primary-blue w-6 h-6 md:w-8 md:h-8" />;
      case 'json': return <Braces className="text-accent-emerald w-6 h-6 md:w-8 md:h-8" />;
      case 'yaml': return <FileCog className="text-accent-purple w-6 h-6 md:w-8 md:h-8" />;
      case 'xml': return <Code className="text-amber-700 w-6 h-6 md:w-8 md:h-8" />;
      case 'html': return <FileCode2 className="text-pink-500 w-6 h-6 md:w-8 md:h-8" />;
      case 'csv': return <FileSpreadsheet className="text-green-500 w-6 h-6 md:w-8 md:h-8" />;
      case 'log': return <ScrollText className="text-blue-400 w-6 h-6 md:w-8 md:h-8" />;
      case 'text': return <FileText className="text-gray-400 w-6 h-6 md:w-8 md:h-8" />;
      default: return <FileText className="text-primary-blue w-6 h-6 md:w-8 md:h-8" />;
    }
  };

  const getFileColorClass = (type: string) => {
    switch (type) {
      case 'markdown': return 'group-hover:border-primary-blue/30';
      case 'json': return 'group-hover:border-accent-emerald/30';
      case 'yaml': return 'group-hover:border-accent-purple/30';
      case 'xml': return 'group-hover:border-amber-700/30';
      case 'html': return 'group-hover:border-pink-500/30';
      case 'csv': return 'group-hover:border-green-500/30';
      case 'log': return 'group-hover:border-blue-400/30';
      case 'text': return 'group-hover:border-gray-400/30';
      default: return 'group-hover:border-primary-blue/30';
    }
  };

  const getFileTextColorClass = (type: string) => {
    switch (type) {
      case 'markdown': return 'group-hover:text-primary-blue';
      case 'json': return 'group-hover:text-accent-emerald';
      case 'yaml': return 'group-hover:text-accent-purple';
      case 'xml': return 'group-hover:text-amber-700';
      case 'html': return 'group-hover:text-pink-500';
      case 'csv': return 'group-hover:text-green-500';
      case 'log': return 'group-hover:text-blue-400';
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
          <h1 className="text-lg font-bold tracking-tight text-white">My Files</h1>
          <div className="w-10 h-10"></div>
        </header>

        {/* Desktop Header */}
        <header className="hidden md:flex h-24 items-center justify-between px-8 lg:px-12 flex-shrink-0">
          <div className="flex-1 max-w-2xl">
            <h1 className="text-3xl font-bold text-white">My Files</h1>
          </div>
          <div className="flex items-center gap-4 ml-6">
            <div className="relative group w-64">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-secondary group-focus-within:text-primary-blue transition-colors" />
              <input 
                type="text" 
                placeholder="Search files..." 
                className="w-full h-12 pl-12 pr-6 rounded-full bg-surface-variant border-none text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-primary-blue focus:bg-surface-dark transition-all outline-none"
              />
            </div>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto px-4 md:px-8 lg:px-12 pb-12 custom-scrollbar">
          <div className="flex items-center justify-between mb-4 md:mb-6 mt-4 md:mt-0">
            <h3 className="text-sm md:text-lg font-bold md:font-semibold text-text-secondary md:text-text-primary uppercase md:normal-case">All Files ({files.length})</h3>
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
            {files.length === 0 ? (
              <div className="col-span-full py-12 flex flex-col items-center justify-center text-text-secondary">
                <FolderOpen className="w-16 h-16 mb-4 opacity-20" />
                <p className="mb-8 text-center">Your library is empty. Create a file to get started!</p>
                <div className="bg-surface-dark/50 p-6 rounded-2xl border border-white/5 max-w-md w-full">
                  <h4 className="text-sm font-semibold text-text-primary mb-4 uppercase tracking-wider text-center">Supported Formats</h4>
                  <ul className="grid grid-cols-2 gap-3 text-sm">
                    <li className="flex items-center gap-2"><div className="w-1.5 h-1.5 rounded-full bg-primary-blue"></div>Markdown (.md)</li>
                    <li className="flex items-center gap-2"><div className="w-1.5 h-1.5 rounded-full bg-accent-orange"></div>JSON (.json)</li>
                    <li className="flex items-center gap-2"><div className="w-1.5 h-1.5 rounded-full bg-accent-emerald"></div>YAML (.yaml / .yml)</li>
                    <li className="flex items-center gap-2"><div className="w-1.5 h-1.5 rounded-full bg-accent-purple"></div>XML (.xml)</li>
                    <li className="flex items-center gap-2"><div className="w-1.5 h-1.5 rounded-full bg-red-400"></div>HTML (.html)</li>
                    <li className="flex items-center gap-2"><div className="w-1.5 h-1.5 rounded-full bg-green-400"></div>CSV (.csv)</li>
                    <li className="flex items-center gap-2 col-span-2 justify-center mt-1"><div className="w-1.5 h-1.5 rounded-full bg-gray-400"></div>Plain text (.txt, .log)</li>
                  </ul>
                </div>
              </div>
            ) : (
              files.map((file) => (
                <motion.div key={file.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}>
                  <div className="m3-card bg-surface-variant p-4 md:p-6 flex flex-row md:flex-col justify-between items-center md:items-start h-auto md:h-64 group relative">
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
                      <div className="flex items-center gap-1 relative">
                        <button 
                          onClick={(e) => { e.preventDefault(); e.stopPropagation(); toggleStar(file.id); }}
                          className={`w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center transition-colors ${file.isStarred ? 'text-accent-orange' : 'text-text-secondary'}`}
                        >
                          <Star className={`w-5 h-5 ${file.isStarred ? 'fill-accent-orange' : ''}`} />
                        </button>
                        <button 
                          onClick={(e) => { e.preventDefault(); e.stopPropagation(); setMenuFileId(menuFileId === file.id ? null : file.id); }}
                          className="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center text-text-secondary transition-colors"
                        >
                          <MoreVertical className="w-5 h-5" />
                        </button>

                        {/* Context Menu */}
                        {menuFileId === file.id && (
                          <>
                            <div className="fixed inset-0 z-40" onClick={(e) => { e.preventDefault(); e.stopPropagation(); setMenuFileId(null); }} />
                            <div className="absolute right-0 top-full mt-1 w-48 bg-surface-dark border border-white/10 rounded-xl shadow-2xl z-50 py-1 overflow-hidden">
                              <button
                                onClick={(e) => {
                                  e.preventDefault(); e.stopPropagation();
                                  openFileInEditor(file.id);
                                  router.push(`/editor?id=${file.id}`);
                                  setMenuFileId(null);
                                }}
                                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-text-primary hover:bg-white/5 transition-colors"
                              >
                                <ExternalLink className="w-4 h-4 text-text-secondary" />
                                Open in Editor
                              </button>
                              <button
                                onClick={(e) => {
                                  e.preventDefault(); e.stopPropagation();
                                  setRenameFileId(file.id);
                                  setRenameValue(file.name);
                                  setMenuFileId(null);
                                }}
                                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-text-primary hover:bg-white/5 transition-colors"
                              >
                                <Pencil className="w-4 h-4 text-text-secondary" />
                                Rename
                              </button>
                              <button
                                onClick={(e) => {
                                  e.preventDefault(); e.stopPropagation();
                                  setDeleteConfirmId(file.id);
                                  setMenuFileId(null);
                                }}
                                className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:bg-red-500/10 transition-colors"
                              >
                                <Trash2 className="w-4 h-4" />
                                Delete
                              </button>
                            </div>
                          </>
                        )}
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

      {/* Rename Dialog */}
      <AnimatePresence>
        {renameFileId && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
            onClick={() => setRenameFileId(null)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e: React.MouseEvent) => e.stopPropagation()}
              className="bg-surface-variant rounded-2xl p-6 w-full max-w-sm shadow-2xl border border-white/10"
            >
              <h3 className="text-lg font-semibold text-text-primary mb-4">Rename File</h3>
              <input
                value={renameValue}
                onChange={(e) => setRenameValue(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' && renameValue.trim()) {
                    renameFile(renameFileId, renameValue.trim());
                    setRenameFileId(null);
                  }
                }}
                className="w-full px-4 py-3 rounded-xl bg-surface-dark border border-white/10 text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-primary-blue outline-none"
                autoFocus
              />
              <div className="flex gap-2 justify-end mt-4">
                <button
                  onClick={() => setRenameFileId(null)}
                  className="px-4 py-2 rounded-full text-sm font-medium text-text-secondary hover:bg-white/5 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={() => {
                    if (renameValue.trim()) {
                      renameFile(renameFileId, renameValue.trim());
                      setRenameFileId(null);
                    }
                  }}
                  className="px-4 py-2 rounded-full text-sm font-medium bg-primary-blue text-white hover:bg-primary-blue/90 transition-colors"
                >
                  Rename
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Delete Confirmation Dialog */}
      <AnimatePresence>
        {deleteConfirmId && (() => {
          const fileToDelete = files.find(f => f.id === deleteConfirmId);
          return (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
              onClick={() => setDeleteConfirmId(null)}
            >
              <motion.div
                initial={{ scale: 0.95, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.95, opacity: 0 }}
                onClick={(e: React.MouseEvent) => e.stopPropagation()}
                className="bg-surface-variant rounded-2xl p-6 w-full max-w-sm shadow-2xl border border-white/10"
              >
                <h3 className="text-lg font-semibold text-text-primary mb-2">Delete File</h3>
                <p className="text-sm text-text-secondary mb-6">
                  Are you sure you want to delete &ldquo;{fileToDelete?.name}&rdquo;? This action cannot be undone.
                </p>
                <div className="flex gap-2 justify-end">
                  <button
                    onClick={() => setDeleteConfirmId(null)}
                    className="px-4 py-2 rounded-full text-sm font-medium text-text-secondary hover:bg-white/5 transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={() => {
                      deleteFile(deleteConfirmId);
                      setDeleteConfirmId(null);
                    }}
                    className="px-4 py-2 rounded-full text-sm font-medium bg-red-500 text-white hover:bg-red-600 transition-colors"
                  >
                    Delete
                  </button>
                </div>
              </motion.div>
            </motion.div>
          );
        })()}
      </AnimatePresence>

      <BottomNav />
    </div>
  );
}
