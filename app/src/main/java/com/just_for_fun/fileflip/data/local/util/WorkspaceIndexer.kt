package com.just_for_fun.fileflip.data.local.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.just_for_fun.fileflip.data.local.dao.ChunkDao
import com.just_for_fun.fileflip.data.local.dao.FileDao
import com.just_for_fun.fileflip.data.local.entity.ChunkEntity
import com.just_for_fun.fileflip.data.local.entity.FileEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkspaceIndexer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileDao: FileDao,
    private val chunkDao: ChunkDao,
    private val embeddingGenerator: OnnxEmbeddingGenerator
) {
    private val TAG = "WorkspaceIndexer"

    suspend fun indexWorkspace(workspaceId: String, rootPathUri: String) = withContext(Dispatchers.IO) {
        if (!embeddingGenerator.isModelAvailable()) {
            Log.d(TAG, "Embedding model not downloaded yet. Skipping workspace indexing.")
            return@withContext
        }

        try {
            val rootUri = Uri.parse(rootPathUri)
            val rootFile = DocumentFile.fromTreeUri(context, rootUri)
            if (rootFile == null || !rootFile.exists()) {
                Log.e(TAG, "Root workspace directory does not exist: $rootPathUri")
                return@withContext
            }

            Log.d(TAG, "Starting indexing for workspace: $workspaceId ($rootPathUri)")

            val scannedFiles = mutableListOf<DocumentFile>()
            scanFilesRecursively(rootFile, scannedFiles)

            // Keep track of visited file path URIs to delete files from DB that were deleted on disk
            val diskFilePaths = scannedFiles.map { it.uri.toString() }.toSet()

            // 1. Clean up orphaned database files (files tracked in DB but no longer present on disk)
            val dbFiles = fileDao.getFilesForWorkspaceSync(workspaceId)
            for (dbFile in dbFiles) {
                if (!diskFilePaths.contains(dbFile.path)) {
                    Log.d(TAG, "Cleaning up deleted file from DB: ${dbFile.path}")
                    chunkDao.deleteChunksForFile(dbFile.id)
                    fileDao.deleteFile(dbFile)
                }
            }

            // 2. Index each disk file
            for (docFile in scannedFiles) {
                val filePath = docFile.uri.toString()
                val fileName = docFile.name ?: "unknown"
                val lastModifiedDisk = docFile.lastModified()
                val fileSizeDisk = docFile.length()

                val existingDbFile = fileDao.getFileByPath(filePath)
                
                // Read text to compute hash
                val content = readFileContent(docFile)
                val sha256 = computeSha256(content)

                if (existingDbFile != null) {
                    if (existingDbFile.sha256Hash == sha256 && existingDbFile.lastModified == lastModifiedDisk) {
                        // File unchanged, skip
                        continue
                    }
                    Log.d(TAG, "File updated on disk. Re-indexing: $fileName")
                    // Delete previous chunks
                    chunkDao.deleteChunksForFile(existingDbFile.id)
                    
                    // Generate new chunks and embeddings
                    val chunks = RangedTextChunker.chunkText(content)
                    insertChunks(existingDbFile.id, chunks)

                    // Update file record
                    val updatedFile = existingDbFile.copy(
                        lastModified = lastModifiedDisk,
                        sha256Hash = sha256
                    )
                    fileDao.updateFile(updatedFile)
                } else {
                    Log.d(TAG, "New file detected. Indexing: $fileName")
                    // Create new file record
                    val fileId = UUID.randomUUID().toString()
                    val newFile = FileEntity(
                        id = fileId,
                        workspaceId = workspaceId,
                        path = filePath,
                        lastModified = lastModifiedDisk,
                        sha256Hash = sha256
                    )
                    fileDao.insertFile(newFile)

                    val chunks = RangedTextChunker.chunkText(content)
                    insertChunks(fileId, chunks)
                }
            }

            Log.d(TAG, "Workspace indexing complete.")
        } catch (e: Exception) {
            Log.e(TAG, "Error indexing workspace", e)
        }
    }

    private suspend fun insertChunks(fileId: String, chunks: List<TextChunk>) {
        for (chunk in chunks) {
            val embedding = embeddingGenerator.getEmbedding(chunk.text)
            if (embedding != null) {
                val embeddingBytes = SimilaritySearchHelper.toByteArray(embedding)
                val chunkEntity = ChunkEntity(
                    id = UUID.randomUUID().toString(),
                    fileId = fileId,
                    chunkIndex = chunk.index,
                    startByte = chunk.startByte,
                    endByte = chunk.endByte,
                    vectorEmbedding = embeddingBytes
                )
                chunkDao.insertChunk(chunkEntity)
            }
        }
    }

    private fun scanFilesRecursively(file: DocumentFile, result: MutableList<DocumentFile>) {
        if (file.isDirectory) {
            file.listFiles().forEach { child ->
                scanFilesRecursively(child, result)
            }
        } else {
            val name = file.name?.lowercase() ?: ""
            val ext = name.substringAfterLast(".", "")
            val supportedExtensions = listOf("md", "txt", "json", "csv", "kt", "java", "py", "js", "ts", "html", "css")
            if (ext in supportedExtensions) {
                result.add(file)
            }
        }
    }

    private fun readFileContent(file: DocumentFile): String {
        return try {
            context.contentResolver.openInputStream(file.uri)?.use { input ->
                input.bufferedReader().use { it.readText() }
            } ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read content from file: ${file.uri}", e)
            ""
        }
    }

    private fun computeSha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(text.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
}
