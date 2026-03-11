'use client';

import React, { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react';
import {
  FileData,
  saveFileToDB,
  deleteFileFromDB,
  getAllFilesFromDB,
  generateId,
  saveOpenFileIds,
  getOpenFileIds,
  saveActiveFileId,
  getActiveFileId,
} from './store';

interface FileContextType {
  files: FileData[];
  isLoading: boolean;
  activeFileId: string | null;
  setActiveFileId: (id: string | null) => void;
  openFileIds: string[];
  hasUnsavedChanges: Record<string, boolean>;
  createFile: (name: string, content: string, type: FileData['type']) => Promise<FileData>;
  updateFile: (id: string, updates: Partial<FileData>) => Promise<void>;
  deleteFile: (id: string) => Promise<void>;
  renameFile: (id: string, newName: string) => Promise<void>;
  openLocalFile: (file?: File) => Promise<FileData | null>;
  openUrlFile: (url: string, name?: string) => Promise<FileData | null>;
  saveLocalFile: (id: string) => Promise<void>;
  toggleStar: (id: string) => Promise<void>;
  openFileInEditor: (id: string) => void;
  closeFileInEditor: (id: string, force?: boolean) => boolean;
}

const FileContext = createContext<FileContextType | undefined>(undefined);

export const FileProvider = ({ children }: { children: ReactNode }) => {
  const [files, setFiles] = useState<FileData[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [activeFileId, setActiveFileIdState] = useState<string | null>(null);
  const [openFileIds, setOpenFileIdsState] = useState<string[]>([]);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState<Record<string, boolean>>({});
  const [savedContent, setSavedContent] = useState<Record<string, string>>({});

  useEffect(() => {
    const loadFiles = async () => {
      try {
        const loadedFiles = await getAllFilesFromDB();
        const sorted = loadedFiles.sort((a, b) => b.lastModified - a.lastModified);
        setFiles(sorted);

        const storedOpenIds = await getOpenFileIds();
        const validOpenIds = storedOpenIds.filter(id => sorted.some(f => f.id === id));
        setOpenFileIdsState(validOpenIds);

        const storedActiveId = await getActiveFileId();
        if (storedActiveId && sorted.some(f => f.id === storedActiveId)) {
          setActiveFileIdState(storedActiveId);
        }

        const contentMap: Record<string, string> = {};
        sorted.forEach(f => { contentMap[f.id] = f.content; });
        setSavedContent(contentMap);
      } finally {
        setIsLoading(false);
      }
    };
    loadFiles();
  }, []);

  const setOpenFileIds = useCallback((ids: string[] | ((prev: string[]) => string[])) => {
    setOpenFileIdsState(prev => {
      const next = typeof ids === 'function' ? ids(prev) : ids;
      saveOpenFileIds(next);
      return next;
    });
  }, []);

  const setActiveFileId = useCallback((id: string | null) => {
    setActiveFileIdState(id);
    saveActiveFileId(id);
  }, []);

  const createFile = async (name: string, content: string, type: FileData['type']) => {
    const newFile: FileData = {
      id: generateId(),
      name,
      content,
      type,
      lastModified: Date.now(),
      isStarred: false,
    };
    await saveFileToDB(newFile);
    setFiles(prev => [newFile, ...prev]);
    setActiveFileId(newFile.id);
    setSavedContent(prev => ({ ...prev, [newFile.id]: content }));
    setOpenFileIds(prev => prev.includes(newFile.id) ? prev : [...prev, newFile.id]);
    return newFile;
  };

  const updateFile = async (id: string, updates: Partial<FileData>) => {
    const file = files.find(f => f.id === id);
    if (!file) return;

    const updatedFile = { ...file, ...updates, lastModified: Date.now() };
    await saveFileToDB(updatedFile);
    setFiles(prev => prev.map(f => f.id === id ? updatedFile : f));

    if (updates.content !== undefined) {
      const original = savedContent[id];
      setHasUnsavedChanges(prev => ({ ...prev, [id]: updates.content !== original }));
    }
  };

  const deleteFile = async (id: string) => {
    await deleteFileFromDB(id);
    setFiles(prev => prev.filter(f => f.id !== id));
    setOpenFileIds(prev => prev.filter(fid => fid !== id));
    setHasUnsavedChanges(prev => { const n = { ...prev }; delete n[id]; return n; });
    setSavedContent(prev => { const n = { ...prev }; delete n[id]; return n; });
    if (activeFileId === id) {
      setActiveFileId(null);
    }
  };

  const renameFile = async (id: string, newName: string) => {
    const file = files.find(f => f.id === id);
    if (!file) return;

    const ext = newName.split('.').pop()?.toLowerCase() || '';
    let type: FileData['type'] = file.type;
    if (ext === 'md' || ext === 'markdown') type = 'markdown';
    else if (ext === 'json') type = 'json';
    else if (ext === 'html' || ext === 'htm') type = 'html';
    else if (ext === 'yaml' || ext === 'yml') type = 'yaml';
    else if (ext === 'xml') type = 'xml';
    else if (ext === 'log') type = 'log';
    else if (ext === 'csv') type = 'csv';
    else if (ext === 'txt') type = 'text';

    const updatedFile = { ...file, name: newName, type, lastModified: Date.now() };
    await saveFileToDB(updatedFile);
    setFiles(prev => prev.map(f => f.id === id ? updatedFile : f));
  };

  const toggleStar = async (id: string) => {
    const file = files.find(f => f.id === id);
    if (file) {
      await updateFile(id, { isStarred: !file.isStarred });
    }
  };

  const processFile = async (file: File, handle?: FileSystemFileHandle): Promise<FileData> => {
    const content = await file.text();

    let type: FileData['type'] = 'text';
    const ext = file.name.split('.').pop()?.toLowerCase() || '';
    if (ext === 'md' || ext === 'markdown') type = 'markdown';
    else if (ext === 'json') type = 'json';
    else if (ext === 'html' || ext === 'htm') type = 'html';
    else if (ext === 'yaml' || ext === 'yml') type = 'yaml';
    else if (ext === 'xml') type = 'xml';
    else if (ext === 'log') type = 'log';
    else if (ext === 'csv') type = 'csv';

    const newFile: FileData = {
      id: generateId(),
      name: file.name,
      content,
      type,
      lastModified: file.lastModified,
      isStarred: false,
      handle: handle,
    };

    await saveFileToDB(newFile);
    setFiles(prev => [newFile, ...prev].sort((a, b) => b.lastModified - a.lastModified));
    setActiveFileId(newFile.id);
    setSavedContent(prev => ({ ...prev, [newFile.id]: content }));
    setOpenFileIds(prev => prev.includes(newFile.id) ? prev : [...prev, newFile.id]);

    return newFile;
  }

  // Open local file - with fallback for unsupported browsers
  const openLocalFile = async (fileFromDrop?: File): Promise<FileData | null> => {
    if (fileFromDrop) {
      return processFile(fileFromDrop);
    }
    
    // Try File System Access API first
    if ('showOpenFilePicker' in window) {
      try {
        const [fileHandle] = await (window as any).showOpenFilePicker({
          types: [
            {
              description: 'Supported Files',
              accept: {
                'text/plain': ['.txt', '.md', '.log'],
                'application/json': ['.json'],
                'text/html': ['.html', '.htm'],
                'text/yaml': ['.yaml', '.yml'],
                'application/xml': ['.xml'],
                'text/csv': ['.csv'],
              },
            },
          ],
        });
        const file = await fileHandle.getFile();
        return processFile(file, fileHandle);
      } catch (err) {
        // User cancelled or error - do nothing
        console.log('File picker cancelled or error:', err);
        return null;
      }
    } else {
      // Fallback: use hidden file input
      return new Promise((resolve) => {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '.txt,.md,.log,.json,.html,.yaml,.yml,.xml,.csv,.htm';
        input.onchange = async (event) => {
          const target = event.target as HTMLInputElement;
          const file = target.files?.[0];
          if (file) {
            const newFile = await processFile(file);
            resolve(newFile);
          } else {
            resolve(null);
          }
          // Reset input so same file can be selected again
          target.value = '';
        };
        input.click();
      });
    }
  };

  // Open a file from URL - temporary, not saved to IndexedDB
  const openUrlFile = async (url: string, name?: string): Promise<FileData | null> => {
    try {
      const response = await fetch(url);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const content = await response.text();

      // Determine file type from URL or content-type
      let type: FileData['type'] = 'text';
      const urlObj = new URL(url);
      const pathname = urlObj.pathname;
      const ext = pathname.split('.').pop()?.toLowerCase() || '';
      
      if (ext === 'md' || ext === 'markdown') type = 'markdown';
      else if (ext === 'json') type = 'json';
      else if (ext === 'html' || ext === 'htm') type = 'html';
      else if (ext === 'yaml' || ext === 'yml') type = 'yaml';
      else if (ext === 'xml') type = 'xml';
      else if (ext === 'log') type = 'log';
      else if (ext === 'csv') type = 'csv';
      else if (ext === 'txt') type = 'text';

      // Generate name from URL if not provided
      const fileName = name || pathname.split('/').pop() || 'Untitled';

      const newFile: FileData = {
        id: generateId(),
        name: fileName,
        content,
        type,
        lastModified: Date.now(),
        isStarred: false,
        // No handle - this is a temporary file
      };

      // Add to files but DON'T save to IndexedDB (temporary)
      setFiles(prev => [newFile, ...prev]);
      setActiveFileId(newFile.id);
      setSavedContent(prev => ({ ...prev, [newFile.id]: content }));
      setOpenFileIds(prev => prev.includes(newFile.id) ? prev : [...prev, newFile.id]);
      
      return newFile;
    } catch (err) {
      console.error('Failed to open file from URL:', err);
      alert('Failed to load file from URL. Please check the URL and try again.');
      return null;
    }
  };

  const saveLocalFile = async (id: string) => {
    const file = files.find(f => f.id === id);
    if (!file) return;

    if (!('showSaveFilePicker' in window)) {
      // Fallback: download file using blob URL
      const blob = new Blob([file.content], { type: 'text/plain' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = file.name;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      setSavedContent(prev => ({ ...prev, [id]: file.content }));
      setHasUnsavedChanges(prev => ({ ...prev, [id]: false }));
      return;
    }

    try {
      let handle = file.handle;
      if (!handle) {
        handle = await (window as any).showSaveFilePicker({
          suggestedName: file.name,
          types: [{
            description: 'Supported Files',
            accept: { 'text/plain': ['.txt', '.md', '.json', '.html', '.yaml', '.yml', '.xml', '.log', '.csv'] },
          }],
        });
      }

      const writable = await handle.createWritable();
      await writable.write(file.content);
      await writable.close();

      await updateFile(id, { handle });
      setSavedContent(prev => ({ ...prev, [id]: file.content }));
      setHasUnsavedChanges(prev => ({ ...prev, [id]: false }));
    } catch (err) {
      console.error(err);
    }
  };

  const openFileInEditor = useCallback((id: string) => {
    setOpenFileIds(prev => prev.includes(id) ? prev : [...prev, id]);
    setActiveFileId(id);
  }, [setOpenFileIds, setActiveFileId]);

  const closeFileInEditor = useCallback((id: string, force = false): boolean => {
    if (!force && hasUnsavedChanges[id]) {
      return false;
    }
    setOpenFileIds(prev => {
      const next = prev.filter(fid => fid !== id);
      if (activeFileId === id) {
        const closedIdx = prev.indexOf(id);
        const newActive = next[Math.min(closedIdx, next.length - 1)] || null;
        setActiveFileId(newActive);
      }
      return next;
    });
    setHasUnsavedChanges(prev => { const n = { ...prev }; delete n[id]; return n; });
    return true;
  }, [hasUnsavedChanges, activeFileId, setOpenFileIds, setActiveFileId]);

  return (
    <FileContext.Provider value={{
      files,
      isLoading,
      activeFileId,
      setActiveFileId,
      openFileIds,
      hasUnsavedChanges,
      createFile,
      updateFile,
      deleteFile,
      renameFile,
      openLocalFile,
      openUrlFile,
      saveLocalFile,
      toggleStar,
      openFileInEditor,
      closeFileInEditor,
    }}>
      {children}
    </FileContext.Provider>
  );
};

export const useFiles = () => {
  const context = useContext(FileContext);
  if (context === undefined) {
    throw new Error('useFiles must be used within a FileProvider');
  }
  return context;
};
