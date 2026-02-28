package com.just_for_fun.fileflip.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.just_for_fun.fileflip.ui.util.FileIconHelper
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.ui.theme.LocalAppColors
import com.just_for_fun.fileflip.ui.viewmodels.FileExplorerViewModel
import java.net.URLEncoder

// Colors derived from Dashboard Design (Theme-Aware)
private val PrimaryBlue @Composable get() = LocalAppColors.current.primaryBlue
private val BackgroundDark @Composable get() = LocalAppColors.current.background
private val SurfaceDark @Composable get() = LocalAppColors.current.surface
private val TextWhite @Composable get() = LocalAppColors.current.textPrimary
private val TextGray @Composable get() = LocalAppColors.current.textSecondary

enum class ExplorerTab {
    Files, Starred, Recent, Settings
}

@Composable
fun FileExplorerScreen(
    navController: NavController, 
    initialTab: String = "files",
    viewModel: FileExplorerViewModel = hiltViewModel()
) {
    val initialExplorerTab = when (initialTab.lowercase()) {
        "starred" -> ExplorerTab.Starred
        "recent" -> ExplorerTab.Recent
        "settings" -> ExplorerTab.Settings
        else -> ExplorerTab.Files
    }
    
    var currentTab by remember { mutableStateOf(initialExplorerTab) }
    var isSearching by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = { 
            ExplorerTopBar(
                navController = navController, 
                currentTab = currentTab,
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                onToggleSearch = { 
                    isSearching = !isSearching 
                    if (!isSearching) viewModel.updateSearchQuery("")
                }
            ) 
        },
        bottomBar = {
            ExplorerBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    if (tab == ExplorerTab.Settings) {
                        navController.navigate("settings")
                    } else {
                        currentTab = tab
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentTab == ExplorerTab.Files) {
                FloatingActionButton(
                    onClick = { navController.navigate("editor/empty") },
                    containerColor = PrimaryBlue,
                    contentColor = BackgroundDark,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = 20.dp,
                            spotColor = PrimaryBlue,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (currentTab) {
                ExplorerTab.Files -> FilesTabContent(navController, viewModel)
                ExplorerTab.Starred -> StarredTabContent(navController)
                ExplorerTab.Recent -> RecentTabContent(navController, viewModel)
                else -> {}
            }
        }
    }
}

@Composable
fun FilesTabContent(navController: NavController, viewModel: FileExplorerViewModel) {
    val files by viewModel.filteredFiles.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val selectedExtensions by viewModel.selectedExtensions.collectAsState()
    
    var showSortFilterSheet by remember { mutableStateOf(false) }
    var showFileActionSheet by remember { mutableStateOf(false) }
    var selectedFileForAction by remember { mutableStateOf<MarkdownFile?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            SectionTitle("FOLDERS")
        }
        items(sampleFolders) { folder ->
            FolderItem(folder, navController)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DOCUMENTS",
                    color = TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = sortOption,
                    color = PrimaryBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showSortFilterSheet = true }
                )
            }
        }
        
        if (files.isEmpty()) {
            item {
                EmptyStateMessage(
                    icon = Icons.Rounded.Search,
                    message = "No documents found"
                )
            }
        } else {
            items(files) { file ->
                val extension = file.name.substringAfterLast(".", "").lowercase()
                val (icon, color) = FileIconHelper.getIconAndColor(extension)
                
                FileItemRow(
                    name = file.name,
                    size = "---",
                    date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(file.lastModified),
                    icon = icon,
                    iconColor = color,
                    onClick = {
                        val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                        navController.navigate("editor/$encodedPath")
                    }, 
                    onMenuClick = {
                        selectedFileForAction = file
                        showFileActionSheet = true
                    }
                )
            }
        }
    }

    if (showFileActionSheet && selectedFileForAction != null) {
        val extension = selectedFileForAction!!.name.substringAfterLast(".", "").lowercase()
        val (icon, color) = FileIconHelper.getIconAndColor(extension)
        FileActionBottomSheet(
            name = selectedFileForAction!!.name,
            size = "---",
            date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(selectedFileForAction!!.lastModified),
            icon = icon,
            iconColor = color,
            onDismiss = { showFileActionSheet = false },
            onDelete = {
                viewModel.deleteFile(selectedFileForAction!!.path)
                showFileActionSheet = false
                selectedFileForAction = null
            },
            onRename = {
                showRenameDialog = true
                showFileActionSheet = false
            }
        )
    }
    
    if (showRenameDialog && selectedFileForAction != null) {
        RenameFileDialog(
            currentName = selectedFileForAction!!.name,
            onDismiss = { 
                showRenameDialog = false 
                selectedFileForAction = null
            },
            onRename = { newName ->
                viewModel.renameFile(selectedFileForAction!!.path, newName)
                showRenameDialog = false
                selectedFileForAction = null
            }
        )
    }

    if (showSortFilterSheet) {
        SortFilterBottomSheet(
            currentSortOption = sortOption,
            selectedExtensions = selectedExtensions,
            onSortOptionSelected = { viewModel.updateSortOption(it) },
            onExtensionsSelected = { viewModel.updateSelectedExtensions(it) },
            onDismiss = { showSortFilterSheet = false }
        )
    }
}

@Composable
fun StarredTabContent(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyStateMessage(
            icon = Icons.Rounded.StarBorder,
            message = "Starred files coming soon"
        )
    }
}

