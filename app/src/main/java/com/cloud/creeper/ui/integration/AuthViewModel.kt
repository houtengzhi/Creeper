package com.cloud.creeper.ui.integration

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.creeper.base.DataState
import com.cloud.creeper.repository.auth.AuthRepos
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.util.SERVICE_GITHUB
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/6/6.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepos: AuthRepos, private val dbRepos: DbRepos): ViewModel() {

    companion object {
        const val TAG = "AuthViewModel"
    }

    private val _getTokenState = MutableStateFlow<DataState<ServiceAuth>>(DataState.initial())
    val getTokenState = _getTokenState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _getTokenState.value)

    private val _saveTokenState = MutableStateFlow<DataState<ServiceAuth>>(DataState.initial())
    val saveTokenState = _saveTokenState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _saveTokenState.value)

    private val _deleteTokenState = MutableStateFlow<DataState<Boolean>>(DataState.initial())
    val deleteTokenState = _deleteTokenState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _deleteTokenState.value)

    init {
        getAuthInfo(SERVICE_GITHUB)
    }

    fun getAuthInfo(serviceName: String) {
        _getTokenState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "getAccessToken() exception for ${throwable.message}")
            _getTokenState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            val auth = dbRepos.suspendQueryServiceAuth(serviceName)
            _getTokenState.update {
                DataState(isLoading = false, data = auth, throwable = null)
            }
        }
    }

    fun saveAuthInfo(auth: ServiceAuth) {
        Log.d(TAG, "saveAuthInfo() ${auth}")
        _saveTokenState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "saveAuthInfo() exception for ${throwable.message}")
            _saveTokenState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            dbRepos.suspendInsertServiceAuth(auth)
            _saveTokenState.update {
                DataState(isLoading = false, data = auth, throwable = null)
            }
        }
    }

    fun deleteAccessToken(auth: ServiceAuth) {
        _deleteTokenState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "getAccessToken() exception for ${throwable.message}")
            _deleteTokenState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            dbRepos.suspendDeleteServiceAuth(auth)
            _deleteTokenState.update {
                DataState(isLoading = false, data = true, throwable = null)
            }
        }
    }
}