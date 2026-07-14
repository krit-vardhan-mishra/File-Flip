package com.just_for_fun.fileflip.data.ai

import com.just_for_fun.fileflip.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

interface AiProvider {

    suspend fun chat(
        systemPrompt: String,
        history: List<ChatMessageEntity>,
        apiKey: String,
        modelName: String
    ): String

    fun chatStream(
        systemPrompt: String,
        history: List<ChatMessageEntity>,
        apiKey: String,
        modelName: String
    ): Flow<String>

    fun providerName(): String
}
