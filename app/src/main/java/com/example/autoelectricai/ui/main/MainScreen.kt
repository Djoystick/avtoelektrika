package com.example.autoelectricai.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.autoelectricai.ui.subscription.SubscriptionScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.autoelectricai.theme.AmberPrimary
import com.example.autoelectricai.theme.DarkBackground
import com.example.autoelectricai.theme.DarkSurface
import com.example.autoelectricai.theme.TextPrimary
import com.example.autoelectricai.theme.TextSecondary
import com.example.autoelectricai.theme.ErrorRed
import com.example.autoelectricai.theme.TextHint
import com.example.autoelectricai.ui.diagnosis.DiagnosisScreen
import com.example.autoelectricai.ui.knowledgebase.KnowledgeBaseScreen
import com.example.autoelectricai.ui.leaderboard.LeaderboardScreen
import com.example.autoelectricai.ui.moderation.ModerationScreen
import com.example.autoelectricai.ui.profile.ProfileScreen
import com.example.autoelectricai.ui.profile.AwardsScreen
import com.example.autoelectricai.ui.profile.BookmarksScreen
import com.example.autoelectricai.ui.settings.SettingsScreen
import com.example.autoelectricai.data.update.DownloadState
import android.content.Intent
import com.example.autoelectricai.ui.auth.AuthScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Diagnosis : BottomNavItem("diagnosis", "Диагностика", Icons.Default.Build)
    object KnowledgeBase : BottomNavItem("knowledge_base", "Энциклопедия", Icons.Default.MenuBook)
    object Profile : BottomNavItem("profile", "Профиль", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    startDestination: String = BottomNavItem.Diagnosis.route,
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val downloadState by updateViewModel.downloadState.collectAsState()
    val context = LocalContext.current



    val items = listOf(
        BottomNavItem.Diagnosis,
        BottomNavItem.KnowledgeBase,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check if the current route is one of the main bottom nav items
    val isBottomBarVisible = items.any { it.route == currentDestination?.route }

    // Map routes to TopAppBar titles
    val topBarTitle = when (currentDestination?.route) {
        BottomNavItem.Diagnosis.route -> "АвтоЭлектрик AI"
        BottomNavItem.KnowledgeBase.route -> "База Знаний"
        BottomNavItem.Profile.route -> "Мой Профиль"
        else -> ""
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            if (isBottomBarVisible) {
                TopAppBar(
                    title = {
                        Text(
                            text = topBarTitle,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки", tint = TextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
                )
            }
        },
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextPrimary
                ) {
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkBackground,
                                selectedTextColor = AmberPrimary,
                                indicatorColor = AmberPrimary,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("auth") {
                AuthScreen(onAuthSuccess = {
                    navController.navigate(BottomNavItem.Diagnosis.route) {
                        popUpTo("auth") { inclusive = true }
                    }
                })
            }
            composable(BottomNavItem.Diagnosis.route) {
                DiagnosisScreen()
            }
            composable(BottomNavItem.KnowledgeBase.route) {
                KnowledgeBaseScreen(
                    onAuthorClick = { identifier ->
                        navController.navigate("authorProfile/$identifier")
                    }
                )
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onNavigateToAwards = { navController.navigate("awards") },
                    onNavigateToBookmarks = { navController.navigate("bookmarks") }
                )
            }
            
            composable(
                route = "authorProfile/{identifier}",
                arguments = listOf(androidx.navigation.navArgument("identifier") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val identifier = backStackEntry.arguments?.getString("identifier")
                ProfileScreen(
                    emailOverride = identifier,
                    onBack = { navController.popBackStack() }
                )
            }
            
            // Nested or full-screen routes
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToModeration = { navController.navigate("moderation") },
                    onNavigateToLeaderboard = { navController.navigate("leaderboard") },
                    onNavigateToProfile = { navController.navigate(BottomNavItem.Profile.route) { popUpTo(navController.graph.findStartDestination().id) } }
                )
            }
            composable("moderation") {
                ModerationScreen(onBack = { navController.popBackStack() })
            }
            composable("leaderboard") {
                LeaderboardScreen(onBack = { navController.popBackStack() })
            }
            composable("awards") {
                AwardsScreen(onBack = { navController.popBackStack() })
            }
            composable("bookmarks") {
                BookmarksScreen(onBack = { navController.popBackStack() })
            }
            composable("subscription") {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
