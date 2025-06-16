package com.humblecoders.teachersapp.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.humblecoders.teachersapp.model.AttendanceRecord
import com.humblecoders.teachersapp.model.SessionData
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class FirebaseRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchSubjects(): List<String> {
        return try {
            val document = db.collection("subjects_list").document("subjects_list").get().await()
            document.get("subjects_list") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchClasses(): List<String> {
        return try {
            val document = db.collection("classes").document("classes_list").get().await()
            document.get("classes_list") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchRooms(): List<String> {
        return try {
            val document = db.collection("rooms").document("rooms_list").get().await()
            document.get("rooms_list") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun activateSession(sessionData: SessionData): Boolean {
        return try {
            sessionData.classes.forEach { className ->
                val sessionRef = db.collection("activeSessions").document(className)
                val data = mapOf(
                    "date" to sessionData.date,
                    "isActive" to true,
                    "isExtra" to sessionData.isExtra,
                    "room" to sessionData.room,
                    "sessionId" to sessionData.sessionId,
                    "subject" to sessionData.subject,
                    "type" to sessionData.type
                )
                sessionRef.set(data).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun endSession(sessionData: SessionData): Boolean {
        return try {
            // End session in activeSessions
            sessionData.classes.forEach { className ->
                val sessionRef = db.collection("activeSessions").document(className)
                sessionRef.update("isActive", false).await()
            }

            // Update subject counters
            val subjectRef = db.collection("subjects").document(sessionData.subject)
            val subjectDoc = subjectRef.get().await()

            if (subjectDoc.exists()) {
                sessionData.classes.forEach { className ->
                    val currentValue = subjectDoc.getLong("${className}.${sessionData.type}") ?: 0L
                    subjectRef.update("${className}.${sessionData.type}", currentValue + 1).await()
                }
            } else {
                val data = mutableMapOf<String, Any>()
                sessionData.classes.forEach { className ->
                    data["${className}.${sessionData.type}"] = 1
                }
                subjectRef.set(data).await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun fetchAttendance(sessionData: SessionData): List<AttendanceRecord> {
        return try {
            val currentDate = Date()
            val formatter = SimpleDateFormat("yyyy_MM", Locale.getDefault())
            val collectionName = "attendance_${formatter.format(currentDate)}"

            val attendanceRecords = mutableListOf<AttendanceRecord>()

            sessionData.classes.forEach { className ->
                val querySnapshot = db.collection(collectionName)
                    .whereEqualTo("present", true)
                    .get()
                    .await()

                querySnapshot.documents.forEach { doc ->
                    val data = doc.data ?: return@forEach

                    val rollNumber = when (val rollNumberValue = data["rollNumber"]) {
                        is Int -> rollNumberValue.toString()
                        is String -> rollNumberValue
                        else -> return@forEach
                    }

                    val group = data["group"] as? String ?: return@forEach
                    val timestamp = data["timestamp"] as? Timestamp ?: return@forEach
                    val docDate = data["date"] as? String ?: return@forEach
                    val docSubject = data["subject"] as? String ?: return@forEach
                    val docType = data["type"] as? String ?: return@forEach
                    val deviceRoom = data["deviceRoom"] as? String ?: ""

                    val docIsExtra = when (val isExtraValue = data["isExtra"]) {
                        is Boolean -> isExtraValue
                        is Int -> isExtraValue == 1
                        else -> return@forEach
                    }

                    val timestampString = formatFirebaseTimestamp(timestamp)

                    // Check all conditions
                    val dateMatch = docDate == sessionData.date
                    val subjectMatch = docSubject == sessionData.subject
                    val typeMatch = docType == sessionData.type
                    val groupMatch = group == className
                    val extraMatch = docIsExtra == sessionData.isExtra
                    val roomMatch = deviceRoom.isNotEmpty() && deviceRoom.startsWith(sessionData.room)

                    if (dateMatch && subjectMatch && typeMatch && groupMatch && extraMatch && roomMatch) {
                        attendanceRecords.add(
                            AttendanceRecord(
                                rollNumber = rollNumber,
                                group = group,
                                timestamp = timestampString
                            )
                        )
                    }
                }
            }

            attendanceRecords.sortedBy { it.rollNumber }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun formatFirebaseTimestamp(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a zzz", Locale.getDefault())
        return formatter.format(date)
    }
}