package com.example.nova.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.actions.ActionManager
import com.example.nova.actions.ActionResult
import com.example.nova.actions.ActionRiskLevel
import com.example.nova.actions.ActionType
import com.example.nova.actions.PendingAction
import com.example.nova.ai.AiConfig
import com.example.nova.ai.AiResponseParser
import com.example.nova.ai.GeminiService
import com.example.nova.ai.IntentClassifier
import com.example.nova.data.local.ActionEntity
import com.example.nova.data.local.ConversationEntity
import com.example.nova.data.local.MessageEntity
import com.example.nova.data.preferences.NovaPreferences
import com.example.nova.firebase.AuthRepository
import com.example.nova.repository.AssistantRepository
import com.example.nova.voice.SpeechRecognitionManager
import com.example.nova.voice.TextToSpeechManager
import com.example.nova.voice.VoiceState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "AssistantViewModel"
    private val app = application

    private val assistantRepository = AssistantRepository(application)
    private val authRepository = AuthRepository(application)
    private val preferences = NovaPreferences(application)
    private val geminiService = GeminiService()

    val speechRecognitionManager = SpeechRecognitionManager(application)
    val textToSpeechManager = TextToSpeechManager(application)

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _selectedImage = MutableStateFlow<Bitmap?>(null)
    val selectedImage: StateFlow<Bitmap?> = _selectedImage.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _pendingAction = MutableStateFlow<PendingAction?>(null)
    val pendingAction: StateFlow<PendingAction?> = _pendingAction.asStateFlow()

    private val _activePlan = MutableStateFlow<List<String>?>(null)
    val activePlan: StateFlow<List<String>?> = _activePlan.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    // Observe active conversation's messages
    val messages: StateFlow<List<MessageEntity>> = _activeConversationId.flatMapLatest { convId ->
        if (convId != null) {
            assistantRepository.observeMessages(convId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Observe action history
    val actionsHistory: StateFlow<List<ActionEntity>> = authRepository.currentUser.flatMapLatest { user ->
        val uid = user?.uid ?: ""
        assistantRepository.observeActions(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Observe all conversations
    val conversations: StateFlow<List<ConversationEntity>> = authRepository.currentUser.flatMapLatest { user ->
        val uid = user?.uid ?: ""
        assistantRepository.observeConversations(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    init {
        // Ensure a default active conversation exists
        viewModelScope.launch {
            val uid = authRepository.getUserId()
            val existing = assistantRepository.observeConversations(uid).first()
            if (existing.isNotEmpty()) {
                _activeConversationId.value = existing.first().id
            } else {
                val newId = assistantRepository.createConversation(uid, "Welcome to Nova")
                _activeConversationId.value = newId
                // Welcome message from Nova
                assistantRepository.addMessage(
                    conversationId = newId,
                    userId = uid,
                    role = "assistant",
                    content = "Hello! I am Nova, your AI personal assistant. How can I assist you today? You can ask me questions, give voice commands, or ask me to perform actions like setting alarms, searching the web, or opening apps."
                )
            }
        }
    }

    fun selectConversation(conversationId: String) {
        _activeConversationId.value = conversationId
    }

    fun createNewConversation(title: String = "New Conversation") {
        viewModelScope.launch {
            val uid = authRepository.getUserId()
            val id = assistantRepository.createConversation(uid, title)
            _activeConversationId.value = id
        }
    }

    fun setSelectedImage(bitmap: Bitmap?) {
        _selectedImage.value = bitmap
    }

    fun startVoiceInput() {
        if (_voiceState.value == VoiceState.LISTENING) {
            stopVoiceInput()
            return
        }

        textToSpeechManager.stop()
        _recognizedText.value = ""
        _voiceState.value = VoiceState.LISTENING

        speechRecognitionManager.startListening(
            onResult = { text ->
                _recognizedText.value = text
                _voiceState.value = VoiceState.IDLE
                if (text.isNotBlank()) {
                    sendMessage(text, isVoiceTriggered = true)
                }
            },
            onPartialResult = { partial ->
                _recognizedText.value = partial
            },
            onError = { error ->
                _voiceState.value = VoiceState.IDLE
                _actionFeedback.value = error
            }
        )
    }

    fun stopVoiceInput() {
        speechRecognitionManager.stopListening()
        _voiceState.value = VoiceState.IDLE
    }

    fun sendMessage(userText: String, isVoiceTriggered: Boolean = false) {
        val prompt = userText.trim()
        if (prompt.isEmpty() && _selectedImage.value == null) return

        val convId = _activeConversationId.value ?: return
        val uid = authRepository.getUserId()
        val imageToProcess = _selectedImage.value
        _selectedImage.value = null // consume image

        viewModelScope.launch {
            // 1. Add user message to repository
            assistantRepository.addMessage(
                conversationId = convId,
                userId = uid,
                role = "user",
                content = prompt.ifBlank { "[Attached Image]" }
            )

            // Update conversation title if first message
            val currentMsgs = messages.value
            if (currentMsgs.size <= 1) {
                val shortTitle = if (prompt.length > 25) prompt.take(25) + "..." else prompt
                assistantRepository.updateConversationTitle(convId, uid, shortTitle)
            }

            val syncCloud = preferences.syncToCloudFlow.first()
            val voiceAutoplay = preferences.voiceAutoPlayFlow.first()
            val speed = preferences.speechSpeedFlow.first()
            textToSpeechManager.setSpeechRate(speed)

            val shouldSpeak = when (voiceAutoplay) {
                "ALWAYS" -> true
                "VOICE_ONLY" -> isVoiceTriggered
                else -> false
            }

            // 2. CHECK LOCAL INTENT CLASSIFIER (INSTANT & 100% RELIABLE FOR DEVICE ACTIONS)
            if (imageToProcess == null) {
                val localIntent = IntentClassifier.classifyLocal(prompt)
                if (localIntent != null) {
                    val actionType = localIntent.actionType
                    val parameters = localIntent.parameters

                    val evalResult = ActionManager.evaluateAction(
                        actionType = actionType,
                        parameters = parameters
                    )

                    if (evalResult.requiresUserInput && evalResult.pendingAction != null) {
                        // High risk action -> prompt confirmation dialog
                        val msgId = assistantRepository.addMessage(
                            conversationId = convId,
                            userId = uid,
                            role = "assistant",
                            content = localIntent.defaultResponse,
                            actionType = actionType.name,
                            actionStatus = "PENDING",
                            actionPayload = parameters.toString(),
                            syncToCloud = syncCloud
                        )
                        _pendingAction.value = evalResult.pendingAction.copy(messageId = msgId)
                        _voiceState.value = VoiceState.IDLE
                        if (shouldSpeak) {
                            speakMessage(localIntent.defaultResponse)
                        }
                    } else {
                        // Low/medium risk action -> execute immediately!
                        val execResult = ActionManager.executeAction(app, actionType, parameters)
                        val finalResponse = if (execResult.success) {
                            when (actionType) {
                                ActionType.GET_CURRENT_TIME, ActionType.GET_CURRENT_DATE -> execResult.message
                                else -> localIntent.defaultResponse
                            }
                        } else {
                            execResult.message
                        }

                        val msgId = assistantRepository.addMessage(
                            conversationId = convId,
                            userId = uid,
                            role = "assistant",
                            content = finalResponse,
                            actionType = actionType.name,
                            actionStatus = if (execResult.success) "EXECUTED" else "FAILED",
                            actionPayload = parameters.toString(),
                            syncToCloud = syncCloud
                        )

                        assistantRepository.logAction(
                            userId = uid,
                            actionType = actionType.name,
                            riskLevel = actionType.defaultRisk.name,
                            description = execResult.message,
                            status = if (execResult.success) "SUCCESS" else "FAILED",
                            syncToCloud = syncCloud
                        )

                        _actionFeedback.value = execResult.message
                        _voiceState.value = VoiceState.IDLE

                        if (shouldSpeak) {
                            speakMessage(finalResponse)
                        }
                    }
                    return@launch
                }
            }

            // 3. AI SEMANTIC UNDERSTANDING & REASONING (GEMINI)
            _voiceState.value = VoiceState.THINKING

            try {
                val deepReasoning = preferences.highThinkingFlow.first()

                // Build conversation history
                val history = currentMsgs.takeLast(6).map { it.role to it.content }

                // Call Gemini via GeminiService with AiConfig
                val aiResponseRaw = geminiService.generateAssistantResponse(
                    userPrompt = prompt,
                    history = history,
                    useDeepReasoning = deepReasoning,
                    imageAttachment = imageToProcess
                )

                // 4. Parse response for structured action or multi-step plan
                val parsed = AiResponseParser.parse(aiResponseRaw)

                if (parsed.isPlan && parsed.planSteps.isNotEmpty()) {
                    _activePlan.value = parsed.planSteps
                }

                // Save assistant message
                val msgId = assistantRepository.addMessage(
                    conversationId = convId,
                    userId = uid,
                    role = "assistant",
                    content = parsed.displayText,
                    actionType = parsed.actionType?.name,
                    actionStatus = if (parsed.actionType != null) "PENDING" else null,
                    actionPayload = parsed.parameters.toString(),
                    syncToCloud = syncCloud
                )

                // Evaluate action if present
                if (parsed.actionType != null) {
                    val evalResult = ActionManager.evaluateAction(
                        actionType = parsed.actionType,
                        parameters = parsed.parameters,
                        messageId = msgId
                    )

                    if (evalResult.requiresUserInput && evalResult.pendingAction != null) {
                        _pendingAction.value = evalResult.pendingAction
                        _voiceState.value = VoiceState.IDLE
                    } else {
                        val execResult = ActionManager.executeAction(app, parsed.actionType, parsed.parameters)
                        assistantRepository.updateMessageActionStatus(
                            msgId,
                            if (execResult.success) "EXECUTED" else "FAILED"
                        )
                        assistantRepository.logAction(
                            userId = uid,
                            actionType = parsed.actionType.name,
                            riskLevel = parsed.actionType.defaultRisk.name,
                            description = execResult.message,
                            status = if (execResult.success) "SUCCESS" else "FAILED",
                            syncToCloud = syncCloud
                        )
                        _actionFeedback.value = execResult.message
                    }
                }

                if (shouldSpeak && _pendingAction.value == null) {
                    _voiceState.value = VoiceState.SPEAKING
                    speakMessage(parsed.displayText)
                } else {
                    _voiceState.value = VoiceState.IDLE
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error in assistant response loop", e)
                _voiceState.value = VoiceState.IDLE

                // Use centralized error classification to avoid raw SDK error strings
                val friendlyError = AiConfig.classifyError(e)

                assistantRepository.addMessage(
                    conversationId = convId,
                    userId = uid,
                    role = "assistant",
                    content = friendlyError
                )

                if (shouldSpeak) {
                    speakMessage(friendlyError)
                }
            }
        }
    }

    fun confirmPendingAction() {
        val pending = _pendingAction.value ?: return
        _pendingAction.value = null
        val uid = authRepository.getUserId()

        viewModelScope.launch {
            val execResult = ActionManager.executeAction(app, pending.actionType, pending.parameters)
            if (pending.messageId != null) {
                assistantRepository.updateMessageActionStatus(
                    pending.messageId,
                    if (execResult.success) "EXECUTED" else "FAILED"
                )
            }
            assistantRepository.logAction(
                userId = uid,
                actionType = pending.actionType.name,
                riskLevel = pending.riskLevel.name,
                description = execResult.message,
                status = if (execResult.success) "SUCCESS" else "FAILED"
            )
            _actionFeedback.value = execResult.message
        }
    }

    fun cancelPendingAction() {
        val pending = _pendingAction.value ?: return
        _pendingAction.value = null
        viewModelScope.launch {
            if (pending.messageId != null) {
                assistantRepository.updateMessageActionStatus(pending.messageId, "CANCELLED")
            }
            assistantRepository.logAction(
                userId = authRepository.getUserId(),
                actionType = pending.actionType.name,
                riskLevel = pending.riskLevel.name,
                description = "Cancelled by user: ${pending.description}",
                status = "CANCELLED"
            )
            _actionFeedback.value = "Action cancelled."
        }
    }

    fun dismissPlan() {
        _activePlan.value = null
    }

    fun speakMessage(text: String) {
        textToSpeechManager.stop()
        _voiceState.value = VoiceState.SPEAKING
        textToSpeechManager.speak(
            text = text,
            onDone = { _voiceState.value = VoiceState.IDLE },
            onError = { _voiceState.value = VoiceState.IDLE }
        )
    }

    fun stopSpeaking() {
        textToSpeechManager.stop()
        _voiceState.value = VoiceState.IDLE
    }

    fun clearFeedback() {
        _actionFeedback.value = null
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.stopListening()
        textToSpeechManager.shutdown()
    }
}
