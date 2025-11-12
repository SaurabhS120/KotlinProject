# Jetpack Compose Navigation - Setup Guide

This project implements official Jetpack Compose Navigation for Kotlin Multiplatform using
`androidx.navigation:navigation-compose`.

## Dependencies Added

### gradle/libs.versions.toml

```toml
[versions]
navigation-compose = "2.8.0-alpha08"

[libraries]
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
```

### composeApp/build.gradle.kts

```kotlin
commonMain.dependencies {
    implementation(libs.androidx.navigation.compose)
}
```

## Project Structure

```
composeApp/src/commonMain/kotlin/org/example/project/
├── App.kt                          # Main app entry point with NavController setup
├── navigation/
│   ├── Screen.kt                   # Navigation routes/destinations
│   └── NavGraph.kt                 # Navigation graph configuration
└── screens/
    ├── HomeScreen.kt               # Home screen with navigation actions
    ├── DetailsScreen.kt            # Details screen
    └── ProfileScreen.kt            # Profile screen
```

## Key Components

### 1. Navigation Routes (Screen.kt)

Defines all available navigation destinations using a sealed class:

```kotlin
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Details : Screen("details")
    data object Profile : Screen("profile")
}
```

### 2. Navigation Graph (NavGraph.kt)

Sets up the navigation graph with all routes and their corresponding screens:

```kotlin
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToDetails = { navController.navigate(Screen.Details.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        
        composable(route = Screen.Details.route) {
            DetailsScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(route = Screen.Profile.route) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
```

### 3. Main App Entry Point (App.kt)

Initializes the NavController and displays the NavGraph:

```kotlin
@Composable
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavGraph(navController = navController)
    }
}
```

## Screen Implementations

### HomeScreen

- Entry point of the app
- Contains buttons to navigate to Details and Profile screens
- Demonstrates snackbar functionality
- Uses Material3 design

### DetailsScreen

- Shows detailed information
- Uses Card component with elevation
- Has a back button to return to previous screen

### ProfileScreen

- Displays user profile information
- Custom ProfileItem composable for key-value pairs
- Material3 theming with different color schemes

## Navigation Actions

### Navigate Forward

```kotlin
navController.navigate(Screen.Details.route)
```

### Navigate Back

```kotlin
navController.popBackStack()
```

## Features Demonstrated

1. **Multi-screen navigation** - Home, Details, and Profile screens
2. **Type-safe navigation** - Using sealed classes for routes
3. **Material3 Design** - Modern UI components and theming
4. **Scaffold with Snackbar** - Proper Material3 layout structure
5. **Safe content padding** - Handles system bars properly
6. **Different color schemes** - Each screen uses different Material3 color variants

## Building and Running

The project builds successfully for all platforms:

- Android
- iOS (Arm64 and SimulatorArm64)

To build:

```bash
./gradlew build
```

## Migration Notes

If migrating from the old navigation approach:

1. Routes can be incrementally migrated screen by screen
2. No need to migrate all at once
3. Old string-based routes can coexist with new type-safe routes
4. Consider using Kotlin Serialization for passing complex arguments

## Future Enhancements

Consider these additional features:

- **Type-safe arguments** - Using `@Serializable` data classes for passing parameters
- **Deep linking** - Connect external links to navigation routes
- **Nested navigation** - Multiple NavHost instances for complex flows
- **Bottom navigation** - Tab-based navigation using BottomNavigation
- **Navigation with ViewModels** - Integrate with lifecycle-viewmodel-compose

## Resources

- [Official Navigation Compose Documentation](https://developer.android.com/jetpack/compose/navigation)
- [Kotlin Multiplatform Navigation Guide](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-navigation-routing.html)
- [Navigation Compose GitHub](https://github.com/JetBrains/compose-multiplatform)
