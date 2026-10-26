package com.example.nova.actions

enum class ActionType(val displayName: String, val defaultRisk: ActionRiskLevel) {
    OPEN_APP("Open App", ActionRiskLevel.LOW),
    WEB_SEARCH("Search Web", ActionRiskLevel.LOW),
    OPEN_URL("Open Website", ActionRiskLevel.LOW),
    SET_TIMER("Set Timer", ActionRiskLevel.MEDIUM),
    CREATE_ALARM("Set Alarm", ActionRiskLevel.MEDIUM),
    CREATE_CALENDAR_EVENT("Create Event", ActionRiskLevel.MEDIUM),
    OPEN_SETTINGS("Open Settings", ActionRiskLevel.LOW),
    MAKE_PHONE_CALL("Phone Call", ActionRiskLevel.HIGH),
    SEND_SMS("Send SMS", ActionRiskLevel.HIGH),
    SEND_EMAIL("Send Email", ActionRiskLevel.HIGH),
    GET_CURRENT_TIME("Check Time", ActionRiskLevel.LOW),
    GET_CURRENT_DATE("Check Date", ActionRiskLevel.LOW),
    SHOW_NOTIFICATION("Show Notification", ActionRiskLevel.LOW),
    START_MAPS("Open Navigation", ActionRiskLevel.LOW),
    SEARCH_MAPS("Search Maps", ActionRiskLevel.LOW),
    OPEN_CAMERA("Open Camera", ActionRiskLevel.LOW),
    OPEN_GALLERY("Open Photos", ActionRiskLevel.LOW);

    companion object {
        fun fromString(name: String?): ActionType? {
            if (name == null) return null
            return entries.firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
        }
    }
}
