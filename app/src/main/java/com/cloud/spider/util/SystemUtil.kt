package com.cloud.spider.util

import android.app.ActivityManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import java.util.UUID

/**
 *
 * Created by cloud on 2024/1/30.
 */
object SystemUtil {

    fun isServiceRunning(context: Context, serviceName: String): Boolean {
        val am: ActivityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices: List<ActivityManager.RunningServiceInfo> = am.getRunningServices(50)
        for (serviceInfo in runningServices) {
            val name = serviceInfo.service.className
            if (serviceName.equals(name)) {
                return true
            }
        }
        return false
    }

    fun isNotificationEnabled(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun generateSubscriptionSourceId(): String {
        return "SS_${UUID.randomUUID()}"
    }

    fun generateConverterId(): String {
        return "CO_${UUID.randomUUID()}"
    }
}