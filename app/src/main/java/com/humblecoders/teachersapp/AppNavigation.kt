package com.humblecoders.teachersapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    if (!authViewModel.isInitialized) {
        // Show splash/loading screen while checking login status
        SplashScreen()
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(authViewModel = authViewModel)

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

            LaunchedEffect(authViewModel.isLoggedIn) {
                if (!authViewModel.isLoggedIn) {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
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

@Composable
fun SplashScreen() {
    val gradientColors = listOf(
        Color(0xFF5CB8FF),
        Color(0xFF94A6FF)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = Color(0xFF5CB8FF)
                )
            }

            Text(
                text = "Smart Attend",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}