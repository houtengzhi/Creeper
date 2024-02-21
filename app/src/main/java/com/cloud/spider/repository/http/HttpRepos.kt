package com.cloud.spider.repository.http

import com.cloud.spider.protocol.core.ApiResponse
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 *
 * Created by cloud on 2024/2/21.
 */
class HttpRepos(val httpClient: OkHttpClient) {

    fun getSubscriptionContent(url: String): ApiResponse<String> {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = httpClient.newCall(request).execute()
        return if (response.isSuccessful) {
            val result = response.body!!.string()
            response.body?.close()
            ApiResponse.Success(result)
        } else {
            ApiResponse.Error(response.code, response.message)
        }
    }
}