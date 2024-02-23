package com.cloud.spider.compose

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cloud.spider.protocol.ClientType
import com.cloud.spider.repository.file.FileRepos
import com.cloud.spider.repository.http.HttpRepos
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 *
 * Created by cloud on 2024/2/21.
 */
@HiltViewModel
class ConvertViewModel @Inject constructor(val httpRepos: HttpRepos, val fileRepos: FileRepos):  ViewModel() {

    var converterName by mutableStateOf("")
        private set

    fun updateConverterName(input: String) {
        converterName = input
    }

    val subscriptionUrlList = mutableStateListOf<String>()

    var clientType by mutableStateOf(ClientType.Clash)
        private set

    fun updateClientType(input: String) {
        clientType = ClientType.valueOf(input)
    }
}
