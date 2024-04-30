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
import com.cloud.spider.protocol.ClientType
import com.cloud.spider.protocol.ProxyConfig
import com.cloud.spider.protocol.core.ApiResponse
import com.cloud.spider.protocol.core.ConverterUtil
import com.cloud.spider.repository.DataRepos
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.entity.ConverterWithSources
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
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
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/2/21.
 */
@HiltViewModel
class ConvertViewModel @Inject constructor(private val dataRepos: DataRepos, private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

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

    private val _editState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val editState = _editState.stateIn(viewModelScope, SharingStarted.Lazily, _editState.value)

    private val _deleteState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val deleteState = _deleteState.stateIn(viewModelScope, SharingStarted.Lazily, _deleteState.value)

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
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {

            val file = dataRepos.suspendConvertSubscription(converter)
            if (file != null) {
                dbRepos.suspendInsertConverter(converter)
                _addState.update {
                    DataState(isLoading = false, data = true, throwable = null)
                }

            } else {
                _addState.update {
                    DataState(VMError.EmptyProxyList)
                }
            }
        }
    }

    fun editConverter(converter: ConverterWithSources) {
        _editState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "editConverter exception for ${throwable.message}")
            _editState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            val file = dataRepos.suspendConvertSubscription(converter)
            if (file != null) {
                dbRepos.suspendUpdateConverter(converter)
                _editState.update {
                    DataState(isLoading = false, data = true, throwable = null)
                }

            } else {
                _editState.update {
                    DataState(VMError.EmptyProxyList)
                }
            }
        }
    }

    fun deleteConverter(converter: ConverterWithSources) {
        _deleteState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "deleteConverter exception for ${throwable.message}")
            _deleteState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Main + coroutineExceptionHandler) {
            dbRepos.suspendDeleteConverter(converter)
            _deleteState.update {
                DataState(isLoading = false, data = true, throwable = null)
            }
        }
    }

    fun updateConverter(converter: ConverterWithSources) {


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

                dbRepos.suspendInsertConverter(converter)
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
