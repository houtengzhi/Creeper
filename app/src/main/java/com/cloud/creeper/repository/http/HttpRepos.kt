package com.cloud.creeper.repository.http

import android.util.Log
import com.cloud.creeper.protocol.core.ApiResponse
import com.cloud.creeper.repository.Gist
import com.cloud.creeper.repository.GistInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 *
 * Created by cloud on 2024/2/21.
 */
class HttpRepos(private val httpClient: OkHttpClient, private val githubService: GithubService) {

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

    suspend fun suspendGetGistList(accessToken: String): List<Gist> {
        return githubService.getGistList("Bearer $accessToken")
    }

    suspend fun suspendGetGist(gistId: String, accessToken: String): Gist {
        return githubService.getGist(gistId, "Bearer $accessToken")
    }

    suspend fun suspendCreateGist(gistInput: GistInput, accessToken: String): Gist {
        val jsonObject = JSONObject()
        gistInput.description?.let {
            jsonObject.put("description", it)
        }
        jsonObject.put("public", gistInput.public)
        val fileJsonObject = JSONObject()

        gistInput.files.forEach {
            val obj = JSONObject()
            obj.put("content", it.content)
            fileJsonObject.put(it.fileName, obj)
        }
        jsonObject.put("files", fileJsonObject)

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return githubService.createGist(requestBody, "Bearer $accessToken")
    }

    suspend fun suspendUpdateGist(gistId: String, gistInput: GistInput, accessToken: String): Gist {
        val jsonObject = JSONObject()
        gistInput.description?.let {
            jsonObject.put("description", it)
        }

        val fileJsonObject = JSONObject()

        gistInput.files.forEach {
            val obj = JSONObject()
            obj.put("content", it.content)
            obj.put("filename", it.fileName)
            fileJsonObject.put(it.fileName, obj)
        }
        jsonObject.put("files", fileJsonObject)

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return githubService.updateGist(gistId, requestBody, "Bearer $accessToken")
    }

    suspend fun suspendDeleteGistFile(gistId: String, gistInput: GistInput, accessToken: String): Gist {
        val jsonObject = JSONObject()
        val fileJsonObject = JSONObject()

        gistInput.files.forEach {
            fileJsonObject.put(it.fileName, null)
        }
        jsonObject.put("files", fileJsonObject)

        val requestBody = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
        return githubService.updateGist(gistId, requestBody, "Bearer $accessToken")
    }

    suspend fun suspendDeleteGist(gistId: String, accessToken: String) {
        githubService.deleteGist(gistId, "Bearer $accessToken")
    }

    suspend fun suspendVerifyUser(token: String) {
        githubService.getAuthenticatedUser("Bearer $token")
    }
}