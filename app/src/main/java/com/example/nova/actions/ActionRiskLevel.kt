package com.example.nova.actions

enum class ActionRiskLevel(val label: String, val requiresConfirmation: Boolean) {
    LOW("Low Risk", false),
    MEDIUM("Medium Risk", false),
    HIGH("High Risk", true);

    companion object {
        fun fromString(level: String?): ActionRiskLevel {
            return entries.firstOrNull { it.name.equals(level?.trim(), ignoreCase = true) } ?: LOW
        }
    }
}
