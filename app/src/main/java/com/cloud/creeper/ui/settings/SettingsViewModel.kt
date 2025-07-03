package com.cloud.creeper.ui.settings

import androidx.lifecycle.ViewModel
import com.cloud.creeper.server.ServerManage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(): ViewModel() {

    fun getServerPort() = ServerManage.getPort()

    fun saveServerPort(port: Int) {
        ServerManage.savePort(port)
    }
}