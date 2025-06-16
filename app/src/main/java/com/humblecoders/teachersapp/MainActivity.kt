package com.humblecoders.teachersapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humblecoders.teachersapp.repository.AuthRepository
import com.humblecoders.teachersapp.repository.FirebaseRepository
import com.humblecoders.teachersapp.ui.theme.TeachersAppTheme
import com.humblecoders.teachersapp.viewmodel.AttendanceViewModel
import com.humblecoders.teachersapp.viewmodel.AuthViewModel
import com.humblecoders.teachersapp.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository
    private lateinit var firebaseRepository: FirebaseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize repositories
        authRepository = AuthRepository(this)
        firebaseRepository = FirebaseRepository()

        setContent {
            TeachersAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Create ViewModels with repositories
                    val authViewModel = viewModel<AuthViewModel> {
                        AuthViewModelFactory(authRepository, firebaseRepository)
                            .create(AuthViewModel::class.java)
                    }

                    val homeViewModel = viewModel<HomeViewModel> {
                        HomeViewModelFactory(authRepository, firebaseRepository)
                            .create(HomeViewModel::class.java)
                    }

                    val attendanceViewModel = viewModel<AttendanceViewModel> {
                        AttendanceViewModelFactory(firebaseRepository)
                            .create(AttendanceViewModel::class.java)
                    }

                    AppNavigation(
                        authViewModel = authViewModel,
                        homeViewModel = homeViewModel,
                        attendanceViewModel = attendanceViewModel
                    )
                }
            }
        }
    }
}

// ViewModelFactory classes
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