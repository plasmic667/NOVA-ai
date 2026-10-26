package com.example.nova.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nova.ui.components.ActionConfirmationDialog
import com.example.nova.ui.components.AgentPlanDialog
import com.example.nova.ui.components.NovaOrb
import com.example.nova.viewmodel.AssistantViewModel
import com.example.nova.voice.VoiceState
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: AssistantViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToActions: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()
    val recognizedText by viewModel.recognizedText.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()
    val activePlan by viewModel.activePlan.collectAsState()
    val actionFeedback by viewModel.actionFeedback.collectAsState()

    var textInput by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Permission launcher for speech recognition
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Microphone permission required for voice commands.")
            }
        }
    }

    LaunchedEffect(actionFeedback) {
        actionFeedback?.let { feedback ->
            snackbarHostState.showSnackbar(feedback)
            viewModel.clearFeedback()
        }
    }

    val greeting = rememberGreeting()
    val formattedDate = rememberFormattedDate()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepSpaceBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NOVA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                color = NovaCyan
                            )
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier.testTag("nav_to_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Open Chat",
                            tint = TextPrimaryDark
                        )
                    }
                    IconButton(
                        onClick = onNavigateToActions,
                        modifier = Modifier.testTag("nav_to_actions_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Actions History",
                            tint = TextPrimaryDark
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("nav_to_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSpaceBackground
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top greeting block
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$greeting 👋",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (voiceState) {
                        VoiceState.LISTENING -> "Listening to you..."
                        VoiceState.THINKING -> "Nova is processing..."
                        VoiceState.SPEAKING -> "Speaking response..."
                        VoiceState.IDLE -> "How can I help you today?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (voiceState) {
                        VoiceState.LISTENING -> NovaCyan
                        VoiceState.THINKING -> NovaViolet
                        else -> TextSecondaryDark
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Center Interactive Nova Orb
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                NovaOrb(
                    voiceState = voiceState,
                    size = 230.dp,
                    onClick = {
                        val hasMicPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasMicPerm) {
                            viewModel.startVoiceInput()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                )
            }

            // Real-time speech transcript hint
            AnimatedVisibility(visible = recognizedText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepSpaceSurfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "\"$recognizedText\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NovaCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Suggested Command Chips
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Try asking Nova:",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondaryDark,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val suggestions = listOf(
                        "Open YouTube",
                        "Search the web",
                        "Set a timer for 10 minutes",
                        "Find restaurants near me",
                        "What's the date today?",
                        "Open Camera"
                    )

                    suggestions.forEach { suggestion ->
                        AssistChip(
                            onClick = {
                                viewModel.sendMessage(suggestion)
                                onNavigateToChat()
                            },
                            label = { Text(suggestion, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = DeepSpaceSurface,
                                labelColor = TextPrimaryDark
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = DeepSpaceSurfaceVariant
                            ),
                            modifier = Modifier.testTag("suggestion_chip_$suggestion")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Ask Nova or give a command...", color = TextSecondaryDark, fontSize = 14.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = DeepSpaceSurfaceVariant,
                        focusedContainerColor = DeepSpaceSurface,
                        unfocusedContainerColor = DeepSpaceSurface,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Microphone button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (voiceState == VoiceState.LISTENING) NovaCyan else DeepSpaceSurfaceVariant
                        )
                        .clickable {
                            val hasMicPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasMicPerm) {
                                viewModel.startVoiceInput()
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                        .testTag("home_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (voiceState == VoiceState.LISTENING) Color(0xFF0A0E1A) else NovaCyan
                    )
                }

                if (textInput.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(NovaCyan)
                            .clickable {
                                val text = textInput
                                textInput = ""
                                viewModel.sendMessage(text)
                                onNavigateToChat()
                            }
                            .testTag("home_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color(0xFF04202C)
                        )
                    }
                }
            }
        }
    }

    // Modal dialogs for sensitive actions and agent plans
    pendingAction?.let { pending ->
        ActionConfirmationDialog(
            pendingAction = pending,
            onConfirm = { viewModel.confirmPendingAction() },
            onCancel = { viewModel.cancelPendingAction() }
        )
    }

    activePlan?.let { planSteps ->
        AgentPlanDialog(
            planSteps = planSteps,
            onContinue = {
                viewModel.dismissPlan()
                viewModel.confirmPendingAction()
            },
            onCancel = {
                viewModel.dismissPlan()
                viewModel.cancelPendingAction()
            }
        )
    }
}

@Composable
private fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
}

@Composable
private fun rememberFormattedDate(): String {
    return SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date())
}
