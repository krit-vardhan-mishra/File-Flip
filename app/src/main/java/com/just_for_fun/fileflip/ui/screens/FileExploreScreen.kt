package com.just_for_fun.fileflip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.just_for_fun.fileflip.data.DemoFilesData
import java.net.URLEncoder
import android.util.Log

// Colors derived from Dashboard HTML/Design
private val PrimaryBlue = Color(0xFF0DA6F2)
private val BackgroundDark = Color(0xFF101C22)
private val SurfaceDark = Color(0xFF1A2830) // Used for cards
private val TextWhite = Color(0xFFF1F5F9)
private val TextGray = Color(0xFF94A3B8)
private val IconOrange = Color(0xFFFF9F1C) // Approx for "article" icon
private val IconEmerald = Color(0xFF10B981) // Approx for "source" icon

enum class ExplorerTab {
    Files, Starred, Recent, Settings
}

@Composable
fun FileExplorerScreen(navController: NavController, initialTab: String = "files") {
    val initialExplorerTab = when (initialTab.lowercase()) {
        "starred" -> ExplorerTab.Starred
        "recent" -> ExplorerTab.Recent
        "settings" -> ExplorerTab.Settings
        else -> ExplorerTab.Files
    }
    
    var currentTab by remember { mutableStateOf(initialExplorerTab) }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = { ExplorerTopBar(navController, currentTab) },
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
                    onClick = { /* New File Action */ },
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
                ExplorerTab.Files -> FilesTabContent(navController)
                ExplorerTab.Starred -> StarredTabContent(navController)
                ExplorerTab.Recent -> RecentTabContent(navController)
                else -> {}
            }
        }
    }
}

@Composable
fun FilesTabContent(navController: NavController) {
    val files = remember { mutableStateOf(DemoFilesData.getFilesForTab("files")) }
    var showSortFilterSheet by remember { mutableStateOf(false) }
    var showFileActionSheet by remember { mutableStateOf(false) }
    var selectedFileForAction by remember { mutableStateOf<com.just_for_fun.fileflip.data.DemoFile?>(null) }
    var sortOption by remember { mutableStateOf("Name A-Z") }
    var selectedExtensions by remember { mutableStateOf(setOf<String>()) }

    // Filter and sort files
    val filteredAndSortedFiles = remember(sortOption, selectedExtensions, files.value) {
        var result = files.value

        // Apply extension filter
        if (selectedExtensions.isNotEmpty()) {
            result = result.filter { file ->
                selectedExtensions.contains(file.extension.lowercase())
            }
        }

        // Apply sorting
        result = when (sortOption) {
            "Name A-Z" -> result.sortedBy { it.name.lowercase() }
            "Name Z-A" -> result.sortedByDescending { it.name.lowercase() }
            "Date Newest" -> result.sortedByDescending { it.date }
            "Date Oldest" -> result.sortedBy { it.date }
            "Size Largest" -> result.sortedByDescending { parseFileSize(it.size) }
            "Size Smallest" -> result.sortedBy { parseFileSize(it.size) }
            else -> result
        }

        result
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom nav
    ) {
        // Folders Section
        item {
            SectionTitle("FOLDERS")
        }
        items(sampleFolders) { folder ->
            FolderItem(folder, navController)
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }

        // Documents Section
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
        items(filteredAndSortedFiles) { file ->
            FileItemRow(file, navController, onClick = {
                Log.d("FileFlip", "FileExploreScreen: File clicked - ${file.name}, path: ${file.path}")
                val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                navController.navigate("editor/$encodedPath")
            }, onMenuClick = {
                selectedFileForAction = file
                showFileActionSheet = true
            })
        }
    }

    // File Action Bottom Sheet
    if (showFileActionSheet && selectedFileForAction != null) {
        val context = LocalContext.current
        FileActionBottomSheet(
            file = selectedFileForAction!!,
            onDismiss = { showFileActionSheet = false },
            onDelete = {
                DemoFilesData.markAsDeleted(selectedFileForAction!!.id, context)
                showFileActionSheet = false
                selectedFileForAction = null
            }
        )
    }

    // Sort & Filter Bottom Sheet
    if (showSortFilterSheet) {
        SortFilterBottomSheet(
            currentSortOption = sortOption,
            selectedExtensions = selectedExtensions,
            onSortOptionSelected = { sortOption = it },
            onExtensionsSelected = { selectedExtensions = it },
            onDismiss = { showSortFilterSheet = false }
        )
    }
}

@Composable
fun StarredTabContent(navController: NavController) {
    val files = remember { mutableStateOf(DemoFilesData.getFilesForTab("starred")) }
    var showFileActionSheet by remember { mutableStateOf(false) }
    var selectedFileForAction by remember { mutableStateOf<com.just_for_fun.fileflip.data.DemoFile?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            SectionTitle("STARRED FILES")
        }

        if (files.value.isEmpty()) {
            item {
                EmptyStateMessage(
                    icon = Icons.Rounded.StarBorder,
                    message = "No starred files yet"
                )
            }
        } else {
            items(files.value) { file ->
                FileItemRow(file, navController, onClick = {
                    Log.d("FileFlip", "FileExploreScreen: Starred file clicked - ${file.name}, path: ${file.path}")
                    val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                    navController.navigate("editor/$encodedPath")
                }, onMenuClick = {
                    selectedFileForAction = file
                    showFileActionSheet = true
                })
            }
        }
    }

    // File Action Bottom Sheet
    if (showFileActionSheet && selectedFileForAction != null) {
        val context = LocalContext.current
        FileActionBottomSheet(
            file = selectedFileForAction!!,
            onDismiss = { showFileActionSheet = false },
            onDelete = {
                DemoFilesData.markAsDeleted(selectedFileForAction!!.id, context)
                showFileActionSheet = false
                selectedFileForAction = null
            }
        )
    }
}

