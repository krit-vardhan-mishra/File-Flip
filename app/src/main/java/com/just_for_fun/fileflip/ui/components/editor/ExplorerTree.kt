package com.just_for_fun.fileflip.ui.components.editor

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.just_for_fun.fileflip.domain.model.ExplorerItem
import com.just_for_fun.fileflip.ui.util.FileIconHelper
import com.just_for_fun.fileflip.ui.util.PrimaryBlue
import com.just_for_fun.fileflip.ui.util.TextWhite

@Composable
fun ExplorerTree(
    item: ExplorerItem,
    depth: Int,
    onFileClick: (Uri, String) -> Unit,
    expandedFolders: MutableMap<String, Boolean>
) {
    val isExpanded = expandedFolders[item.uri.toString()] ?: false

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (item.isDirectory) {
                        expandedFolders[item.uri.toString()] = !isExpanded
                    } else {
                        onFileClick(item.uri, item.name)
                    }
                }
                .padding(vertical = 8.dp, horizontal = (16 + depth * 12).dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.isDirectory) {
                    Icons.Rounded.FolderOpen
                } else {
                    val ext = item.name.substringAfterLast(".", "").lowercase()
                    FileIconHelper.getIconAndColor(ext).first
                },
                contentDescription = null,
                tint = if (item.isDirectory) PrimaryBlue else FileIconHelper.getIconAndColor(item.name.substringAfterLast(".", "").lowercase()).second,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item.name,
                color = TextWhite,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (item.isDirectory && isExpanded) {
            item.children.forEach { child ->
                ExplorerTree(
                    item = child,
                    depth = depth + 1,
                    onFileClick = onFileClick,
                    expandedFolders = expandedFolders
                )
            }
        }
    }
}