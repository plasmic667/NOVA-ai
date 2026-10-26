package com.example.nova.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actions")
data class ActionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val actionType: String,
    val riskLevel: String,
    val description: String,
    val status: String,
    val createdAtEpoch: Long
)
