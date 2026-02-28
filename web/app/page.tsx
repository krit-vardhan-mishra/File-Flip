'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { motion } from 'motion/react';
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
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import { useFiles } from '@/lib/FileContext';
import { formatDistanceToNow } from 'date-fns';
import BottomNav from '@/components/BottomNav';

export default function Dashboard() {
  const { files, createFile, openLocalFile } = useFiles();
  const router = useRouter();
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);

  const handleCreateNew = async () => {
    const newFile = await createFile('Untitled.md', '# New Document\n\nStart typing here...', 'markdown');
    router.push('/editor');
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
      {/* Desktop Sidebar */}
      <aside className={`hidden md:flex flex-shrink-0 flex-col border-r border-surface-variant bg-surface-dark transition-all duration-300 ${isSidebarOpen ? 'w-72' : 'w-20'}`}>
        <div className={`h-20 flex items-center ${isSidebarOpen ? 'px-6 justify-between' : 'justify-center'}`}>
          {isSidebarOpen && (
            <div className="flex items-center gap-3 overflow-hidden">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-primary-blue to-accent-emerald flex items-center justify-center shadow-lg shadow-primary-blue/20 shrink-0">
                <FileText className="text-white w-6 h-6" />
              </div>
              <span className="text-xl font-bold tracking-tight whitespace-nowrap">MarkPDF</span>
            </div>
          )}
          <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="w-10 h-10 rounded-xl hover:bg-surface-variant flex items-center justify-center text-text-secondary shrink-0 transition-colors">
            {isSidebarOpen ? <ChevronLeft className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
        
        <nav className="flex-1 px-4 py-6 space-y-2">
          <Link href="/" className={`flex items-center gap-4 ${isSidebarOpen ? 'px-4 py-4' : 'p-3 justify-center'} rounded-full bg-primary-container text-primary-blue font-medium transition-all`}>
            <LayoutDashboard className="w-6 h-6 shrink-0" />
            {isSidebarOpen && <span className="whitespace-nowrap">Dashboard</span>}
          </Link>
          <Link href="/library" className={`flex items-center gap-4 ${isSidebarOpen ? 'px-4 py-4' : 'p-3 justify-center'} rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-all`}>
            <FolderOpen className="w-6 h-6 shrink-0" />
            {isSidebarOpen && <span className="whitespace-nowrap">My Files</span>}
          </Link>
          <Link href="/starred" className={`flex items-center gap-4 ${isSidebarOpen ? 'px-4 py-4' : 'p-3 justify-center'} rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-all`}>
            <Star className="w-6 h-6 shrink-0" />
            {isSidebarOpen && <span className="whitespace-nowrap">Starred</span>}
          </Link>
          <Link href="/recent" className={`flex items-center gap-4 ${isSidebarOpen ? 'px-4 py-4' : 'p-3 justify-center'} rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-all`}>
            <Clock className="w-6 h-6 shrink-0" />
            {isSidebarOpen && <span className="whitespace-nowrap">Recent</span>}
          </Link>
          
          <div className="pt-4 mt-4 border-t border-surface-variant">
            <Link href="/settings" className={`flex items-center gap-4 ${isSidebarOpen ? 'px-4 py-4' : 'p-3 justify-center'} rounded-full hover:bg-surface-variant text-text-secondary hover:text-text-primary transition-all`}>
              <Settings className="w-6 h-6 shrink-0" />
              {isSidebarOpen && <span className="whitespace-nowrap">Settings</span>}
            </Link>
          </div>
        </nav>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden relative bg-surface-dark pb-16 md:pb-0">
        {/* Mobile Header */}
        <header className="md:hidden h-16 flex items-center justify-between px-4 flex-shrink-0 border-b border-surface-variant">
          <button className="w-10 h-10 rounded-xl bg-surface-variant flex items-center justify-center text-primary-blue">
            <Menu className="w-6 h-6" />
          </button>
          <h1 className="text-lg font-bold tracking-tight text-white">MarkPDF</h1>
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
            <button onClick={handleCreateNew} className="m3-button h-12 px-6 bg-primary-blue hover:bg-[#0B8AC9] text-white font-semibold flex items-center gap-2 shadow-lg shadow-primary-blue/30">
              <Plus className="w-5 h-5" />
              New File
            </button>
          </div>
        </header>

        <div className="flex-1 overflow-y-auto px-4 md:px-8 lg:px-12 pb-12 custom-scrollbar">
          
          {/* Mobile Actions */}
          <div className="md:hidden mt-6 mb-8 space-y-4">
            <button 
              onClick={handleCreateNew}
              className="w-full rounded-3xl bg-surface-variant border border-primary-blue/20 p-8 flex flex-col items-center justify-center gap-4 relative overflow-hidden group shadow-lg shadow-primary-blue/5"
            >
              <div className="absolute inset-0 bg-gradient-to-b from-primary-blue/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
              <div className="w-16 h-16 rounded-full bg-primary-blue flex items-center justify-center shadow-lg shadow-primary-blue/40">
                <Plus className="w-8 h-8 text-white" />
              </div>
              <div className="text-center z-10">
                <h2 className="text-xl font-bold text-white mb-1">Create New PDF</h2>
                <p className="text-sm text-text-secondary">Convert Markdown instantly</p>
              </div>
            </button>

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
              className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3 md:gap-6"
            >
              {files.length === 0 ? (
                <div className="col-span-full py-12 flex flex-col items-center justify-center text-text-secondary">
                  <FileText className="w-16 h-16 mb-4 opacity-20" />
                  <p className="mb-8 text-center">No files yet. Create one to get started!</p>
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
                  <motion.div key={file.id} variants={itemVariants}>
                    <Link href={`/editor?id=${file.id}`} className="m3-card bg-surface-variant p-4 md:p-6 flex flex-row md:flex-col justify-between items-center md:items-start h-auto md:h-64 cursor-pointer group block">
                      <div className="flex items-center md:items-start md:justify-between w-full md:w-auto gap-4 md:gap-0">
                        <div className={`w-12 h-12 md:w-14 md:h-14 rounded-xl md:rounded-2xl bg-surface-dark flex items-center justify-center border border-white/5 transition-colors ${getFileColorClass(file.type)} shrink-0`}>
                          {getFileIcon(file.type)}
                        </div>
                        <div className="flex-1 md:hidden">
                          <h4 className={`text-base font-semibold text-text-primary mb-0.5 transition-colors truncate ${getFileTextColorClass(file.type)}`}>{file.name}</h4>
                          <p className="text-xs text-text-secondary uppercase tracking-wider">
                            {formatDistanceToNow(file.lastModified, { addSuffix: true })}
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
                          <span>{formatDistanceToNow(file.lastModified, { addSuffix: true })}</span>
                        </div>
                      </div>
                    </Link>
                  </motion.div>
                ))
              )}

              {/* Create New File Card (Desktop Only) */}
              <motion.div variants={itemVariants} className="hidden md:block">
                <button onClick={handleCreateNew} className="w-full m3-card border-2 border-dashed border-surface-variant hover:border-primary-blue bg-transparent p-6 flex flex-col items-center justify-center h-64 cursor-pointer group transition-colors block">
                  <div className="w-16 h-16 rounded-full bg-surface-variant group-hover:bg-primary-blue flex items-center justify-center transition-colors mb-4 mx-auto mt-6">
                    <Plus className="text-text-secondary group-hover:text-white w-8 h-8" />
                  </div>
                  <h4 className="text-lg font-semibold text-text-secondary group-hover:text-primary-blue transition-colors text-center">Create New File</h4>
                </button>
              </motion.div>
            </motion.div>
          </section>
        </div>
      </main>
      <BottomNav />
    </div>
  );
}
