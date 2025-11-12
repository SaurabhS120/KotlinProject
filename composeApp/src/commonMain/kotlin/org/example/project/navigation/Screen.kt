package org.example.project.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Details : Screen("details")
    data object Profile : Screen("profile")
    data object Tasks : Screen("tasks")
    data object Notes : Screen("notes")
}
