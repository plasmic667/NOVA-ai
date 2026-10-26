package com.example.nova.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.viewmodel.AuthViewModel
import com.example.nova.viewmodel.SettingsViewModel
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.RiskHighColor
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val user by authViewModel.currentUser.collectAsState()
    val syncToCloud by settingsViewModel.syncToCloud.collectAsState()
    val voiceAutoPlay by settingsViewModel.voiceAutoPlay.collectAsState()
    val speechSpeed by settingsViewModel.speechSpeed.collectAsState()
    val agentMode by settingsViewModel.agentMode.collectAsState()
    val highThinking by settingsViewModel.highThinking.collectAsState()

    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepSpaceBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Nova Settings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSpaceSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepSpaceSurface)
                    .border(1.dp, DeepSpaceSurfaceVariant, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NovaViolet.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar",
                            tint = NovaCyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.displayName ?: "Google User",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                        )
                        Text(
                            text = user?.email ?: "Signed in with Google",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryDark
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            authViewModel.signOut {
                                Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("settings_sign_out_button")
                    ) {
                        Text("Sign Out", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cloud Synchronization
            Text(
                text = "CLOUD & DATA",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = NovaCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepSpaceSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (syncToCloud) Icons.Default.CloudDone else Icons.Default.CloudOff,
                        contentDescription = null,
                        tint = if (syncToCloud) NovaCyan else TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sync conversations to cloud",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = if (syncToCloud) "Backed up securely in Cloud Firestore" else "Stored locally on this device only",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                }

                Switch(
                    checked = syncToCloud,
                    onCheckedChange = { settingsViewModel.setSyncToCloud(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NovaCyan,
                        checkedTrackColor = NovaCyan.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("cloud_sync_toggle")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice & Speech Settings
            Text(
                text = "VOICE & SPEECH",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = NovaCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepSpaceSurface)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = NovaViolet)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Voice Autoplay Mode",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimaryDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("ALWAYS", "VOICE_ONLY", "NEVER")
                    modes.forEach { mode ->
                        FilterChip(
                            selected = voiceAutoPlay == mode,
                            onClick = { settingsViewModel.setVoiceAutoPlay(mode) },
                            label = {
                                Text(
                                    text = when (mode) {
                                        "ALWAYS" -> "Always"
                                        "VOICE_ONLY" -> "Voice Only"
                                        else -> "Never"
                                    },
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NovaCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NovaCyan,
                                containerColor = DeepSpaceSurfaceVariant,
                                labelColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("autoplay_chip_$mode")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DeepSpaceSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // Speech Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = NovaViolet)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Speech Rate",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimaryDark
                        )
                    }
                    Text(
                        text = "${((speechSpeed * 10).roundToInt() / 10.0)}x",
                        color = NovaCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Slider(
                    value = speechSpeed,
                    onValueChange = { settingsViewModel.setSpeechSpeed(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = NovaCyan,
                        activeTrackColor = NovaCyan,
                        inactiveTrackColor = DeepSpaceSurfaceVariant
                    ),
                    modifier = Modifier.testTag("speech_speed_slider")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Intelligence & Actions
            Text(
                text = "INTELLIGENCE & AGENT MODE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = NovaCyan
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepSpaceSurface)
                    .padding(16.dp)
            ) {
                // Agent Mode Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NovaCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Agent Mode (Action Planner)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "Allows Nova to plan multi-step sequential actions",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Switch(
                        checked = agentMode,
                        onCheckedChange = { settingsViewModel.setAgentMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaCyan,
                            checkedTrackColor = NovaCyan.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("agent_mode_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = DeepSpaceSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                // High Thinking / Deep Reasoning (Gemini 4 Argon)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = NovaViolet)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Deep Reasoning (Gemini 4 Argon)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimaryDark
                            )
                            Text(
                                text = "High thinking mode for complex problem-solving",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    Switch(
                        checked = highThinking,
                        onCheckedChange = { settingsViewModel.setHighThinking(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NovaViolet,
                            checkedTrackColor = NovaViolet.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.testTag("deep_reasoning_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Data Management & Account Deletion
            Text(
                text = "DANGER ZONE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = RiskHighColor
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepSpaceSurface)
                    .border(1.dp, RiskHighColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                // Clear Local Data button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear Local Data",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = TextPrimaryDark
                        )
                        Text(
                            text = "Clears on-device cache, conversations, and action history",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                    OutlinedButton(
                        onClick = { showClearDataDialog = true },
                        modifier = Modifier.testTag("clear_local_data_button")
                    ) {
                        Text("Clear", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = DeepSpaceSurfaceVariant)
                Spacer(modifier = Modifier.height(14.dp))

                // Delete Account button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Delete My Account",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = RiskHighColor
                        )
                        Text(
                            text = "Permanently removes your account and all Firestore cloud data",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }
                    Button(
                        onClick = { showDeleteAccountDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = RiskHighColor),
                        modifier = Modifier.testTag("delete_account_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Confirmation dialog for clearing local data
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear Local Data?") },
            text = { Text("This will delete all locally cached messages, conversations, and action records.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDataDialog = false
                        settingsViewModel.clearLocalData {
                            Toast.makeText(context, "Local data cleared", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaCyan, contentColor = Color(0xFF04202C))
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Serious confirmation dialog for deleting account
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account and All Data?", color = RiskHighColor, fontWeight = FontWeight.Bold) },
            text = {
                Text("This action is permanent and cannot be undone. Your Firebase Authentication account, Firestore cloud conversations, preferences, and action logs will be permanently deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        settingsViewModel.deleteAccountAndData {
                            Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskHighColor)
                ) {
                    Text("Permanently Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
