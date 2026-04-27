package com.example.spotagame

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spotagame.ui.CreateEventScreen
import com.example.spotagame.ui.EventDetailsScreen
import com.example.spotagame.ui.MapScreen
import com.example.spotagame.ui.ProfileScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavGraph.Companion.findStartDestination

enum class SpotGameScreen {
    Map,
    CreateEvent,
    EventDetails,
    ProfileDetails
}

@Composable
fun SpotAGameApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by rememberSaveable { mutableIntStateOf(SpotGameScreen.Map.ordinal) }

    Scaffold(
        bottomBar = {
            NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
                SpotGameScreen.entries.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = currentRoute == index,
                        onClick = {
                            navController.navigate(route = destination.name) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {Icons.Default.ThumbUp},
                        label = { Text(destination.name) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = SpotGameScreen.Map.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = SpotGameScreen.Map.name) {
                MapScreen(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            composable(route = SpotGameScreen.CreateEvent.name) {
                CreateEventScreen(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            composable(route = SpotGameScreen.EventDetails.name) {
                EventDetailsScreen(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            composable(route = SpotGameScreen.ProfileDetails.name) {
                ProfileScreen(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}