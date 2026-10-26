package com.example.nova.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nova.ui.screens.ActionsHistoryScreen
import com.example.nova.ui.screens.AuthGateScreen
import com.example.nova.ui.screens.ChatScreen
import com.example.nova.ui.screens.HomeScreen
import com.example.nova.ui.screens.SettingsScreen
import com.example.nova.viewmodel.AssistantViewModel
import com.example.nova.viewmodel.AuthViewModel
import com.example.nova.viewmodel.SettingsViewModel

object NovaDestinations {
    const val HOME = "home"
    const val CHAT = "chat"
    const val ACTIONS = "actions"
    const val SETTINGS = "settings"
}

@Composable
fun NovaNavigation(
    authViewModel: AuthViewModel = viewModel(),
    assistantViewModel: AssistantViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        AuthGateScreen(authViewModel = authViewModel)
    } else {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = NovaDestinations.HOME
        ) {
            composable(NovaDestinations.HOME) {
                HomeScreen(
                    viewModel = assistantViewModel,
                    onNavigateToChat = { navController.navigate(NovaDestinations.CHAT) },
                    onNavigateToActions = { navController.navigate(NovaDestinations.ACTIONS) },
                    onNavigateToSettings = { navController.navigate(NovaDestinations.SETTINGS) }
                )
            }

            composable(NovaDestinations.CHAT) {
                ChatScreen(
                    viewModel = assistantViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NovaDestinations.ACTIONS) {
                ActionsHistoryScreen(
                    viewModel = assistantViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NovaDestinations.SETTINGS) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
