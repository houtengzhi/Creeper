package com.cloud.spider.repository.http

import android.util.Log
import com.cloud.spider.protocol.core.ApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    fun fetchSubscriptionContent(url: String): Flow<ApiResponse<String>> {
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
}