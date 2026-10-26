package com.example.nova.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TextToSpeechManager(context: Context) {

    private val TAG = "TextToSpeechMgr"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentSpeed: Float = 1.0f

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Default language not supported for TTS, falling back to US English")
                    tts?.language = Locale.US
                }
                tts?.setSpeechRate(currentSpeed)
                Log.d(TAG, "TTS initialized successfully")
            } else {
                Log.e(TAG, "TTS initialization failed with code $status")
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeed = rate.coerceIn(0.5f, 2.0f)
        if (isInitialized) {
            tts?.setSpeechRate(currentSpeed)
        }
    }

    fun speak(
        text: String,
        onStart: () -> Unit = {},
        onDone: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS not initialized yet")
            onError()
            return
        }

        // Clean out action JSON or formatting symbols before speaking
        val cleanedText = cleanSpeechText(text)
        if (cleanedText.isBlank()) {
            onDone()
            return
        }

        val utteranceId = "nova_tts_${System.currentTimeMillis()}"

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                if (id == utteranceId) onStart()
            }

            override fun onDone(id: String?) {
                if (id == utteranceId) onDone()
            }

            override fun onError(id: String?) {
                if (id == utteranceId) onError()
            }
        })

        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to stop TTS", e)
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun cleanSpeechText(input: String): String {
        return input
            .replace(Regex("```[\\s\\S]*?```"), "") // Remove code blocks
            .replace(Regex("\\{[\\s\\S]*?\"action\"[\\s\\S]*?\\}"), "") // Remove JSON blocks
            .replace(Regex("[*#_`~]"), "") // Remove markdown markers
            .trim()
    }
}
