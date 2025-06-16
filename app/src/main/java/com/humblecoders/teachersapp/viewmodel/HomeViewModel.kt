package com.humblecoders.teachersapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humblecoders.teachersapp.model.SessionData
import com.humblecoders.teachersapp.model.TeacherData
import com.humblecoders.teachersapp.repository.AuthRepository
import com.humblecoders.teachersapp.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    var selectedClasses by mutableStateOf<List<String>>(emptyList())
    var selectedSubject by mutableStateOf("")
    var selectedRoom by mutableStateOf("")
    var selectedType by mutableStateOf("lect")
    var isExtraClass by mutableStateOf(false)
    var isSessionActive by mutableStateOf(false)
    var currentSessionData by mutableStateOf<SessionData?>(null)
    var availableRooms by mutableStateOf<List<String>>(emptyList())
    var isLoading by mutableStateOf(false)
    var alertMessage by mutableStateOf("")
    var showAlert by mutableStateOf(false)
    var availableSubjects by mutableStateOf<List<String>>(emptyList())
    var availableClasses by mutableStateOf<List<String>>(emptyList())



    val sessionTypes = listOf(
        "lect" to "Lecture",
        "lab" to "Lab",
        "tut" to "Tutorial"
    )

    val canActivateSession: Boolean
        get() = selectedClasses.isNotEmpty() && selectedSubject.isNotEmpty() && selectedRoom.isNotEmpty()



    init {
        loadRooms()
        loadAvailableSubjects()
        loadAvailableClasses()

    }

    private fun loadAvailableSubjects() {
        viewModelScope.launch {
            availableSubjects = firebaseRepository.fetchSubjects()
        }
    }
    private fun loadAvailableClasses() {
        viewModelScope.launch {
            availableClasses = firebaseRepository.fetchClasses()
        }
    }


    fun addSubjectToTeacher(subject: String, authViewModel: AuthViewModel) {
        val teacherData = authViewModel.teacherData ?: return
        val updatedSubjects = teacherData.subjects.toMutableList()

        if (!updatedSubjects.contains(subject)) {
            updatedSubjects.add(subject)
            val updatedTeacherData = teacherData.copy(subjects = updatedSubjects)

            viewModelScope.launch {
                authRepository.saveTeacherData(updatedTeacherData)
                authViewModel.teacherData = updatedTeacherData
            }
        }
    }

    fun addClassToTeacher(className: String, authViewModel: AuthViewModel) {
        val teacherData = authViewModel.teacherData ?: return
        val updatedClasses = teacherData.classes.toMutableList()

        if (!updatedClasses.contains(className)) {
            updatedClasses.add(className)
            val updatedTeacherData = teacherData.copy(classes = updatedClasses)

            viewModelScope.launch {
                authRepository.saveTeacherData(updatedTeacherData)
                authViewModel.teacherData = updatedTeacherData
            }
        }
    }

    private fun loadRooms() {
        viewModelScope.launch {
            // Load cached rooms first
            availableRooms = authRepository.getCachedRooms()

            // Fetch fresh data from Firebase
            val rooms = firebaseRepository.fetchRooms()
            availableRooms = rooms
            authRepository.saveCachedRooms(rooms)
        }
    }

    fun toggleClassSelection(className: String) {
        selectedClasses = if (selectedClasses.contains(className)) {
            selectedClasses - className
        } else {
            selectedClasses + className
        }
    }

    fun activateSession() {
        if (!canActivateSession) return

        isLoading = true
        viewModelScope.launch {
            val sessionId = UUID.randomUUID().toString()
            val currentDate = getCurrentDate()

            val sessionData = SessionData(
                classes = selectedClasses,
                subject = selectedSubject,
                room = selectedRoom,
                type = selectedType,
                isExtra = isExtraClass,
                date = currentDate,
                sessionId = sessionId,
                isActive = true
            )

            val success = firebaseRepository.activateSession(sessionData)

            isLoading = false
            if (success) {
                isSessionActive = true
                currentSessionData = sessionData
                alertMessage = "Session activated successfully!"
            } else {
                alertMessage = "Failed to activate session. Please try again."
            }
            showAlert = true
        }
    }

    fun endSession() {
        val sessionData = currentSessionData ?: return

        viewModelScope.launch {
            val success = firebaseRepository.endSession(sessionData)

            if (success) {
                isSessionActive = false
                resetForm()
                alertMessage = "Session ended successfully!"
            } else {
                alertMessage = "Failed to end session. Please try again."
            }
            showAlert = true
        }
    }

    fun restartSession() {
        alertMessage = "Session is already active for the selected classes."
        showAlert = true
    }

    fun addNewSubject(subject: String, teacherData: TeacherData?): TeacherData? {
        if (subject.isEmpty() || teacherData == null) return teacherData

        val updatedSubjects = teacherData.subjects.toMutableList()
        if (!updatedSubjects.contains(subject)) {
            updatedSubjects.add(subject)
            val updatedTeacherData = teacherData.copy(subjects = updatedSubjects)

            viewModelScope.launch {
                authRepository.saveTeacherData(updatedTeacherData)
            }

            return updatedTeacherData
        }
        return teacherData
    }

    private fun resetForm() {
        selectedClasses = emptyList()
        selectedSubject = ""
        selectedRoom = ""
        selectedType = "lect"
        isExtraClass = false
    }

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date())
    }

    fun dismissAlert() {
        showAlert = false
        alertMessage = ""
    }
}