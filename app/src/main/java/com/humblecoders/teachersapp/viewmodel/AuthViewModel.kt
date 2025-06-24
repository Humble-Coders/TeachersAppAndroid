package com.humblecoders.teachersapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humblecoders.teachersapp.model.TeacherData
import com.humblecoders.teachersapp.repository.AuthRepository
import com.humblecoders.teachersapp.repository.FirebaseRepository
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    var isLoggedIn by mutableStateOf(false)
        private set

    var isInitialized by mutableStateOf(false)
        private set

    var teacherData by mutableStateOf<TeacherData?>(null)
        internal set

    var name by mutableStateOf("")
    var selectedDesignation by mutableStateOf("Mr.")
    var selectedSubjects by mutableStateOf<List<String>>(emptyList())
    var selectedClasses by mutableStateOf<List<String>>(emptyList())
    var availableSubjects by mutableStateOf<List<String>>(emptyList())
    var availableClasses by mutableStateOf<List<String>>(emptyList())
    var isLoading by mutableStateOf(false)

    val designations = listOf("Mr.", "Mrs.", "Ms.", "Dr.", "Prof.")

    init {
        checkLoginStatus()
        loadData()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val data = authRepository.getTeacherData()
            if (data != null) {
                teacherData = data
                isLoggedIn = true
            }

            // Add 2 second delay to show splash screen
            kotlinx.coroutines.delay(1200)
            isInitialized = true
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            availableSubjects = firebaseRepository.fetchSubjects()
            availableClasses = firebaseRepository.fetchClasses()
        }
    }

    fun login() {
        if (name.isEmpty() || selectedSubjects.isEmpty() || selectedClasses.isEmpty()) return

        isLoading = true
        viewModelScope.launch {
            val data = TeacherData(
                name = name,
                designation = selectedDesignation,
                subjects = selectedSubjects,
                classes = selectedClasses
            )

            authRepository.saveTeacherData(data)
            teacherData = data
            isLoggedIn = true
            isLoading = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.deleteTeacherData()
            teacherData = null
            isLoggedIn = false
        }
    }

    fun addSubject(subject: String) {
        if (subject.isNotEmpty() && !selectedSubjects.contains(subject)) {
            selectedSubjects = selectedSubjects + subject
        }
    }

    fun removeSubject(subject: String) {
        selectedSubjects = selectedSubjects - subject
    }

    fun addClass(className: String) {
        if (className.isNotEmpty() && !selectedClasses.contains(className)) {
            selectedClasses = selectedClasses + className
        }
    }

    fun removeClass(className: String) {
        selectedClasses = selectedClasses - className
    }
}