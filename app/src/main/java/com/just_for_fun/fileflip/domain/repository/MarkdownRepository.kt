package com.just_for_fun.fileflip.domain.repository

import com.just_for_fun.fileflip.domain.model.MarkdownFile
import java.io.File
import kotlinx.coroutines.flow.Flow

interface MarkdownRepository {
    suspend fun getFiles(): List<MarkdownFile>
    suspend fun getFile(path: String): MarkdownFile?
    suspend fun saveFile(file: MarkdownFile)
    suspend fun createNewFile(name: String, content: String): MarkdownFile
    suspend fun renameFile(oldPath: String, newName: String)
    suspend fun deleteFile(path: String)
}
