package com.just_for_fun.fileflip.data.repository

import com.just_for_fun.fileflip.data.local.dao.ChatMessageDao
import com.just_for_fun.fileflip.data.local.dao.ChatSessionDao
import com.just_for_fun.fileflip.data.local.entity.ChatMessageEntity
import com.just_for_fun.fileflip.data.local.entity.ChatSessionEntity
import com.just_for_fun.fileflip.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val sessionDao: ChatSessionDao,
    private val messageDao: ChatMessageDao
) : ChatRepository {

    override fun getSessionsForFile(filePath: String): Flow<List<ChatSessionEntity>> {
        return sessionDao.getSessionsForFile(filePath)
    }

    override fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>> {
        return messageDao.getMessagesForSession(sessionId)
    }

    override suspend fun createSession(filePath: String, title: String): ChatSessionEntity {
        val session = ChatSessionEntity(
            id = UUID.randomUUID().toString(),
            filePath = filePath,
            title = title
        )
        sessionDao.insertSession(session)
        return session
    }

    override suspend fun updateSessionTitle(sessionId: String, title: String) {
        sessionDao.getSessionsForFile("").let {
            // Not ideal - we need a direct query
        }
        // Better approach: use a direct update query
        val session = ChatSessionEntity(
            id = sessionId,
            filePath = "",
            title = title,
            updatedAt = System.currentTimeMillis()
        )
        sessionDao.updateSession(session)
    }

    override suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteSessionById(sessionId)
    }

    override suspend fun addMessage(sessionId: String, role: String, content: String): ChatMessageEntity {
        val message = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            sessionId = sessionId,
            role = role,
            content = content
        )
        messageDao.insertMessage(message)
        return message
    }

    override suspend fun clearMessagesForSession(sessionId: String) {
        messageDao.deleteMessagesForSession(sessionId)
    }

    override suspend fun deleteMessages(messages: List<ChatMessageEntity>) {
        messageDao.deleteMessages(messages)
    }
}
