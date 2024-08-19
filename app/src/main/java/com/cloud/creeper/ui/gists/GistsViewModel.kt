package com.cloud.creeper.ui.gists

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.creeper.base.DataState
import com.cloud.creeper.repository.Gist
import com.cloud.creeper.repository.db.DbRepos
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.repository.http.HttpRepos
import com.cloud.creeper.ui.integration.AuthViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
 * Created by cloud on 2024/7/18.
 */
@HiltViewModel(assistedFactory = GistsViewModel.GistViewModelFactory::class)
class GistsViewModel @AssistedInject constructor(@Assisted private val auth: ServiceAuth?, private val httpRepos: HttpRepos, private val dbRepos: DbRepos,): ViewModel() {

    companion object {
        const val TAG = "GistsViewModel"
    }


    private val _fetchGistsState = MutableStateFlow<DataState<List<Gist>>>(DataState.initial())
    val fetchGistsState = _fetchGistsState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), _fetchGistsState.value)

    init {
        Log.d(TAG, "Auth ${auth}")
        auth?.let {
            fetchGistList(it.accessToken)
        }
    }

    fun fetchGistList(accessToken: String) {
        Log.d(TAG, "fetchGistList()")
        _fetchGistsState.update {
            DataState(true, null, null)
        }
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "fetchGistList() exception for ${throwable.message}")
            _fetchGistsState.update {
                DataState(throwable)
            }
        }
        viewModelScope.launch(Dispatchers.Default + coroutineExceptionHandler) {
            val auth = httpRepos.suspendGetGistList(accessToken)
            Log.d(TAG, "getAuthInfo() auth=$auth")
            _fetchGistsState.update {
                DataState(isLoading = false, data = auth, throwable = null)
            }
        }
    }

    @AssistedFactory
    interface  GistViewModelFactory {
        fun create(serviceAuth: ServiceAuth?): GistsViewModel
    }
}