package com.mealcycle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.mealcycle.app.data.datastore.UserPreferences
import com.mealcycle.app.data.model.User
import com.mealcycle.app.data.repository.UserRepository
import com.mealcycle.app.ui.history.HistoryScreen
import com.mealcycle.app.ui.home.HomeScreen
import com.mealcycle.app.ui.navigation.Screen
import com.mealcycle.app.ui.profile.ProfileScreen
import com.mealcycle.app.ui.splash.SplashScreen
import com.mealcycle.app.ui.stats.UsageStatsScreen
import com.mealcycle.app.ui.theme.MealCycleTheme
import com.mealcycle.app.ui.theme.engine.ThemeEngine
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var themeEngine: ThemeEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch { ensureDefaultUser() }

        // Restore theme source on launch
        lifecycleScope.launch {
            val source = userPreferences.themeSource.first()
            when (source) {
                "wallpaper" -> themeEngine.extractFromWallpaper()
                "custom" -> {
                    val uri = userPreferences.customImageUri.first()
                    if (uri.isNotBlank()) {
                        themeEngine.extractFromImage(android.net.Uri.parse(uri))
                    }
                }
            }
        }

        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(initial = "auto")
            MealCycleTheme(themeMode = themeMode, themeEngine = themeEngine) {
                // ── System UI bars adaptation ──
                AdaptiveSystemBars()
                AppNavHost()
            }
        }
    }

    private suspend fun ensureDefaultUser() {
        val activeId = userPreferences.activeUserId.first()
        val allUsers = userRepository.getAllUsers().first()

        val userExists = allUsers.any { it.userId == activeId }
        if (activeId.isNullOrBlank() || !userExists) {
            if (allUsers.isNotEmpty()) {
                userPreferences.setActiveUserId(allUsers.first().userId)
            } else {
                val defaultUser = User(
                    userId = UUID.randomUUID().toString(),
                    name = "Me",
                    photoUri = null
                )
                userRepository.insertUser(defaultUser)
                userPreferences.setActiveUserId(defaultUser.userId)
            }
        }
    }
}

// ─── System UI Bars ───────────────────────────────────────────────────────────

/**
 * Dynamically adapts status bar and navigation bar colors to the current theme.
 * Auto-detects whether to use dark or light icons based on background luminance.
 */
@Composable
private fun AdaptiveSystemBars() {
    val systemUiController = rememberSystemUiController()
    val bgColor = MaterialTheme.colorScheme.background
    val isDark = bgColor.luminance() < 0.5f

    SideEffect {
        systemUiController.setStatusBarColor(
            color = androidx.compose.ui.graphics.Color.Transparent,
            darkIcons = !isDark
        )
        systemUiController.setNavigationBarColor(
            color = bgColor,
            darkIcons = !isDark
        )
    }
}

// ─── Transition Specs ──────────────────────────────────────────────────────────
private const val NAV_ANIM_DURATION = 300

@Composable
private fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None }
    ) {

        // ── Splash screen (fade only, no slide) ──
        composable(
            Screen.Splash.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToStats   = { navController.navigate(Screen.Stats.route) }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack    = { navController.popBackStack() },
                onNavigateToHome  = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToStats   = { navController.navigate(Screen.Stats.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack    = { navController.popBackStack() },
                onNavigateToHome  = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToStats   = { navController.navigate(Screen.Stats.route) }
            )
        }

        composable(Screen.Stats.route) {
            UsageStatsScreen(
                onNavigateBack      = { navController.popBackStack() },
                onNavigateToHome    = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
    }
}
