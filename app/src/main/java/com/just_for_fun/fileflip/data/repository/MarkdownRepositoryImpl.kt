package com.just_for_fun.fileflip.data.repository

import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.domain.repository.MarkdownRepository
import com.just_for_fun.fileflip.ui.screens.SettingsState
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

class MarkdownRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MarkdownRepository {

    // Use app-specific external storage for now to avoid permission issues in initial testing
    // or use Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) if we have permissions
    private val filesDir: File by lazy { 
        File(context.getExternalFilesDir(null), "Files").apply {
            if (!exists()) mkdirs()
        }
    }

    override suspend fun getFiles(): List<MarkdownFile> = withContext(Dispatchers.IO) {
        if (!filesDir.exists()) return@withContext emptyList()
        
        val supportedExtensions = listOf("md", "json", "yaml", "xml", "txt", "html", "log", "csv")
        filesDir.listFiles { _, name -> 
            val ext = name.substringAfterLast(".", "").lowercase()
            ext in supportedExtensions
        }
            ?.map { file ->
                MarkdownFile(
                    name = file.name,
                    path = file.absolutePath,
                    content = file.readText(),
                    lastModified = file.lastModified()
                )
            } ?: emptyList()
    }

    override suspend fun getFile(path: String): MarkdownFile? = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists()) {
            MarkdownFile(
                name = file.name,
                path = file.absolutePath,
                content = file.readText(),
                lastModified = file.lastModified()
            )
        } else {
            // Check if it's a demo file
            val demoFile = com.just_for_fun.fileflip.data.DemoFilesData.demoFiles.find { it.path == path }
            if (demoFile != null) {
                MarkdownFile(
                    name = demoFile.name,
                    path = demoFile.path,
                    content = demoFile.content,
                    lastModified = System.currentTimeMillis()
                )
            } else {
                null
            }
        }
    }

    override suspend fun saveFile(file: MarkdownFile) = withContext(Dispatchers.IO) {
        val targetFile = File(file.path)

        // If the file doesn't exist and it's a demo file, create it in the proper location
        if (!targetFile.exists()) {
            val demoFile = com.just_for_fun.fileflip.data.DemoFilesData.demoFiles.find { it.path == file.path }
            if (demoFile != null) {
                // Create the file in the files directory with the demo content
                val actualFile = File(filesDir, file.name)
                actualFile.writeText(file.content)
                return@withContext
            }
        }

        // Normal file save
        targetFile.writeText(file.content)
    }

    override suspend fun createNewFile(name: String, content: String): MarkdownFile = withContext(Dispatchers.IO) {
        // Ensure the name has a valid extension, default to .md if none provided
        val finalName = if (name.contains(".")) name else "$name.md"
        
        // Use user's default save directory if set, otherwise use app's default directory
        val targetDir = SettingsState.defaultSaveDirectory?.let { dirPath ->
            File(dirPath).takeIf { it.exists() && it.isDirectory }
        } ?: filesDir
        
        val file = File(targetDir, finalName)
        file.writeText(content)
        MarkdownFile(
            name = finalName,
            path = file.absolutePath,
            content = content,
            lastModified = file.lastModified()
        )
    }

    override suspend fun renameFile(oldPath: String, newName: String) = withContext(Dispatchers.IO) {
        val oldFile = File(oldPath)
        if (oldFile.exists()) {
            val newFile = File(oldFile.parent, newName)
            oldFile.renameTo(newFile)
        }
    }

    override suspend fun deleteFile(path: String) = withContext(Dispatchers.IO) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
        Unit
    }
}
