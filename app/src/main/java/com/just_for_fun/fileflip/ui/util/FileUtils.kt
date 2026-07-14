package com.just_for_fun.fileflip.ui.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.just_for_fun.fileflip.domain.model.ExplorerItem
import java.io.File

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    result = it.getString(nameIndex)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "imported_file_${System.currentTimeMillis()}"
}

fun copyFileToAppStorage(context: android.content.Context, uri: Uri, fileName: String): Boolean {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return false
        val outputDir = context.filesDir
        // Create a unique filename if it already exists to avoid overwriting (optional, but good practice)
        // For now, we'll overwrite as per original logic implies, or maybe we should handle duplicates?
        // Let's stick to simple overwrite for now to match expected behavior or just basic copy.
        val outputFile = java.io.File(outputDir, fileName)

        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

fun copyUriToLocalFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val outputDir = File(context.getExternalFilesDir(null), "Files").apply {
            if (!exists()) mkdirs()
        }
        val outputFile = File(outputDir, fileName)
        inputStream.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        outputFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun copyFileToAppStorageEditorScreen(context: android.content.Context, uri: Uri, fileName: String) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputDir = context.getExternalFilesDir(null)?.let { File(it, "Files") } ?: return
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputFile = File(outputDir, fileName)
        inputStream?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun extractCodeBlocks(text: String): List<String> {
    val pattern = Regex("```(?:[a-zA-Z0-9+#-]+)?\\n([\\s\\S]*?)```")
    return pattern.findAll(text).map { it.groupValues[1] }.toList()
}

// Helper functions for file import
fun getFileNameFromUriEditorScreen(context: android.content.Context, uri: Uri): String? {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    return cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            it.getString(nameIndex)
        } else null
    }
}

fun listDocumentFiles(context: Context, parent: DocumentFile): List<ExplorerItem> {
    val list = mutableListOf<ExplorerItem>()
    try {
        // Determine the correct document ID for building the children URI
        val parentDocId = try {
            android.provider.DocumentsContract.getTreeDocumentId(parent.uri)
                ?: android.provider.DocumentsContract.getDocumentId(parent.uri)
        } catch (e: Exception) {
            try {
                android.provider.DocumentsContract.getDocumentId(parent.uri)
            } catch (e2: Exception) {
                Log.e("FileFlip", "listDocumentFiles: Cannot get docId for '${parent.name}', uri=${parent.uri}", e2)
                null
            }
        }

        if (parentDocId == null) {
            Log.e("FileFlip", "listDocumentFiles: parentDocId is null for '${parent.name}'")
            return emptyList()
        }

        val childrenUri = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(
            parent.uri,
            parentDocId
        )
        Log.d("FileFlip", "listDocumentFiles: Querying childrenUri=$childrenUri for parent='${parent.name}'")

        val cursor = context.contentResolver.query(
            childrenUri,
            arrayOf(
                android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE,
                android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID
            ),
            null, null, null
        )

        cursor?.use { c ->
            Log.d("FileFlip", "listDocumentFiles: cursor count = ${c.count} for parent='${parent.name}'")
            while (c.moveToNext()) {
                val displayName = c.getString(0) ?: "Unknown"
                val mimeType = c.getString(1) ?: ""
                val docId = c.getString(2)
                val isDir = mimeType == android.provider.DocumentsContract.Document.MIME_TYPE_DIR
                val childUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(parent.uri, docId)

                Log.d("FileFlip", "  child: name='$displayName', mime='$mimeType', isDir=$isDir")

                if (isDir) {
                    val childTreeUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(parent.uri, docId)
                    list.add(
                        ExplorerItem(
                            name = displayName,
                            uri = childTreeUri,
                            isDirectory = true,
                            children = listDocumentFilesFromTree(context, parent.uri, docId)
                        )
                    )
                } else {
                    list.add(
                        ExplorerItem(
                            name = displayName,
                            uri = childUri,
                            isDirectory = false
                        )
                    )
                }
            }
        } ?: run {
            Log.e("FileFlip", "listDocumentFiles: cursor is null for parent='${parent.name}'")
        }
    } catch (e: Exception) {
        Log.e("FileFlip", "listDocumentFiles error for parent: ${parent.name}", e)
    }
    return list.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
}

/**
 * Recursively lists children of a sub-directory inside a tree URI using ContentResolver.
 */
fun listDocumentFilesFromTree(context: Context, treeUri: Uri, parentDocId: String): List<ExplorerItem> {
    val list = mutableListOf<ExplorerItem>()
    try {
        val childrenUri = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            parentDocId
        )
        val cursor = context.contentResolver.query(
            childrenUri,
            arrayOf(
                android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE,
                android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID
            ),
            null, null, null
        )
        cursor?.use { c ->
            while (c.moveToNext()) {
                val displayName = c.getString(0) ?: "Unknown"
                val mimeType = c.getString(1) ?: ""
                val docId = c.getString(2)
                val isDir = mimeType == android.provider.DocumentsContract.Document.MIME_TYPE_DIR
                val childUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)

                if (isDir) {
                    list.add(
                        ExplorerItem(
                            name = displayName,
                            uri = childUri,
                            isDirectory = true,
                            children = listDocumentFilesFromTree(context, treeUri, docId)
                        )
                    )
                } else {
                    list.add(
                        ExplorerItem(
                            name = displayName,
                            uri = childUri,
                            isDirectory = false
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e("FileFlip", "listDocumentFilesFromTree error for parentDocId=$parentDocId", e)
    }
    return list.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
}


// --- Custom Markdown Parser Composables ---
@Composable
fun FormattedMarkdownText(text: String, textColor: Color) {
    // Split the text by triple backticks for code blocks
    val parts = text.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Render as a Code Block
                val lines = part.lines()
                val codeContent = if (lines.size > 1 && lines.first().trim().isNotEmpty()) {
                    lines.drop(1).joinToString("\n")
                } else {
                    part
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = codeContent.trim(),
                        color = Color(0xFFD4D4D4),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
            } else {
                // Render as normal text with inline markdown parsing
                if (part.isNotBlank()) {
                    Text(
                        text = parseBasicMarkdown(part),
                        color = textColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}



// Helper function to get file icon and color based on extension
fun getFileIconAndColorEditorScreen(extension: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    val cleanExtension = extension.removePrefix(".")
    return FileIconHelper.getIconAndColor(cleanExtension)
}

fun parseBasicMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        // Regex looks for **bold**, *italic*, and `inline code`
        val pattern = Regex("(\\*\\*[\\s\\S]*?\\*\\*|\\*[\\s\\S]*?\\*|`[\\s\\S]*?`)")
        val matches = pattern.findAll(text)

        for (match in matches) {
            // Append standard text that appeared before the matched styled text
            append(text.substring(currentIndex, match.range.first))

            val matchText = match.value
            when {
                matchText.startsWith("**") && matchText.endsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(matchText.substring(2, matchText.length - 2))
                    }
                }
                matchText.startsWith("*") && matchText.endsWith("*") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(matchText.substring(1, matchText.length - 1))
                    }
                }
                matchText.startsWith("`") && matchText.endsWith("`") -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.DarkGray.copy(alpha = 0.5f)
                        )
                    ) {
                        append(matchText.substring(1, matchText.length - 1))
                    }
                }
            }
            currentIndex = match.range.last + 1
        }
        // Append any remaining text after the last match
        append(text.substring(currentIndex))
    }
}

fun getFolderNameFromUri(context: Context, uri: Uri): String? {
    return try {
        val documentFile = DocumentFile.fromTreeUri(context, uri)
        documentFile?.name
    } catch (e: Exception) {
        null
    }
}