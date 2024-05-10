package com.cloud.spider.repository.http

import android.util.Log
import com.cloud.spider.protocol.core.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 *
 * Created by cloud on 2024/2/21.
 */
class HttpRepos(private val httpClient: OkHttpClient) {

    companion object {
        private const val TAG = "HttpRepos"
    }

    fun fetchUrl(url: String): Flow<ApiResponse<String>> {
        return flow {
            Log.d(TAG, "fetchSubscriptionContent")
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response = httpClient.newCall(request).execute()
            emit(if (response.isSuccessful) {
                val result = response.body!!.string()
                response.body?.close()
                ApiResponse.Success(result)
            } else {
                ApiResponse.Error(response.code, response.message)
            })
        }
    }

    suspend fun suspendFetchUrl(url: String): ApiResponse<String> {
        Log.d(TAG, "suspendFetchUrl(), url=${url}")
        return withContext(context = Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response = httpClient.newCall(request).execute()
            (if (response.isSuccessful) {
                Log.d(TAG, "suspendFetchUrl() successful")
                val result = response.body!!.string()
                response.body?.close()
                ApiResponse.Success(result)
            } else {
                Log.e(TAG, "suspendFetchUrl() failed")
                ApiResponse.Error(response.code, response.message)
            })
        }
    }
}