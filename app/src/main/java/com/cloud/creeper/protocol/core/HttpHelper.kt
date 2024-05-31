package com.cloud.creeper.protocol.core

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 *
 * Created by cloud on 2024/2/19.
 */
class HttpHelper private constructor() {
    private var okHttpClient: OkHttpClient

    init {
        val builder: OkHttpClient.Builder = OkHttpClient.Builder()
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        builder.connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor.apply {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            })
        okHttpClient = builder.build()
    }

    companion object {

        @Volatile private var instance : HttpHelper? = null
        fun getInstance() = instance ?: synchronized(HttpHelper::class.java) {
            instance ?: HttpHelper().also {
                instance = it
            }
        }
    }

    fun fetchSubscription(url: String): ApiResponse<String> {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()
        return if (response.isSuccessful) {
            val result = response.body!!.string()
            response.body?.close()
            ApiResponse.Success(result)
        } else {
            ApiResponse.Error(response.code, response.message)
        }
    }
}