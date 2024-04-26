package com.cloud.spider.ui.converter

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.spider.base.DataState
import com.cloud.spider.base.VMError
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/2/21.
 */
@HiltViewModel
class ConvertViewModel @Inject constructor(private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    companion object {
        const val TAG = "ConvertViewModel"
    }
    var converterName by mutableStateOf("")
        private set

    fun updateConverterName(input: String) {
        converterName = input
    }

    val subscriptionSourceList = mutableStateListOf<SubscriptionSource>()

    val canSaveConverter get() = converterName.isNotEmpty() && subscriptionSourceList.isNotEmpty()

    var clientType by mutableStateOf(ClientType.Clash)
        private set

    fun updateClientType(input: String) {
        clientType = ClientType.valueOf(input)
    }

    private val _addState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val addState = _addState.stateIn(viewModelScope, SharingStarted.Lazily, _addState.value)

    fun testSubscription(url: String) {
        viewModelScope.launch {
            httpRepos.fetchSubscriptionContent(url)
        }
    }

    fun addConverter(converter: ConverterWithSources) {
        _addState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "addConverter() exception for ${throwable.message}")
            _addState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Main + coroutineExceptionHandler) {
           val deferredList = mutableListOf<Deferred<ProxyConfig?>>()
            val proxyConfigList = mutableListOf<ProxyConfig>()

            supervisorScope {
                converter.subscriptionSourceList.forEach { source ->
                    val deferred = async {
                        val apiResponse = httpRepos.suspendFetchSubscriptionContent(source.sourceUrl)
                        when (apiResponse) {
                            is ApiResponse.Success<String> -> {
                                when (source.type) {
                                    ClientType.Clash.text -> {
                                        withContext(Dispatchers.Default) {
                                            val clashConfig = ConverterUtil.deserializeClashConfig(apiResponse.data)
                                            clashConfig
                                        }
                                    }

                                    ClientType.V2Ray.text -> {
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
                            when (converter.converter.outputType) {
                                ClientType.Clash.text -> {
                                    val clashConfig = it.toClashConfig()
                                    proxyConfigList.add(clashConfig)

                                }
                                ClientType.V2Ray.text -> {
                                    val v2RayConfig = it.toV2RayConfig()
                                    proxyConfigList.add(v2RayConfig)
                                }
                                else -> {
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.w(TAG, "fetchSubscriptionContent exception for ${e.message}")
                    }

                }
            }

            if (proxyConfigList.isEmpty()) {
                _addState.update {
                    DataState(VMError.EmptyProxyList)
                }
            } else {
                val content: String
                when (converter.converter.outputType) {
                    ClientType.Clash.text -> {
                        val proxyNodeList = mutableListOf<ClashProxyNode>()
                        proxyConfigList.forEach { config ->
                            val clashConfig = config.toClashConfig()
                            clashConfig.proxy?.let {
                                proxyNodeList.addAll(it)
                            }
                        }
                        content =  ConverterUtil.serializeClashConfig(ClashConfig(proxy = proxyNodeList))
                        converter.converter.outputFileName = "${converter.converter.id}.yaml"

                    }
                    ClientType.V2Ray.text -> {
                        content = ""
                    }
                    else -> {
                        content = ""
                    }
                }
                fileRepos.suspendSaveConverter(converter.converter.outputFileName!!, content)
                dbRepos.insertConverter(converter)
                _addState.update {
                    DataState(isLoading = false, data = true, throwable = null)
                }
            }
        }
    }

    fun _addConverter(converter: ConverterWithSources) {
        viewModelScope.launch {
            val flows = mutableListOf<Flow<ProxyConfig?>>()
            converter.subscriptionSourceList.forEach { source ->
                val flow = httpRepos.fetchSubscriptionContent(source.sourceUrl)
                    .map {
                        when (it) {
                            is ApiResponse.Success<String> -> {
                                when (source.type) {
                                    ClientType.Clash.text -> {
                                        val clashConfig = ConverterUtil.deserializeClashConfig(it.data)
                                        clashConfig
                                    }

                                    ClientType.V2Ray.text -> {
                                        val v2RayConfig = ConverterUtil.readV2RaySubscription(it.data)
                                        v2RayConfig
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
                    .flowOn(Dispatchers.IO)
                flows.add(flow)
            }

            combine(*flows.toTypedArray()) {
                it.toList().filterNotNull()
            }
                .map {
                    it.forEach { proxyConfig ->
                        if (converter.converter.outputType == ClientType.Clash.text) {
                            val clashConfig = proxyConfig.toClashConfig()

                        } else if (converter.converter.outputType == ClientType.V2Ray.text) {
                            val v2RayConfig = proxyConfig.toV2RayConfig()
                        }

                    }


                }
                .onStart {  }
                .catch {  }
                .collect {

                }


            flow {
                Log.d(TAG, "addConverter()")

                dbRepos.insertConverter(converter)
                emit(true)
            }.onStart {
                _addState.update {
                    DataState(true, null, null)
                }
            }
                .map {

                }
                .catch {throwable ->
                    _addState.update {
                        DataState(throwable)
                    }
                }
                .collect {
                    _addState.update {
                        DataState(isLoading = false, data = true, throwable = null)
                    }
                }
        }
    }

    fun subscribeConverterList() = dbRepos.subscribeConverterList()
        .flowOn(Dispatchers.IO)
        .map {
            DataState(it)
        }
        .onStart {
            emit(DataState(true, null, null))
        }
        .catch {
            emit(DataState(it))
        }
        .stateIn(viewModelScope, started = SharingStarted.Lazily, initialValue = DataState.initial())

}
