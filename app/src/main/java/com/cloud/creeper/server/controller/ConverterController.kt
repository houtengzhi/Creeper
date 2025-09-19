package com.cloud.creeper.server.controller

import android.util.Log
import com.cloud.creeper.base.CreeperApp
import com.cloud.creeper.repository.DataRepos
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.server.model.ApiResult
import com.cloud.creeper.server.model.ResponseCode
import com.cloud.creeper.server.model.SubscriptionOutput
import com.cloud.creeper.server.model.SubscriptionsOutput
import com.yanzhenjie.andserver.annotation.Controller
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.net.HttpURLConnection

@Controller
class ConverterController {
    companion object {
        const val TAG = "ConverterController"
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface EntryProvider {
        fun getDbRepos(): DbRepos

        fun getFileRepos(): FileRepos

        fun getDataRepos(): DataRepos
    }

    @ResponseBody
    @GetMapping("/creeper/converters")
    fun listConverters(httpRequest: HttpRequest, httpResponse: HttpResponse): ApiResult<SubscriptionsOutput> {
        Log.d(TAG, "listConverters()")
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val dbRepos = provider.getDbRepos()
        val sourceList = dbRepos.querySubscriptionSourceList().map {
            SubscriptionOutput(it.id, it.name, it.sourceUrl, it.type.name).apply {
                this.description = if (it.description.isNullOrEmpty()) null else it.description
                this.iconPath = it.getClientIconPath()
                this.createdTime = it.createdTime
                this.updatedTime = it.updatedTime
                this.pullStatus = it.pullStatus
            }
        }
        val output = SubscriptionsOutput(sourceList)

        httpResponse.status = HttpURLConnection.HTTP_OK
        val result = ApiResult(ResponseCode.SUCCESS, output)
        return result
    }
}