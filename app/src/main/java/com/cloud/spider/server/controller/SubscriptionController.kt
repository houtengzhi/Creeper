package com.cloud.spider.server.controller

import android.util.Log
import com.cloud.spider.base.SpiderApp
import com.cloud.spider.repository.DataRepos
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.util.CurrentDispatcher
import com.yanzhenjie.andserver.annotation.Controller
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.framework.body.FileBody
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

    @GetMapping("/spider/converter/{converterId}/{fileName}")
    fun downloadConverterFile(@PathVariable("converterId") converterId: String, @PathVariable("fileName") fileName: String, httpRequest: HttpRequest, httpResponse: HttpResponse) {
        Log.d(TAG, "downloadConverterFile: converterId=${converterId}, fileName=${fileName}")
        val provider = EntryPoints.get(SpiderApp.INSTANCE, EntryProvider::class.java)
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
                val file1 = dataRepos.suspendConvertSubscription(converter)

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