package com.cloud.spider.util

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

/**
 *
 * Created by cloud on 2024/5/11.
 */
class CurrentDispatcher: CoroutineDispatcher() {

    private val handler = Handler(Looper.myLooper()!!)
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        handler.post(block)
    }
}