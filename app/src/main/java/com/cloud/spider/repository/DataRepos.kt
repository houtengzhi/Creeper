package com.cloud.spider.repository

import android.util.Log
import com.cloud.spider.protocol.ClashConfig
import com.cloud.spider.protocol.ClientType
import com.cloud.spider.protocol.ProxyConfig
import com.cloud.spider.protocol.clash.ClashProxyNode
import com.cloud.spider.protocol.core.ApiResponse
import com.cloud.spider.protocol.core.ConverterUtil
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.entity.ConverterWithSources
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
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

        val deferredList = mutableListOf<Deferred<ProxyConfig?>>()
        val proxyConfigList = mutableListOf<ProxyConfig>()

        supervisorScope {
            subscriptionSourceList.forEach { source ->
                val deferred = async {
                    val apiResponse = httpRepos.suspendFetchSubscriptionContent(source.sourceUrl)
                    when (apiResponse) {
                        is ApiResponse.Success<String> -> {
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
                                    null
                                }
                            }
                        }

                        is ApiResponse.Error -> {
                            null
                        }

                        is ApiResponse.Exception -> {
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
                    Log.w(TAG, "fetchSubscriptionContent exception for ${e.message}")
                }

            }
        }
        return proxyConfigList
    }

    suspend fun suspendConvertSubscription(converter: ConverterWithSources): File? {
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
                            clashConfig.proxy?.let {
                                proxyNodeList.addAll(it)
                            }
                        }
                        content =  ConverterUtil.serializeClashConfig(ClashConfig(proxy = proxyNodeList))
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
                val file = fileRepos.suspendSaveConverter(converter.converter.outputFileName!!, content)
               file

            } else {
                null
            }
        }
    }
}