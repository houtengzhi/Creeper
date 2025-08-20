package com.cloud.creeper.server.controller

import android.util.Log
import com.cloud.creeper.base.CreeperApp
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.protocol.core.ApiResponse.Error
import com.cloud.creeper.protocol.core.ApiResponse.Exception
import com.cloud.creeper.protocol.core.ApiResponse.Success
import com.cloud.creeper.protocol.core.mapSuccess
import com.cloud.creeper.protocol.core.onError
import com.cloud.creeper.protocol.core.onException
import com.cloud.creeper.protocol.core.onSuccess
import com.cloud.creeper.repository.DataRepos
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.SubscriptionDetails
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.server.GenericHttpException
import com.cloud.creeper.server.model.ApiResult
import com.cloud.creeper.server.model.ResponseCode
import com.cloud.creeper.server.model.SubscriptionDetailsOutput
import com.cloud.creeper.server.model.SubscriptionInput
import com.cloud.creeper.server.model.SubscriptionOutput
import com.cloud.creeper.server.model.SubscriptionsOutput
import com.cloud.creeper.util.CurrentDispatcher
import com.cloud.creeper.util.NetUtil
import com.cloud.creeper.util.SUPPORTED_SOURCE_TYPE_LIST
import com.cloud.creeper.util.SystemUtil
import com.yanzhenjie.andserver.annotation.Controller
import com.yanzhenjie.andserver.annotation.DeleteMapping
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.PathVariable
import com.yanzhenjie.andserver.annotation.PostMapping
import com.yanzhenjie.andserver.annotation.QueryParam
import com.yanzhenjie.andserver.annotation.RequestBody
import com.yanzhenjie.andserver.annotation.ResponseBody
import com.yanzhenjie.andserver.framework.body.FileBody
import com.yanzhenjie.andserver.framework.body.JsonBody
import com.yanzhenjie.andserver.http.HttpHeaders
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.http.StatusCode
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.json.JSONArray
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

    @ResponseBody
    @PostMapping("/creeper/subscriptions")
    fun addSubscriptionSource(@RequestBody subscriptionInput: SubscriptionInput, httpRequest: HttpRequest, httpResponse: HttpResponse): ApiResult<Nothing> {
        Log.d(TAG, "addSubscriptionSources() ${subscriptionInput}")
        if (subscriptionInput.name.isNullOrEmpty()){
            throw GenericHttpException(ResponseCode.ERROR_INVALID_REQUEST, StatusCode.SC_BAD_REQUEST, "Name is required.")
        }
        if (subscriptionInput.sourceUrl.isNullOrEmpty()) {
            throw GenericHttpException(ResponseCode.ERROR_INVALID_REQUEST, StatusCode.SC_BAD_REQUEST, "Source url is required.")
        }
        if (!NetUtil.isValidUrl(subscriptionInput.sourceUrl)) {
            throw GenericHttpException(ResponseCode.ERROR_INVALID_REQUEST, StatusCode.SC_BAD_REQUEST, "Source url is invalid.")
        }
        if (subscriptionInput.type.isNullOrEmpty()) {
            throw GenericHttpException(ResponseCode.ERROR_INVALID_REQUEST, StatusCode.SC_BAD_REQUEST, "Type is required.")
        }
        if (!SUPPORTED_SOURCE_TYPE_LIST.contains(ClientType.valueOf(subscriptionInput.type))) {
            throw GenericHttpException(ResponseCode.ERROR_INVALID_REQUEST, StatusCode.SC_BAD_REQUEST, "Type must be one of ${SUPPORTED_SOURCE_TYPE_LIST.joinToString { it.name }}")
        }
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val dbRepos = provider.getDbRepos()
        val subscriptionSource = SubscriptionSource(SystemUtil.generateSubscriptionSourceId(), subscriptionInput.name, subscriptionInput.sourceUrl, ClientType.valueOf(subscriptionInput.type)).apply {
            this.description = subscriptionInput.description
            createdTime = System.currentTimeMillis()
            updatedTime = System.currentTimeMillis()
        }
        dbRepos.insertSubscriptionSource(subscriptionSource)
        httpResponse.status = HttpURLConnection.HTTP_OK
        return ApiResult(ResponseCode.SUCCESS)
    }

    @ResponseBody
    @PostMapping("/creeper/subscriptions/{source_id}")
    fun updateSubscriptionSource(@PathVariable("source_id") sourceId: String, @RequestBody subscriptionInput: SubscriptionInput, httpRequest: HttpRequest, httpResponse: HttpResponse): ApiResult<Nothing> {
        Log.d(TAG, "updateSubscriptionSource() ${subscriptionInput}")
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val dbRepos = provider.getDbRepos()

        val subscriptionSource = dbRepos.querySubscriptionSourceById(sourceId)
        if (subscriptionSource == null) {
            throw GenericHttpException(ResponseCode.ERROR_NOT_FOUND, StatusCode.SC_NOT_FOUND, "Subscription source not found.")
        } else {

            if (!subscriptionInput.sourceUrl.isNullOrEmpty() && NetUtil.isValidUrl(subscriptionInput.sourceUrl)) {
                throw GenericHttpException(ResponseCode.ERROR_INVALID_REQUEST, StatusCode.SC_BAD_REQUEST, "Source url is invalid.")
            }
            if (!subscriptionInput.type.isNullOrEmpty() && !SUPPORTED_SOURCE_TYPE_LIST.contains(ClientType.valueOf(subscriptionInput.type))) {
                throw GenericHttpException(ResponseCode.ERROR_INVALID_REQUEST, StatusCode.SC_BAD_REQUEST, "Type must be one of ${SUPPORTED_SOURCE_TYPE_LIST.joinToString { it.name }}")
            }

            val name = subscriptionInput.name ?: subscriptionSource.name
            val desc = subscriptionInput.description ?: subscriptionSource.description
            val type =  ClientType.valueOf(subscriptionInput.type ?: subscriptionSource.type.name)
            val url = subscriptionInput.sourceUrl ?: subscriptionSource.sourceUrl

            val newSource = SubscriptionSource(sourceId, name, url, type).apply {
                this.description = desc
                createdTime = System.currentTimeMillis()
                updatedTime = System.currentTimeMillis()
            }
            dbRepos.updateSubscriptionSource(newSource)
        }

        httpResponse.status = HttpURLConnection.HTTP_OK
        return ApiResult(ResponseCode.SUCCESS)
    }

    @ResponseBody
    @DeleteMapping("/creeper/subscriptions/{source_id}")
    fun deleteSubscriptionSource(@PathVariable("source_id") sourceId: String, httpRequest: HttpRequest, httpResponse: HttpResponse): ApiResult<Nothing> {
        Log.d(TAG, "deleteSubscriptionSource() sourceId = ${sourceId}")
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val dbRepos = provider.getDbRepos()
        val dataRepos = provider.getDataRepos()
        val subscriptionSource = dbRepos.querySubscriptionSourceById(sourceId)

        val result: ApiResult<Nothing> = if (subscriptionSource == null) {
            throw GenericHttpException(ResponseCode.ERROR_NOT_FOUND, StatusCode.SC_NOT_FOUND, "Subscription source not found.")
        } else {

            val apiResponse = dataRepos.deleteSubscriptionSource(subscriptionSource)
            when (apiResponse) {
                is Success -> {
                    httpResponse.status = StatusCode.SC_OK
                    ApiResult(code = ResponseCode.SUCCESS)
                }
                is Error -> {
                    throw GenericHttpException(ResponseCode.ERROR_FAILED, StatusCode.SC_INTERNAL_SERVER_ERROR, apiResponse.errorMessage)
                }
                is Exception -> {
                    throw GenericHttpException(ResponseCode.ERROR_FAILED, StatusCode.SC_INTERNAL_SERVER_ERROR, apiResponse.throwable)
                }
            }
        }

        return result
    }

    @ResponseBody
    @GetMapping("/creeper/subscriptions")
    fun listSubscriptionSources(httpRequest: HttpRequest, httpResponse: HttpResponse): ApiResult<SubscriptionsOutput> {
        Log.d(TAG, "listSubscriptionSources()")
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val dbRepos = provider.getDbRepos()
        val sourceList = dbRepos.querySubscriptionSourceList().map {
            SubscriptionOutput(it.id, it.name, it.sourceUrl, it.type.name).apply {
                this.description = if (it.description.isNullOrEmpty()) null else it.description
                this.iconPath = it.getClientIconPath()
                this.createdTime = it.createdTime
                this.pulledTime = it.pulledTime
                this.pullStatus = it.pullStatus
            }
        }
        val output = SubscriptionsOutput(sourceList)

        httpResponse.status = HttpURLConnection.HTTP_OK
        val result = ApiResult(ResponseCode.SUCCESS, output)
        return result
    }

    @ResponseBody
    @GetMapping("/creeper/subscriptions/{source_id}")
    fun getSubscriptionSourceDetails(@PathVariable("source_id") sourceId: String, @QueryParam("force_refresh", required = false) forceRefresh: Boolean, httpRequest: HttpRequest, httpResponse: HttpResponse): ApiResult<SubscriptionDetailsOutput> {
        Log.d(TAG, "listSubscriptionSources()")
        val provider = EntryPoints.get(CreeperApp.INSTANCE, EntryProvider::class.java)
        val dbRepos = provider.getDbRepos()
        val dataRepos = provider.getDataRepos()
        val subscriptionSource = dbRepos.querySubscriptionSourceById(sourceId)

        val result: ApiResult<SubscriptionDetailsOutput> = if (subscriptionSource == null) {
            throw GenericHttpException(ResponseCode.ERROR_NOT_FOUND, StatusCode.SC_NOT_FOUND, "Subscription source not found.")
        } else {
            val apiResponse = dataRepos.getSubscriptionDetails(subscriptionSource, forceRefresh)
            when (apiResponse) {
                is Success -> {
                    httpResponse.status = StatusCode.SC_OK
                    ApiResult(code = ResponseCode.SUCCESS, data = SubscriptionDetailsOutput(apiResponse.data.subscriptionSource.id, apiResponse.data.subscriptionSource.name,
                        apiResponse.data.subscriptionSource.sourceUrl, apiResponse.data.subscriptionSource.type.name, apiResponse.data.nodeList))
                }
                is Error -> {
                    throw GenericHttpException(ResponseCode.ERROR_FAILED, StatusCode.SC_INTERNAL_SERVER_ERROR, apiResponse.errorMessage)
                }
                is Exception -> {
                    throw GenericHttpException(ResponseCode.ERROR_FAILED, StatusCode.SC_INTERNAL_SERVER_ERROR, apiResponse.throwable)
                }
            }
        }

        return result
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