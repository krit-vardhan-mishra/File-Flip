package com.just_for_fun.fileflip.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.GppMaybe
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.just_for_fun.fileflip.ui.theme.ThemeManager
import com.just_for_fun.fileflip.ui.theme.LocalAppColors
import com.just_for_fun.fileflip.data.local.util.ModelDownloader
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint


// Global settings state for demo
object SettingsState {
    var selectedFont by mutableIntStateOf(2) // 0: Default, 1: Serif, 2: Monospace
    var defaultSaveDirectory by mutableStateOf<String?>(null)
    var editorTextSize by mutableFloatStateOf(16f)
    var previewTextSize by mutableFloatStateOf(18f)

    // Provider specific API Keys (decrypted in memory)
    var geminiApiKey by mutableStateOf("")
    var groqApiKey by mutableStateOf("")
    var openRouterApiKey by mutableStateOf("")

    // Provider specific Models
    var geminiModelName by mutableStateOf("gemini-1.5-flash")
    var groqModelName by mutableStateOf("llama3-8b-8192")
    var openRouterModelName by mutableStateOf("google/gemini-2.5-flash")

    // Primary Provider Selection
    var aiProvider by mutableStateOf("Google Gemini") // Default provider
    var translationLanguage by mutableStateOf("Spanish")

    // Compatibility properties to avoid breaking other parts of the app
    val apiKey: String
        get() = when (aiProvider) {
            "Google Gemini" -> geminiApiKey
            "Groq" -> groqApiKey
            "OpenRouter" -> openRouterApiKey
            else -> ""
        }
    
    val aiModelName: String
        get() = when (aiProvider) {
            "Google Gemini" -> geminiModelName
            "Groq" -> groqModelName
            "OpenRouter" -> openRouterModelName
            else -> ""
        }

    var isApiKeyConfigured by mutableStateOf(false)
    var hasShownApiKeyPromptThisSession = false

    fun detectProvider(key: String): String = when {
        key.startsWith("AIza") -> "Google Gemini"
        key.startsWith("gsk_") -> "Groq"
        key.isNotEmpty() -> "OpenRouter"
        else -> ""
    }

    fun suggestModelName(key: String): String = when {
        key.startsWith("AIza") -> "gemini-1.5-flash"
        key.startsWith("gsk_") -> "llama3-8b-8192"
        key.isNotEmpty() -> "google/gemini-2.5-flash"
        else -> ""
    }

    fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        
        val encryptedGemini = prefs.getString("gemini_api_key", "") ?: ""
        geminiApiKey = com.just_for_fun.fileflip.data.local.EncryptionHelper.decrypt(encryptedGemini)
        
        val encryptedGroq = prefs.getString("groq_api_key", "") ?: ""
        groqApiKey = com.just_for_fun.fileflip.data.local.EncryptionHelper.decrypt(encryptedGroq)
        
        val encryptedOpenRouter = prefs.getString("openrouter_api_key", "") ?: ""
        openRouterApiKey = com.just_for_fun.fileflip.data.local.EncryptionHelper.decrypt(encryptedOpenRouter)

        geminiModelName = prefs.getString("gemini_model_name", "gemini-1.5-flash") ?: "gemini-1.5-flash"
        groqModelName = prefs.getString("groq_model_name", "llama3-8b-8192") ?: "llama3-8b-8192"
        openRouterModelName = prefs.getString("openrouter_model_name", "google/gemini-2.5-flash") ?: "google/gemini-2.5-flash"

        aiProvider = prefs.getString("ai_provider", "Google Gemini") ?: "Google Gemini"
        translationLanguage = prefs.getString("translation_language", "Spanish") ?: "Spanish"
        isApiKeyConfigured = apiKey.isNotEmpty()

