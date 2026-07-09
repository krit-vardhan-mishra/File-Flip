package com.just_for_fun.fileflip.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import com.just_for_fun.fileflip.ui.viewmodels.EditorViewModel

data class EditorTool(
    val icon: ImageVector,
    val description: String,
    val action: (EditorViewModel, String, String, TextRange?) -> Unit
)