package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DiagnosisViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KidneyAiApp()
                }
            }
        }
    }
}

object NavRoutes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val RESULT = "result"
    const val HISTORY = "history"
}

@Composable
fun KidneyAiApp(
    authViewModel: AuthViewModel = viewModel(),
    diagnosisViewModel: DiagnosisViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.uiState.collectAsState()
    val diagnosisState by diagnosisViewModel.uiState.collectAsState()

    val startDestination = if (authState.currentUser != null) {
        NavRoutes.DASHBOARD
    } else {
        NavRoutes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(NavRoutes.DASHBOARD) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.DASHBOARD) {
            DashboardScreen(
                user = authState.currentUser,
                viewModel = diagnosisViewModel,
                onNavigateToResult = {
                    navController.navigate(NavRoutes.RESULT)
                },
                onNavigateToHistory = {
                    navController.navigate(NavRoutes.HISTORY)
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.RESULT) {
            val result = diagnosisState.activeResult
            if (result != null) {
                ResultScreen(
                    result = result,
                    onBack = {
                        navController.popBackStack()
                    },
                    onNavigateToHistory = {
                        navController.navigate(NavRoutes.HISTORY)
                    }
                )
            } else {
                // If no result is active, return to dashboard
                navController.popBackStack(NavRoutes.DASHBOARD, inclusive = false)
            }
        }

        composable(NavRoutes.HISTORY) {
            HistoryScreen(
                viewModel = diagnosisViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
