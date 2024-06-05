package com.cloud.creeper.ui

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cloud.creeper.ui.converter.ConverterManagePage
import com.cloud.creeper.ui.converter.NewConverterPage
import com.cloud.creeper.ui.home.HomePage
import com.cloud.creeper.ui.source.SubscriptionManagePage
import com.cloud.creeper.compose.AppTheme
import com.cloud.creeper.ui.integration.AuthorizationPage
import com.cloud.creeper.util.clearForResult
import com.cloud.creeper.util.navigateForResult
import com.cloud.creeper.util.setResult
import kotlinx.coroutines.launch

/**
 *
 * Created by cloud on 2024/1/26.
 */

private const val TAG = "AppMain"
@Composable
fun AppMain() {

    AppTheme {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomePage(onConvertClick = {
                    navController.navigate(Screen.ConverterManage.route)
                },
                    onSubscriptionSourceManageClick = {
                        navController.navigate(Screen.SubscriptionManage.createRoute())
                    },
                    onCloudIntegrationClick = {
                        navController.navigate(Screen.Authorization.route)
                    })
            }
            composable(Screen.ConverterManage.route) {
                ConverterManagePage(onUpClick = {
                    navController.navigateUp()
                }, onNewClick = {
                    navController.navigate(Screen.NewConverter.route)
                })
            }
            composable(Screen.NewConverter.route) {
                NewConverterPage(onUpClick = {
                    navController.navigateUp()
                }, onSubscriptionClick = {

                }, navForResult = { coroutineScope, requestCode, onResult ->
                    val resultFlow = navController.navigateForResult(Screen.SubscriptionManage.createRouteForResult(requestCode))
                    Log.d(TAG, "navigateForResult(), initialValue=${resultFlow?.value}")
                    coroutineScope.launch {
                        resultFlow?.collect { data ->
                            Log.d(TAG, "collect(), data=${data}")
                            if (!data.isEmpty) {
                                onResult(data)
                                Log.d(TAG, "clearForResult()")
                                navController.clearForResult()
                            }
                        }
                    }
                })
            }
            composable(route = Screen.SubscriptionManage.route, arguments = Screen.SubscriptionManage.navArguments ) {
                val requestCode = it.arguments?.getString("requestCode")
                SubscriptionManagePage(onUpClick = {
                    navController.navigateUp()
                }, onNewClick = {

                },
                    onResultSet = { subscriptionSource ->
                        if ("SELECT_SUBSCRIPTION" == requestCode) {
                            val data = Bundle()
                            data.putString("REQUEST_CODE", requestCode)
                            data.putParcelable("SUBSCRIPTION_SOURCE", subscriptionSource)
                            Log.d(TAG, "setResult(), data=${data}")
                            navController.setResult(data)
                            navController.popBackStack()
                        }
                    })
            }

            composable(route = Screen.Authorization.route) {
                AuthorizationPage {
                    navController.navigateUp()
                }
            }

            composable(route = Screen.Gallery.route,
                arguments = Screen.Gallery.navArguments
            ) {
                val sourceId = it.arguments!!.getInt("sourceId")
            }
        }
    }
}

sealed class Screen(val route: String, val navArguments: List<NamedNavArgument> = emptyList()) {
    data object Home: Screen("Home")

    data object ConverterManage: Screen("ConverterManage")

    data object NewConverter: Screen("NewConverter")

    data object SubscriptionManage: Screen(route = "SubscriptionManage?requestCode={requestCode}",
        navArguments = listOf(navArgument("requestCode") {
            type = NavType.StringType
            nullable = true}
        )
    ) {

        fun createRoute(): String = "SubscriptionManage"

        fun createRouteForResult(requestCode: String): String {
            return "SubscriptionManage?requestCode=${requestCode}"
        }
    }

    data object Authorization: Screen("Authorization")

    data object Gallery: Screen(route = "Gallery/{sourceId}",
        navArguments = listOf(navArgument("sourceId") {type = NavType.IntType})
    ) {
        fun createRoute(sourceId: Int) = "Gallery/${sourceId}"
    }
}