@Composable
fun RecentTabContent(navController: NavController) {
    val files = remember { mutableStateOf(DemoFilesData.getFilesForTab("recent")) }
    var showFileActionSheet by remember { mutableStateOf(false) }
    var selectedFileForAction by remember { mutableStateOf<com.just_for_fun.fileflip.data.DemoFile?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            SectionTitle("RECENTLY OPENED")
        }

        items(files.value) { file ->
            FileItemRow(file, navController, onClick = {
                Log.d("FileFlip", "FileExploreScreen: Recent file clicked - ${file.name}, path: ${file.path}")
                val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                navController.navigate("editor/$encodedPath")
            }, onMenuClick = {
                selectedFileForAction = file
                showFileActionSheet = true
            })
        }
    }

    // File Action Bottom Sheet
    if (showFileActionSheet && selectedFileForAction != null) {
        val context = LocalContext.current
        FileActionBottomSheet(
            file = selectedFileForAction!!,
            onDismiss = { showFileActionSheet = false },
            onDelete = {
                DemoFilesData.markAsDeleted(selectedFileForAction!!.id, context)
                showFileActionSheet = false
                selectedFileForAction = null
            }
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
fun ExplorerTopBar(navController: NavController, currentTab: ExplorerTab) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDark.copy(alpha = 0.95f))
            .statusBarsPadding()
    ) {
        // Main Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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

            IconButton(onClick = { /* Search */ }) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search",
                    tint = PrimaryBlue
                )
            }
        }

        // Breadcrumb Row - Only show on Files tab
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
            Divider(color = TextGray.copy(alpha = 0.1f))
        } else {
            // Divider for other tabs to separate header
            Divider(color = TextGray.copy(alpha = 0.1f))
        }
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
                    else -> { /* Navigate to folder */ }
                }
            }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Folder Icon
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

        // Details
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

        IconButton(onClick = { /* Menu */ }) {
            Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = TextGray)
        }
    }
}

@Composable
fun FileItemRow(file: com.just_for_fun.fileflip.data.DemoFile, navController: NavController, onClick: () -> Unit, onMenuClick: () -> Unit) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // File Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SurfaceDark, RoundedCornerShape(12.dp))
                .border(1.dp, TextWhite.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = file.icon,
                contentDescription = null,
                tint = file.iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = file.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (file.isRecent && !file.isStarred) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NEW",
                            color = PrimaryBlue,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Text(
                text = "${file.size} • ${file.date}",
                color = TextGray,
                fontSize = 12.sp
            )
        }

        Box {
            IconButton(onClick = { onMenuClick() }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = null, tint = TextGray)
            }
        }
    }
}

@Composable
fun ExplorerBottomBar(currentTab: ExplorerTab, onTabSelected: (ExplorerTab) -> Unit) {
    Surface(
        color = BackgroundDark.copy(alpha = 0.95f),
        contentColor = TextGray,
        border = androidx.compose.foundation.BorderStroke(1.dp, TextWhite.copy(alpha = 0.05f)),
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
            // Home Indicator
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

// Mock Data
data class FolderData(val name: String, val itemCount: Int, val date: String)
data class FileExplorerData(val name: String, val size: String, val date: String, val path: String, val isNew: Boolean = false)

private val sampleFolders = listOf(
    FolderData("Drafts", 4, "Oct 22, 2023"),
    FolderData("Published", 12, "Yesterday")
)

// Helper function to parse file size for sorting
private fun parseFileSize(size: String): Long {
    val regex = Regex("([0-9.]+)\\s*(KB|MB|GB|B)?")
    val match = regex.find(size) ?: return 0L

    val value = match.groupValues[1].toDoubleOrNull() ?: 0.0
    val unit = match.groupValues[2]

    return when (unit.uppercase()) {
        "GB" -> (value * 1024 * 1024 * 1024).toLong()
        "MB" -> (value * 1024 * 1024).toLong()
        "KB" -> (value * 1024).toLong()
        else -> value.toLong()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionBottomSheet(
    file: com.just_for_fun.fileflip.data.DemoFile,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
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
            // File info header
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
                        imageVector = file.icon,
                        contentDescription = null,
                        tint = file.iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = file.name,
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${file.size} • ${file.date}",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }

            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Delete action
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortFilterBottomSheet(
    currentSortOption: String,
    selectedExtensions: Set<String>,
    onSortOptionSelected: (String) -> Unit,
    onExtensionsSelected: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    // Available file extensions from demo files
    val availableExtensions = remember {
        DemoFilesData.getAllForTemplates()
            .map { it.extension.lowercase() }
            .distinct()
            .sorted()
    }

    val sortOptions = listOf(
        "Name A-Z",
        "Name Z-A",
        "Date Newest",
        "Date Oldest",
        "Size Largest",
        "Size Smallest"
    )

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
            // Header
            Text(
                text = "Sort & Filter",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Sort Options
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

            // Filter Options
            Text(
                text = "Filter by type",
                color = TextGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Extension filter chips
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