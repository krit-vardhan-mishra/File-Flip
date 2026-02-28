package com.just_for_fun.fileflip.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object FileIconHelper {
    val IconOrange = Color(0xFFFF9F1C)
    val IconEmerald = Color(0xFF10B981)

    fun getIconAndColor(extension: String): Pair<ImageVector, Color> {
        return when (extension.lowercase()) {
            "md", "markdown" -> Pair(Icons.AutoMirrored.Rounded.Article, IconOrange)
            "json" -> Pair(Icons.Rounded.Code, IconEmerald)
            "xml" -> Pair(Icons.Rounded.Code, Color(0xFF795548)) // Brown
            "yaml", "yml" -> Pair(Icons.Rounded.Settings, Color(0xFF9C27B0)) // Purple
            "html" -> Pair(Icons.Rounded.Description, Color(0xFFE91E63)) // Pink
            "css" -> Pair(Icons.Rounded.Code, Color(0xFF2196F3)) // Blue
            "js", "javascript" -> Pair(Icons.Rounded.Code, Color(0xFFFFD600)) // Yellow
            "txt", "text" -> Pair(Icons.Rounded.Description, Color(0xFF94A3B8)) // Gray
            "log" -> Pair(Icons.Rounded.Description, Color(0xFF607D8B)) // Blue Grey
            "csv" -> Pair(Icons.Rounded.Description, Color(0xFF4CAF50)) // Green
            "pdf" -> Pair(Icons.Rounded.Description, Color(0xFFF44336)) // Red
            "doc", "docx" -> Pair(Icons.Rounded.Description, Color(0xFF1976D2)) // Blue
            "xls", "xlsx" -> Pair(Icons.Rounded.Description, Color(0xFF388E3C)) // Green
            "ppt", "pptx" -> Pair(Icons.Rounded.Description, Color(0xFFFF5722)) // Orange
            "zip", "rar", "7z" -> Pair(Icons.Rounded.FolderOpen, Color(0xFF795548)) // Brown
            "jpg", "jpeg", "png", "gif", "bmp", "webp" -> Pair(Icons.Rounded.AutoAwesome, Color(0xFF9C27B0)) // Purple
            "mp3", "wav", "flac", "aac" -> Pair(Icons.Rounded.AutoAwesome, Color(0xFFE91E63)) // Pink
            "mp4", "avi", "mkv", "mov" -> Pair(Icons.Rounded.AutoAwesome, Color(0xFF673AB7)) // Purple
            else -> Pair(Icons.AutoMirrored.Rounded.Article, IconOrange) // Default
        }
    }
}
