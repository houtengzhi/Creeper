package com.cloud.creeper.server.controller

import android.util.Log
import com.cloud.creeper.base.CreeperApp
import com.cloud.creeper.repository.DataRepos
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.server.model.SubscriptionInput
import com.cloud.creeper.server.model.SubscriptionOutput
import com.cloud.creeper.util.CurrentDispatcher
import com.yanzhenjie.andserver.annotation.Controller
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.framework.body.JsonBody
import com.yanzhenjie.andserver.http.HttpHeaders
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection

/**
 *
 * Created by cloud on 2024/1/30.
 */
@Controller
class SubscriptionController {

    companion object {
        const val TAG = "SubscriptionController"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryProvider {
        fun getDbRepos(): DbRepos

        fun getFileRepos(): FileRepos

        fun getDataRepos(): DataRepos
    }

    @ResponseBody
    @GetMapping("/subscription")
    fun getSubscription(): String {
        return "test"
    }

    @PostMapping("/creeper/subscriptions")
    fun addSubscriptionSources(@RequestBody subscriptionInput: SubscriptionInput, httpRequest: HttpRequest, httpResponse: HttpResponse) {
        Log.d(TAG, "addSubscriptionSources() ${subscriptionInput}")
        httpResponse.status = HttpURLConnection.HTTP_OK
        val jsonObject : JSONObject  = JSONObject()
        jsonObject.put("code", "RESPONSE_SUCCESS")
        jsonObject.put("data", "")
        httpResponse.setBody(JsonBody(jsonObject))
    }

    @GetMapping("/creeper/subscriptions")
    fun listSubscriptionSources(httpRequest: HttpRequest, httpResponse: HttpResponse) {
        Log.d(TAG, "listSubscriptionSources()")
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val dbRepos = provider.getDbRepos()
        val sourceList = dbRepos.querySubscriptionSourceList()
        val data = sourceList.map {
            SubscriptionOutput(it.name, it.sourceUrl, it.type.name)
        }
        httpResponse.status = HttpURLConnection.HTTP_OK
        val jsonObject = JSONObject()
        jsonObject.put("code", "RESPONSE_SUCCESS")
        jsonObject.put("data", data)
        httpResponse.setBody(JsonBody(jsonObject))
    }

    @GetMapping("/creeper/converter/{converterId}/{fileName}")
    fun downloadConverterFile(@PathVariable("converterId") converterId: String, @PathVariable("fileName") fileName: String, httpRequest: HttpRequest, httpResponse: HttpResponse) {
        Log.d(TAG, "downloadConverterFile: converterId=${converterId}, fileName=${fileName}")
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val fileRepos = provider.getFileRepos()
        val dbRepos = provider.getDbRepos()
        val dataRepos = provider.getDataRepos()
        val converter = dbRepos.queryConverterById(converterId)
        if(converter == null) {
            Log.e(TAG, "downloadConverterFile: converter ${converterId} not found")
            httpResponse.status = HttpURLConnection.HTTP_NOT_FOUND
            return
        }
        val file: File = fileRepos.readConverterFile(fileName)
        if (!file.exists()) {
            Log.i(TAG, "downloadConverterFile: $file not existed")
            val dispatcher = CurrentDispatcher()
            val job = SupervisorJob()
            CoroutineScope(Dispatchers.Default + job).launch {
                val result = dataRepos.suspendConvertSubscription(converter)
                val file1 = result?.converter?.outputFile

                withContext(dispatcher) {
                    if (file1 != null && file1.exists()) {
                        httpResponse.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8")
                        httpResponse.setHeader("Content-disposition", "inline;fileName=${file1.name}")
                        httpResponse.status = HttpURLConnection.HTTP_OK
                        httpResponse.setBody(FileBody(file1))
                    } else {
                        httpResponse.status = HttpURLConnection.HTTP_NOT_FOUND
                    }
                }
            }
            job.cancel()
        } else {
            Log.d(TAG, "downloadConverterFile: file=${file.absolutePath}")
            httpResponse.setHeader(HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8")
            httpResponse.setHeader("Content-Disposition", "inline;fileName=${file.name}")
            httpResponse.status = HttpURLConnection.HTTP_OK
            httpResponse.setBody(FileBody(file))
        }
    }
}