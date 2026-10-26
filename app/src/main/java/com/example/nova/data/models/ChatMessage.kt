package com.example.nova.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = UUID.randomUUID().toString(),
    val conversationId: String = "",
    val userId: String = "",
    val role: String = "assistant", // "user" or "assistant"
    val content: String = "",
    val actionType: String? = null,
    val actionStatus: String? = null, // "PENDING", "APPROVED", "EXECUTED", "CANCELLED", "FAILED"
    val actionPayload: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
)
