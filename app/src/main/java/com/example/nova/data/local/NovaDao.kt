package com.example.nova.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NovaDao {
    @Query("SELECT * FROM conversations WHERE userId = :userId ORDER BY updatedAtEpoch DESC")
    fun observeConversations(userId: String): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAtEpoch ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET actionStatus = :status WHERE id = :messageId")
    suspend fun updateMessageActionStatus(messageId: String, status: String)

    @Query("SELECT * FROM actions WHERE userId = :userId ORDER BY createdAtEpoch DESC")
    fun observeActions(userId: String): Flow<List<ActionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: ActionEntity)

    @Query("DELETE FROM conversations WHERE userId = :userId")
    suspend fun clearUserConversations(userId: String)

    @Query("DELETE FROM messages WHERE userId = :userId")
    suspend fun clearUserMessages(userId: String)

    @Query("DELETE FROM actions WHERE userId = :userId")
    suspend fun clearUserActions(userId: String)

    @Query("DELETE FROM conversations")
    suspend fun clearAllConversations()

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    @Query("DELETE FROM actions")
    suspend fun clearAllActions()
}
