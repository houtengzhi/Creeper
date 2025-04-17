package com.cloud.creeper.util

import android.app.ActivityManager
import android.content.Context
import android.text.format.DateUtils
import androidx.core.app.NotificationManagerCompat
import java.util.UUID

/**
 *
 * Created by cloud on 2024/1/30.
 */
object SystemUtil {

    private const val MINUTE_MILLIS = 1000L * 60
    private const val HOUR_MILLIS = 1000L * 60 * 60
    private const val DAY_MILLIS = 1000L * 60 * 60 * 24
    private const val WEEK_MILLIS = 1000L * 60 * 60 * 24 * 7

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
        return "SS${generateCharUUIDWithTimestamp(8)}"
    }

    fun generateConverterId(): String {
        return "CO${generateCharUUIDWithTimestamp(8)}"
    }

    fun generateCloudRepositoryId(): String {
        return "CR${generateCharUUIDWithTimestamp(8)}"
    }

    fun generateCharUUIDWithTimestamp(n: Int): String {
        val uuid = UUID.randomUUID().toString().replace("-", "")
        val timestamp = System.currentTimeMillis().toString(16).takeLast(4)
        return (uuid.substring(0, n) + timestamp).take(n + 4)
    }

    fun getPulledTimeText(context: Context, millis: Long): String {
        val now = System.currentTimeMillis()
        val offset = now - millis
        return if (offset < MINUTE_MILLIS) {
            "1 min ago"
        } else if (offset < HOUR_MILLIS) {
            "${Math.floorDiv(offset, MINUTE_MILLIS)} min ago"
        } else if (offset < DAY_MILLIS) {
            "${Math.floorDiv(offset, HOUR_MILLIS)} hour ago"
        } else if (offset < WEEK_MILLIS) {
            "${Math.floorDiv(offset, DAY_MILLIS)} day ago"
        } else if (offset < 4 * WEEK_MILLIS) {
            "${Math.floorDiv(offset, WEEK_MILLIS)} week ago"
        } else {
            val date = DateUtils.formatDateTime(
                context, millis,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NUMERIC_DATE
            )
            date
        }
    }

    fun isValidRegex(pattern: String): Boolean {
        return try {
            Regex(pattern)
            true
        } catch (e: Exception) {
            false
        }
    }
}