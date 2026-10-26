package com.example.nova.ai

import com.google.firebase.ai.type.generationConfig

/**
 * Centralized AI configuration for Nova Assistant.
 * Keeps all model parameters, endpoints, timeouts, and error mappings in one place.
 */
object AiConfig {
    // Current supported Gemini models
    const val DEFAULT_MODEL = "gemini-3.8-flash"
    const val DEEP_REASONING_MODEL = "gemini-4-argon"
    const val FAST_MODEL = "gemini-3.5-flash-lite"

    // Timeouts and retries
    const val REQUEST_TIMEOUT_MS = 30_000L
    const val MAX_RETRIES = 2

    // Generation configuration
    val defaultGenerationConfig = generationConfig {
        temperature = 0.2f // Lower temperature for consistent intent and structured action execution
        topK = 40
        topP = 0.95f
    }

    /**
     * Categorizes exceptions into human-friendly, professional Nova error messages.
     * Prevents raw SDK error messages or quota strings from leaking to the user.
     */
    fun classifyError(throwable: Throwable): String {
        val msg = throwable.message?.lowercase().orEmpty()
        val causeMsg = throwable.cause?.message?.lowercase().orEmpty()
        val combined = "$msg $causeMsg"

        return when {
            combined.contains("quota") || combined.contains("rate") || combined.contains("exhausted") || combined.contains("429") -> {
                "Nova has temporarily reached its AI usage limit. Please try again later."
            }
            combined.contains("network") || combined.contains("timeout") || combined.contains("unable to resolve host") || combined.contains("connect") -> {
                "Nova can't connect to the AI right now. Check your internet connection."
            }
            combined.contains("api key") || combined.contains("unauthenticated") || combined.contains("invalid_api_key") -> {
                "Your Gemini API configuration is invalid. Check your API configuration."
            }
            combined.contains("appcheck") || combined.contains("app check") || combined.contains("403") || combined.contains("attestation") -> {
                "Nova's secure AI logic service is authenticating. Please try your request again in a moment."
            }
            combined.contains("500") || combined.contains("503") || combined.contains("internal") || combined.contains("unavailable") -> {
                "Nova's AI service is temporarily unavailable."
            }
            else -> {
                "Nova couldn't process that request right now. Please try again."
            }
        }
    }
}
