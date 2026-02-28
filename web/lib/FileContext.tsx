'use client';

import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { FileData, saveFileToDB, getFileFromDB, deleteFileFromDB, getAllFilesFromDB, generateId } from './store';

interface FileContextType {
  files: FileData[];
  activeFileId: string | null;
  setActiveFileId: (id: string | null) => void;
  createFile: (name: string, content: string, type: FileData['type']) => Promise<FileData>;
  updateFile: (id: string, updates: Partial<FileData>) => Promise<void>;
  deleteFile: (id: string) => Promise<void>;
  openLocalFile: () => Promise<void>;
  saveLocalFile: (id: string) => Promise<void>;
  toggleStar: (id: string) => Promise<void>;
}

const FileContext = createContext<FileContextType | undefined>(undefined);

export const FileProvider = ({ children }: { children: ReactNode }) => {
  const [files, setFiles] = useState<FileData[]>([]);
  const [activeFileId, setActiveFileId] = useState<string | null>(null);

  useEffect(() => {
    const loadFiles = async () => {
      const loadedFiles = await getAllFilesFromDB();
      setFiles(loadedFiles.sort((a, b) => b.lastModified - a.lastModified));
    };
    loadFiles();
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
    return newFile;
  };

  const updateFile = async (id: string, updates: Partial<FileData>) => {
    const file = files.find(f => f.id === id);
    if (!file) return;
    
    const updatedFile = { ...file, ...updates, lastModified: Date.now() };
    await saveFileToDB(updatedFile);
    setFiles(prev => prev.map(f => f.id === id ? updatedFile : f));
  };

  const deleteFile = async (id: string) => {
    await deleteFileFromDB(id);
    setFiles(prev => prev.filter(f => f.id !== id));
    if (activeFileId === id) {
      setActiveFileId(null);
    }
  };

  const toggleStar = async (id: string) => {
    const file = files.find(f => f.id === id);
    if (file) {
      await updateFile(id, { isStarred: !file.isStarred });
    }
  };

  const openLocalFile = async () => {
    if (!('showOpenFilePicker' in window)) {
      alert('File System Access API is not supported in this browser.');
      return;
    }
    try {
      const [fileHandle] = await (window as any).showOpenFilePicker({
        types: [
          {
            description: 'Text Files',
            accept: {
              'text/plain': ['.txt', '.md', '.json', '.html', '.csv'],
            },
          },
        ],
      });
      const file = await fileHandle.getFile();
      const content = await file.text();
      
      let type: FileData['type'] = 'text';
      if (file.name.endsWith('.md')) type = 'markdown';
      else if (file.name.endsWith('.json')) type = 'json';
      else if (file.name.endsWith('.html')) type = 'html';

      const newFile: FileData = {
        id: generateId(),
        name: file.name,
        content,
        type,
        lastModified: file.lastModified,
        isStarred: false,
        handle: fileHandle,
      };
      
      await saveFileToDB(newFile);
      setFiles(prev => [newFile, ...prev]);
      setActiveFileId(newFile.id);
    } catch (err) {
      console.error(err);
    }
  };

  const saveLocalFile = async (id: string) => {
    const file = files.find(f => f.id === id);
    if (!file) return;

    if (!('showSaveFilePicker' in window)) {
      alert('File System Access API is not supported in this browser.');
      return;
    }

    try {
      let handle = file.handle;
      if (!handle) {
        handle = await (window as any).showSaveFilePicker({
          suggestedName: file.name,
          types: [{
            description: 'Text Files',
            accept: { 'text/plain': ['.txt', '.md', '.json', '.html'] },
          }],
        });
      }
      
      const writable = await handle.createWritable();
      await writable.write(file.content);
      await writable.close();
      
      await updateFile(id, { handle });
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <FileContext.Provider value={{
      files,
      activeFileId,
      setActiveFileId,
      createFile,
      updateFile,
      deleteFile,
      openLocalFile,
      saveLocalFile,
      toggleStar
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
