package com.example.nova.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout

class GeminiService {

    private val TAG = "GeminiService"

    private val systemPrompt = """
        You are Nova, an active Android personal assistant.
        You understand natural language and control Android actions on behalf of the user.
        
        CRITICAL RULES:
        1. NEVER say "I am an AI and cannot open apps" or "I cannot perform actions". You CAN perform actions because you are directly integrated with Nova's Android Action Engine.
        2. When the user asks you to open an application, search the web, set an alarm, set a timer, make a phone call, send a text, check the time/date, or open maps, YOU MUST RETURN A STRUCTURED ACTION JSON OBJECT.
        
        ALLOWED ACTIONS:
        - OPEN_APP (parameters: "appName" or "packageName")
        - WEB_SEARCH (parameters: "query")
        - OPEN_URL (parameters: "url")
        - SET_TIMER (parameters: "durationSeconds": number, "message": string)
        - CREATE_ALARM (parameters: "hour": number (0-23), "minute": number (0-59), "message": string)
        - CREATE_CALENDAR_EVENT (parameters: "title": string, "description": string)
        - OPEN_SETTINGS (parameters: "settingType": string)
        - MAKE_PHONE_CALL (parameters: "phoneNumber": string or "contact": string)
        - SEND_SMS (parameters: "recipient": string, "message": string)
        - SEND_EMAIL (parameters: "recipient": string, "subject": string, "body": string)
        - GET_CURRENT_TIME
        - GET_CURRENT_DATE
        - SHOW_NOTIFICATION (parameters: "title": string, "content": string)
        - START_MAPS (parameters: "destination": string)
        - SEARCH_MAPS (parameters: "query": string)
        - OPEN_CAMERA
        - OPEN_GALLERY
        
        OUTPUT FORMATS:
        - If the request requires an action, ALWAYS reply with JSON:
        {
          "response": "Natural conversational confirmation (e.g. 'Opening YouTube for you.')",
          "action": "ACTION_TYPE_NAME",
          "parameters": { ... }
        }
        - If the request is multi-step (Agent Mode):
        {
          "response": "Here's what I'll do: ...",
          "isPlan": true,
          "planSteps": ["1. Open YouTube", "2. Search for video"],
          "action": "FIRST_ACTION_TYPE",
          "parameters": { ... }
        }
        - If the request is a general question, explanation, creative task, or normal chat, reply with friendly, natural conversational text (no JSON).
    """.trimIndent()

    private val defaultModel by lazy {
        Firebase.ai.generativeModel(
            modelName = AiConfig.DEFAULT_MODEL,
            generationConfig = AiConfig.defaultGenerationConfig,
            systemInstruction = content { text(systemPrompt) }
        )
    }

    private val deepReasoningModel by lazy {
        Firebase.ai.generativeModel(
            modelName = AiConfig.DEEP_REASONING_MODEL,
            systemInstruction = content { text(systemPrompt) }
        )
    }

    suspend fun generateAssistantResponse(
        userPrompt: String,
        history: List<Pair<String, String>> = emptyList(),
        useDeepReasoning: Boolean = false,
        imageAttachment: Bitmap? = null
    ): String {
        var lastException: Exception? = null

        for (attempt in 0..AiConfig.MAX_RETRIES) {
            try {
                return withTimeout(AiConfig.REQUEST_TIMEOUT_MS) {
                    val model = if (useDeepReasoning) deepReasoningModel else defaultModel

                    if (imageAttachment != null) {
                        val response = model.generateContent(
                            content {
                                image(imageAttachment)
                                text(userPrompt)
                            }
                        )
                        response.text.orEmpty().ifBlank {
                            "I analyzed the image. What would you like to know about it?"
                        }
                    } else {
                        val contents = mutableListOf<Content>()
                        for ((role, textContent) in history) {
                            val senderRole = if (role.equals("user", ignoreCase = true)) "user" else "model"
                            contents.add(content(senderRole) { text(textContent) })
                        }
                        contents.add(content("user") { text(userPrompt) })

                        val response = model.generateContent(contents)
                        response.text.orEmpty().ifBlank {
                            "How can I help you next?"
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Attempt $attempt failed: ${e.message}")
                if (attempt < AiConfig.MAX_RETRIES) {
                    delay(500L * (attempt + 1))
                }
            }
        }

        // If all attempts failed, throw the last exception so ViewModel can handle it cleanly
        throw lastException ?: IllegalStateException("AI response generation failed.")
    }
}
