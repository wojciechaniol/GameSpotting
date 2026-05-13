package com.example.spotagame

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotagame.data.EventLocation
import com.example.spotagame.ui.createEvent.CreateEventScreen
import com.example.spotagame.ui.EventDetailsScreen
import com.example.spotagame.ui.LoginScreen
import com.example.spotagame.ui.MapScreen
import com.example.spotagame.ui.ProfileScreen
import com.example.spotagame.ui.RegisterScreen
import com.example.spotagame.ui.createEvent.LocationPickerScreen

enum class SpotGameScreen(val icon: ImageVector) {
    Map(icon = Icons.Default.LocationOn),
    CreateEvent(icon = Icons.Default.Add),
    EventDetails(icon = Icons.AutoMirrored.Filled.List),
    ProfileDetails(icon = Icons.Default.Person)
}

private val AUTH_ROUTES = setOf("login", "register")
private const val LOCATION_PICKER_ROUTE = "location_picker"

@Composable
fun SpotAGameApp(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel()
) {
    val user by authViewModel.user.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null
            && currentRoute !in AUTH_ROUTES
            && currentRoute !in LOCATION_PICKER_ROUTE

    // Navigate whenever auth state changes: login → map, logout → login
    LaunchedEffect(user) {
        val route = navController.currentBackStackEntry?.destination?.route
        if (user != null && route in AUTH_ROUTES) {
            navController.navigate(SpotGameScreen.Map.name) {
                popUpTo("login") { inclusive = true }
            }
        } else if (user == null && route != null && route !in AUTH_ROUTES) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                    SpotGameScreen.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.name,
                            onClick = {
                                navController.navigate(destination.name) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.name) },
                            label = { Text(destination.name) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val startDestination = remember { if (user != null) SpotGameScreen.Map.name else "login" }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }
            composable("register") {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(SpotGameScreen.Map.name) {
                MapScreen(modifier = Modifier.fillMaxSize())
            }
            composable(SpotGameScreen.CreateEvent.name) { backStackEntry ->
                val pickedLocation = backStackEntry
                    .savedStateHandle
                    .get<EventLocation>("picked_location")
                CreateEventScreen(
                    onPickLocation = { navController.navigate(LOCATION_PICKER_ROUTE) },
                    pickedLocation = pickedLocation
                )
            }
            composable(LOCATION_PICKER_ROUTE) {
                LocationPickerScreen(
                    onLocationConfirmed = { location ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("picked_location", location)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(SpotGameScreen.EventDetails.name) {
                EventDetailsScreen(modifier = Modifier.fillMaxSize())
            }
            composable(SpotGameScreen.ProfileDetails.name) {
                ProfileScreen(
                    onLogout = { authViewModel.logout() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