        // Migration check for old simple key
        val oldKey = prefs.getString("api_key", "") ?: ""
        if (oldKey.isNotEmpty()) {
            val decryptedOldKey = com.just_for_fun.fileflip.data.local.EncryptionHelper.decrypt(oldKey).ifEmpty { oldKey }
            if (decryptedOldKey.isNotEmpty()) {
                val detected = detectProvider(decryptedOldKey)
                when (detected) {
                    "Google Gemini" -> {
                        geminiApiKey = decryptedOldKey
                        saveGeminiApiKey(context, decryptedOldKey)
                    }
                    "Groq" -> {
                        groqApiKey = decryptedOldKey
                        saveGroqApiKey(context, decryptedOldKey)
                    }
                    "OpenRouter" -> {
                        openRouterApiKey = decryptedOldKey
                        saveOpenRouterApiKey(context, decryptedOldKey)
                    }
                }
                // Clear old plain/encrypted key to finish migration
                prefs.edit().remove("api_key").apply()
            }
        }
    }

    fun saveGeminiApiKey(context: Context, key: String) {
        geminiApiKey = key
        isApiKeyConfigured = apiKey.isNotEmpty()
        val encrypted = com.just_for_fun.fileflip.data.local.EncryptionHelper.encrypt(key)
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("gemini_api_key", encrypted)
            .apply()
    }

    fun saveGroqApiKey(context: Context, key: String) {
        groqApiKey = key
        isApiKeyConfigured = apiKey.isNotEmpty()
        val encrypted = com.just_for_fun.fileflip.data.local.EncryptionHelper.encrypt(key)
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("groq_api_key", encrypted)
            .apply()
    }

    fun saveOpenRouterApiKey(context: Context, key: String) {
        openRouterApiKey = key
        isApiKeyConfigured = apiKey.isNotEmpty()
        val encrypted = com.just_for_fun.fileflip.data.local.EncryptionHelper.encrypt(key)
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("openrouter_api_key", encrypted)
            .apply()
    }

    fun saveGeminiModel(context: Context, model: String) {
        geminiModelName = model
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("gemini_model_name", model)
            .apply()
    }

    fun saveGroqModel(context: Context, model: String) {
        groqModelName = model
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("groq_model_name", model)
            .apply()
    }

    fun saveOpenRouterModel(context: Context, model: String) {
        openRouterModelName = model
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("openrouter_model_name", model)
            .apply()
    }

    fun saveAiProvider(context: Context, provider: String) {
        aiProvider = provider
        isApiKeyConfigured = apiKey.isNotEmpty()
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("ai_provider", provider)
            .apply()
    }

    fun saveTranslationLanguage(context: Context, language: String) {
        translationLanguage = language
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("translation_language", language)
            .apply()
    }
}

