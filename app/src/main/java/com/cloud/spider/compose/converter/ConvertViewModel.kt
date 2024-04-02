package com.cloud.spider.compose.converter

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloud.spider.protocol.ClientType
import com.cloud.spider.repository.db.DbRepos
import com.cloud.spider.repository.entity.SubscriptionSource
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/2/21.
 */
@HiltViewModel
class ConvertViewModel @Inject constructor(private val httpRepos: HttpRepos, private val dbRepos: DbRepos, private val fileRepos: FileRepos):  ViewModel() {

    var converterName by mutableStateOf("")
        private set

    fun updateConverterName(input: String) {
        converterName = input
    }

    val subscriptionSourceList = mutableStateListOf<SubscriptionSource>()

    var clientType by mutableStateOf(ClientType.Clash)
        private set

    fun updateClientType(input: String) {
        clientType = ClientType.valueOf(input)
    }

    fun testSubscription(url: String) {
        viewModelScope.launch {
            httpRepos.getSubscriptionContent(url)
        }
    }

    fun collectState(block: suspend (CoroutineScope) -> Unit) {
        viewModelScope.launch {
            block(this)
        }
    }
}
