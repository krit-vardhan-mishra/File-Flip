package com.just_for_fun.fileflip.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just_for_fun.fileflip.data.ai.ProviderFactory
import com.just_for_fun.fileflip.data.local.entity.ChatMessageEntity
import com.just_for_fun.fileflip.data.local.entity.ChatSessionEntity
import com.just_for_fun.fileflip.domain.repository.ChatRepository
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.domain.repository.MarkdownRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.just_for_fun.fileflip.ui.screens.SettingsState
import java.io.File
import com.just_for_fun.fileflip.data.local.dao.WorkspaceDao
import com.just_for_fun.fileflip.data.local.dao.ChunkDao
import com.just_for_fun.fileflip.data.local.dao.FileDao
import com.just_for_fun.fileflip.data.local.entity.ChunkEntity
import com.just_for_fun.fileflip.data.local.util.WorkspaceIndexer
import com.just_for_fun.fileflip.data.local.util.OnnxEmbeddingGenerator
import com.just_for_fun.fileflip.data.local.util.SimilaritySearchHelper
import com.just_for_fun.fileflip.data.local.util.WebSearchHelper
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

@HiltViewModel
class EditorViewModel @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val repository: MarkdownRepository,
    private val chatRepository: ChatRepository,
    private val providerFactory: ProviderFactory,
    private val workspaceDao: WorkspaceDao,
    private val chunkDao: ChunkDao,
    private val fileDao: FileDao,
    private val workspaceIndexer: WorkspaceIndexer,
    private val embeddingGenerator: OnnxEmbeddingGenerator
) : ViewModel() {

    private val _activeWorkspaceId = MutableStateFlow<String?>(null)
    val activeWorkspaceId: StateFlow<String?> = _activeWorkspaceId.asStateFlow()

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
    private val autoSaveDelayMs = 3000L
    private var autoSaveJob: kotlinx.coroutines.Job? = null
    
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    init {
        viewModelScope.launch {
            _content.collect { newContent ->
                if (!isUndoRedoOperation) {
                    autoSaveJob?.cancel()
                    autoSaveJob = viewModelScope.launch {
                        kotlinx.coroutines.delay(autoSaveDelayMs)
                        saveFile()
                    }
                }
            }
        }
    }

    fun setActiveWorkspace(folderUri: String) {
        viewModelScope.launch {
            val workspace = workspaceDao.getWorkspaceByPath(folderUri)
            if (workspace != null) {
                _activeWorkspaceId.value = workspace.id
                workspaceIndexer.indexWorkspace(workspace.id, workspace.rootPath)
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
                val updatedFile = file.copy(lastModified = System.currentTimeMillis())
                repository.saveFile(updatedFile)

                val existingIndex = _openFiles.value.indexOfFirst { it.path == updatedFile.path }
                if (existingIndex >= 0) {
                    _openFiles.value = _openFiles.value.toMutableList().apply {
                        set(existingIndex, updatedFile)
                    }
                    switchToFile(existingIndex)
                } else {
                    _openFiles.value = _openFiles.value + updatedFile
                    _currentFileIndex.value = _openFiles.value.size - 1
                    _currentFile.value = updatedFile
                    _content.value = updatedFile.content

                    undoStacks[updatedFile.path] = mutableListOf()
                    redoStacks[updatedFile.path] = mutableListOf()
                    updateUndoRedoState()
                    ensureSessionExists(updatedFile.path, updatedFile.name)
                }
            } else {
                val existingIndex = _openFiles.value.indexOfFirst { it.path == path }
                if (existingIndex >= 0) {
                    _fileNotFoundError.value = existingIndex to (File(path).name)
                }
            }
        }
    }
    
    fun switchToFile(index: Int) {
        if (index in _openFiles.value.indices) {
            _currentFile.value?.let { currentFile ->
                val hasChanges = _content.value != currentFile.content
                _hasUnsavedChanges.value = _hasUnsavedChanges.value + (currentFile.path to hasChanges)
            }
            
            val newFile = _openFiles.value[index]
            
            viewModelScope.launch {
                val fileExists = File(newFile.path).exists()
                if (!fileExists) {
                    _fileNotFoundError.value = index to newFile.name
                } else {
                    _currentFileIndex.value = index
                    _currentFile.value = newFile
                    _content.value = newFile.content
                    updateUndoRedoState()
                    ensureSessionExists(newFile.path, newFile.name)
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
        
        if (hasChanges && !forceClose) {
            return false
        }
        
        _openFiles.value = _openFiles.value.filterIndexed { i, _ -> i != index }
        
        undoStacks.remove(fileToClose.path)
        redoStacks.remove(fileToClose.path)
        _hasUnsavedChanges.value = _hasUnsavedChanges.value - fileToClose.path
        
        if (_openFiles.value.isEmpty()) {
            _currentFileIndex.value = 0
            _currentFile.value = null
            _content.value = ""
        } else if (_currentFileIndex.value >= _openFiles.value.size) {
            switchToFile(_openFiles.value.size - 1)
        } else if (_currentFileIndex.value == index) {
            val newIndex = if (index > 0) index - 1 else 0
            switchToFile(newIndex)
        }
        
        return true
    }
    
    fun saveAndCloseFile(index: Int) {
        viewModelScope.launch {
            if (index in _openFiles.value.indices) {
                val fileToClose = _openFiles.value[index]
                
                val originalIndex = _currentFileIndex.value
                if (index != originalIndex) {
                    switchToFile(index)
                }
                
                saveFile()
                closeFile(index, forceClose = true)
                
                if (index < originalIndex) {
                    _currentFileIndex.value = originalIndex - 1
                }
            }
        }
    }

    fun updateContent(newContent: String) {
        val currentFilePath = _currentFile.value?.path ?: return
        
        if (!isUndoRedoOperation && newContent != _content.value) {
            val undoStack = undoStacks.getOrPut(currentFilePath) { mutableListOf() }
            undoStack.add(_content.value)
            redoStacks[currentFilePath]?.clear()
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
            val redoStack = redoStacks.getOrPut(currentFilePath) { mutableListOf() }
            redoStack.add(_content.value)
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
            val undoStack = undoStacks.getOrPut(currentFilePath) { mutableListOf() }
            undoStack.add(_content.value)
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
                
                val currentIndex = _currentFileIndex.value
                if (currentIndex in _openFiles.value.indices) {
                    _openFiles.value = _openFiles.value.toMutableList().apply {
                        set(currentIndex, updatedFile)
                    }
                }
                
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
            
            _openFiles.value = _openFiles.value + newFile
            _currentFileIndex.value = _openFiles.value.size - 1
            _currentFile.value = newFile
            _content.value = newFile.content
            
            undoStacks[newFile.path] = mutableListOf()
            redoStacks[newFile.path] = mutableListOf()
            updateUndoRedoState()
            ensureSessionExists(newFile.path, newFile.name)
        }
    }

    fun createNewFileInDefaultDir(name: String, content: String = "") {
        viewModelScope.launch {
            val defaultDir = SettingsState.defaultSaveDirectory
            if (defaultDir != null) {
                val newFile = repository.createNewFile(name, content)
                
                _openFiles.value = _openFiles.value + newFile
                _currentFileIndex.value = _openFiles.value.size - 1
                _currentFile.value = newFile
                _content.value = newFile.content
                
                undoStacks[newFile.path] = mutableListOf()
                redoStacks[newFile.path] = mutableListOf()
                updateUndoRedoState()
            } else {
                createNewFile(name, content)
            }
        }
    }

    // --- SESSION MANAGEMENT ---
    private val _currentSession = MutableStateFlow<ChatSessionEntity?>(null)
    val currentSession: StateFlow<ChatSessionEntity?> = _currentSession.asStateFlow()

    private val _chatSessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    val chatSessions: StateFlow<List<ChatSessionEntity>> = _chatSessions.asStateFlow()

    private fun ensureSessionExists(filePath: String, fileName: String) {
        viewModelScope.launch {
            chatRepository.getSessionsForFile(filePath).collect { sessions ->
                _chatSessions.value = sessions
                if (sessions.isEmpty()) {
                    val session = chatRepository.createSession(filePath, "Chat - $fileName")
                    _currentSession.value = session
                    loadSessionMessages(session.id)
                } else if (_currentSession.value == null || sessions.none { it.id == _currentSession.value?.id }) {
                    _currentSession.value = sessions.first()
                    loadSessionMessages(sessions.first().id)
                } else {
                    val stillExists = sessions.any { it.id == _currentSession.value?.id }
                    if (!stillExists && sessions.isNotEmpty()) {
                        _currentSession.value = sessions.first()
                        loadSessionMessages(sessions.first().id)
                    }
                }
            }
        }
    }

    fun switchSession(sessionId: String) {
        viewModelScope.launch {
            val session = _chatSessions.value.find { it.id == sessionId }
            if (session != null) {
                _currentSession.value = session
                loadSessionMessages(sessionId)
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val file = _currentFile.value ?: return@launch
            val session = chatRepository.createSession(file.path, "Chat - ${file.name}")
            _currentSession.value = session
            loadSessionMessages(session.id)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepository.deleteSession(sessionId)
            if (_currentSession.value?.id == sessionId) {
                val sessions = _chatSessions.value.filter { it.id != sessionId }
                if (sessions.isNotEmpty()) {
                    _currentSession.value = sessions.first()
                    loadSessionMessages(sessions.first().id)
                } else {
                    _currentSession.value = null
                    _agentChatMessages.value = emptyList()
                }
            }
        }
    }

    private fun loadSessionMessages(sessionId: String) {
        viewModelScope.launch {
            chatRepository.getMessagesForSession(sessionId).collect { messages ->
                _agentChatMessages.value = messages
            }
        }
    }

    // --- AGENTIC CHAT STATE ---
    private val _agentChatMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val agentChatMessages: StateFlow<List<ChatMessageEntity>> = _agentChatMessages.asStateFlow()

    private val _streamingContent = MutableStateFlow<String?>(null)
    val streamingContent: StateFlow<String?> = _streamingContent.asStateFlow()

    // Combined messages: DB messages + in-progress streaming message
    val chatMessages: StateFlow<List<ChatMessageEntity>> = combine(
        _agentChatMessages, _streamingContent
    ) { messages, streaming ->
        if (streaming != null) {
            val list = messages.toMutableList()
            val idx = list.indexOfLast { it.id == "__streaming__" }
            if (idx >= 0) {
                list[idx] = list[idx].copy(content = streaming)
            } else {
                list.add(
                    ChatMessageEntity(
                        id = "__streaming__",
                        sessionId = "",
                        role = "assistant",
                        content = streaming
                    )
                )
            }
            list
        } else {
            messages.filter { it.id != "__streaming__" }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _isAgentLoading = MutableStateFlow(false)
    val isAgentLoading: StateFlow<Boolean> = _isAgentLoading.asStateFlow()

    private val _agentError = MutableStateFlow<String?>(null)
    val agentError: StateFlow<String?> = _agentError.asStateFlow()

    // --- Diff & Selection Context State ---
    private val _pendingChanges = MutableStateFlow<PendingChanges?>(null)
    val pendingChanges: StateFlow<PendingChanges?> = _pendingChanges.asStateFlow()

    var selectedTextContext: String? = null
        private set
    var activeSelectionRange: androidx.compose.ui.text.TextRange? = null
        private set

    fun sendSelectedTextToAgent(selectedText: String, range: androidx.compose.ui.text.TextRange) {
        selectedTextContext = selectedText
        activeSelectionRange = range
        
        viewModelScope.launch {
            val session = _currentSession.value
            if (session != null) {
                chatRepository.addMessage(
                    sessionId = session.id,
                    role = "user",
                    content = "Selected text context:\n```\n$selectedText\n```"
                )
            }
        }
    }

    fun clearSelectedTextContext() {
        selectedTextContext = null
        activeSelectionRange = null
    }

    fun acceptPendingChanges() {
        val pending = _pendingChanges.value ?: return
        updateContent(pending.proposedContent)
        _pendingChanges.value = null
        clearSelectedTextContext()
    }

    fun rejectPendingChanges() {
        _pendingChanges.value = null
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            val session = _currentSession.value
            if (session != null) {
                chatRepository.clearMessagesForSession(session.id)
            }
            _streamingContent.value = null
            _agentError.value = null
            clearSelectedTextContext()
        }
    }

    private fun buildRollingHistory(history: List<ChatMessageEntity>): List<ChatMessageEntity> {
        val finalHistory = mutableListOf<ChatMessageEntity>()
        val summaryMessage = history.find { it.role == "system" && it.content.startsWith("SUMMARY OF PREVIOUS CONVERSATION:") }
        if (summaryMessage != null) {
            finalHistory.add(summaryMessage)
        }
        val recentMessages = history.filter { it.id != summaryMessage?.id }.takeLast(10)
        finalHistory.addAll(recentMessages)
        return finalHistory
    }

    private fun extractSearchQuery(content: String): String? {
        val cleanContent = content.trim().removeSurrounding("```json", "```").removeSurrounding("```", "```").trim()
        return try {
            val json = org.json.JSONObject(cleanContent)
            if (json.optString("name") == "search_web") {
                json.optJSONObject("args")?.optString("query")
            } else {
                null
            }
        } catch (e: Exception) {
            if (content.contains("search_web")) {
                val queryRegex = """"query"\s*:\s*"([^"]+)"""".toRegex()
                queryRegex.find(content)?.groupValues?.get(1)
            } else {
                null
            }
        }
    }

    private fun summarizeConversationIfNeeded(sessionId: String) {
        viewModelScope.launch {
            try {
                val messages = chatRepository.getMessagesForSession(sessionId).first()
                if (messages.size > 20) {
                    val first15 = messages.take(15)
                    val transcript = first15.joinToString("\n") { "${it.role.uppercase()}: ${it.content}" }
                    
                    val provider = providerFactory.getProvider()
                    val apiKey = providerFactory.getApiKey(provider.providerName())
                    val modelName = providerFactory.getModelName(provider.providerName()).ifEmpty {
                        SettingsState.suggestModelName(apiKey)
                    }
                    
                    if (apiKey.isNotEmpty()) {
                        val systemPrompt = "You are a helper that summarizes chat history. " +
                                "Summarize the following conversation history into a single concise paragraph. " +
                                "Keep it focused on the user's goals, key code modifications proposed, and file context. " +
                                "Output ONLY the summary, no preface or pleasantries."
                        val userMsg = ChatMessageEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            sessionId = "",
                            role = "user",
                            content = transcript,
                            timestamp = System.currentTimeMillis()
                        )
                        
                        val summaryResponse = provider.chat(systemPrompt, listOf(userMsg), apiKey, modelName)
                        val summaryText = summaryResponse.trim()
                        
                        if (summaryText.isNotEmpty()) {
                            chatRepository.deleteMessages(first15)
                            chatRepository.addMessage(sessionId, "system", "SUMMARY OF PREVIOUS CONVERSATION:\n$summaryText")
                            Log.d("FileFlip", "Conversation summarized successfully.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FileFlip", "Failed to summarize conversation", e)
            }
        }
    }

    fun sendAgentPrompt(prompt: String) {
        if (prompt.isBlank()) return
        
        val session = _currentSession.value
        if (session == null) {
            _agentError.value = "No active chat session. Please open a file first."
            return
        }
        
        _isAgentLoading.value = true
        _agentError.value = null
        _streamingContent.value = ""
        
        viewModelScope.launch {
            try {
                // RAG Context retrieval
                val queryEmbedding = if (embeddingGenerator.isModelAvailable()) {
                    embeddingGenerator.getEmbedding(prompt)
                } else {
                    null
                }

                val activeWsId = _activeWorkspaceId.value
                val matchingContext = if (queryEmbedding != null && activeWsId != null) {
                    val files = fileDao.getFilesForWorkspaceSync(activeWsId)
                    val matches = mutableListOf<Pair<ChunkEntity, Float>>()
                    for (file in files) {
                        val chunks = chunkDao.getChunksForFile(file.id)
                        for (chunk in chunks) {
                            val chunkVector = SimilaritySearchHelper.toFloatArray(chunk.vectorEmbedding)
                            val sim = SimilaritySearchHelper.computeCosineSimilarity(queryEmbedding, chunkVector)
                            if (sim >= 0.35f) {
                                matches.add(Pair(chunk, sim))
                            }
                        }
                    }
                    val topMatches = matches.sortedByDescending { it.second }.take(4)
                    if (topMatches.isNotEmpty()) {
                        val contextBuilder = StringBuilder("\n--- RELEVANT WORKSPACE CONTEXT ---\n")
                        contextBuilder.append("Below are context snippets from other files in the workspace that might be relevant to the query:\n\n")
                        for ((matchChunk, score) in topMatches) {
                            val matchFile = files.find { it.id == matchChunk.fileId }
                            val matchFileName = matchFile?.path?.let { Uri.parse(it).path?.substringAfterLast("/") } ?: "unknown"
                            val fileDoc = DocumentFile.fromTreeUri(context, Uri.parse(matchFile?.path ?: ""))
                            if (fileDoc != null && fileDoc.exists()) {
                                val fullText = context.contentResolver.openInputStream(fileDoc.uri)?.use { 
                                    it.bufferedReader().use { r -> r.readText() } 
                                } ?: ""
                                if (matchChunk.startByte >= 0 && matchChunk.endByte <= fullText.length) {
                                    val snippet = fullText.substring(matchChunk.startByte, matchChunk.endByte).trim()
                                    contextBuilder.append("Snippet from $matchFileName (Relevance: ${(score * 100).toInt()}%):\n")
                                    contextBuilder.append("```\n$snippet\n```\n\n")
                                }
                            }
                        }
                        contextBuilder.toString()
                    } else {
                        ""
                    }
                } else {
                    ""
                }

                chatRepository.addMessage(
                    sessionId = session.id,
                    role = "user",
                    content = prompt
                )
                
                val fileName = _currentFile.value?.name ?: "untitled"
                val fileContent = if (_pendingChanges.value != null) {
                    _pendingChanges.value!!.proposedContent
                } else {
                    _content.value
                }
                
                val systemPrompt = if (selectedTextContext != null) {
                    "You are FlipFile Agent, a helpful AI assistant. The user has selected a specific text context to work on. " +
                    "You ONLY have access to this selected text. Do not refer to the rest of the file.\n" +
                    "Selected Text:\n```\n$selectedTextContext\n```\n" +
                    "Perform edits, answer questions, or write code for this selected text only. If you write code or suggest changes, provide the modified text inside a markdown code block."
                } else {
                    "You are FlipFile Agent, a helpful AI assistant integrated into a Markdown and code editor. " +
                    "You have access to the user's current file context.\n" +
                    "Active File: $fileName\n" +
                    "Current Content:\n```\n$fileContent\n```\n" +
                    matchingContext +
                    "\nHelp the user edit, format, outline, or write code. If you write code, provide it inside standard markdown code blocks."
                }
                
                runAgentLoop(session.id, systemPrompt, matchingContext)
            } catch (e: Exception) {
                Log.e("FileFlip", "Error in sendAgentPrompt", e)
                _streamingContent.value = null
                _agentError.value = e.message ?: "An unknown error occurred"
            } finally {
                _isAgentLoading.value = false
            }
        }
    }

    private suspend fun runAgentLoop(
        sessionId: String,
        systemPrompt: String,
        matchingContext: String
    ) {
        val allMessages = chatRepository.getMessagesForSession(sessionId).first()
        val rollingMessages = buildRollingHistory(allMessages)

        val finalSystemPrompt = systemPrompt + "\n\n" +
                "If the user asks about recent events, weather, or topics you lack knowledge of, you must call the web search tool by outputting ONLY this JSON format:\n" +
                "```json\n" +
                "{\n" +
                "  \"name\": \"search_web\",\n" +
                "  \"args\": {\n" +
                "    \"query\": \"<search query>\"\n" +
                "  }\n" +
                "}\n" +
                "```\n" +
                "Do not add any other text around the JSON block when calling the tool."

        var currentProvider = providerFactory.getProvider()
        var currentApiKey = providerFactory.getApiKey(currentProvider.providerName())
        var currentModelName = providerFactory.getModelName(currentProvider.providerName()).ifEmpty {
            SettingsState.suggestModelName(currentApiKey)
        }

        if (currentApiKey.isBlank()) {
            throw Exception("API Key is missing for ${currentProvider.providerName()}. Please configure it in Settings.")
        }

        val fullResponse = StringBuilder()
        try {
            currentProvider.chatStream(finalSystemPrompt, rollingMessages, currentApiKey, currentModelName)
                .collect { chunk ->
                    fullResponse.append(chunk)
                    _streamingContent.value = fullResponse.toString()
                }
        } catch (e: Exception) {
            Log.e("FileFlip", "Primary provider ${currentProvider.providerName()} failed, retrying fallback...", e)
            val fallback = providerFactory.getFallbackProvider(currentProvider.providerName())
            if (fallback != null) {
                val fallbackKey = providerFactory.getApiKey(fallback.providerName())
                val fallbackModel = providerFactory.getModelName(fallback.providerName()).ifEmpty {
                    SettingsState.suggestModelName(fallbackKey)
                }
                if (fallbackKey.isNotEmpty()) {
                    fullResponse.clear()
                    _streamingContent.value = "⚠️ Primary provider failed. Retrying with ${fallback.providerName()}..."
                    
                    fallback.chatStream(finalSystemPrompt, rollingMessages, fallbackKey, fallbackModel)
                        .collect { chunk ->
                            if (fullResponse.isEmpty()) {
                                _streamingContent.value = "" // clear warning
                            }
                            fullResponse.append(chunk)
                            _streamingContent.value = fullResponse.toString()
                        }
                } else {
                    throw e
                }
            } else {
                throw e
            }
        }

        _streamingContent.value = null
        val responseText = fullResponse.toString()
        chatRepository.addMessage(sessionId, "assistant", responseText)

        val searchQuery = extractSearchQuery(responseText)
        if (searchQuery != null && searchQuery.isNotBlank()) {
            // Display searching status
            _streamingContent.value = "🔍 Searching the web for \"$searchQuery\"..."
            val searchResults = com.just_for_fun.fileflip.data.local.util.WebSearchHelper.search(searchQuery)
            
            // Save search results as user message
            chatRepository.addMessage(sessionId, "user", "Web search results for \"$searchQuery\":\n\n$searchResults")
            
            _streamingContent.value = null
            // Loop again
            runAgentLoop(sessionId, systemPrompt, matchingContext)
        } else {
            // Completed normally, check summarization
            summarizeConversationIfNeeded(sessionId)
        }
    }

    fun applyCodePatchToEditor(codeBlock: String, selectionRange: androidx.compose.ui.text.TextRange? = null) {
        val currentText = _content.value
        val targetRange = selectionRange ?: activeSelectionRange
        val newText = if (targetRange != null && targetRange.start >= 0 && targetRange.end <= currentText.length) {
            val before = currentText.take(targetRange.start)
            val after = currentText.substring(targetRange.end)
            before + codeBlock + after
        } else {
            if (currentText.endsWith("\n") || currentText.isEmpty()) {
                currentText + codeBlock
            } else {
                currentText + "\n" + codeBlock
            }
        }
        _pendingChanges.value = PendingChanges(
            originalContent = currentText,
            proposedContent = newText,
            selectionRange = targetRange
        )
    }

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    fun translateAndSpeak(text: String, targetLanguage: String, speakTextCallback: (String) -> Unit) {
        val apiKey = SettingsState.apiKey
        if (apiKey.isBlank()) {
            _agentError.value = "API Key is missing for translation."
            return
        }

        _isTranslating.value = true
        viewModelScope.launch {
            try {
                val systemPrompt = "You are a professional translator. Translate the user's text into $targetLanguage. " +
                        "Return ONLY the translated text. Do not add any greetings, markdown formatting, quotes, or explanations."
                val userMessage = ChatMessageEntity(
                    id = java.util.UUID.randomUUID().toString(),
                    sessionId = "",
                    role = "user",
                    content = text,
                    timestamp = System.currentTimeMillis()
                )
                
                val provider = providerFactory.getProvider()
                val modelName = SettingsState.aiModelName.ifEmpty {
                    SettingsState.suggestModelName(apiKey)
                }

                val fullResponse = StringBuilder()
                provider.chatStream(systemPrompt, listOf(userMessage), apiKey, modelName)
                    .collect { chunk ->
                        fullResponse.append(chunk)
                    }

                val translation = fullResponse.toString().trim()
                if (translation.isNotEmpty()) {
                    speakTextCallback(translation)
                }
            } catch (e: Exception) {
                Log.e("FileFlip", "Translation failed", e)
            } finally {
                _isTranslating.value = false
            }
        }
    }
}

data class PendingChanges(
    val originalContent: String,
    val proposedContent: String,
    val selectionRange: androidx.compose.ui.text.TextRange?
)
