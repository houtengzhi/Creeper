package com.cloud.creeper.ui.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.creeper.base.DataState
import com.cloud.creeper.base.VMError
import com.cloud.creeper.protocol.core.ConverterUtil
import com.cloud.creeper.protocol.core.onError
import com.cloud.creeper.protocol.core.onSuccess
import com.cloud.creeper.protocol.core.suspendOnSuccess
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.SourceStatus
import com.cloud.creeper.repository.entity.SubscriptionDetails
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.repository.http.HttpRepos
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * Created by cloud on 2024/2/27.
 */
@HiltViewModel(assistedFactory = SubscriptionViewModel.SubscriptionViewModelFactory::class)
class SubscriptionViewModel @AssistedInject constructor(@Assisted private val subscriptionSource: SubscriptionSource? = null, private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    companion object {
        const val TAG = "SubscriptionViewModel"
    }

    private val _addState = MutableLiveData<DataState<Boolean>>()
    val addState: MutableLiveData<DataState<Boolean>> get() = _addState

    private val _editState = MutableLiveData<DataState<Boolean>>()
    val editState: LiveData<DataState<Boolean>> get() = _editState

    private val _deleteState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val deleteState = _deleteState.stateIn(viewModelScope, SharingStarted.Lazily, _deleteState.value)

    private val _pullState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val pullState = _pullState.stateIn(viewModelScope, SharingStarted.Lazily, _pullState.value)

    private val _subscriptionListState = MutableStateFlow(DataState<List<SubscriptionSource>>(false, null, null))
    val subscriptionListState = _subscriptionListState.stateIn(viewModelScope, SharingStarted.Eagerly, _subscriptionListState.value)

    private val _subscriptionDetailsState = MutableStateFlow<DataState<SubscriptionDetails>>(DataState.initial())
    val subscriptionDetailsState = _subscriptionDetailsState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _subscriptionDetailsState.value)

    init {
        subscriptionSource?.let {
            fetchSubscriptionDetails(it, false)
        }
    }

    val subscribeSubscriptionListState = dbRepos.subscribeSubscriptionSourceList()
            .flowOn(Dispatchers.IO)
            .map {
                Log.d(TAG, "subscribeSubscriptionSourceList map: subscription list size ${it.size}")
                DataState(it)
            }
            .onStart {
                Log.d(TAG, "subscribeSubscriptionSourceList() onStart")
                emit(DataState(true, null, null))
            }
            .onEach {
                Log.d(TAG, "subscribeSubscriptionSourceList() onEach")
            }
            .catch { throwable ->
                Log.e(TAG, "subscribeSubscriptionSourceList() throwable=${throwable.message}")
                emit(DataState(throwable))
            }
            .stateIn(viewModelScope, started = SharingStarted.Eagerly, initialValue = DataState.initial())

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

    fun editSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                Log.d(TAG, "editSubscriptionSource(), source=${source}")
                dbRepos.updateSubscriptionSource(source)
                emit(true)
            }.onStart {
                    _editState.value = DataState(true, null, null)
                }
                .catch {
                    _editState.value = DataState(it)
                }
                .collect {
                    _editState.value = DataState(isLoading = false, data = true, throwable = null)
                }
        }
    }

    fun deleteSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                Log.d(TAG, "deleteSubscriptionSource()")
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
                val list = dbRepos.suspendQuerySubscriptionSourceList()
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

    fun pullSubscription(source: SubscriptionSource) {
        viewModelScope.launch() {
            Log.d(TAG, "pullSubscription()")
            httpRepos.fetchUrl(source.sourceUrl)
                .flowOn(Dispatchers.IO)
                .onStart {
                    Log.d(TAG, "pullSubscription() onStart")
                    _pullState.update {
                        DataState(true, null, null)
                    }
                    source.pullStatus = SourceStatus.PENDING
                    source.updatedTime = System.currentTimeMillis()
                    dbRepos.updateSubscriptionSource(source)
                }
                .flowOn(Dispatchers.Main)
                .onEach {
                    Log.d(TAG, "pullSubscription() onEach")
                    it.onSuccess {
                        fileRepos.saveSubscriptionSource(source.getCacheFileName(), this.data)
                    }

                }
                .flowOn(Dispatchers.IO)
                .catch {throwable ->
                    Log.e(TAG, "pullSubscription() throwable=${throwable.message}")
                    _pullState.update {
                        DataState(throwable)
                    }
                    source.pullStatus = SourceStatus.FAILED
                    source.updatedTime = System.currentTimeMillis()
                    dbRepos.updateSubscriptionSource(source)
                }
                .collect {
                    Log.d(TAG, "pullSubscription() collect")
                    _pullState.update {
                        DataState(isLoading = false, data = true, throwable = null)
                    }
                    source.pullStatus = SourceStatus.UPDATED
                    source.pulledTime = System.currentTimeMillis()
                    source.updatedTime = System.currentTimeMillis()
                    dbRepos.updateSubscriptionSource(source)
                }

        }
    }

    fun fetchSubscriptionDetails(subscriptionSource: SubscriptionSource, forceRefresh: Boolean) {
        Log.d(TAG, "fetchSubscriptionDetails()")
        _subscriptionDetailsState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "fetchSubscriptionDetails() exception for ${throwable.message}")
            throwable.printStackTrace()
            _subscriptionDetailsState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {

            var content: String? = null
            var subscriptionDetails: SubscriptionDetails? = null

            val file = fileRepos.readSubscriptionSourceFile(subscriptionSource.getCacheFileName())
            if (forceRefresh || !file.exists()) {
                val apiResponse = httpRepos.suspendFetchUrl(subscriptionSource.sourceUrl)
                apiResponse.suspendOnSuccess {
                        fileRepos.saveSubscriptionSource(subscriptionSource.getCacheFileName(), this.data)

                        subscriptionSource.pullStatus = SourceStatus.UPDATED
                        subscriptionSource.pulledTime = System.currentTimeMillis()
                        subscriptionSource.updatedTime = System.currentTimeMillis()
                        dbRepos.updateSubscriptionSource(subscriptionSource)
                    }
                    .onSuccess {
                        content = this.data
                    }
                    .onError {
                        _subscriptionDetailsState.update {
                            DataState(this)
                        }
                    }

            } else {
                content = file.readText()
            }

            content?.let {
                val clashConfig = ConverterUtil.parseToClashConfig(subscriptionSource.type, it)
                Log.d(TAG, "fetchSubscriptionDetails(), proxies size=${clashConfig.proxies?.size}")
                if (!clashConfig.proxies.isNullOrEmpty()) {
                    subscriptionDetails = SubscriptionDetails(subscriptionSource, clashConfig.proxies)
                }
            }

            withContext(Dispatchers.Main) {
                Log.d(TAG, "fetchSubscriptionDetails() subscriptionDetails=${subscriptionDetails}")
                if (subscriptionDetails != null) {
                    _subscriptionDetailsState.update {
                        DataState(
                            false,
                            subscriptionDetails,
                            null
                        )
                    }

                } else {
                    _subscriptionDetailsState.update {
                        DataState(VMError.EmptyProxyList)
                    }
                }
            }
        }
    }

    @AssistedFactory
    interface SubscriptionViewModelFactory {
        fun create(subscriptionSource: SubscriptionSource?) : SubscriptionViewModel
    }

}