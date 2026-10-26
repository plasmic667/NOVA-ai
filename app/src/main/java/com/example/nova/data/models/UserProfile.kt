package com.example.nova.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val voiceAutoPlay: String = "VOICE_ONLY", // "ALWAYS", "VOICE_ONLY", "NEVER"
    val speechSpeed: Double = 1.0,
    val agentModeEnabled: Boolean = false,
    val syncToCloud: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
