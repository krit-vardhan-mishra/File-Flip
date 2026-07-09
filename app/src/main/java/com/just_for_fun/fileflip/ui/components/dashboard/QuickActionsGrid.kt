package com.just_for_fun.fileflip.ui.components.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.just_for_fun.fileflip.ui.screens.QuickActionButton

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
