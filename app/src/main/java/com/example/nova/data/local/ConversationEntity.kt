package com.example.nova.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val createdAtEpoch: Long,
    val updatedAtEpoch: Long
)
