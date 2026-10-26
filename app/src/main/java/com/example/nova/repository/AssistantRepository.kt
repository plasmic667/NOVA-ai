package com.example.nova.repository

import android.content.Context
import com.example.nova.data.local.ActionEntity
import com.example.nova.data.local.ConversationEntity
import com.example.nova.data.local.MessageEntity
import com.example.nova.data.local.NovaDatabase
import com.example.nova.data.models.ActionRecord
import com.example.nova.data.models.ChatMessage
import com.example.nova.data.models.Conversation
import com.example.nova.firebase.FirestoreRepository
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AssistantRepository(context: Context) {

    private val database = NovaDatabase.getDatabase(context)
    private val dao = database.novaDao()
    private val firestoreRepository = FirestoreRepository(context)

    fun observeConversations(userId: String): Flow<List<ConversationEntity>> {
        return dao.observeConversations(userId)
    }

    suspend fun createConversation(userId: String, title: String): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = ConversationEntity(
            id = id,
            userId = userId,
            title = title,
            createdAtEpoch = now,
            updatedAtEpoch = now
        )
        dao.insertConversation(entity)

        val model = Conversation(
            id = id,
            userId = userId,
            title = title
        )
        try {
            firestoreRepository.saveConversation(model)
        } catch (_: Exception) {}

        return id
    }

    suspend fun updateConversationTitle(id: String, userId: String, title: String) {
        val now = System.currentTimeMillis()
        val entity = ConversationEntity(
            id = id,
            userId = userId,
            title = title,
            createdAtEpoch = now,
            updatedAtEpoch = now
        )
        dao.insertConversation(entity)
    }

    fun observeMessages(conversationId: String): Flow<List<MessageEntity>> {
        return dao.observeMessages(conversationId)
    }

    suspend fun addMessage(
        conversationId: String,
        userId: String,
        role: String,
        content: String,
        actionType: String? = null,
        actionStatus: String? = null,
        actionPayload: String? = null,
        syncToCloud: Boolean = true
    ): String {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = MessageEntity(
            id = id,
            conversationId = conversationId,
            userId = userId,
            role = role,
            content = content,
            actionType = actionType,
            actionStatus = actionStatus,
            actionPayload = actionPayload,
            createdAtEpoch = now
        )
        dao.insertMessage(entity)

        if (syncToCloud && userId.isNotBlank()) {
            val model = ChatMessage(
                id = id,
                conversationId = conversationId,
                userId = userId,
                role = role,
                content = content,
                actionType = actionType,
                actionStatus = actionStatus,
                actionPayload = actionPayload
            )
            try {
                firestoreRepository.saveMessage(model)
            } catch (_: Exception) {}
        }

        return id
    }

    suspend fun updateMessageActionStatus(messageId: String, status: String) {
        dao.updateMessageActionStatus(messageId, status)
    }

    fun observeActions(userId: String): Flow<List<ActionEntity>> {
        return dao.observeActions(userId)
    }

    suspend fun logAction(
        userId: String,
        actionType: String,
        riskLevel: String,
        description: String,
        status: String,
        syncToCloud: Boolean = true
    ) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val entity = ActionEntity(
            id = id,
            userId = userId,
            actionType = actionType,
            riskLevel = riskLevel,
            description = description,
            status = status,
            createdAtEpoch = now
        )
        dao.insertAction(entity)

        if (syncToCloud && userId.isNotBlank()) {
            val model = ActionRecord(
                id = id,
                userId = userId,
                actionType = actionType,
                riskLevel = riskLevel,
                description = description,
                status = status
            )
            try {
                firestoreRepository.saveActionRecord(model)
            } catch (_: Exception) {}
        }
    }

    suspend fun clearLocalData() {
        dao.clearAllMessages()
        dao.clearAllConversations()
        dao.clearAllActions()
    }

    suspend fun deleteUserCloudAndLocalData(userId: String) {
        firestoreRepository.deleteUserData(userId)
        dao.clearUserMessages(userId)
        dao.clearUserConversations(userId)
        dao.clearUserActions(userId)
    }
}
