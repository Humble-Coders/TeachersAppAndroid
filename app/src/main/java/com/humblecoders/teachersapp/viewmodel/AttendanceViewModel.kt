package com.humblecoders.teachersapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.humblecoders.teachersapp.model.AttendanceRecord
import com.humblecoders.teachersapp.model.SessionData
import com.humblecoders.teachersapp.repository.FirebaseRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    var attendanceList by mutableStateOf<List<AttendanceRecord>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadAttendance(sessionData: SessionData?) {
        sessionData ?: return

        isLoading = true
        viewModelScope.launch {
            val records = firebaseRepository.fetchAttendance(sessionData)
            attendanceList = records
            isLoading = false
        }
    }

    fun formatTimestamp(timestamp: String): String {
        return try {
            // Parse Firebase timestamp format
            val inputFormatter = SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a zzz", Locale.getDefault())
            val outputFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

            val date = inputFormatter.parse(timestamp)
            date?.let { outputFormatter.format(it) } ?: "Time not available"
        } catch (e: Exception) {
            try {
                // Try simpler format
                val inputFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

                val date = inputFormatter.parse(timestamp)
                date?.let { outputFormatter.format(it) } ?: "Time not available"
            } catch (e: Exception) {
                "Time not available"
            }
        }
    }
}