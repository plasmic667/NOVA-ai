package com.example.nova.actions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log

object AppLauncher {

    private const val TAG = "AppLauncher"

    // Popular apps and package mappings
    private val KNOWN_APP_PACKAGES = mapOf(
        "youtube" to listOf("com.google.android.youtube"),
        "chrome" to listOf("com.android.chrome", "com.google.android.apps.chrome"),
        "browser" to listOf("com.android.browser", "com.android.chrome"),
        "whatsapp" to listOf("com.whatsapp", "com.whatsapp.w4b"),
        "maps" to listOf("com.google.android.apps.maps"),
        "google maps" to listOf("com.google.android.apps.maps"),
        "gmail" to listOf("com.google.android.gm"),
        "email" to listOf("com.google.android.gm", "com.android.email"),
        "camera" to listOf("com.android.camera2", "com.android.camera", "com.google.android.GoogleCamera"),
        "photos" to listOf("com.google.android.apps.photos", "com.android.gallery3d"),
        "gallery" to listOf("com.android.gallery3d", "com.google.android.apps.photos"),
        "clock" to listOf("com.google.android.deskclock", "com.android.deskclock"),
        "settings" to listOf("com.android.settings"),
        "calculator" to listOf("com.google.android.calculator", "com.android.calculator2"),
        "contacts" to listOf("com.google.android.contacts", "com.android.contacts"),
        "calendar" to listOf("com.google.android.calendar", "com.android.calendar"),
        "messages" to listOf("com.google.android.apps.messaging", "com.android.mms"),
        "spotify" to listOf("com.spotify.music")
    )

    fun launchApp(context: Context, appQuery: String): ActionResult {
        val cleanQuery = appQuery.trim().lowercase()
            .replace("open ", "")
            .replace("launch ", "")
            .replace("start ", "")
            .replace("the ", "")
            .replace(" app", "")
            .trim()

        val pm = context.packageManager

        // 1. Direct package check if cleanQuery looks like a package ID (e.g. "com.google.android.youtube")
        if (cleanQuery.contains(".")) {
            val directIntent = pm.getLaunchIntentForPackage(cleanQuery)
            if (directIntent != null) {
                directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(directIntent)
                return ActionResult(true, "Opened $cleanQuery", launchedIntent = true)
            }
        }

        // 2. Known app packages matching
        val candidatePackages = KNOWN_APP_PACKAGES[cleanQuery] ?: emptyList()
        for (pkg in candidatePackages) {
            val intent = pm.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return ActionResult(true, "Opened $appQuery", launchedIntent = true)
            }
        }

        // 3. Dynamic scan of all launcher apps on the device
        try {
            val launcherIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(launcherIntent, 0)
            for (info in resolveInfos) {
                val label = info.loadLabel(pm).toString().lowercase()
                val pkgName = info.activityInfo.packageName.lowercase()

                if (label == cleanQuery || label.contains(cleanQuery) || pkgName.contains(cleanQuery)) {
                    val targetPackage = info.activityInfo.packageName
                    val intent = pm.getLaunchIntentForPackage(targetPackage)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        return ActionResult(true, "Opened ${info.loadLabel(pm)}", launchedIntent = true)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error querying launcher activities", e)
        }

        // 4. Intelligent Fallbacks for system services & web alternatives
        when (cleanQuery) {
            "youtube" -> {
                // Open YouTube on web
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                return ActionResult(true, "Opened YouTube.", launchedIntent = true)
            }
            "chrome", "browser", "internet" -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(browserIntent)
                return ActionResult(true, "Opened Web Browser.", launchedIntent = true)
            }
            "camera" -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(cameraIntent)
                return ActionResult(true, "Opened Camera.", launchedIntent = true)
            }
            "maps", "google maps" -> {
                val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(mapsIntent)
                return ActionResult(true, "Opened Maps.", launchedIntent = true)
            }
            "settings" -> {
                val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return ActionResult(true, "Opened Device Settings.", launchedIntent = true)
            }
        }

        // 5. If not installed and no fallback, offer to view on Google Play
        try {
            val searchPlay = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=$cleanQuery&c=apps")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(searchPlay)
            return ActionResult(true, "Opened Google Play to find \"$cleanQuery\".", launchedIntent = true)
        } catch (e: Exception) {
            return ActionResult(false, "Couldn't launch '$appQuery'.")
        }
    }
}
