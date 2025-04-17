package com.cloud.creeper.ui.converter

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.creeper.base.DataState
import com.cloud.creeper.base.VMError
import com.cloud.creeper.protocol.ClientType
import com.cloud.creeper.repository.DataRepos
import com.cloud.creeper.repository.GistFile
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.repository.file.FileRepos
import com.cloud.creeper.repository.http.HttpRepos
import com.cloud.creeper.util.RepositoryType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
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
@HiltViewModel(assistedFactory = ConvertViewModel.ConvertViewModelFactory::class)
class ConvertViewModel @AssistedInject constructor(@Assisted private val initialConverter: ConverterWithSources?, private val dataRepos: DataRepos, private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    companion object {
        const val TAG = "ConvertViewModel"
    }

    fun getInitialConverter() = initialConverter

    var converterName by mutableStateOf("")
        private set

    fun updateConverterName(input: String) {
        converterName = input
    }

    var converterDescription by mutableStateOf("")
        private set

    fun updateConverterDescription(input: String) {
        converterDescription = input
    }

    var converterExclude by mutableStateOf("")
        private set

    fun updateConverterExclude(input: String) {
        converterExclude = input
    }

    val subscriptionSourceList = mutableStateListOf<SubscriptionSource>()

    val canSaveConverter get() = converterName.isNotEmpty() && subscriptionSourceList.isNotEmpty()

    var outputType by mutableStateOf(ClientType.Clash)
        private set

    fun updateClientType(input: ClientType) {
        outputType = input
    }

    var gistFile: GistFile? by mutableStateOf(null)
        private set

    fun updateGistFile(gistFile: GistFile?) {
        this.gistFile = gistFile
    }

    var dataChangedStatus by mutableIntStateOf(0)
        private set

    fun updateDataChangedStatus(status: Int) {
        dataChangedStatus = status
    }

    val supportedCloudRepositories = mutableStateListOf<String>()

    var gistsOn by mutableStateOf(false)
        private set

    fun updateGistsOn(on: Boolean) {
        gistsOn = on
    }

    var gistId: String? by mutableStateOf("")
        private set

    fun updateGistId(id: String?) {
        gistId = id
    }

    var gistFileName: String? by mutableStateOf("")
        private set

    fun updateGistFileName(name: String?) {
        gistFileName = name
    }

    private val _addState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val addState = _addState.stateIn(viewModelScope, SharingStarted.Lazily, _addState.value)

    private val _updateState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val updateState = _updateState.stateIn(viewModelScope, SharingStarted.Lazily, _updateState.value)

    private val _deleteState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val deleteState = _deleteState.stateIn(viewModelScope, SharingStarted.Lazily, _deleteState.value)

    init {
        initialConverter?.let {
            subscriptionSourceList.addAll(it.subscriptionSourceList)
            updateConverterName(it.converter.name)
            it.converter.exclude?.let { it1 ->  updateConverterExclude(it1)}
            it.converter.description?.let { it1 -> updateConverterDescription(it1) }
            updateClientType(it.converter.outputType)

            var s = it.cloudRepositoryList?.map { cloudRepository ->
                cloudRepository.type
            }?.toTypedArray()
            if (s == null) {
                s = arrayOf()
            }
            supportedCloudRepositories.addAll(s)

            it.cloudRepositoryList?.forEach {
                    cloudRepository ->
                if (RepositoryType.REPOSITORY_GITHUB == cloudRepository.type) {
                    updateGistsOn(true)
                    updateGistId(cloudRepository.gistId)
                    updateGistFileName(cloudRepository.gistFileName)
                }
            }
        }
    }

    val subscribeConverterListState = dbRepos.subscribeConverterList()
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

    fun testSubscription(url: String) {
        viewModelScope.launch {
            httpRepos.fetchUrlFlow(url)
        }
    }

    fun addConverter(converter: ConverterWithSources) {
        Log.d(TAG, "addConverter()")
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

            val newConverter = dataRepos.suspendConvertSubscription(converter)
            if (newConverter != null) {
                dbRepos.suspendInsertConverter(newConverter)
                Log.d(TAG, "addConverter() success")
                _addState.update {
                    DataState(isLoading = false, data = true, throwable = null)
                }

            } else {
                Log.e(TAG, "addConverter() proxy list is empty")
                _addState.update {
                    DataState(VMError.EmptyProxyList)
                }
            }
        }
    }

    fun updateConverter(converter: ConverterWithSources) {
        Log.d(TAG, "updateConverter(), $converter")
        _updateState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "updateConverter exception for ${throwable.message}")
            _updateState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            val newConverter = dataRepos.suspendConvertSubscription(converter)
            if (newConverter != null) {
                converter.converter.updatedTime = System.currentTimeMillis()
                dbRepos.suspendUpdateConverter(converter)
                _updateState.update {
                    DataState(isLoading = false, data = true, throwable = null)
                }

            } else {
                _updateState.update {
                    DataState(VMError.EmptyProxyList)
                }
            }
        }
    }

    fun deleteConverter(converter: ConverterWithSources, deleteRemoteRepos: Boolean) {
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
            dataRepos.suspendDeleteConverter(converter, deleteRemoteRepos)
            _deleteState.update {
                DataState(isLoading = false, data = true, throwable = null)
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

    @AssistedFactory
    interface ConvertViewModelFactory {
        fun create(converter: ConverterWithSources?) : ConvertViewModel
    }

}
