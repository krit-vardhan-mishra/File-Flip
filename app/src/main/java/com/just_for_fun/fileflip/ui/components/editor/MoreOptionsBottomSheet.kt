package com.just_for_fun.fileflip.ui.components.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.just_for_fun.fileflip.domain.model.MarkdownFile
import com.just_for_fun.fileflip.ui.util.DividerColor
import com.just_for_fun.fileflip.ui.util.SurfaceDark
import com.just_for_fun.fileflip.ui.util.TextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreOptionsBottomSheet(
    currentFile: MarkdownFile?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onOpenFile: () -> Unit,
    onCreateNewFile: () -> Unit,
    onSearch: () -> Unit,
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Save / Save As
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

                // Open File
                AttachOptionItem(
                    icon = Icons.Rounded.FolderOpen,
                    title = "Open File",
                    subtitle = "Open an existing file",
                    onClick = onOpenFile
                )

                // Create New File
                AttachOptionItem(
                    icon = Icons.Rounded.Add,
                    title = "Create New File",
                    subtitle = "Create a new file from template",
                    onClick = onCreateNewFile
                )

                // Find & Replace
                AttachOptionItem(
                    icon = Icons.Default.Search,
                    title = "Find & Replace",
                    subtitle = "Search and replace text in editor",
                    onClick = onSearch
                )

                // Share as PDF
                AttachOptionItem(
                    icon = Icons.Default.Print,
                    title = "Share as PDF",
                    subtitle = "Convert and share as PDF",
                    onClick = onShareAsPDF
                )

                // Share File
                AttachOptionItem(
                    icon = Icons.Default.Share,
                    title = "Share File",
                    subtitle = "Share file with other apps",
                    onClick = onShareFile
                )

                // Print
                AttachOptionItem(
                    icon = Icons.Default.Print,
                    title = "Print",
                    subtitle = "Print document",
                    onClick = onPrint
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}