// Design Colors - now pulled from theme
private val PrimaryBlue: Color @Composable get() = LocalAppColors.current.primaryBlue
private val BackgroundDark: Color @Composable get() = LocalAppColors.current.background
private val SurfaceDark: Color @Composable get() = LocalAppColors.current.surface
private val BorderColor: Color @Composable get() = LocalAppColors.current.border
private val TextWhite: Color @Composable get() = LocalAppColors.current.textPrimary
private val TextGray: Color @Composable get() = LocalAppColors.current.textSecondary


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val modelDownloader = remember {
        val entryPoint = EntryPoints.get(context.applicationContext, SettingsEntryPoint::class.java)
        entryPoint.modelDownloader()
    }

    LaunchedEffect(Unit) {
        SettingsState.loadSettings(context)
    }

    var textSize by remember { mutableFloatStateOf(SettingsState.editorTextSize) }
    var previewTextSize by remember { mutableFloatStateOf(SettingsState.previewTextSize) }
    var selectedTheme by remember { mutableIntStateOf(ThemeManager.currentThemeIndex) }
    var fontExpanded by remember { mutableStateOf(false) }
    var activeProvider by remember { mutableStateOf(SettingsState.aiProvider) }

    var geminiKeyInput by remember { mutableStateOf(SettingsState.geminiApiKey) }
    var geminiModelInput by remember { mutableStateOf(SettingsState.geminiModelName) }

    var groqKeyInput by remember { mutableStateOf(SettingsState.groqApiKey) }
    var groqModelInput by remember { mutableStateOf(SettingsState.groqModelName) }

    var openRouterKeyInput by remember { mutableStateOf(SettingsState.openRouterApiKey) }
    var openRouterModelInput by remember { mutableStateOf(SettingsState.openRouterModelName) }

    LaunchedEffect(SettingsState.aiProvider, SettingsState.geminiApiKey, SettingsState.groqApiKey, SettingsState.openRouterApiKey) {
        activeProvider = SettingsState.aiProvider
        geminiKeyInput = SettingsState.geminiApiKey
        geminiModelInput = SettingsState.geminiModelName
        groqKeyInput = SettingsState.groqApiKey
        groqModelInput = SettingsState.groqModelName
        openRouterKeyInput = SettingsState.openRouterApiKey
        openRouterModelInput = SettingsState.openRouterModelName
    }

    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            SettingsState.defaultSaveDirectory = uri.toString()
            Log.d("FileFlip", "Selected directory: $uri")
        }
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = "Back",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark.copy(alpha = 0.95f)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // Space for bottom bar
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // --- Editor Section ---
            item { SectionHeader("EDITOR") }
            item {
                SettingsCard {
                    // Text Size
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Text Size", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("${textSize.toInt()}sp", color = PrimaryBlue, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Slider(
                            value = textSize,
                            onValueChange = { 
                                textSize = it
                                SettingsState.editorTextSize = it
                            },
                            valueRange = 10f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryBlue,
                                activeTrackColor = PrimaryBlue,
                                inactiveTrackColor = PrimaryBlue.copy(alpha = 0.2f)
                            )
                        )
                    }

                    Divider(color = BorderColor, thickness = 1.dp)

                    // Font Family
                    ExposedDropdownMenuBox(
                        expanded = fontExpanded,
                        onExpandedChange = { fontExpanded = it },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clickable { fontExpanded = true },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.FontDownload, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Font Family", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (SettingsState.selectedFont) {
                                        0 -> "Default"
                                        1 -> "Serif"
                                        2 -> "Monospace"
                                        else -> "Monospace"
                                    },
                                    color = TextGray,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    if (fontExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.UnfoldMore,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        ExposedDropdownMenu(
                            expanded = fontExpanded,
                            onDismissRequest = { fontExpanded = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Default", color = TextWhite) },
                                onClick = {
                                    SettingsState.selectedFont = 0
                                    fontExpanded = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                            )
                            DropdownMenuItem(
                                text = { Text("Serif", color = TextWhite) },
                                onClick = {
                                    SettingsState.selectedFont = 1
                                    fontExpanded = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                            )
                            DropdownMenuItem(
                                text = { Text("Monospace", color = TextWhite) },
                                onClick = {
                                    SettingsState.selectedFont = 2
                                    fontExpanded = false
                                },
                                colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                            )
                        }
                    }

                    Divider(color = BorderColor, thickness = 1.dp)

                    // Default Save Directory
                    SettingRowItem(
                        icon = Icons.Rounded.FolderOpen,
                        title = "Default Save Directory",
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = SettingsState.defaultSaveDirectory?.let { "Selected" } ?: "Not set",
                                    color = TextGray,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        onClick = { directoryPickerLauncher.launch(null) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- AI Configuration Section ---
            item { SectionHeader("AI AGENT CONFIGURATION") }
            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Active AI Provider",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        var providerExpanded by remember { mutableStateOf(false) }
                        val providersList = listOf("Google Gemini", "Groq", "OpenRouter")
                        ExposedDropdownMenuBox(
                            expanded = providerExpanded,
                            onExpandedChange = { providerExpanded = !providerExpanded }
                        ) {
                            OutlinedTextField(
                                value = activeProvider,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (providerExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.UnfoldMore,
                                        contentDescription = "Expand Provider Menu",
                                        tint = TextGray
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = providerExpanded,
                                onDismissRequest = { providerExpanded = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                providersList.forEach { provider ->
                                    DropdownMenuItem(
                                        text = { Text(provider, color = TextWhite) },
                                        onClick = {
                                            activeProvider = provider
                                            SettingsState.saveAiProvider(context, provider)
                                            providerExpanded = false
                                        },
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Provider API Credentials",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Google Gemini config card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeProvider == "Google Gemini") PrimaryBlue.copy(alpha = 0.4f) else BorderColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Google Gemini", color = if (activeProvider == "Google Gemini") PrimaryBlue else TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = geminiKeyInput,
                                    onValueChange = {
                                        geminiKeyInput = it
                                        SettingsState.saveGeminiApiKey(context, it)
                                    },
                                    placeholder = { Text("Gemini API Key (AIza...)", color = TextGray, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderColor
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = geminiModelInput,
                                    onValueChange = {
                                        geminiModelInput = it
                                        SettingsState.saveGeminiModel(context, it)
                                    },
                                    placeholder = { Text("Gemini Model (gemini-1.5-flash)", color = TextGray, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderColor
                                    )
                                )
                            }
                        }

                        // Groq config card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeProvider == "Groq") PrimaryBlue.copy(alpha = 0.4f) else BorderColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Groq", color = if (activeProvider == "Groq") PrimaryBlue else TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = groqKeyInput,
                                    onValueChange = {
                                        groqKeyInput = it
                                        SettingsState.saveGroqApiKey(context, it)
                                    },
                                    placeholder = { Text("Groq API Key (gsk_...)", color = TextGray, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderColor
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = groqModelInput,
                                    onValueChange = {
                                        groqModelInput = it
                                        SettingsState.saveGroqModel(context, it)
                                    },
                                    placeholder = { Text("Groq Model (llama3-8b-8192)", color = TextGray, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderColor
                                    )
                                )
                            }
                        }

                        // OpenRouter config card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (activeProvider == "OpenRouter") PrimaryBlue.copy(alpha = 0.4f) else BorderColor.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("OpenRouter", color = if (activeProvider == "OpenRouter") PrimaryBlue else TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = openRouterKeyInput,
                                    onValueChange = {
                                        openRouterKeyInput = it
                                        SettingsState.saveOpenRouterApiKey(context, it)
                                    },
                                    placeholder = { Text("OpenRouter API Key", color = TextGray, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderColor
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = openRouterModelInput,
                                    onValueChange = {
                                        openRouterModelInput = it
                                        SettingsState.saveOpenRouterModel(context, it)
                                    },
                                    placeholder = { Text("OpenRouter Model (google/gemini-2.5-flash)", color = TextGray, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                                        focusedBorderColor = PrimaryBlue, unfocusedBorderColor = BorderColor
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI Translation Target Language",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var languageExpanded by remember { mutableStateOf(false) }
                        val languages = listOf("Spanish", "French", "German", "Hindi", "Japanese", "English")
                        
                        ExposedDropdownMenuBox(
                            expanded = languageExpanded,
                            onExpandedChange = { languageExpanded = !languageExpanded }
                        ) {
                            OutlinedTextField(
                                value = SettingsState.translationLanguage,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (languageExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.UnfoldMore,
                                        contentDescription = "Expand Language Menu",
                                        tint = TextGray
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = PrimaryBlue,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = languageExpanded,
                                onDismissRequest = { languageExpanded = false },
                                modifier = Modifier.background(SurfaceDark)
                            ) {
                                languages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, color = TextWhite) },
                                        onClick = {
                                            SettingsState.saveTranslationLanguage(context, lang)
                                            languageExpanded = false
                                        },
                                        colors = androidx.compose.material3.MenuDefaults.itemColors(textColor = TextWhite)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Configuring API keys enables FlipFile's AI capabilities. Empty values disable specific providers.",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
            item {
                LocalAiModelCard(modelDownloader)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- Appearance Section ---
            item { SectionHeader("APPEARANCE") }
            item {
                SettingsCard {
                    // Theme
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Theme", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        ThemeSegmentedControl(selectedTheme) { 
                            selectedTheme = it
                            ThemeManager.setTheme(it, context)
                        }
                    }

                    Divider(color = BorderColor, thickness = 1.dp)

                    // Preview Text Size
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Preview Text Size", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("A", color = TextGray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                value = previewTextSize,
                                onValueChange = { 
                                    previewTextSize = it
                                    SettingsState.previewTextSize = it
                                },
                                valueRange = 12f..24f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = PrimaryBlue,
                                    activeTrackColor = PrimaryBlue,
                                    inactiveTrackColor = PrimaryBlue.copy(alpha = 0.2f)
                                )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("A", color = TextGray, fontSize = 20.sp)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- About Section ---
            item { SectionHeader("ABOUT") }
            item {
                SettingsCard {
                    // Version
                    SettingRowItem(
                        icon = Icons.Rounded.Info,
                        title = "Version",
                        trailingContent = { Text("v${com.just_for_fun.fileflip.BuildConfig.VERSION_NAME}", color = TextGray, fontSize = 14.sp) }
                    )

                    Divider(color = BorderColor, thickness = 1.dp)

                    // GitHub
                    SettingRowItem(
                        icon = Icons.Rounded.Code, // Using Code icon as placeholder for GitHub
                        title = "GitHub Profile",
                        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp)) },
                        onClick = { uriHandler.openUri("https://github.com/krit-vardhan-mishra") }
                    )

                    Divider(color = BorderColor, thickness = 1.dp)
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }

            // --- Branding Footer ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().alpha(0.3f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryBlue, RoundedCornerShape(12.dp))
                            .shadow(10.dp, RoundedCornerShape(12.dp), spotColor = PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "FlipFile",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        color = PrimaryBlue.copy(alpha = 0.8f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingRowItem(
    icon: ImageVector,
    title: String,
    trailingContent: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        trailingContent()
    }
}

@Composable
fun ThemeSegmentedControl(selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val options = listOf("Dark", "GitHub Dark")
        options.forEachIndexed { index, text ->
            val isSelected = selectedIndex == index
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = if (isSelected) colors.primaryBlue else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    color = if (isSelected) Color.White else colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SettingsBottomBar() {
    Surface(
        color = BackgroundDark.copy(alpha = 0.9f),
        contentColor = TextGray,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 24.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsBottomNavItem(Icons.Rounded.FolderOpen, "Files", false)
            SettingsBottomNavItem(Icons.Rounded.EditNote, "Editor", false)
            SettingsBottomNavItem(Icons.Rounded.Settings, "Settings", true)
        }
    }
}

@Composable
fun SettingsBottomNavItem(icon: ImageVector, label: String, isSelected: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) PrimaryBlue else TextGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) PrimaryBlue else TextGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@InstallIn(SingletonComponent::class)
@EntryPoint
interface SettingsEntryPoint {
    fun modelDownloader(): ModelDownloader
}

@Composable
fun LocalAiModelCard(modelDownloader: ModelDownloader) {
    val downloadStatus by modelDownloader.downloadProgress.collectAsState()
    var isDownloaded by remember { mutableStateOf(modelDownloader.isModelDownloaded()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(downloadStatus) {
        isDownloaded = modelDownloader.isModelDownloaded()
    }

    SettingsCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Local AI Model (Optional)",
                color = TextWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Model Name: all-MiniLM-L6-v2 (Quantized)\n" +
                       "Model Size: ~23 MB\n" +
                       "Purpose: Offline-first semantic vector generation for local RAG context.",
                color = TextGray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val status = downloadStatus) {
                is ModelDownloader.DownloadStatus.Downloading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Downloading: ${status.progress}%",
                                color = PrimaryBlue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is ModelDownloader.DownloadStatus.Failed -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Download Failed: ${status.error}",
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    modelDownloader.startDownload()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Retry Download", color = BackgroundDark, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                else -> {
                    if (isDownloaded) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status: Downloaded & Active",
                                color = LocalAppColors.current.iconEmerald,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            TextButton(
                                onClick = {
                                    modelDownloader.deleteModel()
                                    isDownloaded = false
                                }
                            ) {
                                Text("Delete Model", color = Color(0xFFEF4444))
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Status: Not Installed",
                                color = TextGray,
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = {
                                    scope.launch {
                                        modelDownloader.startDownload()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Download Model", color = BackgroundDark, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}