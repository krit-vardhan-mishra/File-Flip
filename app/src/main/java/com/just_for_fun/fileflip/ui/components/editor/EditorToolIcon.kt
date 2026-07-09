package com.just_for_fun.fileflip.ui.components.editor

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.just_for_fun.fileflip.ui.util.PrimaryBlue
import com.just_for_fun.fileflip.ui.util.TextGray

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