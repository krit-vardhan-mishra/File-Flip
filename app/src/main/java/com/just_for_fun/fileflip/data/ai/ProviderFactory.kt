package com.just_for_fun.fileflip.data.ai

import com.just_for_fun.fileflip.ui.screens.SettingsState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderFactory @Inject constructor(
    private val geminiProvider: GeminiProvider,
    private val groqProvider: GroqProvider,
    private val openRouterProvider: OpenRouterProvider
) {

    fun getProvider(): AiProvider {
        return when (SettingsState.aiProvider) {
            "Google Gemini" -> geminiProvider
            "Groq" -> groqProvider
            "OpenRouter" -> openRouterProvider
            else -> {
                when {
                    SettingsState.geminiApiKey.isNotEmpty() -> geminiProvider
                    SettingsState.groqApiKey.isNotEmpty() -> groqProvider
                    SettingsState.openRouterApiKey.isNotEmpty() -> openRouterProvider
                    else -> geminiProvider
                }
            }
        }
    }

    fun getProviderByName(name: String): AiProvider {
        return when (name) {
            "Google Gemini" -> geminiProvider
            "Groq" -> groqProvider
            else -> openRouterProvider
        }
    }

    fun getApiKey(providerName: String): String {
        return when (providerName) {
            "Google Gemini" -> SettingsState.geminiApiKey
            "Groq" -> SettingsState.groqApiKey
            "OpenRouter" -> SettingsState.openRouterApiKey
            else -> ""
        }
    }

    fun getModelName(providerName: String): String {
        return when (providerName) {
            "Google Gemini" -> SettingsState.geminiModelName
            "Groq" -> SettingsState.groqModelName
            "OpenRouter" -> SettingsState.openRouterModelName
            else -> ""
        }
    }

    fun getProviderName(): String {
        return SettingsState.aiProvider.ifEmpty {
            when {
                SettingsState.geminiApiKey.isNotEmpty() -> "Google Gemini"
                SettingsState.groqApiKey.isNotEmpty() -> "Groq"
                SettingsState.openRouterApiKey.isNotEmpty() -> "OpenRouter"
                else -> ""
            }
        }
    }

    fun getFallbackProvider(excludeProviderName: String): AiProvider? {
        val candidates = listOf("Google Gemini", "Groq", "OpenRouter")
        for (candidate in candidates) {
            if (candidate != excludeProviderName) {
                val key = getApiKey(candidate)
                if (key.isNotEmpty()) {
                    return getProviderByName(candidate)
                }
            }
        }
        return null
    }
}
