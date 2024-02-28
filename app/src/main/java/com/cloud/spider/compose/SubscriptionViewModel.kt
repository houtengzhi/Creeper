package com.cloud.spider.compose

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/2/27.
 */
@HiltViewModel
class SubscriptionViewModel @Inject constructor(private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    private val _addState = MutableLiveData<DataState<Boolean>>()
    val addState get() = _addState

    private val _updateState = MutableLiveData<DataState<Boolean>>()
    val updateState get() = _updateState

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
}