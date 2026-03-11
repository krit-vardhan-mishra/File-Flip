package com.just_for_fun.fileflip.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.just_for_fun.fileflip.data.DemoFilesData
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.ui.viewmodels.DashboardViewModel
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale
import com.just_for_fun.fileflip.ui.util.FileIconHelper
import android.util.Log
import com.just_for_fun.fileflip.ui.theme.LocalAppColors

// Colors - theme-aware
private val PrimaryBlue: Color @Composable get() = LocalAppColors.current.primaryBlue
private val BackgroundDark: Color @Composable get() = LocalAppColors.current.background
private val SurfaceDark: Color @Composable get() = LocalAppColors.current.surface
private val TextWhite: Color @Composable get() = LocalAppColors.current.textPrimary
private val TextGray: Color @Composable get() = LocalAppColors.current.textSecondary

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val files by viewModel.files.collectAsState()
    val importedFiles by viewModel.importedFiles.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showTemplatesBottomSheet by remember { mutableStateOf(false) }
    var showFileTypeSelectionBottomSheet by remember { mutableStateOf(false) }
    var showFileActionsBottomSheet by remember { mutableStateOf(false) }
    var selectedFileForActions by remember { mutableStateOf<MarkdownFile?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        // Handle selected files
        var successCount = 0
        uris.forEach { uri ->
            // Filter out images and videos if OpenMultipleDocuments didn't catch them all (though it should with our input)
            // But OpenMultipleDocuments takes input as string array in launch.
            
            // Copy file to app storage
            val fileName = getFileNameFromUri(context, uri)
            if (fileName != null) {
                if (copyFileToAppStorage(context, uri, fileName)) {
                    successCount++
                    // Add to imported files list
                    val internalFile = java.io.File(context.filesDir, fileName)
                    val importedFile = MarkdownFile(
                        name = fileName,
                        path = internalFile.absolutePath,
                        content = "", // Content not needed for listing
                        lastModified = System.currentTimeMillis()
                    )
                    viewModel.addImportedFile(importedFile)
                }
            }
        }
        
        if (successCount > 0) {
            viewModel.loadFiles()
            android.widget.Toast.makeText(context, "Imported $successCount files", android.widget.Toast.LENGTH_SHORT).show()
        } else if (uris.isNotEmpty()) {
             android.widget.Toast.makeText(context, "Failed to import files", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    var showRenameDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BackgroundDark,
                drawerContentColor = TextWhite
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "FileFlip Menu",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
                HorizontalDivider(color = TextGray.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "RECENT FILES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextGray,
                    modifier = Modifier.padding(horizontal = 28.dp)
                )
                Spacer(Modifier.height(12.dp))
                if (files.isEmpty()) {
                    Text("No recent files", color = TextGray, modifier = Modifier.padding(horizontal = 28.dp))
                } else {
                    files.take(5).forEach { file ->
                        val (fileIcon, fileIconColor) = FileIconHelper.getIconAndColor(file.name.substringAfterLast(".", "").lowercase())
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                                .clickable {
                                    scope.launch { drawerState.close() }
                                    Log.d("FileFlip", "DashboardScreen: Recent file clicked - ${file.name}, path: ${file.path}")
                                    val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                                    navController.navigate("editor/$encodedPath")
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(fileIconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = fileIcon,
                                        contentDescription = null,
                                        tint = fileIconColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(file.lastModified),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextGray
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                HorizontalDivider(color = TextGray.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("settings")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                        unselectedContainerColor = Color.Transparent,
                        selectedIconColor = PrimaryBlue,
                        unselectedIconColor = TextGray,
                        selectedTextColor = TextWhite,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("about")
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.Rounded.Info, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                        unselectedContainerColor = Color.Transparent,
                        selectedIconColor = PrimaryBlue,
                        unselectedIconColor = TextGray,
                        selectedTextColor = TextWhite,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationDrawerItem(
                    label = { Text("Visit Website") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fileflip.vercel.app"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.Rounded.Language, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                        unselectedContainerColor = Color.Transparent,
                        selectedIconColor = PrimaryBlue,
                        unselectedIconColor = TextGray,
                        selectedTextColor = TextWhite,
                        unselectedTextColor = TextGray
                    )
                )
                NavigationDrawerItem(
                    label = { Text("GitHub Repository") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/krit-vardhan-mishra/File-Flip"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    icon = { Icon(Icons.Rounded.Code, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.1f),
                        unselectedContainerColor = Color.Transparent,
                        selectedIconColor = PrimaryBlue,
                        unselectedIconColor = TextGray,
                        selectedTextColor = TextWhite,
                        unselectedTextColor = TextGray
                    )
                )
            }
        }
    ) {
        // Main Container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
        ) {
            // Content Scrollable Area
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Status Bar Padding
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))
            Spacer(modifier = Modifier.height(16.dp))

            // --- Top Navigation ---
            TopNavigationBar(
                onMenuClick = {
                    scope.launch { drawerState.open() }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Hero Section (Create) ---
            HeroCreateSection(
                onCreateClick = { showFileTypeSelectionBottomSheet = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Quick Actions ---
            QuickActionsGrid(
                onImportClick = { 
                    filePickerLauncher.launch(
                        arrayOf("application/pdf", "text/*", "application/json", "application/xml", "text/csv")
                    ) 
                },
                onTemplatesClick = { showTemplatesBottomSheet = true }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Imported Files Section ---
            if (importedFiles.isNotEmpty()) {
                Text(
                    text = "JUST IMPORTED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = PrimaryBlue,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    importedFiles.forEach { file ->
                        val fileExtension = file.name.substringAfterLast(".", "").lowercase()
                        val (fileIcon, fileIconColor) = FileIconHelper.getIconAndColor(fileExtension)

                        
                        // Reuse FileListItem or similar card logic
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                     Log.d("FileFlip", "DashboardScreen: Imported file clicked - ${file.name}, path: ${file.path}")
                                     val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                                     val route = if (fileExtension == "pdf") "preview/$encodedPath" else "editor/$encodedPath"
                                     navController.navigate(route)
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)) // Slightly different border for imported
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(fileIconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = fileIcon,
                                        contentDescription = null,
                                        tint = fileIconColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Imported just now",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PrimaryBlue
                                    )
                                }
                                IconButton(onClick = {
                                    selectedFileForActions = file
                                    showFileActionsBottomSheet = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.MoreVert,
                                        contentDescription = "More",
                                        tint = TextGray
                                    )
                                }
                            }
                        }
                    }
                }
            } 
            
            // --- Recent Files Header ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT FILES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextGray
                )
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryBlue,
                    modifier = Modifier.clickable { navController.navigate("file_explorer?tab=recent") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Files List ---
            // Using LazyColumn here but wrapping in box to handle scroll correctly
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp), // Space for floating nav
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (files.isEmpty()) {
                    item {
                        Text("No files found", color = TextGray, modifier = Modifier.padding(top = 20.dp))
                    }
                }
                items(files) { file ->
                    val fileExtension = file.name.substringAfterLast(".", "").lowercase()
                    val (fileIcon, fileIconColor) = FileIconHelper.getIconAndColor(fileExtension)

                    FileListItem(
                        name = file.name,
                        date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(file.lastModified),
//                        size = "${file.length()` / 1024} KB", // Simple size calc
                        size = "0 KB",
                        icon = fileIcon,
                        iconColor = fileIconColor,
                        onClick = {
                            Log.d("FileFlip", "DashboardScreen: File clicked - ${file.name}, path: ${file.path}")
                            val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                            navController.navigate("editor/$encodedPath")
                        },
                        onMoreClick = {
                            selectedFileForActions = file
                            showFileActionsBottomSheet = true
                        }
                    )
                }
            }
        }

            // --- Bottom Navigation Bar ---
            FloatingBottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                navController = navController
            )
        }
    }

    // Bottom Sheets
    if (showTemplatesBottomSheet) {
        TemplatesBottomSheet(
            onDismiss = { showTemplatesBottomSheet = false },
            onTemplateSelected = { content, name ->
                viewModel.createNewFile(name, content)
                showTemplatesBottomSheet = false
            }
        )
    }

    if (showFileTypeSelectionBottomSheet) {
        FileTypeSelectionBottomSheet(
            onDismiss = { showFileTypeSelectionBottomSheet = false },
            onFileTypeSelected = { type, content ->
                val fileName = "new_file.$type"
                viewModel.createNewFile(fileName, content)
                showFileTypeSelectionBottomSheet = false
            }
        )
    }

    if (showFileActionsBottomSheet) {
        FileActionsBottomSheet(
            file = selectedFileForActions,
            onDismiss = {
                showFileActionsBottomSheet = false
                selectedFileForActions = null
            },
            onRename = {
                showRenameDialog = true
                showFileActionsBottomSheet = false
            },
            onDelete = {
                selectedFileForActions?.let { file ->
                    viewModel.deleteFile(file.path)
                    // Also remove from imported if it's there
                    viewModel.removeImportedFile(file)
                }
                showFileActionsBottomSheet = false
                selectedFileForActions = null
            },
            onRemoveFromList = {
                 selectedFileForActions?.let { file ->
                    viewModel.removeImportedFile(file)
                }
                showFileActionsBottomSheet = false
                selectedFileForActions = null
            },
            isImported = importedFiles.any { it.path == selectedFileForActions?.path }
        )
    }
    
    if (showRenameDialog && selectedFileForActions != null) {
        RenameFileDialog(
            file = selectedFileForActions!!,
            onDismiss = { 
                showRenameDialog = false 
                selectedFileForActions = null
            },
            onRename = { newName ->
                selectedFileForActions?.let { file ->
                    viewModel.renameFile(file.path, newName)
                    // If it was imported, we need to update that list too? 
                    // ViewModel's renameFile reloads 'files', but 'importedFiles' might hold old ref.
                    // For simplicity, we just reload main files. Imported files list usually stays as 'session history'.
                    // Ideally, we should update imported list too if we want to reflect rename there.
                    // Let's rely on viewModel reloading 'files' for now, and maybe clear it from imported if complex?
                    // Actually, let's just update the list in VM manually if needed, but for now basic Rename is requested.
                }
                showRenameDialog = false
                selectedFileForActions = null
            }
        )
    }
}

