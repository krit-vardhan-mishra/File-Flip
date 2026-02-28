import { get, set, del, keys } from 'idb-keyval';

export interface FileData {
  id: string;
  name: string;
  content: string;
  type: 'markdown' | 'json' | 'html' | 'text' | 'yaml' | 'xml' | 'log' | 'csv';
  lastModified: number;
  isStarred: boolean;
  handle?: any; // FileSystemFileHandle
}

// Keys for persisting editor state
const OPEN_FILE_IDS_KEY = 'editor-open-file-ids';
const ACTIVE_FILE_ID_KEY = 'editor-active-file-id';

export const saveFileToDB = async (file: FileData) => {
  await set(`file-${file.id}`, file);
};

export const getFileFromDB = async (id: string): Promise<FileData | undefined> => {
  return await get(`file-${id}`);
};

export const deleteFileFromDB = async (id: string) => {
  await del(`file-${id}`);
};

export const getAllFilesFromDB = async (): Promise<FileData[]> => {
  const allKeys = await keys();
  const fileKeys = allKeys.filter(k => typeof k === 'string' && k.startsWith('file-'));
  const files = await Promise.all(fileKeys.map(k => get(k as string)));
  return files.filter(Boolean) as FileData[];
};

export const generateId = () => Math.random().toString(36).substring(2, 15);

// Persist open file IDs to survive refresh
export const saveOpenFileIds = async (ids: string[]) => {
  await set(OPEN_FILE_IDS_KEY, ids);
};

export const getOpenFileIds = async (): Promise<string[]> => {
  return (await get(OPEN_FILE_IDS_KEY)) || [];
};

export const saveActiveFileId = async (id: string | null) => {
  await set(ACTIVE_FILE_ID_KEY, id);
};

export const getActiveFileId = async (): Promise<string | null> => {
  return (await get(ACTIVE_FILE_ID_KEY)) || null;
};
