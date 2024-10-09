package com.cloud.creeper.repository

import android.util.Log
import com.cloud.creeper.protocol.ClashConfig
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.protocol.ProxyConfig
import com.cloud.creeper.protocol.clash.ClashProxyNode
import com.cloud.creeper.protocol.core.ApiResponse
import com.cloud.creeper.protocol.core.ConverterUtil
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.repository.http.HttpRepos
import com.cloud.creeper.util.RepositoryType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.io.File

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
                                        val v2RayConfig = ConverterUtil.readV2RaySubscription(apiResponse.data)
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
                            clashConfig.proxies?.let {
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
                converter.cloudRepositoryList?.let { dataList ->
                    dataList.forEach {
                        if (it.type == RepositoryType.REPOSITORY_GITHUB) {
                            val gistFileInput = GistFileInput(converter.converter.name, content)
                            val des = converter.converter.description
                            val gistInput = GistInput(des, false, mutableListOf(gistFileInput))

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
                null
            }
        }
    }

    suspend fun suspendDeleteConverter(converter: ConverterWithSources) {
        return withContext(Dispatchers.Default) {
            fileRepos.suspendDeleteConverter(converter.converter.outputFileName!!)
            converter.cloudRepositoryList?.forEach {
                if (it.type == RepositoryType.REPOSITORY_GITHUB) {
                    httpRepos.suspendDeleteGistFile(it.gistId!!, it.gistFileName!!, it.accessToken!!)
                } else {

                }
            }
            dbRepos.suspendDeleteConverter(converter)
        }
    }
}