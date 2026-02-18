package com.just_for_fun.fileflip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.just_for_fun.fileflip.ui.theme.FileFlipTheme
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
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let { incomingIntent ->
            when (incomingIntent.action) {
                Intent.ACTION_VIEW -> {
                    val uri = incomingIntent.data
                    uri?.let { fileUri ->
                        Log.d("FileFlip", "Received file intent: $fileUri")

                        // Try to get the file path
                        val filePath = getFilePathFromUri(fileUri)
                        if (filePath != null) {
                            pendingFileUri = fileUri
                            pendingFilePath = filePath
                            Log.d("FileFlip", "File path resolved: $filePath")
                        } else {
                            Log.e("FileFlip", "Could not resolve file path from URI: $fileUri")
                        }
                    }
                }
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String? {
        return try {
            // For file:// URIs
            if (uri.scheme == "file") {
                return uri.path
            }

            // For content:// URIs, try to get the actual file path
            // This is a simplified approach - in a real app you might want more robust handling
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayName = it.getString(it.getColumnIndexOrThrow("_display_name"))
                    // Create a temporary file or handle the content URI appropriately
                    // For now, we'll just return the URI as string
                    return uri.toString()
                }
            }

            // Fallback to URI string
            uri.toString()
        } catch (e: Exception) {
            Log.e("FileFlip", "Error getting file path from URI: ${e.message}")
            null
        }
    }
}