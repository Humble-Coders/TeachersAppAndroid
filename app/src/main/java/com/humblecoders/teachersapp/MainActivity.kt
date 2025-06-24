package com.humblecoders.teachersapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.humblecoders.teachersapp.repository.AuthRepository
import com.humblecoders.teachersapp.repository.FirebaseRepository
import com.humblecoders.teachersapp.ui.theme.TeachersAppTheme
import com.humblecoders.teachersapp.viewmodel.AttendanceViewModel
import com.humblecoders.teachersapp.viewmodel.AuthViewModel
import com.humblecoders.teachersapp.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var firebaseRepository: FirebaseRepository
    private var homeViewModel: HomeViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        authRepository = AuthRepository(this)
        firebaseRepository = FirebaseRepository()

        setContent {
            TeachersAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel = viewModel<AuthViewModel> {
                        AuthViewModelFactory(authRepository, firebaseRepository)
                            .create(AuthViewModel::class.java)
                    }

                    val homeViewModelInstance = viewModel<HomeViewModel> {
                        HomeViewModelFactory(authRepository, firebaseRepository)
                            .create(HomeViewModel::class.java)
                    }

                    // Store reference for lifecycle methods
                    homeViewModel = homeViewModelInstance

                    val attendanceViewModel = viewModel<AttendanceViewModel> {
                        AttendanceViewModelFactory(firebaseRepository)
                            .create(AttendanceViewModel::class.java)
                    }

                    AppNavigation(
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModelInstance,
                        attendanceViewModel = attendanceViewModel
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        // End session when app is paused
        homeViewModel?.let { viewModel ->
            if (viewModel.isSessionActive) {
                lifecycleScope.launch {
                    viewModel.endSessionSilently()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // End session when app is destroyed
        homeViewModel?.let { viewModel ->
            if (viewModel.isSessionActive) {
                lifecycleScope.launch {
                    viewModel.endSessionSilently()
                }
            }
        }
    }
}


class AuthViewModelFactory(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(authRepository, firebaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class HomeViewModelFactory(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(authRepository, firebaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AttendanceViewModelFactory(
    private val firebaseRepository: FirebaseRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(firebaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}