'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'motion/react';
import { 
  FileText, 
  LayoutDashboard, 
  FolderOpen, 
  Star, 
  Clock, 
  Settings,
  Search,
  Bell,
  Plus,
  Award,
  Rocket,
  LayoutGrid,
  List,
  MoreVertical,
  Braces,
  Code,
  Table,
  Menu,
  CloudUpload,
  FileBox,
  X,
  FileCode2,
  FileSpreadsheet,
  FileCog,
  ScrollText,
  Pencil,
  Trash2,
  ExternalLink,
  Link2
} from 'lucide-react';
import { useFiles } from '@/lib/FileContext';
import { formatDistanceToNow } from 'date-fns';
import BottomNav from '@/components/BottomNav';
import Sidebar from '@/components/Sidebar';

// File type definitions matching the Android app
const FILE_TYPES = [
  { type: 'markdown' as const, ext: 'md', label: 'Markdown Document', content: '# New Markdown File\n\nStart writing...' },
  { type: 'json' as const, ext: 'json', label: 'JSON File', content: '{\n  "name": "New File",\n  "data": []\n}' },
  { type: 'yaml' as const, ext: 'yaml', label: 'YAML File', content: '# New YAML File\nname: New File\ndata: []' },
  { type: 'xml' as const, ext: 'xml', label: 'XML File', content: '<?xml version="1.0"?>\n<root>\n  <item>New File</item>\n</root>' },
  { type: 'text' as const, ext: 'txt', label: 'Text File', content: 'New text file content...' },
  { type: 'html' as const, ext: 'html', label: 'HTML Page', content: '<html>\n<body>\n  <h1>New HTML Page</h1>\n</body>\n</html>' },
  { type: 'log' as const, ext: 'log', label: 'Log File', content: '[INFO] New log file created' },
  { type: 'csv' as const, ext: 'csv', label: 'CSV File', content: 'Name,Value\nNew Item,0' },
];

