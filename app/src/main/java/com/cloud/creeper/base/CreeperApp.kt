package com.cloud.creeper.base

import android.app.Application
import com.cloud.creeper.server.ServerManage
import dagger.hilt.android.HiltAndroidApp

/**
 *
 * Created by cloud on 2024/1/26.
 */
@HiltAndroidApp
class CreeperApp: Application() {

    companion object {
        lateinit var INSTANCE: CreeperApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        ServerManage.init(applicationContext)
    }
}