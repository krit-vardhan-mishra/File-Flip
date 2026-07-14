package com.just_for_fun.fileflip.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.fileflip.data.local.dao.WorkspaceDao
import com.just_for_fun.fileflip.data.local.entity.WorkspaceEntity
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.domain.repository.MarkdownRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: MarkdownRepository,
    private val workspaceDao: WorkspaceDao
) : ViewModel() {

    private val _files = MutableStateFlow<List<MarkdownFile>>(emptyList())
    val files: StateFlow<List<MarkdownFile>> = _files.asStateFlow()

    private val _importedFiles = MutableStateFlow<List<MarkdownFile>>(emptyList())
    val importedFiles: StateFlow<List<MarkdownFile>> = _importedFiles.asStateFlow()

    init {
        loadFiles()
    }

    fun loadFiles() {
        viewModelScope.launch {
            _files.value = repository.getFiles().sortedByDescending { it.lastModified }
        }
    }

    fun createNewFile(name: String, content: String = "# New File\nStart writing...") {
        viewModelScope.launch {
             repository.createNewFile(name, content)
             loadFiles()
        }
    }

    fun renameFile(oldPath: String, newName: String) {
        viewModelScope.launch {
            repository.renameFile(oldPath, newName)
            loadFiles()
        }
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            repository.deleteFile(path)
            loadFiles()
        }
    }

    fun addImportedFile(file: MarkdownFile) {
        val currentImported = _importedFiles.value.toMutableList()
        if (currentImported.none { it.path == file.path }) {
            currentImported.add(0, file) // Add to top
            _importedFiles.value = currentImported
        }
    }

    fun removeImportedFile(file: MarkdownFile) {
        val currentImported = _importedFiles.value.toMutableList()
        currentImported.removeAll { it.path == file.path }
        _importedFiles.value = currentImported
    }

    fun importFolder(uri: String, name: String) {
        viewModelScope.launch {
            val existing = workspaceDao.getWorkspaceByPath(uri)
            if (existing == null) {
                val workspace = WorkspaceEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    rootPath = uri
                )
                workspaceDao.insertWorkspace(workspace)
            }
        }
    }
}