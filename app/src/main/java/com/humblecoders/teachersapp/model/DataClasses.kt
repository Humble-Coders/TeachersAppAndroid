package com.humblecoders.teachersapp.model

import kotlinx.serialization.Serializable

@Serializable
data class TeacherData(
    val name: String,
    val designation: String,
    val subjects: List<String>,
    val classes: List<String>
)

@Serializable
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

@Serializable
data class AttendanceRecord(
    val rollNumber: String,
    val group: String,
    val timestamp: String
)