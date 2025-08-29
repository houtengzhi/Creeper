package com.cloud.creeper.repository

import android.util.Log
import com.cloud.creeper.base.DataState
import com.cloud.creeper.base.VMError
import com.cloud.creeper.protocol.ClashConfig
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.protocol.ProxyConfig
import com.cloud.creeper.protocol.clash.ClashProxyNode
import com.cloud.creeper.protocol.core.ApiResponse
import com.cloud.creeper.protocol.core.ConverterUtil
import com.cloud.creeper.protocol.core.onError
import com.cloud.creeper.protocol.core.onSuccess
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.SourceStatus
import com.cloud.creeper.repository.entity.SubscriptionDetails
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.repository.http.HttpRepos
import com.cloud.creeper.ui.source.SubscriptionViewModel
import com.cloud.creeper.util.RepositoryType
import com.cloud.creeper.util.SystemUtil
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext

/**
 *
 * Created by cloud on 2024/4/29.
 */
class DataRepos(val httpRepos: HttpRepos, val dbRepos: DbRepos, val fileRepos: FileRepos) {

    companion object {
        private const val TAG = "DataRepos"
    }

    private suspend fun suspendMergerSubscriptionSources(subscriptionSourceList: List<SubscriptionSource>): List<ProxyConfig> {
        Log.d(TAG, "suspendMergerSubscriptionSources()")
        val deferredList = mutableListOf<Deferred<ProxyConfig?>>()
        val proxyConfigList = mutableListOf<ProxyConfig>()

        supervisorScope {
            subscriptionSourceList.forEach { source ->
                val deferred = async {
                    val apiResponse = httpRepos.suspendFetchUrl(source.sourceUrl)
                    when (apiResponse) {
                        is ApiResponse.Success<String> -> {
                            Log.d(TAG, "fetchSubscription success, sourceType=${source.type}")
                            when (source.type) {
                                ClientType.Clash -> {
                                    withContext(Dispatchers.Default) {
                                        val clashConfig = ConverterUtil.deserializeClashConfig(apiResponse.data)
                                        clashConfig
                                    }
                                }

                                ClientType.V2Ray -> {
                                    withContext(Dispatchers.Default) {
                                        val v2RayConfig = ConverterUtil.deserializeV2RaySubscription(apiResponse.data)
                                        v2RayConfig
                                    }
                                }

                                else -> {
                                    Log.e(TAG, "fetchSubscription not supported type ${source.type}")
                                    null
                                }
                            }
                        }

                        is ApiResponse.Error -> {
                            Log.e(TAG, "fetchSubscription error=${apiResponse}")
                            null
                        }

                        is ApiResponse.Exception -> {
                            Log.e(TAG, "fetchSubscription exception=${apiResponse}")
                            null
                        }

                    }
                }
                deferredList.add(deferred)
            }

            deferredList.forEach { deferred ->
                try {
                    deferred.await()?.let {
                        proxyConfigList.add(it)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.w(TAG, "fetchSubscription exception for: ${e.message}")
                }

            }
        }
        return proxyConfigList
    }

    suspend fun suspendConvertSubscription(converter: ConverterWithSources): ConverterWithSources? {
        Log.d(TAG, "suspendConvertSubscription(), converter=${converter.converter}")
        return withContext(Dispatchers.Default) {
            val proxyConfigList = suspendMergerSubscriptionSources(converter.subscriptionSourceList)

            if (proxyConfigList.isNotEmpty()) {
                val content: String
                when (converter.converter.outputType) {
                    ClientType.Clash -> {
                        val proxyNodeList = mutableListOf<ClashProxyNode>()
                        proxyConfigList.forEach { config ->
                            val clashConfig = config.toClashConfig()
                            clashConfig.proxies?.filter { node ->
                                val exclude = converter.converter.exclude
                                if (exclude.isNullOrEmpty()) {
                                    true
                                } else {
                                    val excluded = if (SystemUtil.isValidRegex(exclude)) {
                                        Regex(exclude).find(node.toString()) != null
                                    } else {
                                        node.toString().contains(exclude)
                                    }
                                    if (excluded) {
                                        Log.v(TAG, "${node.name} is excluded")
                                        false
                                    } else {
                                        true
                                    }
                                }
                            }?.let {
                                proxyNodeList.addAll(it)
                            }
                        }
                        content =  ConverterUtil.serializeClashConfig(ClashConfig(proxies = proxyNodeList))
                        if (converter.converter.outputFileName == null) {
                            converter.converter.outputFileName = "${converter.converter.name}.yaml"
                        }

                    }
                    ClientType.V2Ray -> {
                        content = ""
                    }
                    else -> {
                        content = ""
                    }
                }
                if (converter.cloudRepositoryList.isNullOrEmpty()) {
                    Log.i(TAG, "suspendConvertSubscription() cloud repository list is empty")
                }
                converter.cloudRepositoryList?.let { dataList ->
                    dataList.forEach {
                        if (it.type == RepositoryType.REPOSITORY_GITHUB) {
                            val gistFileInput = GistFileInput(if (it.gistFileName == null) converter.converter.name else it.gistFileName!!, content)
                            val des = converter.converter.description
                            val gistInput = GistInput(des, false, mutableListOf(gistFileInput))

                            Log.d(TAG, "Push to github, gistId=${it.gistId}, fileName=${it.gistFileName}")
                            val gist = if (it.gistId == null) {
                                httpRepos.suspendCreateGist(gistInput, it.accessToken!!)
                            } else {
                                httpRepos.suspendUpdateGist(it.gistId!!, gistInput, it.accessToken!!)
                            }
                            it.url = gist.url
                        }
                    }
                }
                val file = fileRepos.suspendSaveConverter(converter.converter.outputFileName!!, content)
                converter.converter.outputFile = file
               converter

            } else {
                Log.w(TAG, "suspendConvertSubscription() merged proxyConfigList is empty")
                null
            }
        }
    }

    suspend fun suspendDeleteConverter(converter: ConverterWithSources, deleteRemoteRepos: Boolean) {
        Log.d(TAG, "suspendDeleteConverter(), deleteRemoteRepos=${deleteRemoteRepos}")
        return withContext(Dispatchers.Default) {
            fileRepos.suspendDeleteConverter(converter.converter.outputFileName!!)
            if (deleteRemoteRepos) {
                converter.cloudRepositoryList?.forEach {
                    if (it.type == RepositoryType.REPOSITORY_GITHUB) {
                        httpRepos.suspendDeleteGistFile(
                            it.gistId!!,
                            it.gistFileName!!,
                            it.accessToken!!
                        )
                    } else {

                    }
                }
            }
            dbRepos.suspendDeleteConverter(converter)
        }
    }

    fun addSubscriptionSource(subscriptionSource: SubscriptionSource): ApiResponse<SubscriptionSource> {
        Log.d(TAG, "addSubscriptionSource()")
        var content: String?
        when (val apiResponse = httpRepos.fetchUrl(subscriptionSource.sourceUrl)) {
            is ApiResponse.Success -> {
                Log.d(TAG, "fetchSubscriptionContent success, sourceType=${subscriptionSource.type}")
                fileRepos.saveSubscriptionSource(
                    subscriptionSource.getCacheFileName(),
                    apiResponse.data
                )
                subscriptionSource.pullStatus = SourceStatus.UPDATED
                subscriptionSource.pulledTime = System.currentTimeMillis()
                subscriptionSource.updatedTime = System.currentTimeMillis()
                content = apiResponse.data
            }

            is ApiResponse.Error -> {
                Log.e(TAG, "fetchSubscriptionContent error=${apiResponse}")
                return apiResponse
            }

            is ApiResponse.Exception -> {
                Log.e(TAG, "fetchSubscriptionContent exception=${apiResponse}")
                return apiResponse
            }

        }

        content.let {
            val clashConfig = ConverterUtil.parseToClashConfig(subscriptionSource.type, it)
            Log.d(
                TAG, "proxies size=${clashConfig.proxies?.size}"
            )
            if (!clashConfig.proxies.isNullOrEmpty()) {
                dbRepos.insertSubscriptionSource(subscriptionSource)
                return ApiResponse.Success(subscriptionSource)
            }
        }
        return ApiResponse.Error(VMError.EmptyProxyList)
    }

    fun updateSubscriptionSource(subscriptionSource: SubscriptionSource): ApiResponse<SubscriptionSource> {
        Log.d(TAG, "updateSubscriptionSource()")
        val oldSubscriptionSource = dbRepos.querySubscriptionSourceById(subscriptionSource.id)
        if (oldSubscriptionSource == null) {
            return ApiResponse.Error(VMError.SubscriptionSourceNotFound)
        }
        if (subscriptionSource.sourceUrl != oldSubscriptionSource.sourceUrl) {
            var content: String?
            when (val apiResponse = httpRepos.fetchUrl(subscriptionSource.sourceUrl)) {
                is ApiResponse.Success -> {
                    Log.d(TAG, "fetchSubscriptionContent success, sourceType=${subscriptionSource.type}")
                    fileRepos.saveSubscriptionSource(
                        subscriptionSource.getCacheFileName(),
                        apiResponse.data
                    )
                    subscriptionSource.pullStatus = SourceStatus.UPDATED
                    subscriptionSource.pulledTime = System.currentTimeMillis()
                    subscriptionSource.updatedTime = System.currentTimeMillis()
                    content = apiResponse.data
                }

                is ApiResponse.Error -> {
                    Log.e(TAG, "fetchSubscriptionContent error=${apiResponse}")
                    return apiResponse
                }

                is ApiResponse.Exception -> {
                    Log.e(TAG, "fetchSubscriptionContent exception=${apiResponse}")
                    return apiResponse
                }

            }

            content.let {
                val clashConfig = ConverterUtil.parseToClashConfig(subscriptionSource.type, it)
                Log.d(
                    TAG, "proxies size=${clashConfig.proxies?.size}"
                )
                if (!clashConfig.proxies.isNullOrEmpty()) {
                    dbRepos.updateSubscriptionSource(subscriptionSource)
                    return ApiResponse.Success(subscriptionSource)
                }
            }
        } else {
            dbRepos.updateSubscriptionSource(subscriptionSource)
            return ApiResponse.Success(subscriptionSource)
        }
        return ApiResponse.Error(VMError.EmptyProxyList)
    }

    fun deleteSubscriptionSource(
        subscriptionSource: SubscriptionSource
    ): ApiResponse<Boolean> {
        Log.d(TAG, "deleteSubscriptionSource()")
        val file = fileRepos.readSubscriptionSourceFile(subscriptionSource.getCacheFileName())
        if (file.exists()) {
            fileRepos.deleteSubscriptionSource(subscriptionSource.getCacheFileName())
        }
        dbRepos.deleteSubscriptionSource(subscriptionSource)
        return ApiResponse.Success(true)
    }

    fun getSubscriptionDetails(
        subscriptionSource: SubscriptionSource,
        forceRefresh: Boolean
    ): ApiResponse<SubscriptionDetails> {
        Log.d(TAG, "getSubscriptionDetails()")
        var content: String? = null

        val file = fileRepos.readSubscriptionSourceFile(subscriptionSource.getCacheFileName())
        if (forceRefresh || !file.exists()) {
            when (val apiResponse = httpRepos.fetchUrl(subscriptionSource.sourceUrl)) {
                is ApiResponse.Success -> {
                    fileRepos.saveSubscriptionSource(subscriptionSource.getCacheFileName(), apiResponse.data)

                    subscriptionSource.pullStatus = SourceStatus.UPDATED
                    subscriptionSource.pulledTime = System.currentTimeMillis()
                    subscriptionSource.updatedTime = System.currentTimeMillis()
                    dbRepos.updateSubscriptionSource(subscriptionSource)
                    content = apiResponse.data

                }

                is ApiResponse.Error -> {
                    return apiResponse

                }

                is ApiResponse.Exception -> {
                    return apiResponse
                }
            }

        } else {
            content = file.readText()
        }

        var subscriptionDetails: SubscriptionDetails? = null
        content.let {
            val clashConfig = ConverterUtil.parseToClashConfig(subscriptionSource.type, it)
            Log.d(TAG, "fetchSubscriptionDetails(), proxies size=${clashConfig.proxies?.size}"
            )
            if (!clashConfig.proxies.isNullOrEmpty()) {
                subscriptionDetails = SubscriptionDetails(subscriptionSource, clashConfig.proxies)
            }
        }

        return if (subscriptionDetails != null) {
            ApiResponse.Success(subscriptionDetails!!)

        } else {
            ApiResponse.Error(VMError.EmptyProxyList)
        }
    }

    suspend fun suspendAddSubscriptionSource(subscriptionSource: SubscriptionSource): ApiResponse<SubscriptionSource> {
        Log.d(TAG, "suspendAddSubscriptionSource()")
        return withContext(Dispatchers.Default) {
            return@withContext addSubscriptionSource(subscriptionSource)
        }
    }

    suspend fun suspendUpdateSubscriptionSource(subscriptionSource: SubscriptionSource): ApiResponse<SubscriptionSource> {
        Log.d(TAG, "suspendUpdateSubscriptionSource()")
        return withContext(Dispatchers.Default) {
            return@withContext updateSubscriptionSource(subscriptionSource)
        }
    }

    suspend fun suspendDeleteSubscriptionSource(subscriptionSource: SubscriptionSource): ApiResponse<Boolean> {
        Log.d(TAG, "suspendDeleteSubscriptionSource()")
        return withContext(Dispatchers.Default) {
            return@withContext deleteSubscriptionSource(subscriptionSource)
        }
    }

    suspend fun suspendGetSubscriptionDetails(
        subscriptionSource: SubscriptionSource,
        forceRefresh: Boolean
    ): ApiResponse<SubscriptionDetails> {
        Log.d(TAG, "suspendGetSubscriptionDetails()")
        return withContext(Dispatchers.Default) {
            return@withContext getSubscriptionDetails(subscriptionSource, forceRefresh)
        }
    }

}