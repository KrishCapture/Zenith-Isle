package com.example.util

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.util.Log

object PendingIntentExecutor {
    fun executeCallAction(pendingIntent: PendingIntent?, context: Context) {
        if (pendingIntent == null) return
        try {
            val options = ActivityOptions.makeBasic()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                options.setPendingIntentBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }
            pendingIntent.send(context, 0, null, null, null, null, options.toBundle())
        } catch (e: Exception) {
            Log.e("DynamicIsland", "Failed to dispatch PendingIntent with options", e)
            try {
                pendingIntent.send()
            } catch (err: Exception) {
                Log.e("DynamicIsland", "Direct send fallback failed", err)
            }
        }
    }
}