@Composable
fun RecentTabContent(navController: NavController, viewModel: FileExplorerViewModel) {
    val files by viewModel.filteredFiles.collectAsState()
    val recentFiles = remember(files) {
        files.sortedByDescending { it.lastModified }.take(10)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            SectionTitle("RECENTLY MODIFIED")
        }

        if (recentFiles.isEmpty()) {
            item {
                EmptyStateMessage(
                    icon = Icons.Rounded.History,
                    message = "No recent files"
                )
            }
        } else {
            items(recentFiles) { file ->
                val extension = file.name.substringAfterLast(".", "").lowercase()
                val (icon, color) = FileIconHelper.getIconAndColor(extension)
                
                FileItemRow(
                    name = file.name,
                    size = "---",
                    date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(file.lastModified),
                    icon = icon,
                    iconColor = color,
                    onClick = {
                        val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                        navController.navigate("editor/$encodedPath")
                    },
                    onMenuClick = {
                        val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                        navController.navigate("editor/$encodedPath")
                    }
                )
            }
        }
    }
}

@Composable
fun ExplorerTopBar(
    navController: NavController, 
    currentTab: ExplorerTab,
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.95f))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (isSearching) {
                IconButton(onClick = onToggleSearch) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Stop Search",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search files...", color = TextGray) },
                    modifier = Modifier.weight(1f).padding(vertical = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = PrimaryBlue,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )
            } else {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Back",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = when(currentTab) {
                        ExplorerTab.Files -> "Files"
                        ExplorerTab.Starred -> "Starred"
                        ExplorerTab.Recent -> "Recent"
                        else -> "Files"
                    },
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = onToggleSearch) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = PrimaryBlue
                    )
                }
            }
        }

        if (currentTab == ExplorerTab.Files) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Internal Storage", color = TextGray, fontSize = 14.sp)
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(16.dp).padding(horizontal = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "FileFlip",
                        color = PrimaryBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            HorizontalDivider(color = TextGray.copy(alpha = 0.1f))
        } else {
            HorizontalDivider(color = TextGray.copy(alpha = 0.1f))
        }
    }
}

@Composable
fun ExplorerBottomBar(currentTab: ExplorerTab, onTabSelected: (ExplorerTab) -> Unit) {
    Surface(
        color = BackgroundDark.copy(alpha = 0.95f),
        contentColor = TextGray,
        border = BorderStroke(1.dp, TextWhite.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExplorerBottomNavItem(
                    icon = Icons.Rounded.FolderOpen,
                    label = "Files",
                    isSelected = currentTab == ExplorerTab.Files,
                    onClick = { onTabSelected(ExplorerTab.Files) }
                )
                ExplorerBottomNavItem(
                    icon = Icons.Rounded.StarOutline,
                    label = "Starred",
                    isSelected = currentTab == ExplorerTab.Starred,
                    onClick = { onTabSelected(ExplorerTab.Starred) }
                )
                ExplorerBottomNavItem(
                    icon = Icons.Rounded.History,
                    label = "Recent",
                    isSelected = currentTab == ExplorerTab.Recent,
                    onClick = { onTabSelected(ExplorerTab.Recent) }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextGray.copy(alpha = 0.3f))
                )
            }
        }
    }
}

@Composable
fun ExplorerBottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PrimaryBlue else TextGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) PrimaryBlue else TextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun EmptyStateMessage(icon: ImageVector, message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextGray.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = TextGray.copy(alpha = 0.5f),
            fontSize = 16.sp
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = TextGray,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun FolderItem(folder: FolderData, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when (folder.name) {
                    "Drafts", "Published" -> navController.navigate("drafts_published")
                    else -> { }
                }
            }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${folder.itemCount} items • ${folder.date}",
                color = TextGray,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = { }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = TextGray)
        }
    }
}

@Composable
fun FileItemRow(
    name: String, 
    size: String, 
    date: String, 
    icon: ImageVector, 
    iconColor: Color, 
    onClick: () -> Unit, 
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .border(1.dp, TextWhite.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
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
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$size • $date",
                color = TextGray,
                fontSize = 12.sp
            )
        }

        IconButton(onClick = onMenuClick) {
            Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = TextGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionBottomSheet(
    name: String,
    size: String,
    date: String,
    icon: ImageVector,
    iconColor: Color,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .border(1.dp, TextWhite.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
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
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$size • $date",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRename() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EditNote,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Rename",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() }
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Delete",
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RenameFileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SortFilterBottomSheet(
    currentSortOption: String,
    selectedExtensions: Set<String>,
    onSortOptionSelected: (String) -> Unit,
    onExtensionsSelected: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val availableExtensions = listOf("md", "json", "yaml", "xml", "txt", "html", "log", "csv")
    val sortOptions = listOf("Name A-Z", "Name Z-A", "Date Newest", "Date Oldest")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        contentColor = TextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Sort & Filter",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Sort by",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sortOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSortOptionSelected(option) }
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentSortOption == option,
                            onClick = { onSortOptionSelected(option) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryBlue,
                                unselectedColor = TextGray
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = option,
                            color = TextWhite,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Filter by type",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableExtensions.forEach { extension ->
                    val isSelected = selectedExtensions.contains(extension)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val newSelection = if (isSelected) {
                                selectedExtensions - extension
                            } else {
                                selectedExtensions + extension
                            }
                            onExtensionsSelected(newSelection)
                        },
                        label = {
                            Text(
                                text = extension.uppercase(),
                                color = if (isSelected) BackgroundDark else TextWhite
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue,
                            containerColor = SurfaceDark
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) PrimaryBlue else TextGray.copy(alpha = 0.3f)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Data Classes & Mock Data
data class FolderData(val name: String, val itemCount: Int, val date: String)

private val sampleFolders = listOf(
    FolderData("Drafts", 4, "Oct 22, 2023"),
    FolderData("Published", 12, "Yesterday")
)
