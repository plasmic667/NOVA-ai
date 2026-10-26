package com.example.nova.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.nova.data.local.MessageEntity
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.RiskHighColor
import com.example.ui.theme.RiskLowColor
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: MessageEntity,
    onSpeak: (String) -> Unit = {}
) {
    val isUser = message.role.equals("user", ignoreCase = true)
    val context = LocalContext.current
    val timeFormatted = rememberFormattedTime(message.createdAtEpoch)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag(if (isUser) "user_message_bubble" else "nova_message_bubble"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Nova Avatar Orb small
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White, NovaCyan, NovaViolet)
                        )
                    )
                    .padding(end = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Nova",
                    tint = Color(0xFF0A0E1A),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            // Bubble box
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser) {
                            Brush.linearGradient(
                                listOf(Color(0xFF0E7490), Color(0xFF0369A1))
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(DeepSpaceSurfaceVariant, DeepSpaceSurface)
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isUser) NovaCyan.copy(alpha = 0.3f) else NovaViolet.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextPrimaryDark,
                            lineHeight = 20.sp
                        )
                    )

                    // Optional Action Status badge
                    if (!message.actionType.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val status = message.actionStatus ?: "PENDING"
                        val statusColor = when (status) {
                            "EXECUTED", "APPROVED", "SUCCESS" -> RiskLowColor
                            "CANCELLED", "FAILED" -> RiskHighColor
                            else -> NovaCyan
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Action: ${message.actionType} • $status",
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Bottom bar under bubble: timestamp and action icons (copy, TTS)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = timeFormatted,
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Nova Response", message.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(24.dp).testTag("copy_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = { onSpeak(message.content) },
                        modifier = Modifier.size(24.dp).testTag("tts_message_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read aloud",
                            tint = NovaCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberFormattedTime(epochMillis: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(epochMillis))
}
