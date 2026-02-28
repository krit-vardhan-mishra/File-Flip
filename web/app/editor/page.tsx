'use client';

import { useState, useEffect, Suspense } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { motion } from 'motion/react';
import { 
  FileText, 
  Undo, 
  Redo, 
  Save, 
  Download, 
  FolderPlus, 
  FolderOpen, 
  FileCode2, 
  Braces, 
  Code, 
  Folder, 
  Settings, 
  X, 
  ZoomIn, 
  Columns, 
  HelpCircle,
  ChevronLeft,
  Paperclip,
  MoreVertical,
  Bold,
  Italic,
  Link as LinkIcon,
  List,
  Image as ImageIcon,
  Play
} from 'lucide-react';
import ExportModal from '@/components/ExportModal';
import { useFiles } from '@/lib/FileContext';
import CodeMirror from '@uiw/react-codemirror';
import { markdown, markdownLanguage } from '@codemirror/lang-markdown';
import { json } from '@codemirror/lang-json';
import { html } from '@codemirror/lang-html';
import { vscodeDark } from '@uiw/codemirror-theme-vscode';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

function EditorContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const fileIdParam = searchParams.get('id');
  const { files, activeFileId, setActiveFileId, updateFile, saveLocalFile } = useFiles();
  
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const [showPreviewMobile, setShowPreviewMobile] = useState(false);

  useEffect(() => {
    if (fileIdParam && files.some(f => f.id === fileIdParam)) {
      setActiveFileId(fileIdParam);
    } else if (files.length > 0 && !activeFileId) {
      setActiveFileId(files[0].id);
    }
  }, [fileIdParam, files, activeFileId, setActiveFileId]);

  const activeFile = files.find(f => f.id === activeFileId);

  const handleContentChange = (value: string) => {
    if (activeFileId) {
      updateFile(activeFileId, { content: value });
    }
  };

  const handleSave = async () => {
    if (activeFileId) {
      await saveLocalFile(activeFileId);
    }
  };

  const getLanguageExtension = (type: string) => {
    switch (type) {
      case 'markdown': return [markdown({ base: markdownLanguage })];
      case 'json': return [json()];
      case 'html': return [html()];
      default: return [];
    }
  };

  if (!activeFile) {
    return (
      <div className="h-screen flex items-center justify-center bg-md-sys-color-background text-md-sys-color-on-background">
        <div className="text-center">
          <FileText className="w-16 h-16 mx-auto mb-4 opacity-20" />
          <h2 className="text-xl font-semibold mb-2">No File Selected</h2>
          <Link href="/" className="text-md-sys-color-primary hover:underline">Return to Dashboard</Link>
        </div>
      </div>
    );
  }

  return (
    <motion.div 
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="h-screen flex flex-col overflow-hidden selection:bg-md-sys-color-primary selection:text-white bg-md-sys-color-background text-md-sys-color-on-background font-sans"
    >
      {/* Mobile Header */}
      <header className="md:hidden h-14 flex items-center justify-between px-4 bg-md-sys-color-surface border-b border-md-sys-color-outline-variant shrink-0 z-20">
        <div className="flex items-center gap-3">
          <Link href="/" className="p-1 -ml-1 rounded-full hover:bg-md-sys-color-surface-variant transition-colors text-md-sys-color-on-surface-variant">
            <ChevronLeft className="w-6 h-6" />
          </Link>
          <FileText className="w-5 h-5 text-md-sys-color-primary" />
          <span className="text-lg font-bold tracking-tight text-md-sys-color-on-surface">MarkPDF</span>
        </div>
        <div className="flex items-center gap-2">
          <button className="p-2 text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface transition-colors">
            <Paperclip className="w-5 h-5" />
          </button>
          <button onClick={handleSave} className="p-2 text-md-sys-color-primary hover:text-md-sys-color-primary/80 transition-colors">
            <Save className="w-5 h-5" />
          </button>
          <button className="p-2 text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface transition-colors">
            <MoreVertical className="w-5 h-5" />
          </button>
        </div>
      </header>

      {/* Desktop Header */}
      <motion.header 
        initial={{ y: -20, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ delay: 0.1 }}
        className="hidden md:flex h-16 items-center justify-between px-6 bg-md-sys-color-surface border-b border-md-sys-color-outline-variant shrink-0 z-20"
      >
        <div className="flex items-center gap-4">
          <Link href="/" className="p-2 -ml-2 rounded-full hover:bg-md-sys-color-surface-variant transition-colors text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface">
            <ChevronLeft className="w-6 h-6" />
          </Link>
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-lg bg-md-sys-color-primary/10 flex items-center justify-center">
              <FileText className="w-5 h-5 text-md-sys-color-primary" />
            </div>
            <div>
              <h1 className="text-sm font-semibold leading-tight">{activeFile.name}</h1>
              <p className="text-xs text-md-sys-color-on-surface-variant flex items-center gap-2">
                <span>{activeFile.handle ? 'Local File' : 'Cloud Draft'}</span>
                <span className="w-1 h-1 rounded-full bg-md-sys-color-outline-variant"></span>
                <span>Saved</span>
              </p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <div className="flex items-center bg-md-sys-color-surface-variant rounded-full p-1 mr-4">
            <button className="p-2 rounded-full hover:bg-md-sys-color-surface text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface transition-colors">
              <Undo className="w-4 h-4" />
            </button>
            <button className="p-2 rounded-full hover:bg-md-sys-color-surface text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface transition-colors">
              <Redo className="w-4 h-4" />
            </button>
          </div>
          
          <button onClick={handleSave} className="m3-button px-4 h-10 bg-md-sys-color-surface-variant hover:bg-md-sys-color-surface-variant/80 text-md-sys-color-on-surface-variant font-medium flex items-center gap-2">
            <Save className="w-4 h-4" />
            Save
          </button>
          <button onClick={() => setIsExportModalOpen(true)} className="m3-button px-4 h-10 bg-md-sys-color-primary hover:bg-md-sys-color-primary/90 text-md-sys-color-on-primary font-medium flex items-center gap-2 shadow-md shadow-md-sys-color-primary/20">
            <Download className="w-4 h-4" />
            Export
          </button>
        </div>
      </motion.header>

      {/* Main Workspace */}
      <main className="flex-1 flex flex-col md:flex-row overflow-hidden">
        {/* Desktop Sidebar */}
        <aside className="hidden md:flex w-64 flex-shrink-0 flex-col border-r border-md-sys-color-outline-variant bg-md-sys-color-surface-variant/30">
          <div className="p-4 flex items-center justify-between border-b border-md-sys-color-outline-variant/50">
            <h2 className="text-xs font-bold uppercase tracking-wider text-md-sys-color-on-surface-variant">Explorer</h2>
            <div className="flex gap-1">
              <button className="p-1.5 rounded-md hover:bg-md-sys-color-surface-variant text-md-sys-color-on-surface-variant transition-colors">
                <FolderPlus className="w-4 h-4" />
              </button>
            </div>
          </div>
          <div className="flex-1 overflow-y-auto p-2 custom-scrollbar">
            <div className="space-y-1">
              {files.map(file => (
                <button 
                  key={file.id}
                  onClick={() => setActiveFileId(file.id)}
                  className={`w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors ${activeFileId === file.id ? 'bg-md-sys-color-primary/10 text-md-sys-color-primary font-medium' : 'hover:bg-md-sys-color-surface-variant text-md-sys-color-on-surface-variant'}`}
                >
                  <FileText className="w-4 h-4" />
                  <span className="truncate">{file.name}</span>
                </button>
              ))}
            </div>
          </div>
        </aside>

        {/* Editor Area */}
        <div className="flex-1 flex flex-col min-w-0 bg-md-sys-color-background">
          {/* Tabs */}
          <div className="h-12 flex items-end px-2 bg-md-sys-color-surface border-b border-md-sys-color-outline-variant md:bg-md-sys-color-surface-variant/50 shrink-0 overflow-x-auto custom-scrollbar">
            {files.slice(0, 3).map(file => (
              <button 
                key={file.id}
                onClick={() => setActiveFileId(file.id)}
                className={`h-10 px-4 flex items-center gap-2 border-b-2 transition-colors min-w-max ${activeFileId === file.id ? 'border-md-sys-color-primary text-md-sys-color-primary bg-md-sys-color-surface-variant/50 md:bg-md-sys-color-background' : 'border-transparent text-md-sys-color-on-surface-variant hover:bg-md-sys-color-surface-variant/30 md:hover:bg-md-sys-color-surface-variant'}`}
              >
                <FileText className="w-4 h-4" />
                <span className="text-sm font-medium">{file.name}</span>
              </button>
            ))}
          </div>

          {/* Split View Container */}
          <div className="flex-1 flex overflow-hidden relative">
            {/* Code Editor Pane */}
            <div className={`flex-1 flex flex-col min-w-0 ${showPreviewMobile ? 'hidden md:flex' : 'flex'}`}>
              <div className="flex-1 overflow-auto custom-scrollbar bg-[#1E1E1E]">
                <CodeMirror
                  value={activeFile.content}
                  height="100%"
                  theme={vscodeDark}
                  extensions={getLanguageExtension(activeFile.type)}
                  onChange={handleContentChange}
                  className="h-full text-base"
                  basicSetup={{
                    lineNumbers: true,
                    highlightActiveLineGutter: true,
                    foldGutter: true,
                    dropCursor: true,
                    allowMultipleSelections: true,
                    indentOnInput: true,
                    bracketMatching: true,
                    closeBrackets: true,
                    autocompletion: true,
                    rectangularSelection: true,
                    crosshairCursor: true,
                    highlightActiveLine: true,
                    highlightSelectionMatches: true,
                    closeBracketsKeymap: true,
                    defaultKeymap: true,
                    searchKeymap: true,
                    historyKeymap: true,
                    foldKeymap: true,
                    completionKeymap: true,
                    lintKeymap: true,
                  }}
                />
              </div>
            </div>

            {/* Preview Pane (Desktop or Mobile when toggled) */}
            <div className={`flex-1 flex-col min-w-0 border-l border-md-sys-color-outline-variant bg-white ${showPreviewMobile ? 'flex' : 'hidden md:flex'}`}>
              <div className="h-10 flex items-center justify-between px-4 border-b border-md-sys-color-outline-variant bg-md-sys-color-surface shrink-0">
                <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wider text-md-sys-color-on-surface-variant">
                  <ZoomIn className="w-4 h-4" />
                  Live Preview
                </div>
                <div className="flex gap-1">
                  <button className="p-1.5 rounded hover:bg-md-sys-color-surface-variant text-md-sys-color-on-surface-variant transition-colors">
                    <Columns className="w-4 h-4" />
                  </button>
                </div>
              </div>
              <div className="flex-1 overflow-y-auto p-4 md:p-8 custom-scrollbar bg-[#F8F9FA]" id="preview-container">
                <div className="max-w-3xl mx-auto bg-white p-6 md:p-10 rounded-lg shadow-sm border border-gray-200 min-h-full prose prose-slate max-w-none">
                  <ReactMarkdown remarkPlugins={[remarkGfm]}>
                    {activeFile.content}
                  </ReactMarkdown>
                </div>
              </div>
            </div>

            {/* Mobile FAB for Preview Toggle */}
            <button 
              className="md:hidden absolute bottom-20 right-6 w-14 h-14 rounded-2xl bg-md-sys-color-primary text-md-sys-color-on-primary flex items-center justify-center shadow-lg shadow-md-sys-color-primary/40 z-30"
              onClick={() => setShowPreviewMobile(!showPreviewMobile)}
            >
              {showPreviewMobile ? <Code className="w-6 h-6" /> : <Play className="w-6 h-6" />}
            </button>
          </div>
          
          {/* Mobile Bottom Formatting Bar */}
          <div className="md:hidden h-14 bg-md-sys-color-surface border-t border-md-sys-color-outline-variant flex items-center justify-between px-2 overflow-x-auto shrink-0">
            <div className="flex items-center gap-1 min-w-max">
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><Bold className="w-5 h-5" /></button>
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><Italic className="w-5 h-5" /></button>
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><LinkIcon className="w-5 h-5" /></button>
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><Code className="w-5 h-5" /></button>
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><List className="w-5 h-5" /></button>
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><ImageIcon className="w-5 h-5" /></button>
            </div>
            <div className="flex items-center gap-1 border-l border-md-sys-color-outline-variant pl-2 ml-2 min-w-max">
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><Undo className="w-5 h-5" /></button>
              <button className="w-10 h-10 flex items-center justify-center text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface rounded-lg hover:bg-md-sys-color-surface-variant transition-colors"><Redo className="w-5 h-5" /></button>
            </div>
          </div>
        </div>
      </main>

      <ExportModal 
        isOpen={isExportModalOpen} 
        onClose={() => setIsExportModalOpen(false)} 
        contentId="preview-container"
        fileName={activeFile.name}
      />
    </motion.div>
  );
}

export default function Editor() {
  return (
    <Suspense fallback={<div className="h-screen flex items-center justify-center bg-md-sys-color-background text-md-sys-color-on-background">Loading...</div>}>
      <EditorContent />
    </Suspense>
  );
}
