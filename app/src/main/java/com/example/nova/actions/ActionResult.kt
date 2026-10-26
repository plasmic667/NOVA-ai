package com.example.nova.actions

data class ActionResult(
    val success: Boolean,
    val message: String,
    val launchedIntent: Boolean = false,
    val requiresUserInput: Boolean = false,
    val pendingAction: PendingAction? = null
)

data class PendingAction(
    val id: String,
    val messageId: String?,
    val actionType: ActionType,
    val riskLevel: ActionRiskLevel,
    val title: String,
    val description: String,
    val parameters: Map<String, Any?>
)
