package org.example.project.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import org.example.project.screens.DetailsScreen
import org.example.project.screens.HomeScreen
import org.example.project.screens.ProfileScreen
import org.example.project.screens.TasksScreen
import org.example.project.data.TaskRepoProvider

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToDetails = {
                    navController.navigate(Screen.Details.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToTasks = {
                    navController.navigate(Screen.Tasks.route)
                }
            )
        }

        composable(route = Screen.Details.route) {
            DetailsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Tasks.route) {
            TasksScreen(repository = TaskRepoProvider.repo)
        }
    }
}
