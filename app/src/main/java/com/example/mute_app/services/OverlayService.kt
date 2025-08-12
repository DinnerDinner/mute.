package com.example.mute_app.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.mute_app.R

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var isOverlayVisible = false

    private val overlayReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
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

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Register broadcast receiver with proper flags
        val filter = IntentFilter().apply {
            addAction(AppBlockingService.ACTION_SHOW_OVERLAY)
            addAction(AppBlockingService.ACTION_HIDE_OVERLAY)
        }

        // Fix for Android 14+ - use proper context registration flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(overlayReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
//            registerReceiver(overlayReceiver, filter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showOverlay(appName: String, packageName: String) {
        if (isOverlayVisible) return

        try {
            // Create overlay view
            val inflater = LayoutInflater.from(this)
            overlayView = inflater.inflate(R.layout.blocking_overlay, null)

            // Setup overlay content
            setupOverlayContent(overlayView!!, appName, packageName)

            // Window layout params - CRITICAL: Make it system-level overlay
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
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            )

            // Add view to window manager
            windowManager?.addView(overlayView, params)
            isOverlayVisible = true

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupOverlayContent(view: View, appName: String, packageName: String) {
        // Set app name
        view.findViewById<TextView>(R.id.blocked_app_name)?.text = "$appName is blocked"

        // Set blocking message
        view.findViewById<TextView>(R.id.blocking_message)?.text =
            "This app is blocked during your focus session. Take a break and return to your goals."

        // Setup close button
        view.findViewById<Button>(R.id.close_button)?.setOnClickListener {
            hideOverlay()
            // Go back to home screen
            goToHome()
        }

        // Add app icon if possible
        try {
            val pm = packageManager
            val appIcon = pm.getApplicationIcon(packageName)
            view.findViewById<ImageView>(R.id.blocked_app_icon)?.setImageDrawable(appIcon)
        } catch (e: Exception) {
            // Use default icon
            view.findViewById<ImageView>(R.id.blocked_app_icon)?.setImageResource(R.drawable.ic_block)
        }
    }

    private fun hideOverlay() {
        if (isOverlayVisible && overlayView != null) {
            try {
                windowManager?.removeView(overlayView)
                isOverlayVisible = false
                overlayView = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(overlayReceiver)
        } catch (e: Exception) {
            // Receiver may not be registered
        }
        hideOverlay()
    }
}