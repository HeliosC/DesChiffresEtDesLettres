package fr.helios.dcdl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.helios.dcdl.dashboard.DashboardScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import fr.helios.dcdl.game.GameScreen
import fr.helios.dcdl.home.HomeScreen
import fr.helios.dcdl.navigation.AppRoutes

@Composable
@Preview
fun App(
    onNavHostReady: suspend (NavHostController) -> Unit = {}
) {
    val navController = rememberNavController()

    MaterialTheme {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(
                route = AppRoutes.Home.route
            ) {
                HomeScreen(
                    navigateToDashboard = { gameId ->
                        navController.navigate(
                            AppRoutes.Dashboard.createRoute(gameId)
                        )
                    },
                    navigateToGame = { gameId, username ->
                        navController.navigate(
                            AppRoutes.Game.createRoute(gameId = gameId, username = username)
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            composable(
                route = AppRoutes.Dashboard.route,
                arguments = listOf(navArgument(AppRoutes.GAME_ID_ARG) { type = NavType.StringType })
            ) { navBackStackEntry ->
                val gameId: String = navBackStackEntry.savedStateHandle[AppRoutes.GAME_ID_ARG] ?: return@composable
                DashboardScreen(gameId)
            }

            composable(
                route = AppRoutes.Game.route,
                arguments = listOf(
                    navArgument(AppRoutes.GAME_ID_ARG) { type = NavType.StringType },
                    navArgument(AppRoutes.USERNAME_ARG) { type = NavType.StringType }
                )
            ) { navBackStackEntry ->
                val gameId: String = navBackStackEntry.savedStateHandle[AppRoutes.GAME_ID_ARG] ?: return@composable
                val username: String = navBackStackEntry.savedStateHandle[AppRoutes.USERNAME_ARG] ?: return@composable
                GameScreen(gameId = gameId, username = username)
            }
        }
   }

    //https://kotlinlang.org/docs/multiplatform/compose-navigation-routing.html
    LaunchedEffect(navController) {
        println("LAUNCH INVOKE BACK STACK")

        onNavHostReady.invoke(navController)
    }
}