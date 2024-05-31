package com.cloud.creeper.base

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 *
 * Created by cloud on 2024/1/26.
 */
@HiltAndroidApp
class SpiderApp: Application() {

    companion object {
        lateinit var INSTANCE: SpiderApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
}