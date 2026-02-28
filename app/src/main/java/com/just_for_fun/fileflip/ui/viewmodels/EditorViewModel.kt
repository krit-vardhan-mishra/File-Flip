package com.just_for_fun.fileflip.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.domain.repository.MarkdownRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.just_for_fun.fileflip.ui.screens.SettingsState
import java.io.File

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val repository: MarkdownRepository
) : ViewModel() {

    private val _currentFile = MutableStateFlow<MarkdownFile?>(null)
    val currentFile: StateFlow<MarkdownFile?> = _currentFile.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()
    
    // Multiple files support
    private val _openFiles = MutableStateFlow<List<MarkdownFile>>(emptyList())
    val openFiles: StateFlow<List<MarkdownFile>> = _openFiles.asStateFlow()
    
    private val _currentFileIndex = MutableStateFlow(0)
    val currentFileIndex: StateFlow<Int> = _currentFileIndex.asStateFlow()
    
    private val _hasUnsavedChanges = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val hasUnsavedChanges: StateFlow<Map<String, Boolean>> = _hasUnsavedChanges.asStateFlow()
    
    // File not found handling
    private val _fileNotFoundError = MutableStateFlow<Pair<Int, String>?>(null)
    val fileNotFoundError: StateFlow<Pair<Int, String>?> = _fileNotFoundError.asStateFlow()
    
    // Undo/Redo functionality - per file
    private val undoStacks = mutableMapOf<String, MutableList<String>>()
    private val redoStacks = mutableMapOf<String, MutableList<String>>()
    private var isUndoRedoOperation = false
    
    // Auto-save functionality
    private val autoSaveDelayMs = 3000L // 3 seconds
    private var autoSaveJob: kotlinx.coroutines.Job? = null
    
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    init {
        // Observe content changes for auto-save
        viewModelScope.launch {
            _content.collect { newContent ->
                if (!isUndoRedoOperation) {
                    // Cancel previous auto-save job
                    autoSaveJob?.cancel()
                    // Schedule new auto-save
                    autoSaveJob = viewModelScope.launch {
                        kotlinx.coroutines.delay(autoSaveDelayMs)
                        saveFile()
                    }
                }
            }
        }
    }

    fun loadFile(path: String) {
        viewModelScope.launch {
            Log.d("FileFlip", "EditorViewModel: Attempting to load file from path: $path")
            val file = repository.getFile(path)
            Log.d(
                "FileFlip",
                "EditorViewModel: File loaded - ${file?.name ?: "null"}, content length: ${file?.content?.length ?: 0}"
            )
            
            if (file != null) {
                // Update last modified to mark as recent
                val updatedFile = file.copy(lastModified = System.currentTimeMillis())
                repository.saveFile(updatedFile)

                // Check if file is already open
                val existingIndex = _openFiles.value.indexOfFirst { it.path == updatedFile.path }
                if (existingIndex >= 0) {
                    // Update the file in open files list and switch
                    _openFiles.value = _openFiles.value.toMutableList().apply {
                        set(existingIndex, updatedFile)
                    }
                    switchToFile(existingIndex)
                } else {
                    // Add new file to open files
                    _openFiles.value = _openFiles.value + updatedFile
                    _currentFileIndex.value = _openFiles.value.size - 1
                    _currentFile.value = updatedFile
                    _content.value = updatedFile.content

                    // Initialize undo/redo stacks for this file
                    undoStacks[updatedFile.path] = mutableListOf()
                    redoStacks[updatedFile.path] = mutableListOf()
                    updateUndoRedoState()
                }
            }
 else {
                // File not found - check if it's already open and needs to be removed
                val existingIndex = _openFiles.value.indexOfFirst { it.path == path }
                if (existingIndex >= 0) {
                    _fileNotFoundError.value = existingIndex to (File(path).name)
                }
            }
        }
    }
    
    fun switchToFile(index: Int) {
        if (index in _openFiles.value.indices) {
            // Save current file content before switching
            _currentFile.value?.let { currentFile ->
                val hasChanges = _content.value != currentFile.content
                _hasUnsavedChanges.value = _hasUnsavedChanges.value + (currentFile.path to hasChanges)
            }
            
            val newFile = _openFiles.value[index]
            
            // Check if file still exists on disk
            viewModelScope.launch {
                val fileExists = File(newFile.path).exists()
                if (!fileExists) {
                    // File was deleted
                    _fileNotFoundError.value = index to newFile.name
                } else {
                    // Switch to new file
                    _currentFileIndex.value = index
                    _currentFile.value = newFile
                    _content.value = newFile.content
                    updateUndoRedoState()
                }
            }
        }
    }
    
    fun clearFileNotFoundError() {
        _fileNotFoundError.value = null
    }
    
    fun closeFile(index: Int, forceClose: Boolean = false): Boolean {
        if (index !in _openFiles.value.indices) return true
        
        val fileToClose = _openFiles.value[index]
        val hasChanges = _hasUnsavedChanges.value[fileToClose.path] ?: false
        
        // If file has unsaved changes and not forcing close, return false (needs confirmation)
        if (hasChanges && !forceClose) {
            return false
        }
        
        // Remove file from open files
        _openFiles.value = _openFiles.value.filterIndexed { i, _ -> i != index }
        
        // Clean up undo/redo stacks
        undoStacks.remove(fileToClose.path)
        redoStacks.remove(fileToClose.path)
        _hasUnsavedChanges.value = _hasUnsavedChanges.value - fileToClose.path
        
        // Update current file index if needed
        if (_openFiles.value.isEmpty()) {
            _currentFileIndex.value = 0
            _currentFile.value = null
            _content.value = ""
        } else if (_currentFileIndex.value >= _openFiles.value.size) {
            switchToFile(_openFiles.value.size - 1)
        } else if (_currentFileIndex.value == index) {
            // If closing current file, switch to previous or next
            val newIndex = if (index > 0) index - 1 else 0
            switchToFile(newIndex)
        }
        
        return true
    }
    
    fun saveAndCloseFile(index: Int) {
        viewModelScope.launch {
            if (index in _openFiles.value.indices) {
                val fileToClose = _openFiles.value[index]
                
                // Switch to the file temporarily to save it
                val originalIndex = _currentFileIndex.value
                if (index != originalIndex) {
                    switchToFile(index)
                }
                
                // Save the file
                saveFile()
                
                // Close the file
                closeFile(index, forceClose = true)
                
                // If we had switched files, adjust back
                if (index < originalIndex) {
                    _currentFileIndex.value = originalIndex - 1
                }
            }
        }
    }

    fun updateContent(newContent: String) {
        val currentFilePath = _currentFile.value?.path ?: return
        
        if (!isUndoRedoOperation && newContent != _content.value) {
            // Get or create undo stack for this file
            val undoStack = undoStacks.getOrPut(currentFilePath) { mutableListOf() }
            
            // Add current content to undo stack before updating
            undoStack.add(_content.value)
            
            // Clear redo stack when new content is added
            redoStacks[currentFilePath]?.clear()
            
            // Mark file as having unsaved changes
            _hasUnsavedChanges.value = _hasUnsavedChanges.value + (currentFilePath to true)
            
            updateUndoRedoState()
        }
        _content.value = newContent
    }
    
    fun undo() {
        val currentFilePath = _currentFile.value?.path ?: return
        val undoStack = undoStacks[currentFilePath] ?: return
        
        if (undoStack.isNotEmpty()) {
            isUndoRedoOperation = true
            
            // Get or create redo stack
            val redoStack = redoStacks.getOrPut(currentFilePath) { mutableListOf() }
            
            // Push current content to redo stack
            redoStack.add(_content.value)
            
            // Pop from undo stack
            _content.value = undoStack.removeLast()
            
            updateUndoRedoState()
            isUndoRedoOperation = false
        }
    }
    
    fun redo() {
        val currentFilePath = _currentFile.value?.path ?: return
        val redoStack = redoStacks[currentFilePath] ?: return
        
        if (redoStack.isNotEmpty()) {
            isUndoRedoOperation = true
            
            // Get or create undo stack
            val undoStack = undoStacks.getOrPut(currentFilePath) { mutableListOf() }
            
            // Push current content to undo stack
            undoStack.add(_content.value)
            
            // Pop from redo stack
            _content.value = redoStack.removeLast()
            
            updateUndoRedoState()
            isUndoRedoOperation = false
        }
    }
    
    private fun updateUndoRedoState() {
        val currentFilePath = _currentFile.value?.path
        _canUndo.value = currentFilePath?.let { undoStacks[it]?.isNotEmpty() } ?: false
        _canRedo.value = currentFilePath?.let { redoStacks[it]?.isNotEmpty() } ?: false
    }

    fun saveFile() {
        viewModelScope.launch {
            val file = _currentFile.value
            if (file != null) {
                val updatedFile =
                    file.copy(content = _content.value, lastModified = System.currentTimeMillis())
                repository.saveFile(updatedFile)
                _currentFile.value = updatedFile
                
                // Update in open files list
                val currentIndex = _currentFileIndex.value
                if (currentIndex in _openFiles.value.indices) {
                    _openFiles.value = _openFiles.value.toMutableList().apply {
                        set(currentIndex, updatedFile)
                    }
                }
                
                // Mark as saved
                _hasUnsavedChanges.value = _hasUnsavedChanges.value - file.path
            }
        }
    }

    fun saveFileAs(path: String) {
        viewModelScope.launch {
            val fileName = File(path).name
            val newFile = MarkdownFile(
                name = fileName,
                path = path,
                content = _content.value,
                lastModified = System.currentTimeMillis()
            )
            repository.saveFile(newFile)
            _currentFile.value = newFile
        }
    }

    fun createNewFile(name: String, content: String = "") {
        viewModelScope.launch {
            val newFile = repository.createNewFile(name, content)
            
            // Add to open files and switch to it
            _openFiles.value = _openFiles.value + newFile
            _currentFileIndex.value = _openFiles.value.size - 1
            _currentFile.value = newFile
            _content.value = newFile.content
            
            // Initialize undo/redo stacks for this file
            undoStacks[newFile.path] = mutableListOf()
            redoStacks[newFile.path] = mutableListOf()
            updateUndoRedoState()
        }
    }

    fun createNewFileInDefaultDir(name: String, content: String = "") {
        viewModelScope.launch {
            val defaultDir = SettingsState.defaultSaveDirectory
            if (defaultDir != null) {
                // For now, still use app dir, but TODO: implement saving to custom dir
                val newFile = repository.createNewFile(name, content)
                
                // Add to open files and switch to it
                _openFiles.value = _openFiles.value + newFile
                _currentFileIndex.value = _openFiles.value.size - 1
                _currentFile.value = newFile
                _content.value = newFile.content
                
                // Initialize undo/redo stacks for this file
                undoStacks[newFile.path] = mutableListOf()
                redoStacks[newFile.path] = mutableListOf()
                updateUndoRedoState()
            } else {
                createNewFile(name, content)
            }
        }
    }
}