package com.example.nova.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val userId: String,
    val role: String,
    val content: String,
    val actionType: String?,
    val actionStatus: String?,
    val actionPayload: String?,
    val createdAtEpoch: Long
)
