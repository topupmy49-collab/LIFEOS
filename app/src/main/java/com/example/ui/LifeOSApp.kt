package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Note
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.GoalsScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LifeOSViewModel

enum class MainDestination(
    val route: String,
    val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home),
    TASKS("tasks", R.string.nav_tasks, Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    NOTES("notes", R.string.nav_notes, Icons.Filled.Note, Icons.Outlined.Note),
    FINANCE("finance", R.string.nav_finance, Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
    SETTINGS("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}

enum class SubScreen {
    NONE,
    GOALS,
    HABITS
}

@Composable
fun LifeOSApp(
    viewModel: LifeOSViewModel
) {
    val preferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    var currentDestination by remember { mutableStateOf(MainDestination.HOME) }
    var currentSubScreen by remember { mutableStateOf(SubScreen.NONE) }

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    if (!preferences.onboardingCompleted) {
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = {
                currentDestination = MainDestination.HOME
                currentSubScreen = SubScreen.NONE
            },
            currentLanguage = preferences.language
        )
    } else {
        // Handle Back navigation when on secondary sub-screens
        if (currentSubScreen != SubScreen.NONE) {
            BackHandler {
                currentSubScreen = SubScreen.NONE
            }
        } else if (currentDestination != MainDestination.HOME) {
            BackHandler {
                currentDestination = MainDestination.HOME
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (currentSubScreen == SubScreen.NONE) {
                    NavigationBar(
                        containerColor = SurfaceCardDark,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        MainDestination.values().forEach { destination ->
                            val isSelected = currentDestination == destination
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    currentDestination = destination
                                    currentSubScreen = SubScreen.NONE
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                        contentDescription = stringResource(destination.titleRes)
                                    )
                                },
                                label = {
                                    Text(text = stringResource(destination.titleRes))
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = BackgroundDark,
                                    selectedTextColor = NeonLime,
                                    indicatorColor = NeonLime,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_item_${destination.route}")
                            )
                        }
                    }
                }
            },
            containerColor = BackgroundDark
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    currentSubScreen == SubScreen.GOALS -> {
                        GoalsScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    currentSubScreen == SubScreen.HABITS -> {
                        HabitsScreen(
                            viewModel = viewModel,
                            onBack = { currentSubScreen = SubScreen.NONE }
                        )
                    }
                    currentDestination == MainDestination.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToTasks = { currentDestination = MainDestination.TASKS },
                            onNavigateToNotes = { currentDestination = MainDestination.NOTES },
                            onNavigateToFinance = { currentDestination = MainDestination.FINANCE },
                            onNavigateToGoals = { currentSubScreen = SubScreen.GOALS },
                            onNavigateToHabits = { currentSubScreen = SubScreen.HABITS }
                        )
                    }
                    currentDestination == MainDestination.TASKS -> {
                        TasksScreen(viewModel = viewModel)
                    }
                    currentDestination == MainDestination.NOTES -> {
                        NotesScreen(viewModel = viewModel)
                    }
                    currentDestination == MainDestination.FINANCE -> {
                        FinanceScreen(viewModel = viewModel)
                    }
                    currentDestination == MainDestination.SETTINGS -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
