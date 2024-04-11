package com.cloud.spider.ui.converter

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.spider.base.DataState
import com.cloud.spider.protocol.ClientType
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.entity.ConverterWithSources
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
import com.cloud.spider.ui.source.SubscriptionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
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
        viewModelScope.launch {
            flow {
                Log.d(TAG, "addConverter()")
                dbRepos.insertConverter(converter)
                emit(true)
            }.onStart {
                _addState.update {
                    DataState(true, null, null)
                }
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
