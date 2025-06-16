package com.humblecoders.teachersapp.model

data class TeacherData(
    val name: String,
    val designation: String,
    val subjects: List<String>,
    val classes: List<String>
)


data class SessionData(
    val classes: List<String>,
    val subject: String,
    val room: String,
    val type: String, // "lect", "lab", "tut"
    val isExtra: Boolean,
    val date: String,
    val sessionId: String,
    val isActive: Boolean
)

data class AttendanceRecord(
    val rollNumber: String,
    val group: String,
    val timestamp: String
)