@Composable
fun TemplatesBottomSheet(onDismiss: () -> Unit, onTemplateSelected: (String, String) -> Unit) {
    // Full screen modal
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Choose Template",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Add, // Using Add rotated as close
                            contentDescription = "Close",
                            tint = TextGray,
                            modifier = Modifier.rotate(45f)
                        )
                    }
                }

                // Templates Grid
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    val templates = DemoFilesData.getAllForTemplates().map { demoFile ->
                        TemplateData(
                            name = demoFile.name,
                            type = demoFile.extension,
                            icon = demoFile.icon,
                            iconColor = demoFile.iconColor,
                            content = demoFile.content,
                            preview = demoFile.preview
                        )
                    }

                    items(templates) { template ->
                        TemplateCard(
                            template = template,
                            onSelect = {
                                onTemplateSelected(template.content, "template.${template.type}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileTypeSelectionBottomSheet(onDismiss: () -> Unit, onFileTypeSelected: (String, String) -> Unit) {
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
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Choose File Type",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Add, // Using Add rotated as close
                        contentDescription = "Close",
                        tint = TextGray,
                        modifier = Modifier.rotate(45f)
                    )
                }
            }

            // File Types List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val fileTypes = listOf(
                    Triple("md", "Markdown Document", "# New Markdown File\n\nStart writing..."),
                    Triple("json", "JSON File", "{\n  \"name\": \"New File\",\n  \"data\": []\n}"),
                    Triple("yaml", "YAML File", "# New YAML File\nname: New File\ndata: []"),
                    Triple("xml", "XML File", "<?xml version=\"1.0\"?>\n<root>\n  <item>New File</item>\n</root>"),
                    Triple("txt", "Text File", "New text file content..."),
                    Triple("html", "HTML Page", "<html>\n<body>\n  <h1>New HTML Page</h1>\n</body>\n</html>"),
                    Triple("log", "Log File", "[INFO] New log file created"),
                    Triple("csv", "CSV File", "Name,Value\nNew Item,0")
                )
                fileTypes.forEach { (type, name, content) ->
                    androidx.compose.material3.TextButton(
                        onClick = { onFileTypeSelected(type, content) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$name (.$type)", color = TextWhite, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Select",
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionsBottomSheet(
    file: MarkdownFile?,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onRemoveFromList: () -> Unit,
    isImported: Boolean = false
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
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "File Actions",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Add, // Using Add rotated as close
                        contentDescription = "Close",
                        tint = TextGray,
                        modifier = Modifier.rotate(45f)
                    )
                }
            }

            // File Info
            if (file != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (fileIcon, fileIconColor) = FileIconHelper.getIconAndColor(file.name.substringAfterLast(".", "").lowercase())
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(fileIconColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = fileIcon,
                                contentDescription = null,
                                tint = fileIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(file.lastModified),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                androidx.compose.material3.TextButton(
                    onClick = onRename,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = "Rename",
                            tint = TextGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Rename file", color = TextWhite, modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Rename",
                            tint = PrimaryBlue,
                            modifier = Modifier.rotate(45f)
                        )
                    }
                }

                androidx.compose.material3.TextButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Delete file", color = Color(0xFFE53E3E), modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Delete",
                            tint = Color(0xFFE53E3E),
                            modifier = Modifier.rotate(45f)
                        )
                    }
                }

                if (isImported) {
                    androidx.compose.material3.TextButton(
                        onClick = onRemoveFromList,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Remove, // Ensure Remove icon is available or import it
                                contentDescription = "Remove from list",
                                tint = TextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Remove from list", color = TextGray, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Remove",
                                tint = TextGray,
                                modifier = Modifier.rotate(45f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
data class TemplateData(
    val name: String,
    val type: String,
    val icon: ImageVector,
    val iconColor: Color,
    val content: String,
    val preview: String
)

@Composable
fun TemplateCard(template: TemplateData, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with icon and name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(template.iconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = template.icon,
                        contentDescription = null,
                        tint = template.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextWhite
                    )
                    Text(
                        text = ".${template.type} file",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Select template",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Preview section
            Text(
                text = "Preview:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = TextGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextGray.copy(alpha = 0.1f))
            ) {
                Text(
                    text = template.preview,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = TextWhite.copy(alpha = 0.9f),
                    modifier = Modifier.padding(12.dp),
                    maxLines = 8,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
@Composable
fun TopNavigationBar(onMenuClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Menu Button on left
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(40.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .clickable { onMenuClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = PrimaryBlue
            )
        }

        // Title in center
        Text(
            text = "FileFlip",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
    }
}

@Composable
fun HeroCreateSection(onCreateClick: () -> Unit) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clickable { onCreateClick() }
        ) {
            // Glow Effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp) // Slight inset for glow to bleed out
                    .blur(20.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PrimaryBlue.copy(alpha = 0.4f), Color(0xFF2563EB).copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            )

            // Card Content
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(10.dp, CircleShape, spotColor = PrimaryBlue)
                            .background(PrimaryBlue, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = TextWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Create New File",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Edit and convert to PDF",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fileflip.vercel.app")) // Replace with actual URL when deployed
                context.startActivity(intent)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Visit Website",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
        }
    }
}

@Composable
fun QuickActionsGrid(onImportClick: () -> Unit, onTemplatesClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickActionButton(
            icon = Icons.Rounded.CloudUpload,
            label = "Import",
            modifier = Modifier.weight(1f),
            onClick = onImportClick
        )
        QuickActionButton(
            icon = Icons.Rounded.Description,
            label = "Templates",
            modifier = Modifier.weight(1f),
            onClick = onTemplatesClick
        )
    }
}

@Composable
fun QuickActionButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = TextWhite
            )
        }
    }
}

