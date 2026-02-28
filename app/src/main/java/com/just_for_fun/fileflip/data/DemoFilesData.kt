package com.just_for_fun.fileflip.data

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.just_for_fun.fileflip.ui.util.FileIconHelper

// Colors
private val PrimaryBlue = Color(0xFF0DA6F2)
private val BackgroundDark = Color(0xFF101C22)
private val TextGray = Color(0xFF94A3B8)

data class DemoFile(
    val id: String,
    val name: String,
    val extension: String,
    var content: String,
    val size: String,
    val date: String,
    val path: String,
    val isStarred: Boolean = false,
    val isRecent: Boolean = false,
    var isDeleted: Boolean = false,
    val icon: ImageVector,
    val iconColor: Color,
    val preview: String
)

object DemoFilesData {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currentDate = dateFormat.format(Date())

    private const val PREFS_NAME = "demo_files_prefs"
    private const val DELETED_FILES_KEY = "deleted_files"
    private const val CONTENT_UPDATES_KEY = "content_updates"

    fun initialize(context: Context) {
        loadDeletedState(context.applicationContext)
        loadContentUpdates(context.applicationContext)
    }

    private fun loadDeletedState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val deletedIds = prefs.getStringSet(DELETED_FILES_KEY, emptySet()) ?: emptySet()
        demoFiles.forEach { file ->
            file.isDeleted = deletedIds.contains(file.id)
        }
    }

    private fun saveDeletedState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val deletedIds = demoFiles.filter { it.isDeleted }.map { it.id }.toSet()
        prefs.edit().putStringSet(DELETED_FILES_KEY, deletedIds).apply()
    }

    private fun loadContentUpdates(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val contentUpdatesJson = prefs.getString(CONTENT_UPDATES_KEY, "{}") ?: "{}"
        try {
            // Simple JSON parsing for content updates (fileId -> content)
            val contentMap = mutableMapOf<String, String>()
            if (contentUpdatesJson.isNotEmpty() && contentUpdatesJson != "{}") {
                // Parse simple JSON format: {"fileId":"content","fileId2":"content2"}
                val cleanedJson = contentUpdatesJson.removePrefix("{").removeSuffix("}")
                if (cleanedJson.isNotEmpty()) {
                    cleanedJson.split(",").forEach { pair ->
                        val parts = pair.split(":", limit = 2)
                        if (parts.size == 2) {
                            val fileId = parts[0].removeSurrounding("\"")
                            val content = parts[1].removeSurrounding("\"").replace("\\n", "\n").replace("\\\"", "\"")
                            contentMap[fileId] = content
                        }
                    }
                }
            }
            // Apply content updates
            demoFiles.forEach { file ->
                contentMap[file.id]?.let { updatedContent ->
                    file.content = updatedContent
                }
            }
        } catch (e: Exception) {
            // If parsing fails, ignore and use default content
        }
    }

    private fun saveContentUpdates(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val contentUpdates = demoFiles.associate { it.id to it.content }
        // Simple JSON format: {"fileId":"content","fileId2":"content2"}
        val jsonString = contentUpdates.entries.joinToString(",") { (id, content) ->
            "\"$id\":\"${content.replace("\n", "\\n").replace("\"", "\\\"")}\""
        }
        prefs.edit().putString(CONTENT_UPDATES_KEY, "{$jsonString}").apply()
    }

    val demoFiles = listOf(
        DemoFile(
            id = "demo_md_1",
            name = "Project_Documentation.md",
            extension = "md",
            content = "# Project Documentation\n\n## Overview\n\nThis is a sample markdown document template.\n\n## Features\n\n- Easy to read\n- Supports formatting\n- Great for documentation\n\n## Getting Started\n\nStart writing your content here...",
            size = "2.1 KB",
            date = currentDate,
            path = "/demo/Project_Documentation.md",
            isStarred = true,
            isRecent = true,
            icon = Icons.AutoMirrored.Rounded.Article,
            iconColor = FileIconHelper.IconOrange,
            preview = "Project Documentation\n\nOverview\n\nThis is a sample markdown document template.\n\nFeatures\n\n• Easy to read\n• Supports formatting\n• Great for documentation\n\nGetting Started\n\nStart writing your content here..."
        ),
        DemoFile(
            id = "demo_json_1",
            name = "App_Config.json",
            extension = "json",
            content = "{\n  \"appName\": \"FileFlip\",\n  \"version\": \"1.0.0\",\n  \"settings\": {\n    \"theme\": \"dark\",\n    \"language\": \"en\",\n    \"notifications\": true\n  },\n  \"features\": [\n    \"editor\",\n    \"templates\",\n    \"export\"\n  ]\n}",
            size = "1.5 KB",
            date = currentDate,
            path = "/demo/App_Config.json",
            isStarred = false,
            isRecent = true,
            icon = Icons.Rounded.Code,
            iconColor = FileIconHelper.IconEmerald,
            preview = "{\n  \"appName\": \"FileFlip\",\n  \"version\": \"1.0.0\",\n  \"settings\": {\n    \"theme\": \"dark\",\n    \"language\": \"en\",\n    \"notifications\": true\n  },\n  \"features\": [\n    \"editor\",\n    \"templates\",\n    \"export\"\n  ]\n}"
        ),
        DemoFile(
            id = "demo_yaml_1",
            name = "Deployment_Config.yaml",
            extension = "yaml",
            content = "# Application Deployment Configuration\napp:\n  name: FileFlip\n  version: 1.0.0\n  environment: production\n\nserver:\n  host: localhost\n  port: 8080\n  ssl: true\n\ndatabase:\n  type: postgresql\n  host: db.example.com\n  port: 5432\n\nfeatures:\n  - authentication\n  - file_upload\n  - export_pdf",
            size = "1.8 KB",
            date = currentDate,
            path = "/demo/Deployment_Config.yaml",
            isStarred = true,
            isRecent = false,
            icon = Icons.Rounded.Settings,
            iconColor = Color(0xFF9C27B0),
            preview = "# Application Deployment Configuration\napp:\n  name: FileFlip\n  version: 1.0.0\n\nserver:\n  host: localhost\n  port: 8080\n\ndatabase:\n  type: postgresql\n  host: db.example.com"
        ),
        DemoFile(
            id = "demo_xml_1",
            name = "User_Data.xml",
            extension = "xml",
            content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<users>\n  <user id=\"1\">\n    <name>John Doe</name>\n    <email>john@example.com</email>\n    <role>admin</role>\n  </user>\n  <user id=\"2\">\n    <name>Jane Smith</name>\n    <email>jane@example.com</email>\n    <role>user</role>\n  </user>\n</users>",
            size = "1.2 KB",
            date = currentDate,
            path = "/demo/User_Data.xml",
            isStarred = false,
            isRecent = true,
            icon = Icons.Rounded.Code,
            iconColor = Color(0xFF795548),
            preview = "<?xml version=\"1.0\"?>\n<users>\n  <user id=\"1\">\n    <name>John Doe</name>\n    <email>john@example.com</email>\n  </user>\n  <user id=\"2\">\n    <name>Jane Smith</name>\n    <email>jane@example.com</email>\n  </user>\n</users>"
        ),
        DemoFile(
            id = "demo_txt_1",
            name = "Notes.txt",
            extension = "txt",
            content = "Personal Notes\n==============\n\nThis is a simple text file for taking notes.\n\nKey Points:\n- Remember to backup files regularly\n- Use markdown for better formatting\n- Keep notes organized\n\nTODO:\n- Update project documentation\n- Test new features\n- Review code changes\n\n==============\nEnd of notes",
            size = "0.8 KB",
            date = currentDate,
            path = "/demo/Notes.txt",
            isStarred = false,
            isRecent = false,
            icon = Icons.Rounded.Description,
            iconColor = TextGray,
            preview = "Personal Notes\n==============\n\nThis is a simple text file for taking notes.\n\nKey Points:\n- Remember to backup files regularly\n- Use markdown for better formatting\n- Keep notes organized"
        ),
        DemoFile(
            id = "demo_html_1",
            name = "Welcome_Page.html",
            extension = "html",
            content = "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n    <meta charset=\"UTF-8\">\n    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n    <title>Welcome to FileFlip</title>\n    <style>\n        body { font-family: Arial, sans-serif; margin: 20px; }\n        h1 { color: #0DA6F2; }\n        p { line-height: 1.6; }\n    </style>\n</head>\n<body>\n    <h1>Welcome to FileFlip</h1>\n    <p>This is a demo HTML page. FileFlip helps you edit and manage your files with ease.</p>\n    <p>Features include:</p>\n    <ul>\n        <li>Rich text editing</li>\n        <li>Multiple file format support</li>\n        <li>PDF export</li>\n    </ul>\n</body>\n</html>",
            size = "1.9 KB",
            date = currentDate,
            path = "/demo/Welcome_Page.html",
            isStarred = true,
            isRecent = true,
            icon = Icons.Rounded.Description,
            iconColor = Color(0xFFE91E63),
            preview = "<!DOCTYPE html>\n<html>\n<head>\n    <title>Welcome to FileFlip</title>\n</head>\n<body>\n    <h1>Welcome to FileFlip</h1>\n    <p>This is a demo HTML page.</p>\n    <p>Features include:</p>\n    <ul>\n        <li>Rich text editing</li>\n        <li>Multiple file format support</li>\n    </ul>\n</body>\n</html>"
        ),
        DemoFile(
            id = "demo_log_1",
            name = "App_Logs.log",
            extension = "log",
            content = "[2024-01-15 10:00:00] INFO: Application started successfully\n[2024-01-15 10:00:01] INFO: Database connection established\n[2024-01-15 10:00:02] INFO: Loading user preferences\n[2024-01-15 10:00:05] INFO: File editor initialized\n[2024-01-15 10:00:10] WARN: Low memory warning\n[2024-01-15 10:00:15] INFO: Auto-save completed\n[2024-01-15 10:00:20] ERROR: Failed to connect to external service\n[2024-01-15 10:00:25] INFO: Application shutdown",
            size = "1.3 KB",
            date = currentDate,
            path = "/demo/App_Logs.log",
            isStarred = false,
            isRecent = true,
            icon = Icons.Rounded.Info,
            iconColor = Color(0xFFFF9800),
            preview = "[2024-01-15 10:00:00] INFO: Application started successfully\n[2024-01-15 10:00:01] INFO: Database connection established\n[2024-01-15 10:00:02] INFO: Loading user preferences\n[2024-01-15 10:00:05] INFO: File editor initialized\n[2024-01-15 10:00:10] WARN: Low memory warning"
        ),
        DemoFile(
            id = "demo_csv_1",
            name = "User_Statistics.csv",
            extension = "csv",
            content = "User ID,Name,Email,Registration Date,Last Login,Files Created\n1,John Doe,john@example.com,2024-01-01,2024-01-15,25\n2,Jane Smith,jane@example.com,2024-01-05,2024-01-14,18\n3,Bob Johnson,bob@example.com,2024-01-10,2024-01-13,32\n4,Alice Brown,alice@example.com,2024-01-12,2024-01-15,15\n5,Charlie Wilson,charlie@example.com,2024-01-14,2024-01-15,8",
            size = "1.7 KB",
            date = currentDate,
            path = "/demo/User_Statistics.csv",
            isStarred = true,
            isRecent = false,
            icon = Icons.Rounded.GridView,
            iconColor = Color(0xFF4CAF50),
            preview = "User ID,Name,Email,Registration Date,Last Login,Files Created\n1,John Doe,john@example.com,2024-01-01,2024-01-15,25\n2,Jane Smith,jane@example.com,2024-01-05,2024-01-14,18\n3,Bob Johnson,bob@example.com,2024-01-10,2024-01-13,32"
        )
    )

    // Get files for File Explorer tabs (filtering out deleted ones)
    fun getFilesForTab(tab: String): List<DemoFile> {
        return demoFiles.filter { !it.isDeleted }.filter {
            when (tab.lowercase()) {
                "starred" -> it.isStarred
                "recent" -> it.isRecent
                else -> true // "files" tab shows all non-deleted
            }
        }
    }

    // Get all demo files for templates (ignoring deletion status)
    fun getAllForTemplates(): List<DemoFile> {
        return demoFiles
    }

    // Mark a file as deleted
    fun markAsDeleted(fileId: String, context: Context) {
        demoFiles.find { it.id == fileId }?.isDeleted = true
        saveDeletedState(context)
    }

    // Update file content
    fun updateContent(fileId: String, newContent: String, context: Context) {
        demoFiles.find { it.id == fileId }?.content = newContent
        saveContentUpdates(context)
    }

    // Get file content
    fun getContent(fileId: String): String? {
        return demoFiles.find { it.id == fileId }?.content
    }

    // Check if a file is deleted
    fun isDeleted(fileId: String): Boolean {
        return demoFiles.find { it.id == fileId }?.isDeleted ?: false
    }
}