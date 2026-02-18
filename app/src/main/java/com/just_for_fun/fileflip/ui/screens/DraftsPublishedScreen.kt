package com.just_for_fun.fileflip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.navigation.NavController
import com.just_for_fun.fileflip.data.DemoFile
import com.just_for_fun.fileflip.data.DemoFilesData
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

// Colors derived from Dashboard HTML/Design
private val PrimaryBlue = Color(0xFF0DA6F2)
private val BackgroundDark = Color(0xFF101C22)
private val SurfaceDark = Color(0xFF1A2830) // Used for cards
private val TextWhite = Color(0xFFF1F5F9)
private val TextGray = Color(0xFF94A3B8)
private val IconOrange = Color(0xFFFF9F1C) // Approx for "article" icon
private val IconEmerald = Color(0xFF10B981) // Approx for "source" icon

// Sample draft files (unsaved files)
private val draftFiles = listOf(
    DemoFile(
        id = "draft_md_1",
        name = "Unsaved_Notes.md",
        extension = "md",
        content = "# Unsaved Notes\n\nThis is a draft file that hasn't been saved yet.\n\n## TODO\n\n- Finish writing\n- Add more content\n- Save the file",
        size = "0.5 KB",
        date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
        path = "/drafts/Unsaved_Notes.md",
        isStarred = false,
        isRecent = true,
        icon = Icons.AutoMirrored.Rounded.Article,
        iconColor = IconOrange,
        preview = "# Unsaved Notes\n\nThis is a draft file that hasn't been saved yet.\n\n## TODO\n\n- Finish writing\n- Add more content\n- Save the file"
    ),
    DemoFile(
        id = "draft_json_1",
        name = "Temp_Config.json",
        extension = "json",
        content = "{\n  \"status\": \"draft\",\n  \"temp\": true,\n  \"data\": \"unsaved\"\n}",
        size = "0.3 KB",
        date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
        path = "/drafts/Temp_Config.json",
        isStarred = false,
        isRecent = false,
        icon = Icons.Rounded.Code,
        iconColor = IconEmerald,
        preview = "{\n  \"status\": \"draft\",\n  \"temp\": true,\n  \"data\": \"unsaved\"\n}"
    )
)

@Composable
fun DraftsPublishedScreen(navController: NavController) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = { DraftsPublishedTopBar(navController) },
        floatingActionButton = {
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
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Drafts Section
            item {
                SectionTitle("DRAFTS")
            }

            if (draftFiles.isEmpty()) {
                item {
                    EmptyStateMessage(
                        icon = Icons.Rounded.Description,
                        message = "No draft files"
                    )
                }
            } else {
                items(draftFiles) { file ->
                    FileItemRow(file, navController, onClick = {
                        Log.d("FileFlip", "DraftsPublishedScreen: Draft file clicked - ${file.name}, path: ${file.path}")
                        val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                        navController.navigate("editor/$encodedPath")
                    }, onMenuClick = {
                        // Handle menu click for draft files
                    })
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // Published Section
            item {
                SectionTitle("PUBLISHED")
            }

            val publishedFiles = DemoFilesData.getAllForTemplates()
            if (publishedFiles.isEmpty()) {
                item {
                    EmptyStateMessage(
                        icon = Icons.Rounded.Description,
                        message = "No published files"
                    )
                }
            } else {
                items(publishedFiles) { file ->
                    FileItemRow(file, navController, onClick = {
                        Log.d("FileFlip", "DraftsPublishedScreen: Published file clicked - ${file.name}, path: ${file.path}")
                        val encodedPath = URLEncoder.encode(file.path, "UTF-8")
                        navController.navigate("editor/$encodedPath")
                    }, onMenuClick = {
                        // Handle menu click for published files
                    })
                }
            }
        }
    }
}

@Composable
fun DraftsPublishedTopBar(navController: NavController) {
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
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Back",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = "Drafts & Published",
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Placeholder for symmetry
            Box(modifier = Modifier.size(48.dp))
        }
    }
}