package com.example.nova.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "New Conversation",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
