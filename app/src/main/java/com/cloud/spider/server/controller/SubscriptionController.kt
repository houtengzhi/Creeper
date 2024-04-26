package com.cloud.spider.server.controller

import com.cloud.spider.base.SpiderApp
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.file.FileRepos
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.net.HttpURLConnection

/**
 *
 * Created by cloud on 2024/1/30.
 */
@RestController
class SubscriptionController {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryProvider {
        fun getDbRepos(): DbRepos

        fun getFileRepos(): FileRepos
    }

    @GetMapping("/subscription")
    fun getSubscription(): String {
        return "test"
    }

    @GetMapping("/spider/converter/{converterId}")
    fun downloadConverterFile(@PathVariable("converterId") converterId: String, httpRequest: HttpRequest, httpResponse: HttpResponse) {
        val provider = EntryPoints.get(SpiderApp.INSTANCE, EntryProvider::class.java)
        val fileRepos = provider.getFileRepos()
        val dbRepos = provider.getDbRepos()
        val converter = dbRepos.queryConverterById(converterId)
        var file: File? = null
        converter?.let {
            it.converter.outputFileName?.let { fileName ->
                file = fileRepos.readConverterFile(fileName)
            }
        }

        if (file == null || !file!!.exists()) {
            httpResponse.status = HttpURLConnection.HTTP_NOT_FOUND
        } else {
            httpResponse.status = HttpURLConnection.HTTP_OK
            httpResponse.setBody(FileBody(file))
        }
    }
}