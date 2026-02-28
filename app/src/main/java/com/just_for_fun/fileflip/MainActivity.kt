package com.just_for_fun.fileflip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.just_for_fun.fileflip.ui.theme.FileFlipTheme
import com.just_for_fun.fileflip.ui.theme.ThemeManager
import com.just_for_fun.fileflip.ui.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var pendingFileUri: Uri? = null
    private var pendingFilePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle incoming intents
        handleIntent(intent)

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val onboardingCompleted = sharedPreferences.getBoolean("onboarding_completed", false)
        val startDestination = if (onboardingCompleted) "dashboard" else "onboarding"

        // Load saved theme preference
        ThemeManager.loadTheme(this)

        setContent {
            FileFlipTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        startDestination = startDestination,
                        pendingFileUri = pendingFileUri,
                        pendingFilePath = pendingFilePath
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        // Re-compose with updated pending file
        recreate()
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let { incomingIntent ->
            when (incomingIntent.action) {
                Intent.ACTION_VIEW, Intent.ACTION_EDIT -> {
                    val uri = incomingIntent.data
                    uri?.let { fileUri ->
                        Log.d("FileFlip", "Received file intent: $fileUri")

                        // Copy the file content to app storage and get a real file path
                        val filePath = copyUriToAppStorage(fileUri)
                        if (filePath != null) {
                            pendingFileUri = fileUri
                            pendingFilePath = filePath
                            Log.d("FileFlip", "File copied to app storage: $filePath")
                        } else {
                            Log.e("FileFlip", "Could not copy file from URI: $fileUri")
                        }
                    }
                }
                Intent.ACTION_SEND -> {
                    // Handle files shared via "Send to" / Share menu
                    val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM)
                    }
                    uri?.let { fileUri ->
                        Log.d("FileFlip", "Received shared file: $fileUri")
                        val filePath = copyUriToAppStorage(fileUri)
                        if (filePath != null) {
                            pendingFileUri = fileUri
                            pendingFilePath = filePath
                            Log.d("FileFlip", "Shared file copied to app storage: $filePath")
                        }
                    }
                }
            }
        }
    }

    /**
     * Copies the content from a URI (content:// or file://) to the app's local storage
     * and returns the real file path. This is essential for handling files from external
     * apps like WhatsApp, Gmail, etc. that provide content:// URIs.
     */
    private fun copyUriToAppStorage(uri: Uri): String? {
        return try {
            // For file:// URIs, check if file is directly accessible
            if (uri.scheme == "file") {
                val path = uri.path
                if (path != null && File(path).exists()) {
                    return path
                }
            }

            // For content:// URIs, copy to app storage
            val fileName = getDisplayNameFromUri(uri) ?: generateFallbackFileName(uri)
            val outputDir = File(getExternalFilesDir(null), "Files").apply {
                if (!exists()) mkdirs()
            }

            // Avoid overwriting: if file exists, add timestamp
            var outputFile = File(outputDir, fileName)
            if (outputFile.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast(".", fileName)
                val ext = fileName.substringAfterLast(".", "")
                val timestamp = System.currentTimeMillis()
                outputFile = if (ext.isNotEmpty()) {
                    File(outputDir, "${nameWithoutExt}_$timestamp.$ext")
                } else {
                    File(outputDir, "${nameWithoutExt}_$timestamp")
                }
            }

            // Copy content from URI to local file
            contentResolver.openInputStream(uri)?.use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: return null

            Log.d("FileFlip", "File copied successfully: ${outputFile.absolutePath}")
            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("FileFlip", "Error copying file from URI: ${e.message}", e)
            null
        }
    }

    /**
     * Gets the display name (filename) from a content:// URI using ContentResolver.
     */
    private fun getDisplayNameFromUri(uri: Uri): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) cursor.getString(nameIndex) else null
                } else null
            }
        } catch (e: Exception) {
            Log.e("FileFlip", "Error getting display name: ${e.message}")
            null
        }
    }

    /**
     * Generates a fallback file name when the display name cannot be determined.
     * Tries to extract from URI path or MIME type.
     */
    private fun generateFallbackFileName(uri: Uri): String {
        // Try to get extension from URI path
        val pathSegment = uri.lastPathSegment
        if (pathSegment != null && pathSegment.contains(".")) {
            return pathSegment.substringAfterLast("/")
        }

        // Try to get extension from MIME type
        val mimeType = contentResolver.getType(uri)
        val extension = when (mimeType) {
            "text/markdown" -> "md"
            "text/plain" -> "txt"
            "text/html" -> "html"
            "text/xml", "application/xml" -> "xml"
            "text/csv" -> "csv"
            "application/json" -> "json"
            "text/yaml", "application/yaml" -> "yaml"
            else -> "txt"
        }
        return "imported_${System.currentTimeMillis()}.$extension"
    }
}