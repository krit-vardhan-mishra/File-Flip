package com.just_for_fun.fileflip.domain.repository

import com.just_for_fun.fileflip.data.local.entity.ChatMessageEntity
import com.just_for_fun.fileflip.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getSessionsForFile(filePath: String): Flow<List<ChatSessionEntity>>

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    suspend fun createSession(filePath: String, title: String): ChatSessionEntity

    suspend fun updateSessionTitle(sessionId: String, title: String)

    suspend fun deleteSession(sessionId: String)

    suspend fun addMessage(sessionId: String, role: String, content: String): ChatMessageEntity

    suspend fun clearMessagesForSession(sessionId: String)

    suspend fun deleteMessages(messages: List<ChatMessageEntity>)
}
