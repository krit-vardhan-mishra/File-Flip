package com.just_for_fun.fileflip.ui.components.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.just_for_fun.fileflip.ui.theme.LocalAppColors
import com.just_for_fun.fileflip.ui.util.BackgroundDark
import com.just_for_fun.fileflip.ui.util.FileIconHelper
import com.just_for_fun.fileflip.ui.util.PrimaryBlue
import com.just_for_fun.fileflip.ui.util.SurfaceDark
import com.just_for_fun.fileflip.ui.util.TextGray
import com.just_for_fun.fileflip.ui.util.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchReplaceBottomSheet(
    content: String,
    initialMode: String = "search",
    onDismiss: () -> Unit,
    onReplace: (String) -> Unit,
    onMatchesChanged: (matches: List<IntRange>, currentIndex: Int) -> Unit = { _, _ -> },
    onReplaceHighlight: (Pair<IntRange, Color>?) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val replaceScope = androidx.compose.runtime.rememberCoroutineScope()
    val appColors = LocalAppColors.current
    var mode by remember { mutableStateOf(initialMode) }
    var searchText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    var useRegex by remember { mutableStateOf(false) }
    var caseSensitive by remember { mutableStateOf(false) }
    var matchCount by remember { mutableIntStateOf(0) }
    var currentMatchIndex by remember { mutableIntStateOf(0) }
    var matches by remember { mutableStateOf<List<IntRange>>(emptyList()) }

    // Propagate matches to parent for highlighting
    LaunchedEffect(matches, currentMatchIndex) {
        onMatchesChanged(matches, currentMatchIndex)
    }

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
                    // Replace current match (one by one) with red→green highlighting
                    Button(
                        onClick = {
                            if (matches.isNotEmpty() && currentMatchIndex < matches.size) {
                                val matchRange = matches[currentMatchIndex]
                                replaceScope.launch {
                                    // Flash red highlight on the text being replaced
                                    onReplaceHighlight(Pair(matchRange, appColors.replaceHighlightPending))
                                    delay(300)

                                    // Perform the replacement
                                    val before = content.substring(0, matchRange.first)
                                    val after = content.substring(matchRange.last + 1)
                                    val newContent = "$before$replaceText$after"
                                    onReplace(newContent)

                                    // Flash green highlight on the replaced text
                                    val replacedRange = matchRange.first until (matchRange.first + replaceText.length)
                                    onReplaceHighlight(Pair(replacedRange, appColors.replaceHighlightDone))
                                    delay(600)

                                    // Clear replace highlight
                                    onReplaceHighlight(null)
                                }
                                // Recalculate will happen via LaunchedEffect, stay on same index
                                if (currentMatchIndex >= matchCount - 1) {
                                    currentMatchIndex = 0
                                }
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = FileIconHelper.IconOrange,
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