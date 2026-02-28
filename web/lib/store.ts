import { get, set, del, keys } from 'idb-keyval';

export interface FileData {
  id: string;
  name: string;
  content: string;
  type: 'markdown' | 'json' | 'html' | 'text';
  lastModified: number;
  isStarred: boolean;
  handle?: any; // FileSystemFileHandle
}

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
