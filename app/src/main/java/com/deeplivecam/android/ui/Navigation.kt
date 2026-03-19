package com.deeplivecam.android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Navigation graph for the app
 */
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Settings : Screen("settings")
    object Gallery : Screen("gallery")
}

@Composable
fun DeepLiveCamNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Main.route
    ) {
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToGallery = {
                    navController.navigate(Screen.Gallery.route)
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Gallery.route) {
            GalleryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
