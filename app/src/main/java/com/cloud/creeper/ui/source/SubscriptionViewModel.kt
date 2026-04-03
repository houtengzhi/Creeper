package com.cloud.creeper.ui.source

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.creeper.base.DataState
import com.cloud.creeper.protocol.core.onError
import com.cloud.creeper.protocol.core.onException
import com.cloud.creeper.protocol.core.onSuccess
import com.cloud.creeper.repository.DataRepos
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.SubscriptionDetails
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.repository.http.HttpRepos
import com.cloud.creeper.support.livedata.UnPeekLiveData
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

/**
 *
 * Created by cloud on 2024/2/27.
 */
@HiltViewModel(assistedFactory = SubscriptionViewModel.SubscriptionViewModelFactory::class)
class SubscriptionViewModel @AssistedInject constructor(@Assisted private val subscriptionSource: SubscriptionSource? = null, private val dataRepos: DataRepos, private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    companion object {
        const val TAG = "SubscriptionViewModel"
    }

    private val _addState = UnPeekLiveData<DataState<Boolean>>()
    val addState: UnPeekLiveData<DataState<Boolean>> get() = _addState

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
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "addSubscriptionSource() exception for ${throwable.message}")
            throwable.printStackTrace()
            _addState.value = DataState(throwable)
        }
        viewModelScope.launch(coroutineExceptionHandler) {
            flow {
                Log.d(TAG, "addSubscriptionSource()")
                val apiResponse = dataRepos.suspendAddSubscriptionSource(source)
                emit(apiResponse)
            }
                .onStart {
                    Log.d(TAG, "addSubscriptionSource onStart")
                    _addState.value = DataState(true, null, null)
                }
                .catch {
                    Log.e(TAG, "addSubscriptionSource throwable=${it.message}")
                    it.printStackTrace()
                    _addState.value = DataState(it)
                }
                .collect {
                    Log.d(TAG, "addSubscriptionSource collect")
                    it.onSuccess {
                        _addState.value = DataState(isLoading = false, data = true, throwable = null)
                    }.onError {
                        _addState.value = DataState(this)
                    }.onException {
                        _addState.value = DataState(this.throwable)
                    }

                }
        }
    }

    fun resetAddState() {
        _addState.value = DataState.initial()
    }

    fun editSubscriptionSource(source: SubscriptionSource) {
        viewModelScope.launch {
            flow {
                Log.d(TAG, "editSubscriptionSource(), source=${source}")
                dataRepos.suspendUpdateSubscriptionSource(source)
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
                dataRepos.suspendDeleteSubscriptionSource(source)
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

    fun fetchSubscriptionDetails(subscriptionSource: SubscriptionSource, forceRefresh: Boolean) {
        Log.d(TAG, "fetchSubscriptionDetails()")
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "fetchSubscriptionDetails() exception for ${throwable.message}")
            throwable.printStackTrace()
            _subscriptionDetailsState.update {
                DataState(throwable)
            }
        }

        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            flow {
                Log.d(TAG, "fetchSubscriptionDetails()")
                val apiResponse = dataRepos.suspendGetSubscriptionDetails(subscriptionSource, forceRefresh)
                emit(apiResponse)
            }
                .onStart {
                    Log.d(TAG, "fetchSubscriptionDetails onStart")
                    _subscriptionDetailsState.update {
                        DataState(true, null, null)
                    }
                }
                .catch { throwable ->
                    Log.e(TAG, "fetchSubscriptionDetails throwable=${throwable.message}")
                    _subscriptionDetailsState.update {
                        DataState(throwable = throwable)
                    }
                }
                .collect {
                    it.onSuccess {
                        Log.d(TAG, "fetchSubscriptionDetails onSuccess")
                        _subscriptionDetailsState.update {
                            DataState(
                                false,
                                this.data,
                                null
                            )
                        }
                    }.onError {
                        Log.e(TAG, "fetchSubscriptionDetails onError: $this")
                        _subscriptionDetailsState.update {
                            DataState(this)
                        }
                    }.onException {
                        Log.e(TAG, "fetchSubscriptionDetails onException: $this")
                        _subscriptionDetailsState.update {
                            DataState(this.throwable)
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