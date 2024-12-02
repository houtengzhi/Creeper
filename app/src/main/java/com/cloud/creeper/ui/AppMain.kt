package com.cloud.creeper.ui

import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.cloud.creeper.repository.entity.ConverterWithSources
import com.cloud.creeper.repository.entity.ServiceAuth
import com.cloud.creeper.repository.entity.SubscriptionSource
import com.cloud.creeper.ui.converter.ConverterDetailsPage
import com.cloud.creeper.ui.converter.EditConverterPage
import com.cloud.creeper.ui.converter.KEY_GIST_FILE
import com.cloud.creeper.ui.converter.KEY_REQUEST_CODE
import com.cloud.creeper.ui.gists.GistsPage
import com.cloud.creeper.ui.gists.GistsScreen
import com.cloud.creeper.ui.gists.GistsViewModel
import com.cloud.creeper.ui.integration.AuthorizePage
import com.cloud.creeper.ui.source.SubscriptionDetailsPage
import com.cloud.creeper.ui.source.SubscriptionViewModel
import com.cloud.creeper.util.KEY_CONVERTER
import com.cloud.creeper.util.KEY_SUBSCRIPTION_SOURCE
import com.cloud.creeper.util.clearForResult
import com.cloud.creeper.util.navigateForResult
import com.cloud.creeper.util.navigateWithArgs
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
                        navController.navigate(Screen.Authorize.route)
                    })
            }
            composable(Screen.ConverterManage.route) {
                ConverterManagePage(onUpClick = {
                    navController.navigateUp()
                }, onNewClick = {
                    navController.navigate(Screen.NewConverter.route)
                }, onEditClick = {
                    val args = Bundle()
                    args.putParcelable(KEY_CONVERTER, it)
                    navController.navigateWithArgs(Screen.EditConverterScreen.route, args, null, null)
                },
                    onDetailsClick = {
                        val args = Bundle()
                        args.putParcelable(KEY_CONVERTER, it)
                        navController.navigateWithArgs(Screen.ConverterDetailsScreen.route, args, null, null)
                    })
            }
            composable(Screen.NewConverter.route) { navBackStackEntry ->
                NewConverterPage(onUpClick = {
                    navController.navigateUp()
                }, onSubscriptionClick = {

                },
                    onCloudIntegrationClick = {
                        navController.navigate(Screen.Authorize.route)
                    },
                    navForResult = { coroutineScope, requestCode, onResult ->
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
                                   },
                    navToSelectGist = { coroutineScope, requestCode, auth, onResult ->
                        Log.d(TAG, "navToSelectGist(), requestCode=${requestCode}, auth=${auth}")
                        val args = Bundle()
                        args.putParcelable("serviceAuth", auth)
                        args.putString(KEY_REQUEST_CODE, requestCode)
                        val resultFlow = navController.navigateForResult(Screen.GistsScreen.createRoute(), args)
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
            composable(Screen.EditConverterScreen.route) {
                val converter = it.arguments?.getParcelable<ConverterWithSources>(KEY_CONVERTER)
                EditConverterPage(
                    converterWithSources = converter!!,
                    onUpClick = {
                        navController.navigateUp()
                    },
                    onCloudIntegrationClick = {
                        navController.navigate(Screen.Authorize.route)
                    },
                    navForResult = { coroutineScope, requestCode, onResult ->
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

                    },
                    navToSelectGist = { coroutineScope, requestCode, auth, onResult ->
                        Log.d(TAG, "navToSelectGist(), requestCode=${requestCode}, auth=${auth}")
                        val args = Bundle()
                        args.putParcelable("serviceAuth", auth)
                        args.putString(KEY_REQUEST_CODE, requestCode)
                        val resultFlow = navController.navigateForResult(Screen.GistsScreen.createRoute(), args)
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
                    }
                )
            }
            composable(Screen.ConverterDetailsScreen.route) {
                val converter = it.arguments?.getParcelable<ConverterWithSources>(KEY_CONVERTER)
                ConverterDetailsPage(
                    converterWithSources = converter!!,
                    onUpClick = {
                        navController.navigateUp()
                    }
                )
            }
            composable(route = Screen.SubscriptionManage.route, arguments = Screen.SubscriptionManage.navArguments ) {
                val requestCode = it.arguments?.getString("requestCode")
                SubscriptionManagePage(viewModel = hiltViewModel<SubscriptionViewModel, SubscriptionViewModel.SubscriptionViewModelFactory> { factory ->
                    factory.create(null)
                }, onUpClick = {
                    navController.navigateUp()
                }, onNewClick = {

                },
                    onResultSet = { subscriptionSource ->
                        if ("SELECT_SUBSCRIPTION" == requestCode) {
                            val data = Bundle()
                            data.putString(KEY_REQUEST_CODE, requestCode)
                            data.putParcelable("SUBSCRIPTION_SOURCE", subscriptionSource)
                            Log.d(TAG, "setResult(), data=${data}")
                            navController.setResult(data)
                            navController.popBackStack()
                        }
                    },
                    onDetailsClick = { subscriptionSource ->
                        val args = Bundle()
                        args.putParcelable(KEY_SUBSCRIPTION_SOURCE, subscriptionSource)
                        navController.navigateWithArgs(Screen.SubscriptionDetailsScreen.createRoute(), args)
                    })
            }
            composable(route = Screen.SubscriptionDetailsScreen.route) {
                val subscriptionSource = it.arguments?.getParcelable<SubscriptionSource>(
                    KEY_SUBSCRIPTION_SOURCE)
                SubscriptionDetailsPage(viewModel = hiltViewModel<SubscriptionViewModel, SubscriptionViewModel.SubscriptionViewModelFactory> { factory ->
                    factory.create(subscriptionSource)
                }, onUpClick = {
                    navController.navigateUp()
                })
            }

            composable(route = Screen.Authorize.route) {
                AuthorizePage(onUpClick = {
                    navController.navigateUp()
                }) {

                }
            }

            composable(route = Screen.GistsScreen.route) {
                val serviceAuth = it.arguments?.getParcelable<ServiceAuth>("serviceAuth")
                val requestCode = it.arguments?.getString(KEY_REQUEST_CODE)
                Log.d(TAG, "serviceAuth=${serviceAuth}, requestCode=${requestCode}")
                GistsPage(viewModel = hiltViewModel<GistsViewModel, GistsViewModel.GistViewModelFactory> { factory ->
                                                                                                         factory.create(serviceAuth)
                },
                    onUpClick = {
                    navController.navigateUp()
                },
                    onResultSet = { gistFile ->
                        val data = Bundle()
                        data.putString(KEY_REQUEST_CODE, requestCode)
                        data.putParcelable(KEY_GIST_FILE, gistFile)
                        Log.d(TAG, "setResult(), data=${data}")
                        navController.setResult(data)
                        navController.popBackStack()
                    })
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

    data object EditConverterScreen: Screen("EditConverterScreen")

    data object ConverterDetailsScreen: Screen("ConverterDetailsScreen")

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

    data object SubscriptionDetailsScreen: Screen(route = "SubscriptionDetailsScreen") {
        fun createRoute(): String = "SubscriptionDetailsScreen"
    }

    data object Authorize: Screen("Authorize")

    data object GistsScreen: Screen(route = "GistsScreen") {
        fun createRoute(): String = "GistsScreen"

        fun createRouteForResult(serviceAuth: ServiceAuth, requestCode: String): String {
            return "GistsScreen?requestCode=${requestCode}"
        }
    }

    data object AuthInfo: Screen("AuthInfo")

    data object Gallery: Screen(route = "Gallery/{sourceId}",
        navArguments = listOf(navArgument("sourceId") {type = NavType.IntType})
    ) {
        fun createRoute(sourceId: Int) = "Gallery/${sourceId}"
    }
}