package com.example.mute_app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.mute_app.R

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var isOverlayShowing = false
    private val overlayReceiver = OverlayBroadcastReceiver()

    companion object {
        private const val TAG = "OverlayService"
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "overlay_service_channel"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        createNotificationChannel()

        // Register receiver with proper context registration for API levels
        val filter = IntentFilter().apply {
            addAction(AppBlockingService.ACTION_SHOW_OVERLAY)
            addAction(AppBlockingService.ACTION_HIDE_OVERLAY)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(overlayReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(overlayReceiver, filter)
        }

        Log.d(TAG, "OverlayService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(TAG, "OverlayService started in foreground")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Blocking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs while blocking apps during focus sessions"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocking Active")
            .setContentText("Monitoring for blocked apps")
            .setSmallIcon(R.drawable.ic_block)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .build()
    }

    private fun showOverlay(appName: String, packageName: String) {
        if (isOverlayShowing) {
            hideOverlay()
        }

        try {
            // Inflate the blocking layout
            overlayView = LayoutInflater.from(this).inflate(R.layout.blocking_overlay, null)

            // Set up the overlay view
            setupOverlayView(overlayView!!, appName, packageName)

            // Configure window parameters
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.CENTER

            // Add the view to window manager
            windowManager.addView(overlayView, params)
            isOverlayShowing = true

            Log.d(TAG, "Overlay shown for app: $appName")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to show overlay", e)
            isOverlayShowing = false
        }
    }

    private fun setupOverlayView(view: View, appName: String, packageName: String) {
        // Set app name
        val appNameTextView = view.findViewById<TextView>(R.id.blocked_app_name)
        appNameTextView.text = "$appName is blocked"

        // Set app icon (try to get actual app icon)
        val appIconView = view.findViewById<ImageView>(R.id.blocked_app_icon)
        try {
            val appIcon = packageManager.getApplicationIcon(packageName)
            appIconView.setImageDrawable(appIcon)
        } catch (e: Exception) {
            // Keep default block icon if we can't get app icon
            Log.w(TAG, "Could not get app icon for $packageName", e)
        }

        // Set up close button
        val closeButton = view.findViewById<Button>(R.id.close_button)
        closeButton.setOnClickListener {
            hideOverlay()
            // Optional: Go back to home screen to prevent immediate re-opening
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
        }
    }

    private fun hideOverlay() {
        if (isOverlayShowing && overlayView != null) {
            try {
                windowManager.removeView(overlayView)
                overlayView = null
                isOverlayShowing = false
                Log.d(TAG, "Overlay hidden")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to hide overlay", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideOverlay()
        try {
            unregisterReceiver(overlayReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to unregister receiver", e)
        }
        Log.d(TAG, "OverlayService destroyed")
    }

    private inner class OverlayBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "Received broadcast: ${intent?.action}")
            when (intent?.action) {
                AppBlockingService.ACTION_SHOW_OVERLAY -> {
                    val appName = intent.getStringExtra(AppBlockingService.EXTRA_BLOCKED_APP_NAME) ?: "App"
                    val packageName = intent.getStringExtra(AppBlockingService.EXTRA_BLOCKED_PACKAGE) ?: ""
                    showOverlay(appName, packageName)
                }
                AppBlockingService.ACTION_HIDE_OVERLAY -> {
                    hideOverlay()
                }
            }
        }
    }
}