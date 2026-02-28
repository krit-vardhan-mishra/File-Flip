'use client';

import { useState, useEffect, Suspense } from 'react';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { motion, AnimatePresence } from 'motion/react';
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
  Play,
  FileCog,
  FileSpreadsheet,
  ScrollText,
  Circle,
  ChevronRight,
  ChevronDown,
  Eye,
  Loader2
} from 'lucide-react';
import ExportModal from '@/components/ExportModal';
import { useFiles } from '@/lib/FileContext';
import Sidebar from '@/components/Sidebar';
import CodeMirror from '@uiw/react-codemirror';
import { markdown, markdownLanguage } from '@codemirror/lang-markdown';
import { json } from '@codemirror/lang-json';
import { html } from '@codemirror/lang-html';
import { xml } from '@codemirror/lang-xml';
import { StreamLanguage } from '@codemirror/language';
import { yaml } from '@codemirror/legacy-modes/mode/yaml';
import { vscodeDark } from '@uiw/codemirror-theme-vscode';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

// --- Tree View for JSON / XML / YAML ---
function TreeNode({ label, value, depth = 0 }: { label: string; value: unknown; depth?: number }) {
  const [open, setOpen] = useState(true);
  const isObject = value !== null && typeof value === 'object';
  const isArray = Array.isArray(value);

  if (!isObject) {
    const color = typeof value === 'string' ? 'text-green-700' : typeof value === 'number' ? 'text-blue-700' : typeof value === 'boolean' ? 'text-purple-700' : 'text-gray-500';
    return (
      <div className="flex items-start gap-1 py-0.5 not-prose" style={{ paddingLeft: depth * 16 }}>
        <span className="text-sm font-medium text-gray-700 shrink-0">{label}:</span>
        <span className={`text-sm ${color}`}>{value === null ? 'null' : String(value)}</span>
      </div>
    );
  }

  const entries = isArray ? (value as unknown[]).map((v, i) => [String(i), v] as const) : Object.entries(value as Record<string, unknown>);
  const typeLabel = isArray ? `[${entries.length}]` : `{${entries.length}}`;

  return (
    <div className="not-prose" style={{ paddingLeft: depth * 16 }}>
      <button onClick={() => setOpen(!open)} className="flex items-center gap-1 py-0.5 hover:bg-gray-100 rounded w-full text-left transition-colors">
        {open ? <ChevronDown className="w-3.5 h-3.5 text-gray-400 shrink-0" /> : <ChevronRight className="w-3.5 h-3.5 text-gray-400 shrink-0" />}
        <span className="text-sm font-medium text-gray-700">{label}</span>
        <span className="text-xs text-gray-400 ml-1">{typeLabel}</span>
      </button>
      {open && entries.map(([k, v]) => <TreeNode key={k} label={k} value={v} depth={depth + 1} />)}
    </div>
  );
}

function parseYamlSimple(text: string): Record<string, unknown> {
  const result: Record<string, unknown> = {};
  const lines = text.split('\n');
  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith('#')) continue;
    const colonIdx = trimmed.indexOf(':');
    if (colonIdx === -1) continue;
    const key = trimmed.slice(0, colonIdx).trim();
    let val: string | boolean | number | null = trimmed.slice(colonIdx + 1).trim();
    if (val === 'true') val = true;
    else if (val === 'false') val = false;
    else if (val === 'null' || val === '~') val = null;
    else if (!isNaN(Number(val)) && val !== '') val = Number(val);
    result[key] = val;
  }
  return result;
}

function parseXmlSimple(text: string): Record<string, unknown> {
  const result: Record<string, unknown> = {};
  const tagRegex = /<(\w+)([^>]*)>([\s\S]*?)<\/\1>/g;
  let match;
  while ((match = tagRegex.exec(text)) !== null) {
    const [, tag, , inner] = match;
    if (inner.includes('<')) {
      result[tag] = parseXmlSimple(inner);
    } else {
      result[tag] = inner.trim();
    }
  }
  return result;
}

