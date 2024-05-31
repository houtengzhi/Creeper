package com.cloud.creeper.util

import android.os.Bundle
import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import kotlinx.coroutines.flow.StateFlow

/**
 *
 * Created by cloud on 2024/3/11.
 */

const val NavResultKey = "NavResultKey"

fun NavController.navigateForResult(route: String,
                                    navOptions: NavOptions? = null,
                                    navigatorExtras: Navigator.Extras? = null): StateFlow<Bundle>? {
    val stateHandle = this.currentBackStackEntry?.savedStateHandle
    navigate(route, navOptions, navigatorExtras)
    Log.d("NavController", "navigateForResult(), currentBackStackEntry=${this.currentBackStackEntry}, stateHandle=${stateHandle}")
    return stateHandle?.getStateFlow<Bundle>(NavResultKey, Bundle.EMPTY)
}

fun NavController.setResult(data: Bundle) {
    Log.d("NavController", "setResult(), previousBackStackEntry=${this.previousBackStackEntry}, stateHandle=${this.previousBackStackEntry?.savedStateHandle}")
    this.previousBackStackEntry?.let {
        it.savedStateHandle[NavResultKey] = data
    }
}

fun NavController.clearForResult() {
    Log.d("NavController", "clearForResult(), ${this.previousBackStackEntry}, ${this.previousBackStackEntry?.savedStateHandle}")
    this.previousBackStackEntry?.savedStateHandle?.let {
        it.remove<Any>(NavResultKey)
        it[NavResultKey] = Bundle.EMPTY
    }
}