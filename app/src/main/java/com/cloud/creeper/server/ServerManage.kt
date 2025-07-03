package com.cloud.creeper.server

import android.content.Context
import android.content.SharedPreferences

/**
 *
 * Created by cloud on 2024/5/6.
 */
object ServerManage {
    const val DEFAULT_PORT = 11991

    private const val PREF_NAME = "server_manage_pref"
    private lateinit var prefs: SharedPreferences

    private const val KEY_PORT = "server_port";

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    private fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    fun getPort(): Int {
        return prefs.getInt(KEY_PORT, DEFAULT_PORT)
    }

    fun savePort(port: Int) {
        prefs.edit().putInt(KEY_PORT, port).apply()
    }
}