export default function Dashboard() {
  const { files, isLoading, createFile, openLocalFile, openUrlFile, deleteFile, renameFile, openFileInEditor } = useFiles();
  const router = useRouter();
  const [showFileTypeDialog, setShowFileTypeDialog] = useState(false);
  const [showCreateMenu, setShowCreateMenu] = useState(false);
  const [showUrlDialog, setShowUrlDialog] = useState(false);
  const [urlInput, setUrlInput] = useState('');
  const [menuFileId, setMenuFileId] = useState<string | null>(null);
  const [renameFileId, setRenameFileId] = useState<string | null>(null);
  const [renameValue, setRenameValue] = useState('');
  const [deleteConfirmId, setDeleteConfirmId] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);

  const handleDragEnter = (e: React.DragEvent<HTMLElement>) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.dataTransfer.items && e.dataTransfer.items.length > 0) {
      setIsDragging(true);
    }
  };

  const handleDragLeave = (e: React.DragEvent<HTMLElement>) => {
    e.preventDefault();
    e.stopPropagation();
    // Check if the cursor is leaving the component and its children
    if (!e.currentTarget.contains(e.relatedTarget as Node)) {
      setIsDragging(false);
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLElement>) => {
    e.preventDefault();
    e.stopPropagation();
  };

  const handleDrop = async (e: React.DragEvent<HTMLElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const droppedFile = e.dataTransfer.files[0];
      
      const newFile = await openLocalFile(droppedFile);
      if (newFile) {
        router.push(`/editor?id=${newFile.id}`);
      }
      
      e.dataTransfer.clearData();
    }
  };

  const handleCreateNew = () => {
    setShowFileTypeDialog(true);
  };

  const handleUploadFile = () => {
    setShowCreateMenu(false);
    openLocalFile();
  };

  const handleCreateNewClick = () => {
    setShowCreateMenu(false);
    setShowFileTypeDialog(true);
  };

  const handleOpenFromUrl = () => {
    setShowCreateMenu(false);
    setShowUrlDialog(true);
  };

  const handleUrlSubmit = async () => {
    if (!urlInput.trim()) return;
    
    // Add https:// if no protocol specified
    let url = urlInput.trim();
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = 'https://' + url;
    }
    
    setShowUrlDialog(false);
    const newFile = await openUrlFile(url);
    if (newFile) {
      router.push(`/editor?id=${newFile.id}`);
    }
    setUrlInput('');
  };

  const handleFileTypeSelected = async (ft: typeof FILE_TYPES[number]) => {
    setShowFileTypeDialog(false);
    const newFile = await createFile(`Untitled.${ft.ext}`, ft.content, ft.type);
    router.push(`/editor?id=${newFile.id}`);
  };

  const handleMenuToggle = (fileId: string, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setMenuFileId(prev => prev === fileId ? null : fileId);
  };

  const handleRenameStart = (fileId: string) => {
    const file = files.find(f => f.id === fileId);
    if (file) {
      setRenameValue(file.name);
      setRenameFileId(fileId);
      setMenuFileId(null);
    }
  };

  const handleRenameSubmit = async () => {
    if (renameFileId && renameValue.trim()) {
      await renameFile(renameFileId, renameValue.trim());
      setRenameFileId(null);
      setRenameValue('');
    }
  };

  const handleDeleteConfirm = async () => {
    if (deleteConfirmId) {
      await deleteFile(deleteConfirmId);
      setDeleteConfirmId(null);
    }
  };

  const handleOpenInEditor = (fileId: string) => {
    openFileInEditor(fileId);
    setMenuFileId(null);
    router.push(`/editor?id=${fileId}`);
  };

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: {
        type: "spring" as const,
        stiffness: 300,
        damping: 24
      }
    }
  };

  const getFileIcon = (type: string) => {
    switch (type) {
      case 'markdown': return <FileText className="text-primary-blue w-6 h-6 md:w-8 md:h-8" />;
      case 'json': return <Braces className="text-accent-emerald w-6 h-6 md:w-8 md:h-8" />;
      case 'yaml': return <FileCog className="text-accent-purple w-6 h-6 md:w-8 md:h-8" />;
      case 'xml': return <Code className="text-amber-700 w-6 h-6 md:w-8 md:h-8" />;
      case 'html': return <FileCode2 className="text-pink-500 w-6 h-6 md:w-8 md:h-8" />;
      case 'csv': return <FileSpreadsheet className="text-green-500 w-6 h-6 md:w-8 md:h-8" />;
      case 'log': return <ScrollText className="text-blue-grey w-6 h-6 md:w-8 md:h-8" />;
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
      <main 
        className="flex-1 flex flex-col h-screen overflow-hidden relative bg-surface-dark pb-16 md:pb-0"
        onDragEnter={handleDragEnter}
        onDragOver={handleDragOver}
      >
        <AnimatePresence>
          {isDragging && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="absolute inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center"
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >
              <div className="w-11/12 max-w-2xl h-64 md:h-80 border-4 border-dashed border-primary-blue rounded-3xl flex flex-col items-center justify-center pointer-events-none">
                <CloudUpload className="w-16 h-16 text-primary-blue mb-4" />
                <p className="text-xl md:text-2xl font-bold text-white">Drop the file to open it here</p>
                <p className="text-base text-text-secondary mt-1">File will be added to your library</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        {/* Mobile Header */}
        <header className="md:hidden h-16 flex items-center justify-between px-4 flex-shrink-0 border-b border-surface-variant">
          <button className="w-10 h-10 rounded-xl bg-surface-variant flex items-center justify-center text-primary-blue">
            <Menu className="w-6 h-6" />
          </button>
          <h1 className="text-lg font-bold tracking-tight text-white">FileFlip</h1>
          <div className="w-10 h-10"></div> {/* Spacer for centering */}
        </header>

        {/* Desktop Header */}
        <header className="hidden md:flex h-24 items-center justify-between px-8 lg:px-12 flex-shrink-0">
          <div className="flex-1 max-w-2xl">
            <div className="relative group">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-text-secondary group-focus-within:text-primary-blue transition-colors" />
              <input 
                type="text" 
                placeholder="Search files, folders, or templates..." 
                className="w-full h-14 pl-12 pr-6 rounded-full bg-surface-variant border-none text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-primary-blue focus:bg-surface-dark transition-all outline-none"
              />
            </div>
          </div>
          <div className="flex items-center gap-4 ml-6">
            <button className="w-12 h-12 rounded-full flex items-center justify-center hover:bg-surface-variant text-text-primary transition-colors relative">
              <Bell className="w-6 h-6" />
              <span className="absolute top-3 right-3 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-surface-dark"></span>
            </button>
            <div className="relative">
              <button 
                onClick={() => setShowCreateMenu(!showCreateMenu)}
                className="m3-button h-12 px-6 bg-primary-blue hover:bg-[#0B8AC9] text-white font-semibold flex items-center gap-2 shadow-lg shadow-primary-blue/30"
              >
                <Plus className="w-5 h-5" />
                Create
              </button>
              {/* Dropdown Menu */}
              <AnimatePresence>
                {showCreateMenu && (
                  <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    className="absolute right-0 top-full mt-2 w-56 bg-surface-dark border border-surface-variant rounded-2xl shadow-xl z-50 overflow-hidden"
                  >
                    <button
                      onClick={handleCreateNewClick}
                      className="w-full flex items-center gap-3 px-4 py-3 text-text-primary hover:bg-surface-variant transition-colors"
                    >
                      <div className="w-10 h-10 rounded-xl bg-primary-blue/20 flex items-center justify-center">
                        <Plus className="w-5 h-5 text-primary-blue" />
                      </div>
                      <div className="text-left">
                        <p className="font-semibold">Create New File</p>
                        <p className="text-xs text-text-secondary">Start from scratch</p>
                      </div>
                    </button>
                    <button
                      onClick={handleUploadFile}
                      className="w-full flex items-center gap-3 px-4 py-3 text-text-primary hover:bg-surface-variant transition-colors border-t border-surface-variant"
                    >
                      <div className="w-10 h-10 rounded-xl bg-accent-emerald/20 flex items-center justify-center">
                        <CloudUpload className="w-5 h-5 text-accent-emerald" />
                      </div>
                      <div className="text-left">
                        <p className="font-semibold">Upload File</p>
                        <p className="text-xs text-text-secondary">Import from device</p>
                      </div>
                    </button>
                    <button
                      onClick={handleOpenFromUrl}
                      className="w-full flex items-center gap-3 px-4 py-3 text-text-primary hover:bg-surface-variant transition-colors border-t border-surface-variant"
                    >
                      <div className="w-10 h-10 rounded-xl bg-accent-purple/20 flex items-center justify-center">
                        <Link2 className="w-5 h-5 text-accent-purple" />
                      </div>
                      <div className="text-left">
                        <p className="font-semibold">Open from URL</p>
                        <p className="text-xs text-text-secondary">View file temporarily</p>
                      </div>
                    </button>
                  </motion.div>
                )}
              </AnimatePresence>
              {/* Click outside to close */}
              {showCreateMenu && (
                <div 
                  className="fixed inset-0 z-40" 
                  onClick={() => setShowCreateMenu(false)}
                />
              )}
            </div>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto px-4 md:px-8 lg:px-12 pb-12 custom-scrollbar">
          
          {/* Mobile Actions */}
          <div className="md:hidden mt-6 mb-8 space-y-4">
            <div className="relative">
              <button 
                onClick={() => setShowCreateMenu(!showCreateMenu)}
                className="w-full rounded-3xl bg-surface-variant border border-primary-blue/20 p-8 flex flex-col items-center justify-center gap-4 relative overflow-hidden group shadow-lg shadow-primary-blue/5"
              >
                <div className="absolute inset-0 bg-gradient-to-b from-primary-blue/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
                <div className="w-16 h-16 rounded-full bg-primary-blue flex items-center justify-center shadow-lg shadow-primary-blue/40">
                  <Plus className="w-8 h-8 text-white" />
                </div>
                <div className="text-center z-10">
                  <h2 className="text-xl font-bold text-white mb-1">Create New</h2>
                  <p className="text-sm text-text-secondary">Choose an option</p>
                </div>
              </button>
              {/* Mobile Dropdown */}
              <AnimatePresence>
                {showCreateMenu && (
                  <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    exit={{ opacity: 0, y: -10 }}
                    className="absolute top-full left-0 right-0 mt-2 bg-surface-dark border border-surface-variant rounded-2xl shadow-xl z-50 overflow-hidden"
                  >
                    <button
                      onClick={handleCreateNewClick}
                      className="w-full flex items-center gap-3 px-4 py-4 text-text-primary hover:bg-surface-variant transition-colors"
                    >
                      <div className="w-12 h-12 rounded-xl bg-primary-blue/20 flex items-center justify-center">
                        <Plus className="w-6 h-6 text-primary-blue" />
                      </div>
                      <div className="text-left">
                        <p className="font-semibold text-lg">Create New File</p>
                        <p className="text-sm text-text-secondary">Start from scratch</p>
                      </div>
                    </button>
                    <button
                      onClick={handleUploadFile}
                      className="w-full flex items-center gap-3 px-4 py-4 text-text-primary hover:bg-surface-variant transition-colors border-t border-surface-variant"
                    >
                      <div className="w-12 h-12 rounded-xl bg-accent-emerald/20 flex items-center justify-center">
                        <CloudUpload className="w-6 h-6 text-accent-emerald" />
                      </div>
                      <div className="text-left">
                        <p className="font-semibold text-lg">Upload File</p>
                        <p className="text-sm text-text-secondary">Import from device</p>
                      </div>
                    </button>
                    <button
                      onClick={handleOpenFromUrl}
                      className="w-full flex items-center gap-3 px-4 py-4 text-text-primary hover:bg-surface-variant transition-colors border-t border-surface-variant"
                    >
                      <div className="w-12 h-12 rounded-xl bg-accent-purple/20 flex items-center justify-center">
                        <Link2 className="w-6 h-6 text-accent-purple" />
                      </div>
                      <div className="text-left">
                        <p className="font-semibold text-lg">Open from URL</p>
                        <p className="text-sm text-text-secondary">View file temporarily</p>
                      </div>
                    </button>
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <button onClick={openLocalFile} className="rounded-2xl bg-surface-variant p-4 flex items-center justify-center gap-3 text-white font-medium hover:bg-surface-variant/80 transition-colors">
                <CloudUpload className="w-5 h-5 text-primary-blue" />
                Import
              </button>
              <button className="rounded-2xl bg-surface-variant p-4 flex items-center justify-center gap-3 text-white font-medium hover:bg-surface-variant/80 transition-colors">
                <FileBox className="w-5 h-5 text-primary-blue" />
                Templates
              </button>
            </div>
          </div>

          {/* Desktop Hero Section */}
          <motion.section 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ type: "spring", stiffness: 300, damping: 24 }}
            className="hidden md:block mt-4 mb-10"
          >
            <div className="w-full rounded-[32px] hero-gradient p-8 lg:p-10 text-white relative overflow-hidden group cursor-pointer shadow-xl shadow-primary-blue/10">
              <div className="absolute -right-10 -top-10 w-64 h-64 bg-white opacity-10 rounded-full blur-3xl group-hover:scale-110 transition-transform duration-700"></div>
              <div className="absolute right-40 bottom-10 w-32 h-32 bg-accent-emerald opacity-20 rounded-full blur-2xl"></div>
              
              <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
                <div className="max-w-xl">
                  <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/20 backdrop-blur-sm text-sm font-medium mb-4 border border-white/10">
                    <Award className="w-4 h-4 text-accent-orange" />
                    <span>Pro Feature</span>
                  </div>
                  <h2 className="text-3xl lg:text-4xl font-bold mb-3 tracking-tight">Unlock Premium Power</h2>
                  <p className="text-blue-50 text-lg opacity-90 leading-relaxed">
                    Get unlimited PDF exports, GitHub-style themes, and cloud sync across all your devices.
                  </p>
                  <button className="mt-6 px-6 py-3 bg-white text-primary-blue rounded-full font-bold hover:bg-gray-50 transition-colors shadow-md">
                    Upgrade Now
                  </button>
                </div>
                <div className="hidden md:block pr-8">
                  <Rocket className="w-32 h-32 text-white opacity-20" />
                </div>
              </div>
            </div>
          </motion.section>

          {/* Recent Files */}
          <section>
            <div className="flex items-center justify-between mb-4 md:mb-6">
              <h3 className="text-sm md:text-2xl font-bold md:font-semibold text-text-secondary md:text-text-primary tracking-wider md:tracking-normal uppercase md:normal-case">Recent Files</h3>
              <div className="hidden md:flex gap-2">
                <button className="w-10 h-10 rounded-full flex items-center justify-center hover:bg-surface-variant text-text-secondary">
                  <LayoutGrid className="w-5 h-5" />
                </button>
                <button className="w-10 h-10 rounded-full flex items-center justify-center hover:bg-surface-variant text-text-secondary">
                  <List className="w-5 h-5" />
                </button>
              </div>
              <Link href="/recent" className="md:hidden text-sm font-medium text-primary-blue">
                View All
              </Link>
            </div>

            <motion.div 
              variants={containerVariants}
              initial="hidden"
              animate="visible"
              key={isLoading ? 'loading' : files.length > 0 ? 'files' : 'templates'}
              className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3 md:gap-6"
            >
              {isLoading ? (
                // Loading skeleton
                <>
                  {[1, 2, 3, 4].map((i) => (
                    <motion.div key={`skeleton-${i}`} variants={itemVariants}>
                      <div className="m3-card bg-surface-variant p-4 md:p-6 h-auto md:h-64 animate-pulse">
                        <div className="flex items-center gap-4 md:flex-col md:items-start">
                          <div className="w-12 h-12 md:w-14 md:h-14 rounded-xl md:rounded-2xl bg-surface-dark shrink-0"></div>
                          <div className="flex-1 md:mt-4 space-y-2 w-full">
                            <div className="h-4 bg-surface-dark rounded w-3/4"></div>
                            <div className="h-3 bg-surface-dark rounded w-1/2"></div>
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  ))}
                </>
              ) : files.length === 0 ? (
                // Show template files matching the Android app
                <>
                  {([
                    { id: 'template1', name: 'Sample Document.md', type: 'markdown' as const, content: '# Sample Document\n\nThis is a template for creating markdown files. Start writing your content here...' },
                    { id: 'template2', name: 'Data Template.json', type: 'json' as const, content: '{\n  "name": "Example",\n  "description": "A sample JSON file",\n  "items": []\n}' },
                    { id: 'template3', name: 'Config.yaml', type: 'yaml' as const, content: '# YAML Configuration\nname: Example\ndata:\n  - item1\n  - item2' },
                    { id: 'template4', name: 'Data.xml', type: 'xml' as const, content: '<?xml version="1.0"?>\n<root>\n  <item>Example</item>\n</root>' },
                    { id: 'template5', name: 'Web Page.html', type: 'html' as const, content: '<!DOCTYPE html>\n<html>\n<head>\n  <title>Sample Page</title>\n</head>\n<body>\n  <h1>Hello World</h1>\n</body>\n</html>' },
                    { id: 'template6', name: 'Notes.txt', type: 'text' as const, content: 'This is a sample text file.\n\nAdd your notes here...' },
                    { id: 'template7', name: 'App.log', type: 'log' as const, content: '[INFO] Application started\n[INFO] Loading modules...' },
                    { id: 'template8', name: 'Data.csv', type: 'csv' as const, content: 'Name,Value,Category\nItem 1,100,A\nItem 2,200,B' },
                  ]).map((file) => (
                    <motion.div key={file.id} variants={itemVariants}>
                      <div role="button" tabIndex={0} onClick={() => createFile(file.name, file.content, file.type)} onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); createFile(file.name, file.content, file.type); } }} className="m3-card bg-surface-variant p-4 md:p-6 flex flex-row md:flex-col justify-between items-center md:items-start h-auto md:h-64 cursor-pointer group w-full text-left">
                        <div className="flex items-center md:items-start md:justify-between w-full md:w-auto gap-4 md:gap-0">
                          <div className={`w-12 h-12 md:w-14 md:h-14 rounded-xl md:rounded-2xl bg-surface-dark flex items-center justify-center border border-white/5 transition-colors ${getFileColorClass(file.type)} shrink-0`}>
                            {getFileIcon(file.type)}
                          </div>
                          <div className="flex-1 md:hidden">
                            <h4 className={`text-base font-semibold text-text-primary mb-0.5 transition-colors truncate ${getFileTextColorClass(file.type)}`}>{file.name}</h4>
                            <p className="text-xs text-text-secondary uppercase tracking-wider">
                              Template
                            </p>
                          </div>
                          <button className="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center text-text-secondary" onClick={(e) => { e.preventDefault(); e.stopPropagation(); }}>
                            <MoreVertical className="w-5 h-5" />
                          </button>
                        </div>
                        <div className="hidden md:block mt-4 w-full">
                          <h4 className={`text-lg font-semibold text-text-primary mb-1 transition-colors truncate ${getFileTextColorClass(file.type)}`}>{file.name}</h4>
                          <p className="text-sm text-text-secondary mb-4 line-clamp-2">{file.content.substring(0, 100)}...</p>
                          <div className="flex items-center gap-2 text-xs text-text-secondary font-medium uppercase tracking-wider">
                            <span>Template</span>
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  ))}
                  {/* Create New File Card */}
                  <motion.div variants={itemVariants}>
                    <button onClick={handleCreateNew} className="w-full m3-card border-2 border-dashed border-surface-variant hover:border-primary-blue bg-transparent p-6 flex flex-col items-center justify-center h-64 cursor-pointer group transition-colors">
                      <div className="w-16 h-16 rounded-full bg-surface-variant group-hover:bg-primary-blue flex items-center justify-center transition-colors mb-4 mx-auto mt-6">
                        <Plus className="text-text-secondary group-hover:text-white w-8 h-8" />
                      </div>
                      <h4 className="text-lg font-semibold text-text-secondary group-hover:text-primary-blue transition-colors text-center">Create New File</h4>
                    </button>
                  </motion.div>
                </>
              ) : (
                files.map((file) => (
                  <motion.div key={file.id} variants={itemVariants}>
                    <div className="m3-card bg-surface-variant p-4 md:p-6 flex flex-row md:flex-col justify-between items-center md:items-start h-auto md:h-64 cursor-pointer group relative">
                      <Link href={`/editor?id=${file.id}`} className="absolute inset-0 z-0" onClick={() => openFileInEditor(file.id)}></Link>
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
                        <div className="relative">
                          <span role="button" tabIndex={0} className="w-8 h-8 rounded-full hover:bg-white/10 flex items-center justify-center text-text-secondary" onClick={(e) => handleMenuToggle(file.id, e)} onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); e.stopPropagation(); setMenuFileId(prev => prev === file.id ? null : file.id); } }}>
                            <MoreVertical className="w-5 h-5" />
                          </span>
                          {/* Context Menu */}
                          {menuFileId === file.id && (
                            <>
                              <div className="fixed inset-0 z-40" onClick={() => setMenuFileId(null)}></div>
                              <div className="absolute right-0 top-10 w-48 bg-surface-dark border border-surface-variant rounded-2xl shadow-xl z-50 overflow-hidden py-1">
                                <button onClick={() => handleOpenInEditor(file.id)} className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-text-primary hover:bg-surface-variant transition-colors">
                                  <ExternalLink className="w-4 h-4 text-primary-blue" /> Open in Editor
                                </button>
                                <button onClick={() => handleRenameStart(file.id)} className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-text-primary hover:bg-surface-variant transition-colors">
                                  <Pencil className="w-4 h-4 text-accent-emerald" /> Rename
                                </button>
                                <button onClick={() => { setDeleteConfirmId(file.id); setMenuFileId(null); }} className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-red-400 hover:bg-red-500/10 transition-colors">
                                  <Trash2 className="w-4 h-4" /> Delete
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

              {/* Create New File Card (when files exist) */}
              {!isLoading && files.length > 0 && (
                <motion.div variants={itemVariants} className="hidden md:block">
                  <button onClick={handleCreateNew} className="w-full m3-card border-2 border-dashed border-surface-variant hover:border-primary-blue bg-transparent p-6 flex flex-col items-center justify-center h-64 cursor-pointer group transition-colors">
                    <div className="w-16 h-16 rounded-full bg-surface-variant group-hover:bg-primary-blue flex items-center justify-center transition-colors mb-4 mx-auto mt-6">
                      <Plus className="text-text-secondary group-hover:text-white w-8 h-8" />
                    </div>
                    <h4 className="text-lg font-semibold text-text-secondary group-hover:text-primary-blue transition-colors text-center">Create New File</h4>
                  </button>
                </motion.div>
              )}
            </motion.div>
          </section>
        </div>
      </main>
      <BottomNav />

      {/* File Type Selection Dialog */}
      <AnimatePresence>
        {showFileTypeDialog && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-end md:items-center justify-center"
            onClick={() => setShowFileTypeDialog(false)}
          >
            <motion.div
              initial={{ y: 100, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 100, opacity: 0 }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              className="bg-surface-dark w-full md:max-w-md md:rounded-3xl rounded-t-3xl border border-surface-variant overflow-hidden"
              onClick={(e) => e.stopPropagation()}
            >
              {/* Header */}
              <div className="flex items-center justify-between p-6 pb-2">
                <h2 className="text-xl font-bold text-white">Choose File Type</h2>
                <button
                  onClick={() => setShowFileTypeDialog(false)}
                  className="w-8 h-8 rounded-full hover:bg-surface-variant flex items-center justify-center text-text-secondary transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* File Type List */}
              <div className="px-6 pb-6 pt-2 space-y-1 max-h-[60vh] overflow-y-auto custom-scrollbar">
                {FILE_TYPES.map((ft) => (
                  <button
                    key={ft.type}
                    onClick={() => handleFileTypeSelected(ft)}
                    className="w-full flex items-center justify-between px-4 py-3 rounded-2xl hover:bg-surface-variant text-left transition-colors group"
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-xl bg-surface-variant group-hover:bg-surface-dark flex items-center justify-center transition-colors">
                        {getFileIcon(ft.type)}
                      </div>
                      <div>
                        <p className="text-sm font-semibold text-text-primary">{ft.label}</p>
                        <p className="text-xs text-text-secondary">.{ft.ext}</p>
                      </div>
                    </div>
                    <Plus className="w-5 h-5 text-primary-blue opacity-0 group-hover:opacity-100 transition-opacity" />
                  </button>
                ))}
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Rename Dialog */}
      <AnimatePresence>
        {renameFileId && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            onClick={() => setRenameFileId(null)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-surface-dark w-full max-w-sm rounded-3xl border border-surface-variant p-6"
              onClick={(e) => e.stopPropagation()}
            >
              <h2 className="text-lg font-bold text-white mb-4">Rename File</h2>
              <input
                autoFocus
                value={renameValue}
                onChange={(e) => setRenameValue(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') handleRenameSubmit(); }}
                className="w-full h-12 px-4 rounded-xl bg-surface-variant border border-white/10 text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-primary-blue outline-none"
                placeholder="File name"
              />
              <div className="flex justify-end gap-3 mt-6">
                <button onClick={() => setRenameFileId(null)} className="px-5 py-2.5 rounded-full text-sm font-medium text-text-secondary hover:bg-surface-variant transition-colors">Cancel</button>
                <button onClick={handleRenameSubmit} className="px-5 py-2.5 rounded-full text-sm font-medium bg-primary-blue text-white hover:bg-primary-blue/90 transition-colors">Rename</button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Delete Confirmation Dialog */}
      <AnimatePresence>
        {deleteConfirmId && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            onClick={() => setDeleteConfirmId(null)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-surface-dark w-full max-w-sm rounded-3xl border border-surface-variant p-6"
              onClick={(e) => e.stopPropagation()}
            >
              <h2 className="text-lg font-bold text-white mb-2">Delete File</h2>
              <p className="text-sm text-text-secondary mb-6">Are you sure you want to delete &ldquo;{files.find(f => f.id === deleteConfirmId)?.name}&rdquo;? This action cannot be undone.</p>
              <div className="flex justify-end gap-3">
                <button onClick={() => setDeleteConfirmId(null)} className="px-5 py-2.5 rounded-full text-sm font-medium text-text-secondary hover:bg-surface-variant transition-colors">Cancel</button>
                <button onClick={handleDeleteConfirm} className="px-5 py-2.5 rounded-full text-sm font-medium bg-red-500 text-white hover:bg-red-600 transition-colors">Delete</button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* URL Dialog */}
      <AnimatePresence>
        {showUrlDialog && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-end md:items-center justify-center"
            onClick={() => setShowUrlDialog(false)}
          >
            <motion.div
              initial={{ y: 100, opacity: 0 }}
              animate={{ y: 0, opacity: 1 }}
              exit={{ y: 100, opacity: 0 }}
              transition={{ type: "spring", damping: 25, stiffness: 300 }}
              className="bg-surface-dark w-full md:max-w-md md:rounded-3xl rounded-t-3xl border border-surface-variant overflow-hidden p-6"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-bold text-white flex items-center gap-3">
                  <Link2 className="w-6 h-6 text-accent-purple" />
                  Open from URL
                </h2>
                <button
                  onClick={() => setShowUrlDialog(false)}
                  className="w-8 h-8 rounded-full hover:bg-surface-variant flex items-center justify-center text-text-secondary transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <p className="text-sm text-text-secondary mb-4">
                Enter a URL to view a file temporarily. The file will not be saved to your library.
              </p>
              <input
                type="url"
                value={urlInput}
                onChange={(e) => setUrlInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') handleUrlSubmit(); }}
                placeholder="https://example.com/file.md"
                className="w-full h-12 px-4 rounded-xl bg-surface-variant border border-white/10 text-text-primary placeholder-text-secondary focus:ring-2 focus:ring-accent-purple outline-none"
                autoFocus
              />
              <div className="flex justify-end gap-3 mt-6">
                <button 
                  onClick={() => setShowUrlDialog(false)} 
                  className="px-5 py-2.5 rounded-full text-sm font-medium text-text-secondary hover:bg-surface-variant transition-colors"
                >
                  Cancel
                </button>
                <button 
                  onClick={handleUrlSubmit} 
                  disabled={!urlInput.trim()}
                  className="px-5 py-2.5 rounded-full text-sm font-medium bg-accent-purple text-white hover:bg-accent-purple/90 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  Open File
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
