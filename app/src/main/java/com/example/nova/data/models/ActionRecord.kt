package com.example.nova.data.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID

@IgnoreExtraProperties
data class ActionRecord(
    @DocumentId val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val actionType: String = "",
    val riskLevel: String = "LOW", // "LOW", "MEDIUM", "HIGH"
    val description: String = "",
    val status: String = "SUCCESS", // "SUCCESS", "CANCELLED", "FAILED"
    @ServerTimestamp val createdAt: Timestamp? = null
)
