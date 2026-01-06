package fr.helios.dcdl.navigation

sealed class AppRoutes(val route: String) {
    data object Home: AppRoutes("home")

    data object Dashboard: AppRoutes("dashboard/{$GAME_ID_ARG}") {
        fun createRoute(gameId: String): String {
            return "dashboard/$gameId"
        }
    }

    data object Game: AppRoutes("game/{$GAME_ID_ARG}/player/{$USERNAME_ARG}") {
        fun createRoute(gameId: String, username: String): String {
            return "game/$gameId/player/$username"
        }
    }

    companion object {
        const val GAME_ID_ARG = "gameId"
        const val USERNAME_ARG = "username"
    }
}