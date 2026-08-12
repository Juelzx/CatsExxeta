package com.example.catsexxeta.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.catsexxeta.feature.breedDetail.BreedDetailScreen
import com.example.catsexxeta.feature.breeds.BreedListRoute
import com.example.catsexxeta.feature.favorites.FavoritesScreen

private data class TopLevelDestination(
    val route: Any,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val topLevelDestinations = listOf(
    TopLevelDestination(BreedsRoute, "Breeds", Icons.Filled.Home),
    TopLevelDestination(FavoritesRoute, "Favorites", Icons.Filled.Favorite)
)

@Composable
fun CatNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBar {
                topLevelDestinations.forEach { destination ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == destination.route::class.qualifiedName
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BreedsRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<BreedsRoute> {
                BreedListRoute(
                    onNavigateToDetail = { breedId ->
                        navController.navigate(BreedDetailRoute(breedId))
                    }
                )
            }
            composable<BreedDetailRoute> { backStackEntry ->
                val route: BreedDetailRoute = backStackEntry.toRoute()
                BreedDetailScreen(
                    breedId = route.breedId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable<FavoritesRoute> {
                FavoritesScreen()
            }
        }
    }
}
