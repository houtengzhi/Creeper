package com.cloud.spider.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cloud.spider.compose.home.HomePage
import com.cloud.spider.ui.AppTheme

/**
 *
 * Created by cloud on 2024/1/26.
 */

@Composable
fun AppMain() {

    AppTheme {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route) {
                HomePage()
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
    data object Gallery: Screen(route = "Gallery/{sourceId}",
        navArguments = listOf(navArgument("sourceId") {type = NavType.IntType})
    ) {
        fun createRoute(sourceId: Int) = "Gallery/${sourceId}"
    }
}