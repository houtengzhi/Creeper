package com.cloud.spider.util

import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.flow.StateFlow

/**
 *
 * Created by cloud on 2024/3/11.
 */

const val NavResultKey = ""

fun NavController.navigateForResult(route: String,
                                    navOptions: NavOptions? = null,
                                    navigatorExtras: Navigator.Extras? = null): StateFlow<Bundle?>? {
    val stateHandle = this.currentBackStackEntry?.savedStateHandle
    navigate(route, navOptions, navigatorExtras)
    return stateHandle?.getStateFlow<Bundle>(NavResultKey, Bundle.EMPTY)
}

fun NavController.setResult(data: Bundle) {
    this.previousBackStackEntry?.let {
        it.savedStateHandle[NavResultKey] = data
    }
}