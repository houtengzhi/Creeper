package com.cloud.creeper.ui.converter

import android.util.Log
import androidx.compose.runtime.getValue
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

    var converterDescription by mutableStateOf("")
        private set

    fun updateConverterDescription(input: String) {
        converterDescription = input
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

    val supportedCloudRepositories = mutableStateListOf<String>()

    private val _addState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val addState = _addState.stateIn(viewModelScope, SharingStarted.Lazily, _addState.value)

    private val _editState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val editState = _editState.stateIn(viewModelScope, SharingStarted.Lazily, _editState.value)

    private val _deleteState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val deleteState = _deleteState.stateIn(viewModelScope, SharingStarted.Lazily, _deleteState.value)

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
            httpRepos.fetchUrl(url)
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
                Log.d(TAG, "addConverter() file=${file.path}")
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
