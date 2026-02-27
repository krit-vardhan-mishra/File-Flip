package com.just_for_fun.fileflip.ui.screens

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material3.TextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import android.net.Uri
import android.util.Log
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.activity.ComponentActivity
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.ui.viewmodels.EditorViewModel
import java.io.File
import kotlinx.coroutines.launch

// Helper function to get file icon and color based on extension
fun getFileIconAndColorEditorScreen(extension: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    return when (extension.lowercase()) {
        ".md", ".markdown" -> Pair(Icons.AutoMirrored.Rounded.Article, IconOrange)
        ".json" -> Pair(Icons.Rounded.Code, IconEmerald)
        ".xml" -> Pair(Icons.Rounded.Code, Color(0xFF795548)) // Brown
        ".yaml", ".yml" -> Pair(Icons.Rounded.Settings, Color(0xFF9C27B0)) // Purple
        ".html" -> Pair(Icons.Rounded.Description, Color(0xFFE91E63)) // Pink
        ".css" -> Pair(Icons.Rounded.Code, Color(0xFF2196F3)) // Blue
        ".js", ".javascript" -> Pair(Icons.Rounded.Code, Color(0xFFFFD600)) // Yellow
        ".txt", ".text" -> Pair(Icons.Rounded.Description, TextGray)
        ".log" -> Pair(Icons.Rounded.Description, Color(0xFF607D8B)) // Blue Grey
        ".csv" -> Pair(Icons.Rounded.Description, Color(0xFF4CAF50)) // Green
        ".pdf" -> Pair(Icons.Rounded.Description, Color(0xFFF44336)) // Red
        ".doc", ".docx" -> Pair(Icons.Rounded.Description, Color(0xFF1976D2)) // Blue
        ".xls", ".xlsx" -> Pair(Icons.Rounded.Description, Color(0xFF388E3C)) // Green
        ".ppt", ".pptx" -> Pair(Icons.Rounded.Description, Color(0xFFFF5722)) // Orange
        ".zip", ".rar", ".7z" -> Pair(Icons.Rounded.FolderOpen, Color(0xFF795548)) // Brown
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp" -> Pair(Icons.Rounded.AutoAwesome, Color(0xFF9C27B0)) // Purple
        ".mp3", ".wav", ".flac", ".aac" -> Pair(Icons.Rounded.AutoAwesome, Color(0xFFE91E63)) // Pink
        ".mp4", ".avi", ".mkv", ".mov" -> Pair(Icons.Rounded.AutoAwesome, Color(0xFF673AB7)) // Purple
        else -> Pair(Icons.AutoMirrored.Rounded.Article, IconOrange) // Default
    }
}

