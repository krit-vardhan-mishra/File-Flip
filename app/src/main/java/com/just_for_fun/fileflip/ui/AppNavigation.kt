package com.just_for_fun.fileflip.ui

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.just_for_fun.fileflip.ui.screens.AboutScreen
import com.just_for_fun.fileflip.ui.screens.DashboardScreen
import com.just_for_fun.fileflip.ui.screens.DraftsPublishedScreen
import com.just_for_fun.fileflip.ui.screens.EditorScreen
import com.just_for_fun.fileflip.ui.screens.FileExplorerScreen
import com.just_for_fun.fileflip.ui.screens.OnboardingScreen
import com.just_for_fun.fileflip.ui.screens.PreviewScreen
import com.just_for_fun.fileflip.ui.screens.ProScreen
import com.just_for_fun.fileflip.ui.screens.SettingsScreen

@Composable
fun AppNavigation(
    startDestination: String = "onboarding",
    pendingFileUri: Uri? = null,
    pendingFilePath: String? = null
) {
    val navController = rememberNavController()

    // Handle pending file from external intent — always open in editor first
    LaunchedEffect(pendingFilePath) {
        pendingFilePath?.let { filePath ->
            val encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8")
            // Always open in editor so user can review/edit before previewing
            val route = "editor/$encodedPath"

            // Navigate to the editor screen
            navController.navigate(route) {
                // Clear the back stack to avoid going back to onboarding/dashboard
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("dashboard") { DashboardScreen(navController) }
        composable(
            route = "editor/{filePath}",
            arguments = listOf(androidx.navigation.navArgument("filePath") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val encodedFilePath = backStackEntry.arguments?.getString("filePath") ?: return@composable
            val filePath = java.net.URLDecoder.decode(encodedFilePath, "UTF-8")
            EditorScreen(navController, filePath)
        }
        composable(
            route = "preview/{filePath}",
            arguments = listOf(androidx.navigation.navArgument("filePath") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val encodedFilePath = backStackEntry.arguments?.getString("filePath") ?: return@composable
            val filePath = java.net.URLDecoder.decode(encodedFilePath, "UTF-8")
            PreviewScreen(navController, filePath)
        }
        composable("settings") { SettingsScreen(navController) }

        // Removed standalone "tutorial" route

        composable(
            "file_explorer?tab={tab}",
            arguments = listOf(
                androidx.navigation.navArgument("tab") {
                    type = androidx.navigation.NavType.StringType
                    defaultValue = "files"
                }
            )
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: "files"
            FileExplorerScreen(navController, tab)
        }
        composable("drafts_published") { DraftsPublishedScreen(navController) }
        composable("pro") { ProScreen(navController) }
        composable("about") { AboutScreen(navController) }
    }
}