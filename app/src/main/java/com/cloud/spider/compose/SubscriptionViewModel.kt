package com.cloud.spider.compose

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/2/27.
 */
@HiltViewModel
class SubscriptionViewModel @Inject constructor(private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    private val _addState = MutableLiveData<DataState<Boolean>>()
    val addState: MutableLiveData<DataState<Boolean>> get() = _addState

    private val _updateState = MutableLiveData<DataState<Boolean>>()
    val updateState: LiveData<DataState<Boolean>> get() = _updateState

    private val _subscriptionListState = MutableStateFlow(DataState<List<SubscriptionSource>>(true, null, null))
    val subscriptionListState = _subscriptionListState.stateIn(viewModelScope, SharingStarted.Eagerly, _subscriptionListState.value)

    fun addSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                dbRepos.insertSubscriptionSource(source)
                emit(true)
            }.flowOn(Dispatchers.IO)
                .onStart {
                    _addState.value = DataState(true, null, null)
                }
                .catch {
                    _addState.value = DataState(it)
                }
                .collect {
                    _addState.value = DataState(isLoading = false, data = true, throwable = null)
                }
        }
    }

    fun updateSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                dbRepos.updateSubscriptionSource(source)
                emit(true)
            }.flowOn(Dispatchers.IO)
                .onStart {
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

    fun getSubscriptionSourceList() =
        viewModelScope.launch {
            flow {
                val list = dbRepos.querySubscriptionSourceList()
                emit(list)
            }.flowOn(Dispatchers.IO)
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

}