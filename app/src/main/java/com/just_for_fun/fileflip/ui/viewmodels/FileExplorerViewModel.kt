package com.just_for_fun.fileflip.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.domain.repository.MarkdownRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileExplorerViewModel @Inject constructor(
    private val repository: MarkdownRepository
) : ViewModel() {

    private val _allFiles = MutableStateFlow<List<MarkdownFile>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow("Date Newest")
    val sortOption: StateFlow<String> = _sortOption.asStateFlow()

    private val _selectedExtensions = MutableStateFlow<Set<String>>(emptySet())
    val selectedExtensions: StateFlow<Set<String>> = _selectedExtensions.asStateFlow()

    val filteredFiles: StateFlow<List<MarkdownFile>> = combine(
        _allFiles, _searchQuery, _sortOption, _selectedExtensions
    ) { files, query, sort, extensions ->
        var result = files

        // Filter by search query
        if (query.isNotEmpty()) {
            result = result.filter { it.name.contains(query, ignoreCase = true) }
        }

        // Filter by extensions
        if (extensions.isNotEmpty()) {
            result = result.filter { file ->
                val ext = file.name.substringAfterLast(".", "").lowercase()
                extensions.contains(ext)
            }
        }

        // Apply sorting
        when (sort) {
            "Name A-Z" -> result.sortedBy { it.name.lowercase() }
            "Name Z-A" -> result.sortedByDescending { it.name.lowercase() }
            "Date Newest" -> result.sortedByDescending { it.lastModified }
            "Date Oldest" -> result.sortedBy { it.lastModified }
            else -> result
        }
    }.let { flow ->
        val state = MutableStateFlow<List<MarkdownFile>>(emptyList())
        viewModelScope.launch {
            flow.collect { state.value = it }
        }
        state.asStateFlow()
    }

    init {
        loadFiles()
    }

    fun loadFiles() {
        viewModelScope.launch {
            _allFiles.value = repository.getFiles()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortOption(option: String) {
        _sortOption.value = option
    }

    fun updateSelectedExtensions(extensions: Set<String>) {
        _selectedExtensions.value = extensions
    }

    fun deleteFile(path: String) {
        viewModelScope.launch {
            repository.deleteFile(path)
            loadFiles()
        }
    }
    
    fun renameFile(path: String, newName: String) {
        viewModelScope.launch {
            repository.renameFile(path, newName)
            loadFiles()
        }
    }
}