@Composable
fun FileListItem(
    name: String,
    date: String,
    size: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Rounded.Article,
    iconColor: Color = FileIconHelper.IconOrange,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File Icon Box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$date • $size", // Formatted date string
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = TextGray
                )
            }

            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = "More",
                    tint = TextGray
                )
            }
        }
    }
}

@Composable
fun FloatingBottomBar(modifier: Modifier = Modifier, navController: NavController) {
    // Glassmorphic-ish Bottom Bar
    Box(
        modifier = modifier
            .padding(horizontal = 40.dp) // Indent to make it float
            .height(64.dp)
            .fillMaxWidth()
            .shadow(16.dp, CircleShape, ambientColor = Color.Black.copy(alpha = 0.5f))
            .background(SurfaceDark.copy(alpha = 0.95f), CircleShape) // Slightly opaque
            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                icon = Icons.Rounded.GridView,
                label = "Home",
                isSelected = true,
                onClick = { /* Already on home */ }
            )
            BottomBarItem(
                icon = Icons.Rounded.FolderOpen,
                label = "Library",
                isSelected = false,
                onClick = { navController.navigate("file_explorer") }
            )
            BottomBarItem(
                icon = Icons.AutoMirrored.Rounded.Article,
                label = "Editor",
                isSelected = false,
                onClick = { navController.navigate("editor/empty") }
            )
            // BottomBarItem(
            //     icon = Icons.Rounded.AutoAwesome,
            //     label = "Pro",
            //     isSelected = false,
            //     onClick = { navController.navigate("pro") }
            // )
            BottomBarItem(
                icon = Icons.Rounded.Settings,
                label = "Settings",
                isSelected = false,
                onClick = { navController.navigate("settings") }
            )
        }
    }
}

// Helper functions for file import
// Helper functions for file import
fun getFileNameFromUri(context: android.content.Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "imported_file_${System.currentTimeMillis()}"
}

fun copyFileToAppStorage(context: android.content.Context, uri: Uri, fileName: String): Boolean {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val outputDir = context.filesDir
        // Create a unique filename if it already exists to avoid overwriting (optional, but good practice)
        // For now, we'll overwrite as per original logic implies, or maybe we should handle duplicates? 
        // Let's stick to simple overwrite for now to match expected behavior or just basic copy.
        val outputFile = java.io.File(outputDir, fileName)
        
        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

@Composable
fun BottomBarItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PrimaryBlue else TextGray,
            modifier = Modifier.size(24.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = PrimaryBlue
            )
        }
    }
}
@Composable
fun RenameFileDialog(
    file: MarkdownFile,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(file.name) }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename File", color = TextWhite) },
        text = {
            Column {
                Text("Enter new name:", color = TextGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onRename(newName) }) {
                Text("Rename", color = PrimaryBlue)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextGray)
            }
        },
        containerColor = SurfaceDark,
        textContentColor = TextWhite,
        titleContentColor = TextWhite
    )
}