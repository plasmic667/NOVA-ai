package com.example.nova.ai

import com.example.nova.actions.ActionType
import org.json.JSONObject

data class ParsedAiResponse(
    val displayText: String,
    val actionType: ActionType? = null,
    val parameters: Map<String, Any?> = emptyMap(),
    val isPlan: Boolean = false,
    val planSteps: List<String> = emptyList()
)

object AiResponseParser {

    fun parse(rawResponse: String): ParsedAiResponse {
        val trimmed = rawResponse.trim()

        // 1. Check if response is JSON (or contains a JSON object)
        val jsonString = extractJson(trimmed)
        if (jsonString != null) {
            try {
                val json = JSONObject(jsonString)
                val responseText = json.optString("response", "").ifBlank {
                    json.optString("message", "")
                }
                val actionStr = json.optString("action", "")
                val actionType = ActionType.fromString(actionStr)

                val paramsMap = mutableMapOf<String, Any?>()
                val paramsObj = json.optJSONObject("parameters")
                if (paramsObj != null) {
                    val keys = paramsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        paramsMap[key] = paramsObj.get(key)
                    }
                }

                val isPlan = json.optBoolean("isPlan", false)
                val planSteps = mutableListOf<String>()
                val stepsArray = json.optJSONArray("planSteps")
                if (stepsArray != null) {
                    for (i in 0 until stepsArray.length()) {
                        planSteps.add(stepsArray.getString(i))
                    }
                }

                val finalDisplay = if (responseText.isNotBlank()) {
                    responseText
                } else if (actionType != null) {
                    "Executing ${actionType.displayName}..."
                } else {
                    trimmed
                }

                return ParsedAiResponse(
                    displayText = finalDisplay,
                    actionType = actionType,
                    parameters = paramsMap,
                    isPlan = isPlan,
                    planSteps = planSteps
                )
            } catch (e: Exception) {
                // If parsing JSON fails, fall through to conversational text
            }
        }

        // 2. Normal conversational text without action JSON
        return ParsedAiResponse(displayText = trimmed)
    }

    private fun extractJson(text: String): String? {
        val clean = text.trim()
        if (clean.startsWith("{") && clean.endsWith("}")) {
            return clean
        }
        val firstBrace = clean.indexOf('{')
        val lastBrace = clean.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace > firstBrace) {
            return clean.substring(firstBrace, lastBrace + 1)
        }
        return null
    }
}
