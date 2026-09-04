package com.mealcycle.app.ui.navigation

/**
 * All navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object Splash  : Screen("splash")
    object Home    : Screen("home")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Stats   : Screen("stats")
}
