package com.cloud.spider.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Color
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.cloud.spider.R
import com.cloud.spider.util.NetUtil
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/1/30.
 */
@AndroidEntryPoint
class SpiderService: Service() {

    companion object {
        const val TAG = "SpiderService"

        const val NOTIFICATION_CHANNEL_NAME = "Spider"

        const val NOTIFICATION_CHANNEL_ID = "com.cloud.spider"
    }

    private val mBinder = SBinder()

    private lateinit var mServer: Server

    override fun onCreate() {
        super.onCreate()

        mServer = AndServer.webServer(this)
            .port(8080)
            .timeout(15, TimeUnit.SECONDS)
            .listener(object : Server.ServerListener {
                override fun onStarted() {
                    val netAddress = NetUtil.getLocalIPAddress()
                    Log.i(TAG, "server start, ip=${netAddress?.hostAddress}")
                }

                override fun onStopped() {
                    Log.i(TAG, "server stop")
                }

                override fun onException(e: Exception?) {
                    Log.e(TAG, "onException, ${e?.message}")
                    e?.printStackTrace()
                }

            })
            .build()

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME)
            } else {
                NOTIFICATION_CHANNEL_ID
            }
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Spider")

        startForeground(100, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        channel.lightColor = Color.BLUE
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(channel)
        return channelId
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServer()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopServer()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder {
        return mBinder
    }

    private fun startServer() {
        Log.d(TAG, "startServer()")
        mServer.startup()
    }

    private fun stopServer() {
        Log.d(TAG, "stopServer()")
        mServer.shutdown()
    }

    fun isRunning() = mServer.isRunning

    inner class SBinder: Binder() {
        val service: SpiderService get() = this@SpiderService
    }
}