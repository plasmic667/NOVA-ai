package com.example.nova.actions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ActionManager {

    private const val CHANNEL_ID = "nova_assistant_channel"

    init {
        // Notification channel created lazily in initNotificationChannel
    }

    private fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Nova Assistant"
            val descriptionText = "Notifications and reminders from Nova"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Pre-validates an action. If it is high-risk, or if explicit confirmation is requested,
     * returns an ActionResult indicating confirmation is needed before actual execution.
     */
    fun evaluateAction(
        actionType: ActionType,
        parameters: Map<String, Any?>,
        messageId: String? = null,
        forceConfirm: Boolean = false
    ): ActionResult {
        val riskLevel = actionType.defaultRisk
        val requiresConfirmation = forceConfirm || riskLevel == ActionRiskLevel.HIGH

        if (requiresConfirmation) {
            val description = buildConfirmationDescription(actionType, parameters)
            val pending = PendingAction(
                id = UUID.randomUUID().toString(),
                messageId = messageId,
                actionType = actionType,
                riskLevel = riskLevel,
                title = "Confirm ${actionType.displayName}",
                description = description,
                parameters = parameters
            )
            return ActionResult(
                success = true,
                message = "Requires user confirmation: $description",
                requiresUserInput = true,
                pendingAction = pending
            )
        }

        return ActionResult(
            success = true,
            message = "Action approved for immediate execution",
            requiresUserInput = false
        )
    }

    private fun buildConfirmationDescription(actionType: ActionType, parameters: Map<String, Any?>): String {
        return when (actionType) {
            ActionType.MAKE_PHONE_CALL -> {
                val target = parameters["phoneNumber"] ?: parameters["contact"] ?: parameters["recipient"] ?: "the contact"
                "Do you want me to call $target?"
            }
            ActionType.SEND_SMS -> {
                val recipient = parameters["recipient"] ?: parameters["contact"] ?: "Recipient"
                val body = parameters["message"] ?: parameters["text"] ?: ""
                "Send SMS to $recipient:\n\"$body\""
            }
            ActionType.SEND_EMAIL -> {
                val recipient = parameters["recipient"] ?: parameters["email"] ?: "Recipient"
                val subject = parameters["subject"] ?: "No subject"
                val body = parameters["body"] ?: ""
                "Send Email to $recipient\nSubject: $subject\nMessage: $body"
            }
            ActionType.CREATE_ALARM -> {
                val time = "${parameters["hour"] ?: 7}:${parameters["minute"] ?: "00"}"
                "Set an alarm for $time?"
            }
            ActionType.SET_TIMER -> {
                val secs = parameters["durationSeconds"] ?: parameters["duration"] ?: 600
                "Set a timer for $secs seconds?"
            }
            ActionType.CREATE_CALENDAR_EVENT -> {
                val title = parameters["title"] ?: "Meeting"
                "Add calendar event: $title"
            }
            else -> "Execute ${actionType.displayName}?"
        }
    }

    /**
     * Executes the action using legitimate Android APIs/Intents.
     */
    fun executeAction(context: Context, actionType: ActionType, parameters: Map<String, Any?>): ActionResult {
        return try {
            when (actionType) {
                ActionType.OPEN_APP -> {
                    val target = parameters["packageName"]?.toString()
                        ?: parameters["appName"]?.toString()
                        ?: parameters["query"]?.toString()
                        ?: return ActionResult(false, "App name or package missing.")
                    AppLauncher.launchApp(context, target)
                }

                ActionType.WEB_SEARCH -> {
                    val query = parameters["query"]?.toString()
                        ?: return ActionResult(false, "Search query missing.")
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$encoded")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Searching web for \"$query\"", launchedIntent = true)
                }

                ActionType.OPEN_URL -> {
                    var url = parameters["url"]?.toString()
                        ?: return ActionResult(false, "URL is missing.")
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://$url"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Opened $url", launchedIntent = true)
                }

                ActionType.SET_TIMER -> {
                    val seconds = (parameters["durationSeconds"] as? Number)?.toInt()
                        ?: (parameters["duration"] as? Number)?.toInt()
                        ?: 600
                    val message = parameters["message"]?.toString() ?: "Nova Timer"
                    val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        putExtra(AlarmClock.EXTRA_MESSAGE, message)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        ActionResult(true, "Set timer for ${seconds / 60} minutes (${seconds}s).", launchedIntent = true)
                    } else {
                        ActionResult(false, "Clock application not available to set timer.")
                    }
                }

                ActionType.CREATE_ALARM -> {
                    val hour = (parameters["hour"] as? Number)?.toInt() ?: 7
                    val minute = (parameters["minute"] as? Number)?.toInt() ?: 0
                    val message = parameters["message"]?.toString() ?: "Nova Alarm"
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, minute)
                        putExtra(AlarmClock.EXTRA_MESSAGE, message)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
                        ActionResult(true, "Set alarm for $formattedTime.", launchedIntent = true)
                    } else {
                        ActionResult(false, "Clock application not available on this device.")
                    }
                }

                ActionType.CREATE_CALENDAR_EVENT -> {
                    val title = parameters["title"]?.toString() ?: parameters["event"]?.toString() ?: "New Event"
                    val desc = parameters["description"]?.toString() ?: "Created by Nova"
                    val intent = Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, title)
                        .putExtra(CalendarContract.Events.DESCRIPTION, desc)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    context.startActivity(intent)
                    ActionResult(true, "Opened Calendar to add \"$title\".", launchedIntent = true)
                }

                ActionType.OPEN_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Opened Device Settings.", launchedIntent = true)
                }

                ActionType.MAKE_PHONE_CALL -> {
                    val rawPhone = parameters["phoneNumber"]?.toString()
                        ?: parameters["phone"]?.toString()
                        ?: parameters["contact"]?.toString()
                        ?: return ActionResult(false, "Phone number missing.")

                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$rawPhone")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Prepared phone call to $rawPhone.", launchedIntent = true)
                }

                ActionType.SEND_SMS -> {
                    val recipient = parameters["recipient"]?.toString()
                        ?: parameters["phoneNumber"]?.toString()
                        ?: ""
                    val body = parameters["message"]?.toString()
                        ?: parameters["text"]?.toString()
                        ?: ""
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$recipient")).apply {
                        putExtra("sms_body", body)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Opened SMS composer for $recipient.", launchedIntent = true)
                }

                ActionType.SEND_EMAIL -> {
                    val recipient = parameters["recipient"]?.toString()
                        ?: parameters["email"]?.toString()
                        ?: ""
                    val subject = parameters["subject"]?.toString() ?: ""
                    val body = parameters["body"]?.toString() ?: parameters["message"]?.toString() ?: ""
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$recipient")).apply {
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Opened Email composer for $recipient.", launchedIntent = true)
                }

                ActionType.GET_CURRENT_TIME -> {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val currentTime = timeFormat.format(Date())
                    ActionResult(true, "The current time is $currentTime.")
                }

                ActionType.GET_CURRENT_DATE -> {
                    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())
                    ActionResult(true, "Today is $currentDate.")
                }

                ActionType.SHOW_NOTIFICATION -> {
                    initNotificationChannel(context)
                    val title = parameters["title"]?.toString() ?: "Nova Assistant"
                    val content = parameters["content"]?.toString() ?: parameters["message"]?.toString() ?: "Reminder"

                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)

                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
                    ActionResult(true, "Notification shown: $content")
                }

                ActionType.START_MAPS -> {
                    val destination = parameters["destination"]?.toString()
                        ?: parameters["query"]?.toString()
                        ?: "Current Location"
                    val encoded = URLEncoder.encode(destination, "UTF-8")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$encoded")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        setPackage("com.google.android.apps.maps")
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        // Fallback to standard geo intent
                        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(fallback)
                    }
                    ActionResult(true, "Navigating to $destination", launchedIntent = true)
                }

                ActionType.SEARCH_MAPS -> {
                    val query = parameters["query"]?.toString() ?: "restaurants near me"
                    val encoded = URLEncoder.encode(query, "UTF-8")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$encoded")).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Searching maps for $query", launchedIntent = true)
                }

                ActionType.OPEN_CAMERA -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                        ActionResult(true, "Opened Camera", launchedIntent = true)
                    } else {
                        ActionResult(false, "Camera app not found.")
                    }
                }

                ActionType.OPEN_GALLERY -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    ActionResult(true, "Opened Photo Gallery", launchedIntent = true)
                }
            }
        } catch (e: Exception) {
            ActionResult(false, "Failed to execute ${actionType.displayName}: ${e.localizedMessage}")
        }
    }
}
