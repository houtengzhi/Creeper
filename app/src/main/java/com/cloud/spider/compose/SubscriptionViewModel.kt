package com.cloud.spider.compose

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.spider.base.DataState
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/2/27.
 */
@HiltViewModel
class SubscriptionViewModel @Inject constructor(private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    companion object {
        const val TAG = "SubscriptionViewModel"
    }

    private val _addState = MutableLiveData<DataState<Boolean>>()
    val addState: MutableLiveData<DataState<Boolean>> get() = _addState

    private val _updateState = MutableLiveData<DataState<Boolean>>()
    val updateState: LiveData<DataState<Boolean>> get() = _updateState

    private val _deleteState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val deleteState = _deleteState.stateIn(viewModelScope, SharingStarted.Lazily, _deleteState.value)

    private val _subscriptionListState = MutableStateFlow(DataState<List<SubscriptionSource>>(false, null, null))
    val subscriptionListState = _subscriptionListState.stateIn(viewModelScope, SharingStarted.Eagerly, _subscriptionListState.value)

    init {

    }

    fun addSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                Log.d(TAG, "addSubscriptionSource()")
                dbRepos.insertSubscriptionSource(source)
                emit(true)
            }
                .onStart {
                    Log.d(TAG, "addSubscriptionSource onStart")
                    _addState.value = DataState(true, null, null)
                }
                .catch {
                    Log.e(TAG, "addSubscriptionSource throwable=${it.message}")
                    _addState.value = DataState(it)
                }
                .collect {
                    Log.d(TAG, "addSubscriptionSource collect")
                    _addState.value = DataState(isLoading = false, data = true, throwable = null)
                }
        }
    }

    fun updateSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                dbRepos.updateSubscriptionSource(source)
                emit(true)
            }.onStart {
                    _updateState.value = DataState(true, null, null)
                }
                .catch {
                    _updateState.value = DataState(it)
                }
                .collect {
                    _updateState.value = DataState(isLoading = false, data = true, throwable = null)
                }
        }
    }

    fun deleteSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                dbRepos.deleteSubscriptionSource(source)
                emit(true)
            }.onStart {
                _deleteState.update {
                    DataState(true, null, null)
                }
            }
                .catch {throwable ->
                    _deleteState.update {
                        DataState(throwable)
                    }
                }
                .collect {
                    _deleteState.update {
                        DataState(isLoading = false, data = true, throwable = null)
                    }
                }
        }
    }

    fun getSubscriptionSourceList() =
        viewModelScope.launch {
            flow {
                val list = dbRepos.querySubscriptionSourceList()
                emit(list)
            }
                .onStart {
                    _subscriptionListState.update {
                        DataState(true, null, null)
                    }
                }
                .catch { throwable ->
                    _subscriptionListState.update {
                        DataState(throwable)
                    }
                }
                .collect {
                    _subscriptionListState.update {
                        DataState(false, null, null)
                    }
                }
        }

    fun subscribeSubscriptionSourceList() = dbRepos.subscribeSubscriptionSourceList()
        .flowOn(Dispatchers.IO)
        .map {
            Log.d(TAG, "subscribeSubscriptionSourceList map: subscription list size ${it.size}")
            DataState(it)
        }
        .onStart {
            Log.d(TAG, "subscribeSubscriptionSourceList onStart")
            emit(DataState(true, null, null))
        }
        .catch { throwable ->
            Log.e(TAG, "subscribeSubscriptionSourceList throwable=${throwable.message}")
            emit(DataState(throwable))
        }
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = DataState.initial())

}