function TreeView({ content, type }: { content: string; type: 'json' | 'xml' | 'yaml' }) {
  try {
    let data: unknown;
    if (type === 'json') {
      data = JSON.parse(content);
    } else if (type === 'xml') {
      data = parseXmlSimple(content);
    } else {
      data = parseYamlSimple(content);
    }
    return <TreeNode label="root" value={data} />;
  } catch {
    return (
      <pre className="whitespace-pre-wrap break-words text-sm font-mono text-gray-800 not-prose">
        {content}
      </pre>
    );
  }
}

function CsvTable({ content }: { content: string }) {
  const rows = content.split('\n').filter(r => r.trim()).map(r => r.split(',').map(c => c.trim()));
  if (rows.length === 0) return <p className="text-gray-400 text-sm">Empty CSV</p>;
  const [header, ...body] = rows;
  return (
    <div className="overflow-x-auto not-prose">
      <table className="w-full text-sm border-collapse">
        <thead>
          <tr className="bg-gray-100">
            {header.map((h, i) => (
              <th key={i} className="border border-gray-300 px-3 py-2 text-left font-semibold text-gray-700">{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {body.map((row, ri) => (
            <tr key={ri} className={ri % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
              {row.map((cell, ci) => (
                <td key={ci} className="border border-gray-300 px-3 py-1.5 text-gray-600">{cell}</td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function EditorContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const fileIdParam = searchParams.get('id');
  const { files, isLoading, activeFileId, setActiveFileId, updateFile, saveLocalFile, openFileIds, hasUnsavedChanges, openFileInEditor, closeFileInEditor } = useFiles();
  
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const [showPreviewMobile, setShowPreviewMobile] = useState(false);
  const [closeConfirmFileId, setCloseConfirmFileId] = useState<string | null>(null);

  useEffect(() => {
    if (fileIdParam && files.some(f => f.id === fileIdParam)) {
      openFileInEditor(fileIdParam);
    } else if (openFileIds.length > 0 && !activeFileId) {
      setActiveFileId(openFileIds[0]);
    }
  }, [fileIdParam, files, activeFileId, setActiveFileId, openFileInEditor, openFileIds]);

  const activeFile = files.find(f => f.id === activeFileId);
  const openFiles = files.filter(f => openFileIds.includes(f.id));

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

  const handleCloseTab = (fileId: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const isLastFile = openFiles.length <= 1;
    const closed = closeFileInEditor(fileId);
    if (!closed) {
      setCloseConfirmFileId(fileId);
    } else if (isLastFile) {
      router.push('/');
    }
  };

  const handleCloseConfirmSave = async () => {
    if (closeConfirmFileId) {
      const isLastFile = openFiles.length <= 1;
      await saveLocalFile(closeConfirmFileId);
      closeFileInEditor(closeConfirmFileId, true);
      setCloseConfirmFileId(null);
      if (isLastFile) router.push('/');
    }
  };

  const handleCloseConfirmDiscard = () => {
    if (closeConfirmFileId) {
      const isLastFile = openFiles.length <= 1;
      closeFileInEditor(closeConfirmFileId, true);
      setCloseConfirmFileId(null);
      if (isLastFile) router.push('/');
    }
  };

  const getFileIcon = (type: string, size = 'w-4 h-4') => {
    switch (type) {
      case 'markdown': return <FileText className={`text-blue-400 ${size}`} />;
      case 'json': return <Braces className={`text-emerald-400 ${size}`} />;
      case 'yaml': return <FileCog className={`text-purple-400 ${size}`} />;
      case 'xml': return <Code className={`text-amber-500 ${size}`} />;
      case 'html': return <FileCode2 className={`text-pink-500 ${size}`} />;
      case 'csv': return <FileSpreadsheet className={`text-green-500 ${size}`} />;
      case 'log': return <ScrollText className={`text-blue-300 ${size}`} />;
      case 'text': return <FileText className={`text-gray-400 ${size}`} />;
      default: return <FileText className={`text-blue-400 ${size}`} />;
    }
  };

  const getLanguageExtension = (type: string) => {
    switch (type) {
      case 'markdown': return [markdown({ base: markdownLanguage })];
      case 'json': return [json()];
      case 'html': return [html()];
      case 'xml': return [xml()];
      case 'yaml': return [StreamLanguage.define(yaml)];
      case 'csv':
      case 'log':
      case 'text':
      default: return [];
    }
  };

  if (isLoading) {
    return (
      <div className="h-screen flex items-center justify-center bg-md-sys-color-background text-md-sys-color-on-background">
        <Loader2 className="w-8 h-8 animate-spin text-md-sys-color-primary" />
      </div>
    );
  }

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
    <div className="h-screen flex overflow-hidden selection:bg-md-sys-color-primary selection:text-white bg-md-sys-color-background">
      <Sidebar />

      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="flex-1 flex flex-col overflow-hidden"
      >
        {/* Mobile Header */}
      <header className="md:hidden h-14 flex items-center justify-between px-3 bg-md-sys-color-surface border-b border-md-sys-color-outline-variant shrink-0 z-20">
        <div className="flex items-center gap-2 min-w-0 flex-1">
          <Link href="/" className="p-1.5 rounded-full hover:bg-md-sys-color-surface-variant transition-colors text-md-sys-color-on-surface-variant shrink-0">
            <ChevronLeft className="w-5 h-5" />
          </Link>
          {showPreviewMobile ? (
            <div className="flex items-center gap-2 min-w-0">
              <Eye className="w-5 h-5 text-md-sys-color-primary shrink-0" />
              <span className="text-base font-semibold text-md-sys-color-on-surface truncate">Preview</span>
            </div>
          ) : (
            <div className="flex items-center gap-2 min-w-0">
              {getFileIcon(activeFile.type, 'w-5 h-5')}
              <span className="text-base font-semibold text-md-sys-color-on-surface truncate">{activeFile.name}</span>
              {hasUnsavedChanges[activeFile.id] && (
                <Circle className="w-2 h-2 fill-accent-orange text-accent-orange shrink-0" />
              )}
            </div>
          )}
        </div>
        <div className="flex items-center gap-0.5 shrink-0">
          {!showPreviewMobile && (
            <button onClick={handleSave} className="p-2 text-md-sys-color-primary hover:text-md-sys-color-primary/80 transition-colors rounded-full">
              <Save className="w-5 h-5" />
            </button>
          )}
          <button onClick={() => setIsExportModalOpen(true)} className="p-2 text-md-sys-color-on-surface-variant hover:text-md-sys-color-on-surface transition-colors rounded-full">
            <Download className="w-5 h-5" />
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
              {activeFile ? getFileIcon(activeFile.type, 'w-5 h-5') : <FileText className="w-5 h-5 text-md-sys-color-primary" />}
            </div>
            <div>
              <h1 className="text-sm font-semibold leading-tight flex items-center gap-2">
                {activeFile?.name ?? 'No File'}
                {activeFile && hasUnsavedChanges[activeFile.id] && (
                  <Circle className="w-2 h-2 fill-accent-orange text-accent-orange" />
                )}
              </h1>
              <p className="text-xs text-md-sys-color-on-surface-variant flex items-center gap-2">
                <span>{activeFile?.handle ? 'Local File' : 'Cloud Draft'}</span>
                <span className="w-1 h-1 rounded-full bg-md-sys-color-outline-variant"></span>
                <span>{activeFile && hasUnsavedChanges[activeFile.id] ? 'Unsaved' : 'Saved'}</span>
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
      <main className="flex-1 flex flex-col md:flex-row overflow-hidden min-h-0">
        {/* Desktop Sidebar — Open Files Only */}
        <aside className="hidden md:flex w-56 flex-shrink-0 flex-col border-r border-md-sys-color-outline-variant bg-md-sys-color-surface-variant/30">
          <div className="p-4 flex items-center justify-between border-b border-md-sys-color-outline-variant/50">
            <h2 className="text-xs font-bold uppercase tracking-wider text-md-sys-color-on-surface-variant">Open Files</h2>
          </div>
          <div className="flex-1 overflow-y-auto p-2 custom-scrollbar">
            <div className="space-y-0.5">
              {openFiles.length === 0 ? (
                <p className="text-xs text-md-sys-color-on-surface-variant/50 px-3 py-4 text-center">No files open</p>
              ) : (
                openFiles.map(file => (
                  <div
                    key={file.id}
                    className={`group/item w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors cursor-pointer ${activeFileId === file.id ? 'bg-md-sys-color-primary/10 text-md-sys-color-primary font-medium' : 'hover:bg-md-sys-color-surface-variant text-md-sys-color-on-surface-variant'}`}
                    onClick={() => setActiveFileId(file.id)}
                  >
                    {getFileIcon(file.type)}
                    <span className="truncate flex-1">{file.name}</span>
                    {hasUnsavedChanges[file.id] && (
                      <Circle className="w-2 h-2 fill-accent-orange text-accent-orange shrink-0" />
                    )}
                    <button
                      onClick={(e) => handleCloseTab(file.id, e)}
                      className="w-5 h-5 rounded flex items-center justify-center opacity-0 group-hover/item:opacity-100 hover:bg-md-sys-color-surface-variant text-md-sys-color-on-surface-variant transition-all shrink-0"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>
        </aside>

        {/* Editor Area */}
        <div className="flex-1 flex flex-col min-w-0 min-h-0 bg-md-sys-color-background">
          {/* Tabs — Open Files Only */}
          <div className={`h-12 flex items-end px-2 bg-md-sys-color-surface border-b border-md-sys-color-outline-variant md:bg-md-sys-color-surface-variant/50 shrink-0 overflow-x-auto custom-scrollbar ${showPreviewMobile ? 'hidden md:flex' : 'flex'}`}>
            {openFiles.map(file => (
              <div
                key={file.id}
                onClick={() => setActiveFileId(file.id)}
                className={`group/tab h-10 px-4 flex items-center gap-2 border-b-2 transition-colors min-w-max cursor-pointer ${activeFileId === file.id ? 'border-md-sys-color-primary text-md-sys-color-primary bg-md-sys-color-surface-variant/50 md:bg-md-sys-color-background' : 'border-transparent text-md-sys-color-on-surface-variant hover:bg-md-sys-color-surface-variant/30 md:hover:bg-md-sys-color-surface-variant'}`}
              >
                {getFileIcon(file.type)}
                <span className="text-sm font-medium">{file.name}</span>
                {hasUnsavedChanges[file.id] && (
                  <Circle className="w-2 h-2 fill-accent-orange text-accent-orange" />
                )}
                <button
                  onClick={(e) => handleCloseTab(file.id, e)}
                  className="w-5 h-5 rounded flex items-center justify-center opacity-0 group-hover/tab:opacity-100 hover:bg-md-sys-color-surface-variant text-md-sys-color-on-surface-variant transition-all ml-1"
                >
                  <X className="w-3 h-3" />
                </button>
              </div>
            ))}
          </div>

          {/* Split View Container */}
          {activeFile ? (
          <div className="flex-1 flex overflow-hidden relative min-h-0">
            {/* Code Editor Pane */}
            <div className={`flex-1 flex flex-col min-w-0 min-h-0 ${showPreviewMobile ? 'hidden md:flex' : 'flex'}`}>
              <div className="flex-1 overflow-auto custom-scrollbar bg-[#1E1E1E] min-h-0">
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

            {/* Preview Pane (Desktop split or Mobile fullscreen when toggled) */}
            <div className={`flex-1 flex-col min-w-0 min-h-0 bg-white md:border-l md:border-md-sys-color-outline-variant ${showPreviewMobile ? 'flex' : 'hidden md:flex'}`}>
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
                <div className="max-w-3xl mx-auto bg-white p-6 md:p-10 rounded-lg shadow-sm border border-gray-200 min-h-full prose prose-slate">
                  {activeFile.type === 'markdown' ? (
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                      {activeFile.content}
                    </ReactMarkdown>
                  ) : activeFile.type === 'html' ? (
                    <iframe
                      srcDoc={activeFile.content}
                      className="w-full min-h-[60vh] border-0"
                      title="HTML Preview"
                      sandbox="allow-scripts"
                    />
                  ) : activeFile.type === 'json' ? (
                    <TreeView content={activeFile.content} type="json" />
                  ) : activeFile.type === 'xml' ? (
                    <TreeView content={activeFile.content} type="xml" />
                  ) : activeFile.type === 'yaml' ? (
                    <TreeView content={activeFile.content} type="yaml" />
                  ) : activeFile.type === 'csv' ? (
                    <CsvTable content={activeFile.content} />
                  ) : (
                    <pre className="whitespace-pre-wrap break-words text-sm font-mono text-gray-800 not-prose">
                      {activeFile.content}
                    </pre>
                  )}
                </div>
              </div>
            </div>

            {/* Mobile FAB for Preview Toggle */}
            <button 
              className={`md:hidden absolute right-4 w-14 h-14 rounded-2xl bg-md-sys-color-primary text-md-sys-color-on-primary flex items-center justify-center shadow-lg shadow-md-sys-color-primary/40 z-30 active:scale-95 transition-transform ${showPreviewMobile ? 'bottom-6' : 'bottom-[4.5rem]'}`}
              onClick={() => setShowPreviewMobile(!showPreviewMobile)}
            >
              {showPreviewMobile ? <Code className="w-6 h-6" /> : <Eye className="w-6 h-6" />}
            </button>
          </div>
          ) : (
            <div className="flex-1 flex items-center justify-center text-md-sys-color-on-surface-variant/50">
              <div className="text-center space-y-3">
                <FileText className="w-16 h-16 mx-auto opacity-30" />
                <p className="text-lg font-medium">No file open</p>
                <p className="text-sm">Open a file from the dashboard or library to start editing</p>
              </div>
            </div>
          )}
          
          {/* Mobile Bottom Formatting Bar — hidden in preview mode */}
          <div className={`md:hidden h-14 bg-md-sys-color-surface border-t border-md-sys-color-outline-variant items-center justify-between px-2 overflow-x-auto shrink-0 ${showPreviewMobile ? 'hidden' : 'flex'}`}>
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

      {/* Close Confirmation Dialog */}
      <AnimatePresence>
        {closeConfirmFileId && (() => {
          const confirmFile = files.find(f => f.id === closeConfirmFileId);
          return (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
              onClick={() => setCloseConfirmFileId(null)}
            >
              <motion.div
                initial={{ scale: 0.95, opacity: 0 }}
                animate={{ scale: 1, opacity: 1 }}
                exit={{ scale: 0.95, opacity: 0 }}
                onClick={(e: React.MouseEvent) => e.stopPropagation()}
                className="bg-md-sys-color-surface rounded-2xl p-6 w-full max-w-sm shadow-2xl"
              >
                <h3 className="text-lg font-semibold text-md-sys-color-on-surface mb-2">Unsaved Changes</h3>
                <p className="text-sm text-md-sys-color-on-surface-variant mb-6">
                  &ldquo;{confirmFile?.name}&rdquo; has unsaved changes. Do you want to save before closing?
                </p>
                <div className="flex gap-2 justify-end">
                  <button
                    onClick={() => setCloseConfirmFileId(null)}
                    className="px-4 py-2 rounded-full text-sm font-medium text-md-sys-color-on-surface-variant hover:bg-md-sys-color-surface-variant transition-colors"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleCloseConfirmDiscard}
                    className="px-4 py-2 rounded-full text-sm font-medium text-red-600 hover:bg-red-50 transition-colors"
                  >
                    Discard
                  </button>
                  <button
                    onClick={handleCloseConfirmSave}
                    className="px-4 py-2 rounded-full text-sm font-medium bg-md-sys-color-primary text-md-sys-color-on-primary hover:bg-md-sys-color-primary/90 transition-colors"
                  >
                    Save & Close
                  </button>
                </div>
              </motion.div>
            </motion.div>
          );
        })()}
      </AnimatePresence>

      <ExportModal 
        isOpen={isExportModalOpen} 
        onClose={() => setIsExportModalOpen(false)} 
        contentId="preview-container"
        fileName={activeFile?.name ?? 'untitled'}
      />
      </motion.div>
    </div>
  );
}

export default function Editor() {
  return (
    <Suspense fallback={<div className="h-screen flex items-center justify-center bg-md-sys-color-background text-md-sys-color-on-background">Loading...</div>}>
      <EditorContent />
    </Suspense>
  );
}
