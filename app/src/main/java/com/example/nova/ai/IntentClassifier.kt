package com.example.nova.ai

import com.example.nova.actions.ActionType
import java.util.regex.Pattern

data class ClassifiedIntent(
    val actionType: ActionType,
    val parameters: Map<String, Any?>,
    val defaultResponse: String
)

object IntentClassifier {

    /**
     * Attempts fast, deterministic classification of common Android assistant commands.
     * Returns a ClassifiedIntent if recognized with high confidence, or null if query should defer to LLM.
     */
    fun classifyLocal(input: String): ClassifiedIntent? {
        val trimmed = input.trim()
        val lower = trimmed.lowercase()

        // 1. Time query
        if (lower in listOf("what time is it", "what's the time", "current time", "tell me the time", "check time", "time")) {
            return ClassifiedIntent(
                actionType = ActionType.GET_CURRENT_TIME,
                parameters = emptyMap(),
                defaultResponse = "Checking current time..."
            )
        }

        // 2. Date query
        if (lower in listOf("what's today's date", "what is today's date", "what is the date", "what's the date", "what day is today", "current date", "today's date", "date")) {
            return ClassifiedIntent(
                actionType = ActionType.GET_CURRENT_DATE,
                parameters = emptyMap(),
                defaultResponse = "Checking today's date..."
            )
        }

        // 3. Open Camera
        if (lower in listOf("open camera", "launch camera", "take a picture", "take a photo", "camera")) {
            return ClassifiedIntent(
                actionType = ActionType.OPEN_CAMERA,
                parameters = emptyMap(),
                defaultResponse = "Opening Camera for you."
            )
        }

        // 4. Open Gallery / Photos
        if (lower in listOf("open gallery", "open photos", "show photos", "view photos", "gallery", "photos")) {
            return ClassifiedIntent(
                actionType = ActionType.OPEN_GALLERY,
                parameters = emptyMap(),
                defaultResponse = "Opening Photos."
            )
        }

        // 5. Open Settings
        if (lower in listOf("open settings", "launch settings", "device settings", "system settings", "settings")) {
            return ClassifiedIntent(
                actionType = ActionType.OPEN_SETTINGS,
                parameters = emptyMap(),
                defaultResponse = "Opening Device Settings."
            )
        }

        // 6. Open App: "open [app]", "launch [app]", "start [app]"
        val openAppPattern = Pattern.compile("^(?:open|launch|start|run)\\s+(?:the\\s+)?([a-zA-Z0-9\\s]+?)(?:\\s+app)?$", Pattern.CASE_INSENSITIVE)
        val openAppMatcher = openAppPattern.matcher(trimmed)
        if (openAppMatcher.matches()) {
            val appName = openAppMatcher.group(1)?.trim() ?: ""
            if (appName.isNotBlank() && appName.lowercase() !in listOf("a timer", "timer", "an alarm", "alarm", "settings", "camera", "gallery", "photos", "maps")) {
                return ClassifiedIntent(
                    actionType = ActionType.OPEN_APP,
                    parameters = mapOf("appName" to appName, "target" to appName),
                    defaultResponse = "Opening $appName..."
                )
            }
        }

        // 7. Web Search: "search for [query]", "search the web for [query]", "google [query]"
        val searchPattern = Pattern.compile("^(?:search(?:\\s+the\\s+web)?(?:\\s+for)?|google)\\s+(.+)$", Pattern.CASE_INSENSITIVE)
        val searchMatcher = searchPattern.matcher(trimmed)
        if (searchMatcher.matches()) {
            val query = searchMatcher.group(1)?.trim() ?: ""
            if (query.isNotBlank()) {
                return ClassifiedIntent(
                    actionType = ActionType.WEB_SEARCH,
                    parameters = mapOf("query" to query),
                    defaultResponse = "Searching the web for \"$query\"..."
                )
            }
        }

        // 8. Maps search / Directions: "find [query] near me", "restaurants near me", "directions to [place]", "open maps"
        if (lower in listOf("open maps", "open google maps", "maps", "navigation")) {
            return ClassifiedIntent(
                actionType = ActionType.START_MAPS,
                parameters = mapOf("destination" to ""),
                defaultResponse = "Opening Google Maps."
            )
        }
        val mapsPattern = Pattern.compile("^(?:find|search(?:\\s+for)?)\\s+(.+?\\s+near\\s+me)$", Pattern.CASE_INSENSITIVE)
        val mapsMatcher = mapsPattern.matcher(trimmed)
        if (mapsMatcher.matches() || lower.endsWith("near me")) {
            val query = if (mapsMatcher.matches()) mapsMatcher.group(1)?.trim() ?: trimmed else trimmed
            return ClassifiedIntent(
                actionType = ActionType.SEARCH_MAPS,
                parameters = mapOf("query" to query),
                defaultResponse = "Finding $query on Maps..."
            )
        }
        val directionsPattern = Pattern.compile("^(?:show\\s+)?directions\\s+to\\s+(.+)$", Pattern.CASE_INSENSITIVE)
        val directionsMatcher = directionsPattern.matcher(trimmed)
        if (directionsMatcher.matches()) {
            val destination = directionsMatcher.group(1)?.trim() ?: ""
            return ClassifiedIntent(
                actionType = ActionType.START_MAPS,
                parameters = mapOf("destination" to destination),
                defaultResponse = "Starting navigation to $destination..."
            )
        }

        // 9. Timer: "set a timer for [X] minutes", "timer for [X] mins"
        val timerPattern = Pattern.compile("^(?:set\\s+(?:a\\s+)?)?timer(?:\\s+for)?\\s+(\\d+)\\s*(minutes?|mins?|seconds?|secs?|hours?|hrs?)$", Pattern.CASE_INSENSITIVE)
        val timerMatcher = timerPattern.matcher(trimmed)
        if (timerMatcher.matches()) {
            val num = timerMatcher.group(1)?.toIntOrNull() ?: 10
            val unit = timerMatcher.group(2)?.lowercase() ?: "minutes"
            val totalSeconds = when {
                unit.startsWith("sec") -> num
                unit.startsWith("hour") || unit.startsWith("hr") -> num * 3600
                else -> num * 60
            }
            return ClassifiedIntent(
                actionType = ActionType.SET_TIMER,
                parameters = mapOf("durationSeconds" to totalSeconds, "message" to "Nova Timer"),
                defaultResponse = "Setting a timer for $num $unit."
            )
        }

        // 10. Alarm: "set an alarm for 7 AM", "alarm for 6:30 PM"
        val alarmPattern = Pattern.compile("^(?:set\\s+(?:an\\s+)?)?alarm(?:\\s+for|\\s+at)?\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?$", Pattern.CASE_INSENSITIVE)
        val alarmMatcher = alarmPattern.matcher(trimmed)
        if (alarmMatcher.matches()) {
            var hour = alarmMatcher.group(1)?.toIntOrNull() ?: 7
            val minute = alarmMatcher.group(2)?.toIntOrNull() ?: 0
            val ampm = alarmMatcher.group(3)?.lowercase()

            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            return ClassifiedIntent(
                actionType = ActionType.CREATE_ALARM,
                parameters = mapOf("hour" to hour, "minute" to minute, "message" to "Nova Alarm"),
                defaultResponse = "Setting an alarm for ${String.format("%02d:%02d", hour, minute)}."
            )
        }

        // 11. Phone call: "call [target]"
        val callPattern = Pattern.compile("^call\\s+(.+)$", Pattern.CASE_INSENSITIVE)
        val callMatcher = callPattern.matcher(trimmed)
        if (callMatcher.matches()) {
            val contact = callMatcher.group(1)?.trim() ?: ""
            return ClassifiedIntent(
                actionType = ActionType.MAKE_PHONE_CALL,
                parameters = mapOf("phoneNumber" to contact, "contact" to contact),
                defaultResponse = "Preparing to call $contact. Please confirm."
            )
        }

        // 12. SMS: "send (a) message to [recipient] saying/that [msg]", "text [recipient] that/saying [msg]"
        val smsPattern = Pattern.compile("^(?:send\\s+(?:a\\s+)?(?:sms|message)\\s+to|text)\\s+([a-zA-Z0-9\\s+]+?)\\s+(?:saying|that)\\s+(.+)$", Pattern.CASE_INSENSITIVE)
        val smsMatcher = smsPattern.matcher(trimmed)
        if (smsMatcher.matches()) {
            val recipient = smsMatcher.group(1)?.trim() ?: ""
            val messageText = smsMatcher.group(2)?.trim() ?: ""
            return ClassifiedIntent(
                actionType = ActionType.SEND_SMS,
                parameters = mapOf("recipient" to recipient, "message" to messageText),
                defaultResponse = "Ready to send text to $recipient: \"$messageText\""
            )
        }

        return null
    }
}
