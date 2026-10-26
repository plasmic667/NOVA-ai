package com.example.nova.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nova.actions.ActionRiskLevel
import com.example.nova.actions.ActionType
import com.example.nova.actions.PendingAction
import com.example.ui.theme.DeepSpaceSurface
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.RiskHighColor
import com.example.ui.theme.RiskMediumColor

@Composable
fun ActionConfirmationDialog(
    pendingAction: PendingAction,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val riskColor = when (pendingAction.riskLevel) {
        ActionRiskLevel.HIGH -> RiskHighColor
        ActionRiskLevel.MEDIUM -> RiskMediumColor
        ActionRiskLevel.LOW -> NovaCyan
    }

    val actionIcon = when (pendingAction.actionType) {
        ActionType.MAKE_PHONE_CALL -> Icons.Default.Call
        ActionType.SEND_SMS -> Icons.Default.Sms
        ActionType.SEND_EMAIL -> Icons.Default.Email
        else -> Icons.Default.Security
    }

    val confirmButtonText = when (pendingAction.actionType) {
        ActionType.MAKE_PHONE_CALL -> "Call"
        ActionType.SEND_SMS, ActionType.SEND_EMAIL -> "Send"
        ActionType.CREATE_ALARM -> "Set Alarm"
        ActionType.SET_TIMER -> "Start Timer"
        ActionType.CREATE_CALENDAR_EVENT -> "Add to Calendar"
        else -> "Confirm"
    }

    AlertDialog(
        onDismissRequest = onCancel,
        modifier = Modifier.testTag("action_confirmation_dialog"),
        containerColor = DeepSpaceSurface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(riskColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = pendingAction.actionType.displayName,
                    tint = riskColor,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = pendingAction.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Risk badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(riskColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "SECURITY CHECK • ${pendingAction.riskLevel.label.uppercase()}",
                        color = riskColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = pendingAction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pendingAction.riskLevel == ActionRiskLevel.HIGH) riskColor else NovaCyan,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("action_dialog_confirm_button")
            ) {
                Text(confirmButtonText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.testTag("action_dialog_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
