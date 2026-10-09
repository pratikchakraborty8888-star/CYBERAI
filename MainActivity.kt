package com.example.cyber_ai

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.TelecomManager
import android.view.WindowManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val channelName = "com.cyber.ai/device_control"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            .setMethodCallHandler { call, result ->
                when (call.method) {
                    "wakeScreen" -> {
                        val pm = getSystemService(POWER_SERVICE) as PowerManager
                        @Suppress("DEPRECATION")
                        val lock = pm.newWakeLock(
                            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                            "CyberAI:ScreenWake"
                        )
                        lock.acquire(2500)
                        lock.release()
                        result.success(true)
                    }
                    "openApp" -> {
                        val name = call.argument<String>("appName").orEmpty()
                        result.success(openInstalledApp(name))
                    }
                    "playMusic" -> {
                        val query = call.argument<String>("query").orEmpty()
                        result.success(openMusicSearch(query))
                    }
                    "answerCall" -> {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val telecom = getSystemService(TELECOM_SERVICE) as TelecomManager
                                telecom.acceptRingingCall()
                                result.success(true)
                            } else result.error("UNSUPPORTED", "Answer-call requires Android 8+", null)
                        } catch (e: SecurityException) {
                            result.error("PERMISSION", "Android did not grant call-control permission", null)
                        }
                    }
                    "endCall" -> {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val telecom = getSystemService(TELECOM_SERVICE) as TelecomManager
                                result.success(telecom.endCall())
                            } else result.error("UNSUPPORTED", "Ending calls requires Android 9+", null)
                        } catch (e: SecurityException) {
                            result.error("PERMISSION", "Android did not grant call-control permission", null)
                        }
                    }
                    else -> result.notImplemented()
                }
            }
    }

    private fun openInstalledApp(name: String): Boolean {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val match = apps.firstOrNull {
            pm.getApplicationLabel(it).toString().contains(name, ignoreCase = true)
        } ?: return false
        val intent = pm.getLaunchIntentForPackage(match.packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        return true
    }

    private fun openMusicSearch(query: String): Boolean {
        val uri = Uri.parse("https://music.youtube.com/search?q=" + Uri.encode(query))
        // ReVanced package IDs vary by build; try known Music packages, then any handler.
        val packages = listOf(
            "app.revanced.android.apps.youtube.music",
            "com.google.android.apps.youtube.music",
            "app.rvx.android.apps.youtube.music",
            "com.google.android.apps.youtube.music.revanced"
        )
        for (pkg in packages) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage(pkg)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                    return true
                }
            } catch (_: Exception) { }
        }
        return try {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            true
        } catch (_: Exception) { false }
    }
}
