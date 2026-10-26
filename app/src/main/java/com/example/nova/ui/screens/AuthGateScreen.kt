package com.example.nova.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.ui.components.NovaOrb
import com.example.nova.viewmodel.AuthViewModel
import com.example.nova.voice.VoiceState
import com.example.ui.theme.DeepSpaceBackground
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.DeepSpaceSurfaceVariant
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.RiskHighColor
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun AuthGateScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Nova Logo & Title
            Text(
                text = "NOVA",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 6.sp,
                    color = NovaCyan
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "AI Personal Assistant",
                style = MaterialTheme.typography.titleMedium,
                color = NovaViolet,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Central Animated Nova Orb
            NovaOrb(
                voiceState = VoiceState.IDLE,
                size = 180.dp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Welcome explanation card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepSpaceSurface)
                    .border(1.dp, DeepSpaceSurfaceVariant, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = NovaCyan)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Secure Personal Assistant",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Sign in to synchronize your conversations, customize voice settings, and execute controlled device actions securely with Firebase & Gemini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                FeatureCheckRow("Voice & natural language commands")
                FeatureCheckRow("Legitimate Android device actions")
                FeatureCheckRow("Cloud sync powered by Firebase")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error display if sign in fails
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = RiskHighColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            // Interactive "Sign in with Google" button
            Button(
                onClick = {
                    (context as? Activity)?.let { activity ->
                        authViewModel.signInWithGoogle(activity)
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovaCyan,
                    contentColor = Color(0xFF04202C)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color(0xFF04202C),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF04202C),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureCheckRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = NovaCyan,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            color = TextPrimaryDark,
            fontSize = 12.sp
        )
    }
}