// Design Colors
private val PrimaryBlue = Color(0xFF0DA6F2)
private val BackgroundDark = Color(0xFF101C22)
private val SurfaceDark = Color(0xFF1A2830)
private val GutterColor = Color(0xFF152329) // Slightly lighter than background
private val TextWhite = Color(0xFFF1F5F9)
private val TextGray = Color(0xFF94A3B8)
private val DividerColor = Color(0xFF0DA6F2).copy(alpha = 0.1f)
private val IconOrange = Color(0xFFFF9F1C) // Approx for "article" icon
private val IconEmerald = Color(0xFF10B981) // Approx for "source" icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    navController: NavController,
    filePath: String
) {
    val context = LocalContext.current
    
    // Scope ViewModel to activity so it persists across navigation
    val viewModel: EditorViewModel = hiltViewModel(viewModelStoreOwner = context as ComponentActivity)
    
    val currentFile by viewModel.currentFile.collectAsState()
    val content by viewModel.content.collectAsState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue()) }
    
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceDark
            ) {
                // Drawer Header
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
                        "MarkPDF",
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
    ) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Column {
                // Main Header
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "MarkPDF",
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
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
                        IconButton(onClick = { showAttachBottomSheet = true }) {
                            Icon(Icons.Outlined.AttachFile, contentDescription = "Attach", tint = TextGray)
                        }
                        IconButton(onClick = { viewModel.saveFile() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = PrimaryBlue)
                        }
                        IconButton(onClick = { 
                            searchReplaceInitialMode = "search"
                            showSearchReplaceSheet = true 
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray)
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
            if (filePath != "empty") {
                FloatingActionButton(
                    onClick = {
                        viewModel.saveFile()
                        val encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
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
                SelectionContainer {
                    TextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            viewModel.updateContent(newValue.text)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        textStyle = TextStyle(
                            color = TextWhite,
                            fontSize = 15.sp,
                            fontFamily = fontFamily,
                            lineHeight = 22.sp
                        ),
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
    if (showAttachBottomSheet) {
        AttachBottomSheet(
            currentFile = currentFile,
            onDismiss = { showAttachBottomSheet = false },
            onSave = {
                if (currentFile != null) {
                    viewModel.saveFile()
                } else {
                    showSaveAsBottomSheet = true
                }
                showAttachBottomSheet = false
            },
            onOpenFile = {
                filePickerLauncher.launch("*/*")
                showAttachBottomSheet = false
            },
            onCreateNewFile = {
                showFileTypeSelectionBottomSheet = true
                showAttachBottomSheet = false
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
    
    // More Options Bottom Sheet (replaces dropdown menu)
    if (showMoreOptionsBottomSheet) {
        MoreOptionsBottomSheet(
            onDismiss = { showMoreOptionsBottomSheet = false },
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
            onDismiss = { showSearchReplaceSheet = false },
            onReplace = { newContent ->
                viewModel.updateContent(newContent)
                textFieldValue = TextFieldValue(
                    text = newContent,
                    selection = TextRange(newContent.length)
                )
            }
        )
    }
    } // End ModalNavigationDrawer
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditorTab(
    title: String, 
    isActive: Boolean,
    hasUnsavedChanges: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Article,
                contentDescription = null,
                tint = if (isActive) PrimaryBlue else TextGray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (hasUnsavedChanges) "$title *" else title,
                color = if (isActive) PrimaryBlue else TextGray,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
            )
        }
        if (isActive) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(PrimaryBlue)
            )
        }
    }
}

@Composable
fun EditorToolIcon(icon: ImageVector, contentDescription: String, isSelected: Boolean = false, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isSelected) PrimaryBlue else TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

// File Type and Toolbar Tools
enum class FileType {
    MARKDOWN, JSON, YAML, XML, HTML, TEXT, LOG, CSV, UNKNOWN;

    companion object {
        fun fromExtension(extension: String): FileType = when (extension.lowercase()) {
            "md" -> MARKDOWN
            "json" -> JSON
            "yaml", "yml" -> YAML
            "xml" -> XML
            "html", "htm" -> HTML
            "txt" -> TEXT
            "log" -> LOG
            "csv" -> CSV
            else -> UNKNOWN
        }
    }

    fun getToolbarTools(
        onShowValidation: (title: String, message: String, isError: Boolean) -> Unit = { _, _, _ -> },
        onShowWordCount: () -> Unit = {},
        onShowFindReplace: () -> Unit = {}
    ): List<EditorTool> = when (this) {
        MARKDOWN -> listOf(
            EditorTool(Icons.Default.FormatBold, "Insert Bold") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content****")
            },
            EditorTool(Icons.Default.FormatItalic, "Insert Italic") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content**")
            },
            EditorTool(Icons.Default.Link, "Insert Link") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content[](url)")
            },
            EditorTool(Icons.Default.Code, "Insert Code Block") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content```\n\n```")
            },
            EditorTool(Icons.AutoMirrored.Filled.FormatListBulleted, "Insert List") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content\n- ")
            },
            EditorTool(Icons.Outlined.Image, "Insert Image") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content![](image-url)")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        JSON -> listOf(
            EditorTool(Icons.Default.Code, "Format JSON") { viewModel, content, selectedText, range ->
                try {
                    val json = org.json.JSONObject(content)
                    viewModel.updateContent(json.toString(2))
                } catch (e: Exception) {
                    // Invalid JSON, keep as is
                }
            },
            EditorTool(Icons.Default.Description, "Validate JSON") { _, content, _, _ ->
                try {
                    org.json.JSONObject(content)
                    onShowValidation("JSON Validation", "✓ Valid JSON - No errors found.", false)
                } catch (e: Exception) {
                    try {
                        org.json.JSONArray(content)
                        onShowValidation("JSON Validation", "✓ Valid JSON Array - No errors found.", false)
                    } catch (e2: Exception) {
                        onShowValidation("JSON Validation Error", "✗ Invalid JSON:\n${e.message}", true)
                    }
                }
            },
            EditorTool(Icons.Default.Code, "Add Object") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content{\n  \"key\": \"value\"\n}")
            },
            EditorTool(Icons.Default.Code, "Add Array") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content[\n  \"item1\",\n  \"item2\"\n]")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        YAML -> listOf(
            EditorTool(Icons.Default.Code, "Format YAML") { viewModel, content, selectedText, range ->
                viewModel.updateContent("# YAML Configuration\nkey: value\nnested:\n  subkey: subvalue\n")
            },
            EditorTool(Icons.Default.Description, "Validate YAML") { _, content, _, _ ->
                try {
                    val yaml = org.yaml.snakeyaml.Yaml()
                    yaml.load<Any>(content)
                    onShowValidation("YAML Validation", "✓ Valid YAML - No errors found.", false)
                } catch (e: Exception) {
                    onShowValidation("YAML Validation Error", "✗ Invalid YAML:\n${e.message}", true)
                }
            },
            EditorTool(Icons.Default.Code, "Add Section") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content\n# New Section\nsection:\n  key: value")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        XML -> listOf(
            EditorTool(Icons.Default.Code, "Format XML") { viewModel, content, selectedText, range ->
                viewModel.updateContent("<?xml version=\"1.0\"?>\n<root>\n  <element>${content}</element>\n</root>")
            },
            EditorTool(Icons.Default.Description, "Validate XML") { _, content, _, _ ->
                try {
                    val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    val builder = factory.newDocumentBuilder()
                    builder.parse(org.xml.sax.InputSource(java.io.StringReader(content)))
                    onShowValidation("XML Validation", "✓ Valid XML - No errors found.", false)
                } catch (e: Exception) {
                    onShowValidation("XML Validation Error", "✗ Invalid XML:\n${e.message}", true)
                }
            },
            EditorTool(Icons.Default.Code, "Add Element") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<element></element>")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        HTML -> listOf(
            EditorTool(Icons.Default.FormatBold, "Insert Bold") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<strong></strong>")
            },
            EditorTool(Icons.Default.FormatItalic, "Insert Italic") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<em></em>")
            },
            EditorTool(Icons.Default.Link, "Insert Link") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<a href=\"url\"></a>")
            },
            EditorTool(Icons.Default.Code, "Insert Code") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<code></code>")
            },
            EditorTool(Icons.Default.Code, "Insert Paragraph") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content<p></p>")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        CSV -> listOf(
            EditorTool(Icons.Default.Description, "Format CSV") { viewModel, content, _, _ ->
                // Auto-align CSV columns by padding cells
                try {
                    val rows = content.split("\n").filter { it.isNotBlank() }
                    if (rows.isNotEmpty()) {
                        val parsedRows = rows.map { row ->
                            row.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                                .map { it.trim().removeSurrounding("\"") }
                        }
                        val colCount = parsedRows.maxOf { it.size }
                        val colWidths = (0 until colCount).map { col ->
                            parsedRows.maxOf { row -> (row.getOrNull(col) ?: "").length }
                        }
                        val formatted = parsedRows.joinToString("\n") { row ->
                            (0 until colCount).joinToString(", ") { col ->
                                (row.getOrNull(col) ?: "").padEnd(colWidths[col])
                            }
                        }
                        viewModel.updateContent(formatted)
                    }
                } catch (e: Exception) {
                    // Keep content as-is on error
                }
            },
            EditorTool(Icons.Default.Code, "Add Row") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content,New Value\n")
            },
            EditorTool(Icons.Default.Code, "Add Column") { viewModel, content, selectedText, range ->
                viewModel.updateContent("$content,New Column")
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
        TEXT, LOG, UNKNOWN -> listOf(
            EditorTool(Icons.Default.Description, "Word Count") { _, _, _, _ ->
                onShowWordCount()
            },
            EditorTool(Icons.Default.Code, "Line Numbers") { viewModel, content, _, _ ->
                // Add/remove line numbers prefix to each line
                val lines = content.lines()
                val hasLineNumbers = lines.firstOrNull()?.matches(Regex("^\\d+[:|.]\\s.*")) == true
                val result = if (hasLineNumbers) {
                    lines.joinToString("\n") { it.replace(Regex("^\\d+[:|.]\\s"), "") }
                } else {
                    lines.mapIndexed { index, line -> "${index + 1}: $line" }.joinToString("\n")
                }
                viewModel.updateContent(result)
            },
            EditorTool(Icons.Default.Code, "Find & Replace") { _, _, _, _ ->
                onShowFindReplace()
            },
            EditorTool(Icons.AutoMirrored.Filled.Undo, "Undo") { viewModel, content, selectedText, range ->
                viewModel.undo()
            },
            EditorTool(Icons.AutoMirrored.Filled.Redo, "Redo") { viewModel, content, selectedText, range ->
                viewModel.redo()
            }
        )
    }
    
    // Formatting tools for selected text - context-aware based on file type
    fun getFormattingTools(): List<EditorTool> = when (this) {
        MARKDOWN -> listOf(
            EditorTool(Icons.Default.FormatBold, "Bold") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before**$selectedText**$after")
                }
            },
            EditorTool(Icons.Default.FormatItalic, "Italic") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before*$selectedText*$after")
                }
            },
            EditorTool(Icons.Default.FormatStrikethrough, "Strikethrough") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before~~$selectedText~~$after")
                }
            },
            EditorTool(Icons.Default.Link, "Link") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before[$selectedText](url)$after")
                }
            },
            EditorTool(Icons.Default.Code, "Inline Code") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before`$selectedText`$after")
                }
            },
            EditorTool(Icons.AutoMirrored.Filled.FormatListBulleted, "List") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    val listItems = selectedText.lines().joinToString("\n") { "- $it" }
                    viewModel.updateContent("$before$listItems$after")
                }
            },
            EditorTool(Icons.Default.FormatQuote, "Blockquote") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    val quoted = selectedText.lines().joinToString("\n") { "> $it" }
                    viewModel.updateContent("$before$quoted$after")
                }
            }
        )
        HTML -> listOf(
            EditorTool(Icons.Default.FormatBold, "Bold") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<strong>$selectedText</strong>$after")
                }
            },
            EditorTool(Icons.Default.FormatItalic, "Italic") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<em>$selectedText</em>$after")
                }
            },
            EditorTool(Icons.Default.Link, "Link") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<a href=\"url\">$selectedText</a>$after")
                }
            },
            EditorTool(Icons.Default.Code, "Code") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<code>$selectedText</code>$after")
                }
            }
        )
        JSON -> listOf(
            EditorTool(Icons.Default.Code, "Format") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    try {
                        val json = org.json.JSONObject(selectedText.trim())
                        val before = content.take(range.start)
                        val after = content.substring(range.end)
                        viewModel.updateContent("$before${json.toString(2)}$after")
                    } catch (e: Exception) {
                        // Not valid JSON, keep as is
                    }
                }
            },
            EditorTool(Icons.Default.Code, "Wrap Object") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before{\"value\": $selectedText}$after")
                }
            },
            EditorTool(Icons.Default.Code, "Wrap Array") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before[$selectedText]$after")
                }
            }
        )
        XML -> listOf(
            EditorTool(Icons.Default.Code, "Wrap Element") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<element>$selectedText</element>$after")
                }
            },
            EditorTool(Icons.Default.Code, "Add Attribute") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before<element attribute=\"value\">$selectedText</element>$after")
                }
            }
        )
        YAML -> listOf(
            EditorTool(Icons.Default.Code, "Comment") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    val commented = selectedText.lines().joinToString("\n") { "# $it" }
                    viewModel.updateContent("$before$commented$after")
                }
            },
            EditorTool(Icons.Default.Code, "Make Key") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before$selectedText: value$after")
                }
            }
        )
        TEXT, LOG, CSV, UNKNOWN -> listOf(
            EditorTool(Icons.Default.Code, "Uppercase") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before${selectedText.uppercase()}$after")
                }
            },
            EditorTool(Icons.Default.Code, "Lowercase") { viewModel, content, selectedText, range ->
                if (range != null && selectedText.isNotEmpty()) {
                    val before = content.take(range.start)
                    val after = content.substring(range.end)
                    viewModel.updateContent("$before${selectedText.lowercase()}$after")
                }
            }
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorFileTypeSelectionBottomSheet(onDismiss: () -> Unit, onFileTypeSelected: (String, String) -> Unit) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Create New File",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // File Types List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fileTypes = listOf(
                    "Markdown" to "# New Markdown File\n\nWrite your content here...",
                    "JSON" to "{\n  \"key\": \"value\"\n}",
                    "YAML" to "key: value\nnested:\n  subkey: subvalue",
                    "XML" to "<?xml version=\"1.0\"?>\n<root>\n  <element>content</element>\n</root>",
                    "HTML" to "<!DOCTYPE html>\n<html>\n<head>\n  <title>Title</title>\n</head>\n<body>\n  <h1>Hello World</h1>\n</body>\n</html>",
                    "Text" to "",
                    "Log" to "",
                    "CSV" to "Column1,Column2,Column3\nValue1,Value2,Value3"
                )

                fileTypes.forEach { (type, content) ->
                    FileTypeItem(
                        type = type,
                        onClick = {
                            onFileTypeSelected(type, content)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun FileTypeItem(type: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (icon, color) = getFileIconAndColorEditorScreen(".$type".lowercase())
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = type,
            color = TextWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    onDismiss: () -> Unit,
    onShareAsPDF: () -> Unit,
    onShareFile: () -> Unit,
    onPrint: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                "More Options",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            androidx.compose.material3.HorizontalDivider(
                color = DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Share as PDF
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onShareAsPDF)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Share as PDF",
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                    Text(
                        "Convert and share as PDF",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
            
            // Share File
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onShareFile)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = IconOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Share File",
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                    Text(
                        "Share file with other apps",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
            
            // Print
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onPrint)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = IconEmerald,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Print",
                        fontSize = 16.sp,
                        color = TextWhite
                    )
                    Text(
                        "Print document",
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachBottomSheet(
    currentFile: MarkdownFile?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onOpenFile: () -> Unit,
    onCreateNewFile: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "File Options",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Options
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Only show Save option if there's a file opened
                if (currentFile != null) {
                    AttachOptionItem(
                        icon = Icons.Default.Save,
                        title = "Save",
                        subtitle = "Save changes to ${currentFile.name}",
                        onClick = onSave
                    )
                } else {
                    AttachOptionItem(
                        icon = Icons.Default.Save,
                        title = "Save As",
                        subtitle = "Save to a new file",
                        onClick = onSave
                    )
                }
                AttachOptionItem(
                    icon = Icons.Rounded.FolderOpen,
                    title = "Open File",
                    subtitle = "Open an existing file",
                    onClick = onOpenFile
                )
                AttachOptionItem(
                    icon = Icons.Rounded.Add,
                    title = "Create New File",
                    subtitle = "Create a new file from template",
                    onClick = onCreateNewFile
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AttachOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = TextGray,
                fontSize = 14.sp
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = TextGray,
            modifier = Modifier.size(20.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAsBottomSheet(
    onDismiss: () -> Unit,
    onSaveAs: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Save As",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Choose a location to save the file",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            // Save As button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSaveAs() }
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Select Location",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

data class EditorTool(
    val icon: ImageVector,
    val description: String,
    val action: (EditorViewModel, String, String, TextRange?) -> Unit
)

// Helper functions for file import
fun getFileNameFromUriEditorScreen(context: android.content.Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.getString(nameIndex)
        } else null
    }
}

fun copyFileToAppStorageEditorScreen(context: android.content.Context, uri: Uri, fileName: String) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputDir = context.getExternalFilesDir(null)?.let { File(it, "Files") } ?: return
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputFile = File(outputDir, fileName)
        inputStream?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// --- Word Count Row helper ---
@Composable
fun WordCountRow(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextGray, fontSize = 14.sp)
        Text(
            count.toString(),
            color = PrimaryBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// --- Search & Replace Bottom Sheet ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchReplaceBottomSheet(
    content: String,
    initialMode: String = "search",
    onDismiss: () -> Unit,
    onReplace: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var mode by remember { mutableStateOf(initialMode) } // "search" or "replace"
    var searchText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var useRegex by remember { mutableStateOf(false) }
    var caseSensitive by remember { mutableStateOf(false) }
    var matchCount by remember { mutableStateOf(0) }
    var currentMatchIndex by remember { mutableStateOf(0) }
    var matches by remember { mutableStateOf<List<IntRange>>(emptyList()) }

    // Compute matches whenever search text, content, or options change
    LaunchedEffect(searchText, content, useRegex, caseSensitive) {
        if (searchText.isEmpty()) {
            matchCount = 0
            currentMatchIndex = 0
            matches = emptyList()
        } else {
            try {
                val found = mutableListOf<IntRange>()
                if (useRegex) {
                    val flags = if (caseSensitive) setOf<RegexOption>() else setOf(RegexOption.IGNORE_CASE)
                    Regex(searchText, flags).findAll(content).forEach {
                        found.add(it.range)
                    }
                } else {
                    val searchIn = if (caseSensitive) content else content.lowercase()
                    val searchFor = if (caseSensitive) searchText else searchText.lowercase()
                    var startIndex = 0
                    while (true) {
                        val index = searchIn.indexOf(searchFor, startIndex)
                        if (index < 0) break
                        found.add(index until (index + searchFor.length))
                        startIndex = index + 1
                    }
                }
                matches = found
                matchCount = found.size
                if (currentMatchIndex >= found.size) currentMatchIndex = 0
            } catch (e: Exception) {
                matches = emptyList()
                matchCount = 0
                currentMatchIndex = 0
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Mode selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { mode = "search" },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (mode == "search") PrimaryBlue else BackgroundDark,
                        contentColor = if (mode == "search") Color.White else TextGray
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Search", fontSize = 14.sp)
                }
                Button(
                    onClick = { mode = "replace" },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (mode == "replace") PrimaryBlue else BackgroundDark,
                        contentColor = if (mode == "replace") Color.White else TextGray
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Replace", fontSize = 14.sp)
                }
            }

            // Search field
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search text...", color = TextGray.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = TextWhite, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = BackgroundDark,
                    unfocusedContainerColor = BackgroundDark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Replace field (only in replace mode)
            if (mode == "replace") {
                TextField(
                    value = replaceText,
                    onValueChange = { replaceText = it },
                    placeholder = { Text("Replace with...", color = TextGray.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.FindReplace, contentDescription = null, tint = TextGray) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = TextWhite, fontFamily = FontFamily.Monospace, fontSize = 14.sp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = BackgroundDark,
                        unfocusedContainerColor = BackgroundDark,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Options row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { caseSensitive = !caseSensitive }
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = caseSensitive,
                        onCheckedChange = { caseSensitive = it },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = PrimaryBlue,
                            uncheckedColor = TextGray
                        )
                    )
                    Text("Aa", color = if (caseSensitive) PrimaryBlue else TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { useRegex = !useRegex }
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = useRegex,
                        onCheckedChange = { useRegex = it },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(
                            checkedColor = PrimaryBlue,
                            uncheckedColor = TextGray
                        )
                    )
                    Text(".*", color = if (useRegex) PrimaryBlue else TextGray, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                }
            }

            // Match count and navigation
            if (searchText.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (matchCount > 0) "${currentMatchIndex + 1} of $matchCount match${if (matchCount != 1) "es" else ""}"
                        else "No matches found",
                        color = if (matchCount > 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // Navigation arrows (prev/next match)
                    if (matchCount > 1) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { currentMatchIndex = if (currentMatchIndex > 0) currentMatchIndex - 1 else matchCount - 1 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Previous match", tint = PrimaryBlue,
                                    modifier = Modifier.graphicsLayer(rotationZ = 180f))
                            }
                            IconButton(
                                onClick = { currentMatchIndex = (currentMatchIndex + 1) % matchCount },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = "Next match", tint = PrimaryBlue)
                            }
                        }
                    }
                }
            }

            // Replace action buttons (only in replace mode)
            if (mode == "replace" && searchText.isNotEmpty() && matchCount > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Replace current match (one by one)
                    Button(
                        onClick = {
                            if (matches.isNotEmpty() && currentMatchIndex < matches.size) {
                                val matchRange = matches[currentMatchIndex]
                                val before = content.substring(0, matchRange.first)
                                val after = content.substring(matchRange.last + 1)
                                val newContent = "$before$replaceText$after"
                                onReplace(newContent)
                                // Recalculate will happen via LaunchedEffect, stay on same index
                                if (currentMatchIndex >= matchCount - 1) {
                                    currentMatchIndex = 0
                                }
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = IconOrange,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.NavigateNext, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replace", fontSize = 13.sp)
                    }

                    // Replace All
                    Button(
                        onClick = {
                            val result = try {
                                if (useRegex) {
                                    val flags = if (caseSensitive) setOf<RegexOption>() else setOf(RegexOption.IGNORE_CASE)
                                    content.replace(Regex(searchText, flags), replaceText)
                                } else {
                                    content.replace(searchText, replaceText, ignoreCase = !caseSensitive)
                                }
                            } catch (e: Exception) {
                                content
                            }
                            onReplace(result)
                            onDismiss()
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replace All", fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}