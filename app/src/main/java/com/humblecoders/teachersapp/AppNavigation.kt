package com.humblecoders.teachersapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.humblecoders.teachersapp.screen.AttendanceScreen
import com.humblecoders.teachersapp.screen.HomeScreen
import com.humblecoders.teachersapp.screen.LoginScreen
import com.humblecoders.teachersapp.viewmodel.AttendanceViewModel
import com.humblecoders.teachersapp.viewmodel.AuthViewModel
import com.humblecoders.teachersapp.viewmodel.HomeViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    attendanceViewModel: AttendanceViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(authViewModel = authViewModel)

            // Navigate to home when logged in
            // ✅ Fix - Use LaunchedEffect
            LaunchedEffect(authViewModel.isLoggedIn) {
                if (authViewModel.isLoggedIn) {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }

        composable("home") {
            HomeScreen(
                authViewModel = authViewModel,
                homeViewModel = homeViewModel,
                onNavigateToAttendance = {
                    navController.navigate("attendance")
                }
            )

            // Navigate to login when logged out
            if (!authViewModel.isLoggedIn) {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }

        composable("attendance") {
            AttendanceScreen(
                sessionData = homeViewModel.currentSessionData,
                attendanceViewModel = attendanceViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}