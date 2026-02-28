package com.just_for_fun.fileflip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.just_for_fun.fileflip.ui.theme.ThemeManager
import com.just_for_fun.fileflip.ui.theme.LocalAppColors

// Global settings state for demo
object SettingsState {
    var selectedFont by mutableIntStateOf(2) // 0: Default, 1: Serif, 2: Monospace
    var defaultSaveDirectory by mutableStateOf<String?>(null)
    var editorTextSize by mutableFloatStateOf(16f)
    var previewTextSize by mutableFloatStateOf(18f)
}

// Design Colors - now pulled from theme
private val PrimaryBlue: Color @Composable get() = LocalAppColors.current.primaryBlue
private val BackgroundDark: Color @Composable get() = LocalAppColors.current.background
private val SurfaceDark: Color @Composable get() = LocalAppColors.current.surface
private val BorderColor: Color @Composable get() = LocalAppColors.current.border
private val TextWhite: Color @Composable get() = LocalAppColors.current.textPrimary
private val TextGray: Color @Composable get() = LocalAppColors.current.textSecondary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val uriHandler = LocalUriHandler.current
    var textSize by remember { mutableFloatStateOf(SettingsState.editorTextSize) }
    var previewTextSize by remember { mutableFloatStateOf(SettingsState.previewTextSize) }
    var selectedTheme by remember { mutableIntStateOf(ThemeManager.currentThemeIndex) }
    var fontExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            SettingsState.defaultSaveDirectory = uri.toString()
            Log.d("FileFlip", "Selected directory: $uri")
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // --- Editor Section ---
            item { SectionHeader("EDITOR") }
            item {
                SettingsCard {
                    // Text Size
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Text Size", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("${textSize.toInt()}sp", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Slider(
                            value = textSize,
                            onValueChange = { 
                                textSize = it
                                SettingsState.editorTextSize = it
                            },
                            valueRange = 10f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryBlue,
                                activeTrackColor = PrimaryBlue,
                                inactiveTrackColor = PrimaryBlue.copy(alpha = 0.2f)
                            )
                        )
                    }

                    Divider(color = BorderColor, thickness = 1.dp)

                    // Font Family
                    ExposedDropdownMenuBox(
                        expanded = fontExpanded,
                        onExpandedChange = { fontExpanded = it },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clickable { fontExpanded = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.FontDownload, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Font Family", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (SettingsState.selectedFont) {
                                        0 -> "Default"
                                        1 -> "Serif"
                                        2 -> "Monospace"
                                        else -> "Monospace"
                                    },
                                    color = TextGray,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    if (fontExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.UnfoldMore,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = fontExpanded,
                            onDismissRequest = { fontExpanded = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Default", color = TextWhite) },
                                onClick = {
                                    SettingsState.selectedFont = 0
                                    fontExpanded = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                            )
                            DropdownMenuItem(
                                text = { Text("Serif", color = TextWhite) },
                                onClick = {
                                    SettingsState.selectedFont = 1
                                    fontExpanded = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                            )
                            DropdownMenuItem(
                                text = { Text("Monospace", color = TextWhite) },
                                onClick = {
                                    SettingsState.selectedFont = 2
                                    fontExpanded = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                            )
                        }
                    }

                    Divider(color = BorderColor, thickness = 1.dp)

                    // Default Save Directory
                    SettingRowItem(
                        icon = Icons.Rounded.FolderOpen,
                        title = "Default Save Directory",
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = SettingsState.defaultSaveDirectory?.let { "Selected" } ?: "Not set",
                                    color = TextGray,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        onClick = { directoryPickerLauncher.launch(null) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- Appearance Section ---
            item { SectionHeader("APPEARANCE") }
            item {
                SettingsCard {
                    // Theme
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        ThemeSegmentedControl(selectedTheme) { 
                            selectedTheme = it
                            ThemeManager.setTheme(it, context)
                        }
                    }

                    Divider(color = BorderColor, thickness = 1.dp)

                    // Preview Text Size
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Preview Text Size", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                value = previewTextSize,
                                onValueChange = { 
                                    previewTextSize = it
                                    SettingsState.previewTextSize = it
                                },
                                valueRange = 12f..24f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryBlue,
                                    activeTrackColor = PrimaryBlue,
                                    inactiveTrackColor = PrimaryBlue.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("A", color = TextGray, fontSize = 20.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- About Section ---
            item { SectionHeader("ABOUT") }
            item {
                SettingsCard {
                    // Version
                    SettingRowItem(
                        icon = Icons.Rounded.Info,
                        title = "Version",
                        trailingContent = { Text("v${com.just_for_fun.fileflip.BuildConfig.VERSION_NAME}", color = TextGray, fontSize = 14.sp) }
                    )

                    Divider(color = BorderColor, thickness = 1.dp)

                    // GitHub
                    SettingRowItem(
                        icon = Icons.Rounded.Code, // Using Code icon as placeholder for GitHub
                        title = "GitHub Profile",
                        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp)) },
                        onClick = { uriHandler.openUri("https://github.com/krit-vardhan-mishra") }
                    )

                    Divider(color = BorderColor, thickness = 1.dp)
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }

            // --- Branding Footer ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().alpha(0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryBlue, RoundedCornerShape(12.dp))
                            .shadow(10.dp, RoundedCornerShape(12.dp), spotColor = PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "FlipFile",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = PrimaryBlue.copy(alpha = 0.8f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingRowItem(
    icon: ImageVector,
    title: String,
    trailingContent: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        trailingContent()
    }
}

@Composable
fun ThemeSegmentedControl(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val options = listOf("Dark", "GitHub Dark")
        options.forEachIndexed { index, text ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isSelected) colors.primaryBlue else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.White else colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingsBottomBar() {
    Surface(
        color = BackgroundDark.copy(alpha = 0.9f),
        contentColor = TextGray,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsBottomNavItem(Icons.Rounded.FolderOpen, "Files", false)
            SettingsBottomNavItem(Icons.Rounded.EditNote, "Editor", false)
            SettingsBottomNavItem(Icons.Rounded.Settings, "Settings", true)
        }
    }
}

@Composable
fun SettingsBottomNavItem(icon: ImageVector, label: String, isSelected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
            fontWeight = FontWeight.Medium
        )
    }
}