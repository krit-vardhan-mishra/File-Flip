package com.just_for_fun.fileflip.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.just_for_fun.fileflip.domain.model.ExplorerItem
import com.just_for_fun.fileflip.ui.components.editor.GlobalSearchOverlay
import com.just_for_fun.fileflip.ui.components.editor.AgentSidebarContent
import com.just_for_fun.fileflip.ui.components.editor.EditorFileTypeSelectionBottomSheet
import com.just_for_fun.fileflip.ui.components.editor.EditorTab
import com.just_for_fun.fileflip.ui.components.editor.EditorToolIcon
import com.just_for_fun.fileflip.ui.components.editor.ExplorerTree
import com.just_for_fun.fileflip.ui.components.editor.MoreOptionsBottomSheet
import com.just_for_fun.fileflip.ui.components.editor.SaveAsBottomSheet
import com.just_for_fun.fileflip.domain.model.SearchHighlightTransformation
import com.just_for_fun.fileflip.ui.components.editor.SearchReplaceBottomSheet
import com.just_for_fun.fileflip.ui.components.editor.WordCountRow
import com.just_for_fun.fileflip.ui.theme.LocalAppColors
import com.just_for_fun.fileflip.ui.util.*
import com.just_for_fun.fileflip.ui.viewmodels.EditorViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    navController: NavController,
    filePath: String,
    folderUri: String? = null
) {
    val context = LocalContext.current

    // Scope ViewModel to activity so it persists across navigation
    val viewModel: EditorViewModel = hiltViewModel(viewModelStoreOwner = context as ComponentActivity)

    val currentFile by viewModel.currentFile.collectAsState()
    val content by viewModel.content.collectAsState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }

    // Explorer tree states
    var explorerItems by remember { mutableStateOf<List<ExplorerItem>>(emptyList()) }
    var rootFolderName by remember { mutableStateOf("") }
    val expandedFolders = remember { mutableStateMapOf<String, Boolean>() }
    var isLoadingExplorer by remember { mutableStateOf(false) }
    var explorerDebugInfo by remember { mutableStateOf("") }

    // Global Search Overlay State
    var showGlobalSearch by remember { mutableStateOf(false) }

    LaunchedEffect(folderUri) {
        if (folderUri != null) {
            isLoadingExplorer = true
            Log.d("FileFlip", "EditorScreen: LaunchedEffect START for folderUri = $folderUri")

            // Auto-expand root
            expandedFolders[folderUri] = true

            // Check persisted URI permissions
            val persistedPermissions = context.contentResolver.persistedUriPermissions
            Log.d("FileFlip", "EditorScreen: Persisted URI permissions count = ${persistedPermissions.size}")
            persistedPermissions.forEach { perm ->
                Log.d("FileFlip", "  Persisted: uri=${perm.uri}, read=${perm.isReadPermission}, write=${perm.isWritePermission}")
            }

            // Do IO work and capture results
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val rootUri = Uri.parse(folderUri)
                    Log.d("FileFlip", "EditorScreen: Parsed rootUri = $rootUri")
                    val rootDoc = DocumentFile.fromTreeUri(context, rootUri)
                    Log.d("FileFlip", "EditorScreen: rootDoc = $rootDoc, name=${rootDoc?.name}, canRead=${rootDoc?.canRead()}, isDir=${rootDoc?.isDirectory}")

                    if (rootDoc != null && rootDoc.canRead()) {
                        val name = rootDoc.name ?: "Workspace"
                        val items = listDocumentFiles(context, rootDoc)
                        Log.d("FileFlip", "EditorScreen: Loaded ${items.size} explorer items from '$name'")
                        Triple(name, items, "Loaded ${items.size} items")
                    } else {
                        val reason = when {
                            rootDoc == null -> "rootDoc is null"
                            !rootDoc.canRead() -> "canRead() returned false"
                            else -> "unknown"
                        }
                        Log.e("FileFlip", "EditorScreen: Failed to read directory - $reason")
                        Triple("Workspace", emptyList<ExplorerItem>(), "Error: $reason")
                    }
                } catch (e: Exception) {
                    Log.e("FileFlip", "EditorScreen: Exception during explorer load", e)
                    Triple("Workspace", emptyList<ExplorerItem>(), "Exception: ${e.message}")
                }
            }

            // Apply state updates on Main thread
            rootFolderName = result.first
            explorerItems = result.second
            explorerDebugInfo = result.third
            isLoadingExplorer = false
            Log.d("FileFlip", "EditorScreen: State updated - rootFolderName='$rootFolderName', itemCount=${explorerItems.size}, debug='$explorerDebugInfo'")
        }
    }

    // Multiple files support
    val openFiles by viewModel.openFiles.collectAsState()
    val currentFileIndex by viewModel.currentFileIndex.collectAsState()
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    val fileNotFoundError by viewModel.fileNotFoundError.collectAsState()

    // Bottom sheet states
    var showAttachBottomSheet by remember { mutableStateOf(false) }
    var showFileTypeSelectionBottomSheet by remember { mutableStateOf(false) }
    var showSaveAsBottomSheet by remember { mutableStateOf(false) }
    var showMoreOptionsBottomSheet by remember { mutableStateOf(false) }

    // Close file dialog state
    var fileIndexToClose by remember { mutableStateOf<Int?>(null) }
    var showCloseFileDialog by remember { mutableStateOf(false) }

    // Validation dialog state
    var showValidationDialog by remember { mutableStateOf(false) }
    var validationTitle by remember { mutableStateOf("") }
    var validationMessage by remember { mutableStateOf("") }
    var validationIsError by remember { mutableStateOf(false) }

    // Word count dialog state
    var showWordCountDialog by remember { mutableStateOf(false) }

    // Search & Replace bottom sheet state
    var showSearchReplaceSheet by remember { mutableStateOf(false) }
    var searchReplaceInitialMode by remember { mutableStateOf("search") } // "search" or "replace"

    // Search highlighting state
    var searchMatchRanges by remember { mutableStateOf<List<IntRange>>(emptyList()) }
    var currentSearchMatchIndex by remember { mutableStateOf(0) }
    var replaceHighlight by remember { mutableStateOf<Pair<IntRange, Color>?>(null) }
    val editorScrollState = rememberScrollState()

    // Menu and drawer states
    var showMenu by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    // Update textFieldValue when content changes from viewModel
    LaunchedEffect(content) {
        if (textFieldValue.text != content) {
            textFieldValue = TextFieldValue(
                text = content,
                selection = textFieldValue.selection
            )
        }
    }

    // Scroll to current search match when it changes
    val density = LocalDensity.current
    val lineHeightPx = with(density) { 22.sp.toPx() }
    LaunchedEffect(currentSearchMatchIndex, searchMatchRanges) {
        if (searchMatchRanges.isNotEmpty() && currentSearchMatchIndex < searchMatchRanges.size) {
            val matchRange = searchMatchRanges[currentSearchMatchIndex]
            // Count newlines before match start to find line number
            val textBeforeMatch = content.substring(0, matchRange.first.coerceAtMost(content.length))
            val lineNumber = textBeforeMatch.count { it == '\n' }
            // Scroll to approximate pixel position (line * lineHeight), with some offset above
            val targetScroll = ((lineNumber - 2).coerceAtLeast(0) * lineHeightPx).toInt()
            editorScrollState.animateScrollTo(targetScroll)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = getFileNameFromUriEditorScreen(context, uri)
            if (fileName != null) {
                copyFileToAppStorageEditorScreen(context, uri, fileName)
                // Load the file
                val file = File(context.getExternalFilesDir(null), "Files/$fileName")
                viewModel.loadFile(file.absolutePath)
            }
        }
    }

    val saveAsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/*")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                // Update current file
                val fileName = getFileNameFromUriEditorScreen(context, uri) ?: "untitled.txt"
                viewModel.saveFileAs(uri.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val hasSelection = textFieldValue.selection.start != textFieldValue.selection.end
    val selectedText = if (hasSelection) {
        textFieldValue.text.substring(
            textFieldValue.selection.start,
            textFieldValue.selection.end
        )
    } else {
        ""
    }

    val fontFamily = when (SettingsState.selectedFont) {
        0 -> FontFamily.Default
        1 -> FontFamily.Serif
        2 -> FontFamily.Monospace
        else -> FontFamily.Monospace
    }

    // Detect file type from extension
    val fileExtension = currentFile?.name?.substringAfterLast(".", "")?.lowercase() ?: "md"
    val fileType = FileType.fromExtension(fileExtension)

    // Load file effect
    LaunchedEffect(filePath) {
        if (filePath == "empty") {
            // Don't load any file, just show empty editor
            Log.d("FileFlip", "EditorScreen: Opening empty editor - no file selected")
        } else {
            Log.d("FileFlip", "EditorScreen: Loading file with path: $filePath")
            viewModel.loadFile(filePath)
        }
    }

    // TTS configuration
    var tts by remember { mutableStateOf<android.speech.tts.TextToSpeech?>(null) }
    var speakingMessageId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        tts = android.speech.tts.TextToSpeech(context) { status ->
            if (status != android.speech.tts.TextToSpeech.SUCCESS) {
                Log.e("FileFlip", "TTS Initialization failed!")
            }
        }
    }

    val speakText: (String, String) -> Unit = { messageId: String, text: String ->
        if (speakingMessageId == messageId) {
            tts?.stop()
            speakingMessageId = null
        } else {
            tts?.stop()
            speakingMessageId = messageId
            val cleanText = text.replace(Regex("```[\\s\\S]*?```"), "[code block]")
                .replace(Regex("[#*`_~]"), "")
            tts?.speak(cleanText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // STT configuration
    var onSttResultReceived by remember { mutableStateOf<((String) -> Unit)?>(null) }
    val sttLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
            val spokenText = matches?.firstOrNull() ?: ""
            if (spokenText.isNotEmpty()) {
                onSttResultReceived?.invoke(spokenText)
            }
        }
    }

    val launchSpeechToText = { onResult: (String) -> Unit ->
        onSttResultReceived = onResult
        try {
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak your prompt...")
            }
            sttLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("FileFlip", "STT not supported on this device", e)
            android.widget.Toast.makeText(context, "Speech recognizer not available", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    val rightDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = rightDrawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ModalDrawerSheet(
                        drawerContainerColor = SurfaceDark,
                        modifier = Modifier.width(320.dp)
                    ) {
                        AgentSidebarContent (
                            viewModel = viewModel,
                            navController = navController,
                            onClose = { scope.launch { rightDrawerState.close() } },
                            speakText = speakText,
                            speakingMessageId = speakingMessageId,
                            launchSpeechToText = launchSpeechToText,
                            onTextPatchSelected = { code ->
                                viewModel.applyCodePatchToEditor(code, textFieldValue.selection)
                            }
                        )
                    }
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {

                        ModalDrawerSheet(
                            drawerContainerColor = SurfaceDark
                        ) {
                            if (folderUri != null) {
                                // --- Folder Explorer Sidebar (VS Code Style) ---
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    // Folder Header
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // Clicking root folder toggles expansion of all items
                                                expandedFolders[folderUri] = !(expandedFolders[folderUri] ?: false)
                                            }
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.FolderOpen,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = rootFolderName.uppercase(),
                                            color = TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (expandedFolders[folderUri] == true) Icons.Rounded.ExpandLess else Icons.Rounded.UnfoldMore,
                                            contentDescription = null,
                                            tint = TextGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    androidx.compose.material3.HorizontalDivider(color = DividerColor)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Debug info (visible on screen)
                                    Text(
                                        text = "Items: ${explorerItems.size} | $explorerDebugInfo",
                                        color = TextGray.copy(alpha = 0.5f),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )

                                    if (isLoadingExplorer) {
                                        androidx.compose.material3.CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                                            color = PrimaryBlue
                                        )
                                    } else if (explorerItems.isEmpty()) {
                                        Text(
                                            text = "No files found in this folder",
                                            color = TextGray,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    } else {
                                        // Scrollable list of files/folders
                                        androidx.compose.foundation.lazy.LazyColumn(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            items(explorerItems.size) { index ->
                                                val item = explorerItems[index]
                                                ExplorerTree(
                                                    item = item,
                                                    depth = 0,
                                                    onFileClick = { fileUri, fileName ->
                                                        val localFile = copyUriToLocalFile(context, fileUri, fileName)
                                                        if (localFile != null) {
                                                            viewModel.loadFile(localFile.absolutePath)
                                                            scope.launch { drawerState.close() }
                                                        } else {
                                                            android.widget.Toast.makeText(context, "Failed to open file", android.widget.Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    expandedFolders = expandedFolders
                                                )
                                            }
                                        }
                                    }

                                    // Bottom Navigation Items in Folder Sidebar
                                    androidx.compose.material3.HorizontalDivider(color = DividerColor)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    NavigationDrawerItem(
                                        label = { Text("Settings", color = TextWhite) },
                                        icon = { Icon(Icons.Rounded.Settings, contentDescription = null, tint = TextGray) },
                                        selected = false,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("settings")
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            unselectedContainerColor = Color.Transparent,
                                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier.height(48.dp)
                                    )

                                    NavigationDrawerItem(
                                        label = { Text("About", color = TextWhite) },
                                        icon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = TextGray) },
                                        selected = false,
                                        onClick = {
                                            scope.launch { drawerState.close() }
                                            navController.navigate("about")
                                        },
                                        colors = NavigationDrawerItemDefaults.colors(
                                            unselectedContainerColor = Color.Transparent,
                                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f)
                                        ),
                                        modifier = Modifier.height(48.dp)
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "Flip File",
                                        color = TextWhite,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                androidx.compose.material3.HorizontalDivider(color = DividerColor)

                                Spacer(modifier = Modifier.height(16.dp))

                                // Navigation Items
                                NavigationDrawerItem(
                                    label = { Text("Settings", color = TextWhite) },
                                    icon = { Icon(Icons.Rounded.Settings, contentDescription = null, tint = TextGray) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("settings")
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = Color.Transparent,
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f)
                                    )
                                )

                                NavigationDrawerItem(
                                    label = { Text("About", color = TextWhite) },
                                    icon = { Icon(Icons.Rounded.Info, contentDescription = null, tint = TextGray) },
                                    selected = false,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate("about")
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        unselectedContainerColor = Color.Transparent,
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                ) {
                    // Wrapper Box for applying the global search blur overlay over the Scaffold
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = if (showGlobalSearch) Modifier.blur(16.dp) else Modifier,
                            containerColor = BackgroundDark,
                            topBar = {
                                Column {
                                    // Main Header
                                    TopAppBar(
                                        title = {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                                    .clip(RoundedCornerShape(15.dp))
                                                    .clickable { showGlobalSearch = true },
                                                color = SurfaceDark.copy(alpha = 0.8f),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(horizontal = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val headerText = when {
                                                        currentFile != null -> currentFile!!.name
                                                        rootFolderName.isNotEmpty() -> rootFolderName
                                                        else -> "Search..."
                                                    }
                                                    Text(
                                                        text = headerText,
                                                        color = TextGray.copy(alpha = 0.7f),
                                                        fontSize = 15.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .width(1.dp)
                                                            .height(18.dp)
                                                            .background(DividerColor)
                                                    )
                                                }
                                            }
                                        },
                                        navigationIcon = {
                                            IconButton(onClick = {
                                                scope.launch {
                                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "Menu",
                                                    tint = PrimaryBlue
                                                )
                                            }
                                        },
                                        actions = {
                                            IconButton(onClick = {
                                                scope.launch {
                                                    if (rightDrawerState.isClosed) rightDrawerState.open() else rightDrawerState.close()
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AutoAwesome,
                                                    contentDescription = "AI Agent",
                                                    tint = PrimaryBlue
                                                )
                                            }
                                            IconButton(onClick = { showMoreOptionsBottomSheet = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "More Options", tint = TextGray)
                                            }
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = BackgroundDark
                                        )
                                    )

                                    // Tab Row - Dynamic tabs for open files
                                    if (openFiles.isNotEmpty()) {
                                        LazyRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(BackgroundDark.copy(alpha = 0.5f))
                                                .border(1.dp, DividerColor)
                                        ) {
                                            itemsIndexed(openFiles) { index, file ->
                                                EditorTab(
                                                    title = file.name,
                                                    isActive = index == currentFileIndex,
                                                    hasUnsavedChanges = hasUnsavedChanges[file.path] ?: false,
                                                    onClick = { viewModel.switchToFile(index) },
                                                    onLongClick = {
                                                        fileIndexToClose = index
                                                        val fileHasChanges = hasUnsavedChanges[file.path] ?: false
                                                        if (fileHasChanges) {
                                                            showCloseFileDialog = true
                                                        } else {
                                                            viewModel.closeFile(index, forceClose = true)
                                                        }
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        // Show empty state when no files are open
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(BackgroundDark.copy(alpha = 0.5f))
                                                .border(1.dp, DividerColor)
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                "No files open",
                                                color = TextGray,
                                                fontSize = 14.sp
                                            )
                                        }
                                    }
                                }
                            },
                            floatingActionButton = {
                                if (currentFile != null) {
                                     FloatingActionButton(
                                         onClick = {
                                             viewModel.saveFile()
                                             val encodedPath = java.net.URLEncoder.encode(currentFile!!.path, "UTF-8")
                                             navController.navigate("preview/$encodedPath")
                                         },
                                         containerColor = PrimaryBlue,
                                         contentColor = Color.White,
                                         shape = RoundedCornerShape(16.dp),
                                         modifier = Modifier.padding(bottom = 60.dp) // Space for bottom toolbar
                                     ) {
                                         Icon(Icons.Default.PlayArrow, contentDescription = "Preview", modifier = Modifier.size(32.dp))
                                     }
                                }
                            },
                            bottomBar = {
                                // Quick Tool Bar - Context aware based on file type and selection
                                Surface(
                                    color = BackgroundDark,
                                    modifier = Modifier.fillMaxWidth(),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        // Show tools based on file type and selection state
                                        val toolbarTools = if (hasSelection) {
                                            fileType.getFormattingTools()
                                        } else {
                                            fileType.getToolbarTools(
                                                onShowValidation = { title, message, isError ->
                                                    validationTitle = title
                                                    validationMessage = message
                                                    validationIsError = isError
                                                    showValidationDialog = true
                                                },
                                                onShowWordCount = { showWordCountDialog = true },
                                                onShowFindReplace = {
                                                    searchReplaceInitialMode = "replace"
                                                    showSearchReplaceSheet = true
                                                }
                                            )
                                        }

                                        toolbarTools.forEach { tool ->
                                            EditorToolIcon(
                                                icon = tool.icon,
                                                contentDescription = tool.description,
                                                isSelected = hasSelection,
                                                onClick = {
                                                    if (tool.icon == Icons.Outlined.AttachFile) {
                                                        showAttachBottomSheet = true
                                                    } else {
                                                        tool.action(
                                                            viewModel,
                                                            textFieldValue.text,
                                                            selectedText,
                                                            if (hasSelection) textFieldValue.selection else null
                                                        )
                                                        // Clear selection after formatting
                                                        if (hasSelection) {
                                                            textFieldValue = TextFieldValue(
                                                                text = viewModel.content.value,
                                                                selection = TextRange(textFieldValue.selection.end)
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        ) { paddingValues ->
                            Row(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .fillMaxSize()
                            ) {
                                // Line Numbers Gutter
                                Column(
                                    modifier = Modifier
                                        .width(48.dp)
                                        .fillMaxHeight()
                                        .background(GutterColor)
                                        .verticalScroll(rememberScrollState()), // Note: Syncing scroll is complex, simplified here
                                    horizontalAlignment = Alignment.End
                                ) {
                                    // Simulate line numbers based on content lines
                                    val lineCount = content.count { it == '\n' } + 1
                                    // Limit rendered lines for performance in this basic implementation
                                    // In a real app, use a proper code editor library
                                    val displayLines = minOf(lineCount, 100)

                                    Spacer(modifier = Modifier.height(16.dp))
                                    repeat(displayLines) { index ->
                                        Text(
                                            text = "${index + 1}",
                                            color = if ((index + 1) % 5 == 0) PrimaryBlue.copy(alpha = 0.6f) else TextGray.copy(alpha = 0.3f),
                                            fontSize = 14.sp,
                                            fontFamily = fontFamily,
                                            modifier = Modifier.padding(end = 12.dp, bottom = 2.dp) // Approximate line height match
                                        )
                                    }
                                }

                                // Editor Surface
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .background(BackgroundDark)
                                        .padding(horizontal = 16.dp, vertical = 16.dp)
                                ) {
                                    // Build VisualTransformation for search highlighting
                                    val appColors = LocalAppColors.current
                                    val searchHighlightTransformation = remember(searchMatchRanges, currentSearchMatchIndex, replaceHighlight) {
                                        SearchHighlightTransformation(
                                            matchRanges = searchMatchRanges,
                                            currentMatchIndex = currentSearchMatchIndex,
                                            searchHighlightColor = appColors.searchHighlight,
                                            currentMatchColor = appColors.searchHighlight.copy(alpha = 0.6f),
                                            replaceHighlight = replaceHighlight
                                        )
                                    }

                                    SelectionContainer {
                                        TextField(
                                            value = textFieldValue,
                                            onValueChange = { newValue ->
                                                textFieldValue = newValue
                                                viewModel.updateContent(newValue.text)
                                            },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .verticalScroll(editorScrollState),
                                            textStyle = TextStyle(
                                                color = TextWhite,
                                                fontSize = 15.sp,
                                                fontFamily = fontFamily,
                                                lineHeight = 22.sp
                                            ),
                                            visualTransformation = searchHighlightTransformation,
                                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Overlay Global Search UI on top of Scaffold content
                        if (showGlobalSearch) {
                            GlobalSearchOverlay(
                                onDismiss = { showGlobalSearch = false }
                            )
                        }

                    } // End Wrapper Box

                    // Close File Confirmation Dialog
                    if (showCloseFileDialog && fileIndexToClose != null) {
                        AlertDialog(
                            onDismissRequest = {
                                showCloseFileDialog = false
                                fileIndexToClose = null
                            },
                            title = { Text("Unsaved Changes", color = TextWhite) },
                            text = {
                                Text(
                                    "Do you want to save changes to ${openFiles.getOrNull(fileIndexToClose!!)?.name ?: "this file"}?",
                                    color = TextGray
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        fileIndexToClose?.let { viewModel.saveAndCloseFile(it) }
                                        showCloseFileDialog = false
                                        fileIndexToClose = null
                                    }
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        fileIndexToClose?.let { viewModel.closeFile(it, forceClose = true) }
                                        showCloseFileDialog = false
                                        fileIndexToClose = null
                                    }
                                ) {
                                    Text("Don't Save")
                                }
                            },
                            containerColor = SurfaceDark
                        )
                    }

                    // File Not Found Dialog
                    if (fileNotFoundError != null) {
                        val (errorIndex, fileName) = fileNotFoundError!!
                        AlertDialog(
                            onDismissRequest = {
                                viewModel.closeFile(errorIndex, forceClose = true)
                                viewModel.clearFileNotFoundError()
                            },
                            title = { Text("File Not Found", color = TextWhite) },
                            text = {
                                Text(
                                    "The file \"$fileName\" no longer exists. It may have been deleted.",
                                    color = TextGray
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.closeFile(errorIndex, forceClose = true)
                                        viewModel.clearFileNotFoundError()
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            containerColor = SurfaceDark
                        )
                    }

                    // Bottom Sheets
                    // More Options Bottom Sheet (now contains File options, Find & Replace, and Share/Print options)
                    if (showMoreOptionsBottomSheet) {
                        MoreOptionsBottomSheet(
                            currentFile = currentFile,
                            onDismiss = { showMoreOptionsBottomSheet = false },
                            onSave = {
                                showMoreOptionsBottomSheet = false
                                if (currentFile != null) {
                                    viewModel.saveFile()
                                } else {
                                    showSaveAsBottomSheet = true
                                }
                            },
                            onOpenFile = {
                                showMoreOptionsBottomSheet = false
                                filePickerLauncher.launch("*/*")
                            },
                            onCreateNewFile = {
                                showMoreOptionsBottomSheet = false
                                showFileTypeSelectionBottomSheet = true
                            },
                            onSearch = {
                                showMoreOptionsBottomSheet = false
                                searchReplaceInitialMode = "search"
                                showSearchReplaceSheet = true
                            },
                            onShareAsPDF = {
                                showMoreOptionsBottomSheet = false
                                currentFile?.let {
                                    viewModel.saveFile()
                                    val encodedPath = java.net.URLEncoder.encode(it.path, "UTF-8")
                                    navController.navigate("preview/$encodedPath")
                                }
                            },
                            onShareFile = {
                                showMoreOptionsBottomSheet = false
                                currentFile?.let { file ->
                                    try {
                                        val shareFile = File(file.path)
                                        if (shareFile.exists()) {
                                            val fileUri = androidx.core.content.FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                shareFile
                                            )
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = when (file.name.substringAfterLast(".", "").lowercase()) {
                                                    "md", "markdown" -> "text/markdown"
                                                    "json" -> "application/json"
                                                    "xml" -> "application/xml"
                                                    "yaml", "yml" -> "text/yaml"
                                                    "html", "htm" -> "text/html"
                                                    "csv" -> "text/csv"
                                                    else -> "text/plain"
                                                }
                                                putExtra(Intent.EXTRA_STREAM, fileUri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
                                        }
                                    } catch (e: Exception) {
                                        // Fallback: share as plain text
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, content)
                                            putExtra(Intent.EXTRA_SUBJECT, file.name)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share ${file.name}"))
                                    }
                                }
                            },
                            onPrint = {
                                showMoreOptionsBottomSheet = false
                                currentFile?.let {
                                    viewModel.saveFile()
                                    val encodedPath = java.net.URLEncoder.encode(it.path, "UTF-8")
                                    navController.navigate("preview/$encodedPath")
                                }
                            }
                        )
                    }

                    if (showFileTypeSelectionBottomSheet) {
                        EditorFileTypeSelectionBottomSheet(
                            onDismiss = { showFileTypeSelectionBottomSheet = false },
                            onFileTypeSelected = { type, content ->
                                val extension = when (type) {
                                    "Markdown" -> "md"
                                    "JSON" -> "json"
                                    "YAML" -> "yaml"
                                    "XML" -> "xml"
                                    "HTML" -> "html"
                                    "Text" -> "txt"
                                    "Log" -> "log"
                                    "CSV" -> "csv"
                                    else -> "txt"
                                }
                                val fileName = "untitled.$extension"

                                // Create new file using ViewModel (adds to tabs automatically)
                                viewModel.createNewFile(fileName, content)
                                showFileTypeSelectionBottomSheet = false
                            }
                        )
                    }

                    if (showSaveAsBottomSheet) {
                        SaveAsBottomSheet(
                            onDismiss = { showSaveAsBottomSheet = false },
                            onSaveAs = {
                                saveAsLauncher.launch("untitled.txt")
                                showSaveAsBottomSheet = false
                            }
                        )
                    }

                    // Validation Result Dialog
                    if (showValidationDialog) {
                        AlertDialog(
                            onDismissRequest = { showValidationDialog = false },
                            title = {
                                Text(
                                    validationTitle,
                                    color = if (validationIsError) Color(0xFFF44336) else Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            text = {
                                Text(
                                    validationMessage,
                                    color = TextGray,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp
                                )
                            },
                            confirmButton = {
                                Button(onClick = { showValidationDialog = false }) {
                                    Text("OK")
                                }
                            },
                            containerColor = SurfaceDark
                        )
                    }

                    // Word Count Dialog
                    if (showWordCountDialog) {
                        val wordCount = content.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                        val charCount = content.length
                        val charNoSpaceCount = content.replace(Regex("\\s"), "").length
                        val lineCount = content.lines().size
                        val paragraphCount = content.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() }.size

                        AlertDialog(
                            onDismissRequest = { showWordCountDialog = false },
                            title = {
                                Text("Word Count", color = TextWhite, fontWeight = FontWeight.SemiBold)
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    WordCountRow("Words", wordCount)
                                    WordCountRow("Characters", charCount)
                                    WordCountRow("Characters (no spaces)", charNoSpaceCount)
                                    WordCountRow("Lines", lineCount)
                                    WordCountRow("Paragraphs", paragraphCount)
                                }
                            },
                            confirmButton = {
                                Button(onClick = { showWordCountDialog = false }) {
                                    Text("OK")
                                }
                            },
                            containerColor = SurfaceDark
                        )
                    }

                    // Search & Replace Bottom Sheet
                    if (showSearchReplaceSheet) {
                        SearchReplaceBottomSheet(
                            content = content,
                            initialMode = searchReplaceInitialMode,
                            onDismiss = {
                                showSearchReplaceSheet = false
                                // Clear all highlights when sheet is dismissed
                                searchMatchRanges = emptyList()
                                currentSearchMatchIndex = 0
                                replaceHighlight = null
                            },
                            onReplace = { newContent ->
                                viewModel.updateContent(newContent)
                                textFieldValue = TextFieldValue(
                                    text = newContent,
                                    selection = TextRange(newContent.length)
                                )
                            },
                            onMatchesChanged = { matches, currentIndex ->
                                searchMatchRanges = matches
                                currentSearchMatchIndex = currentIndex
                            },
                            onReplaceHighlight = { highlight ->
                                replaceHighlight = highlight
                            }
                        )
                    }
                }
            }
        }
    }
}
