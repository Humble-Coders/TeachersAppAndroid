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
            println("Error fetching subjects: ${e.message}")

            emptyList()
        }
    }

    suspend fun fetchClasses(): List<String> {
        return try {
            val document = db.collection("classes").document("classes_list").get().await()
            document.get("classes_list") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            println("Error fetching classes: ${e.message}")

            emptyList()
        }
    }

    suspend fun fetchRooms(): List<String> {
        return try {
            val document = db.collection("rooms").document("rooms_list").get().await()
            document.get("rooms_list") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            println("Error fetching rooms: ${e.message}")

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

            // Update subject counters with proper map structure
            val subjectRef = db.collection("subjects").document(sessionData.subject)
            val subjectDoc = subjectRef.get().await()

            if (subjectDoc.exists()) {
                // Document exists, update each class individually
                sessionData.classes.forEach { className ->
                    val currentValue = subjectDoc.getLong("${className}.${sessionData.type}") ?: 0L
                    subjectRef.update("${className}.${sessionData.type}", currentValue + 1).await()
                }
            } else {
                // Document doesn't exist, create with proper map structure for all classes
                val data = mutableMapOf<String, Map<String, Long>>()
                sessionData.classes.forEach { className ->
                    data[className] = mapOf(
                        "lect" to if (sessionData.type == "lect") 1L else 0L,
                        "lab" to if (sessionData.type == "lab") 1L else 0L,
                        "tut" to if (sessionData.type == "tut") 1L else 0L
                    )
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

            println("=== DEBUG ATTENDANCE FETCH ===")
            println("Collection: $collectionName")
            println("Session data: $sessionData")
            println("Looking for classes: ${sessionData.classes}")
            println("Date: ${sessionData.date}")
            println("Subject: ${sessionData.subject}")
            println("Type: ${sessionData.type}")
            println("Room: ${sessionData.room}")
            println("IsExtra: ${sessionData.isExtra}")

            val attendanceRecords = mutableListOf<AttendanceRecord>()

            // First, let's get ALL present records and see what we have
            val querySnapshot = db.collection(collectionName)
                .whereEqualTo("present", true)
                .get()
                .await()

            println("Total present records found: ${querySnapshot.documents.size}")

            querySnapshot.documents.forEachIndexed { index, doc ->
                val data = doc.data ?: return@forEachIndexed

                println("--- Record $index ---")
                println("Document ID: ${doc.id}")
                println("Full data: $data")

                val rollNumber = when (val rollNumberValue = data["rollNumber"]) {
                    is Int -> rollNumberValue.toString()
                    is String -> rollNumberValue
                    else -> {
                        println("Invalid rollNumber: $rollNumberValue")
                        return@forEachIndexed
                    }
                }

                val group = data["group"] as? String ?: run {
                    println("Missing group field")
                    return@forEachIndexed
                }

                val timestamp = data["timestamp"] as? Timestamp ?: run {
                    println("Missing timestamp field")
                    return@forEachIndexed
                }

                val docDate = data["date"] as? String ?: run {
                    println("Missing date field")
                    return@forEachIndexed
                }

                val docSubject = data["subject"] as? String ?: run {
                    println("Missing subject field")
                    return@forEachIndexed
                }

                val docType = data["type"] as? String ?: run {
                    println("Missing type field")
                    return@forEachIndexed
                }

                val deviceRoom = data["deviceRoom"] as? String ?: ""

                val docIsExtra = when (val isExtraValue = data["isExtra"]) {
                    is Boolean -> isExtraValue
                    is Int -> isExtraValue == 1
                    else -> {
                        println("Invalid isExtra value: $isExtraValue")
                        return@forEachIndexed
                    }
                }

                // Check each condition
                val dateMatch = docDate == sessionData.date
                val subjectMatch = docSubject == sessionData.subject
                val typeMatch = docType == sessionData.type
                val groupMatch = sessionData.classes.contains(group)
                val extraMatch = docIsExtra == sessionData.isExtra
                val roomMatch = deviceRoom.isNotEmpty() && deviceRoom.startsWith(sessionData.room)

                println("Roll: $rollNumber, Group: $group")
                println("Date match: $dateMatch ($docDate vs ${sessionData.date})")
                println("Subject match: $subjectMatch ($docSubject vs ${sessionData.subject})")
                println("Type match: $typeMatch ($docType vs ${sessionData.type})")
                println("Group match: $groupMatch ($group in ${sessionData.classes})")
                println("Extra match: $extraMatch ($docIsExtra vs ${sessionData.isExtra})")
                println("Room match: $roomMatch ($deviceRoom starts with ${sessionData.room})")

                if (dateMatch && subjectMatch && typeMatch && groupMatch && extraMatch && roomMatch) {
                    val timestampString = formatFirebaseTimestamp(timestamp)
                    attendanceRecords.add(
                        AttendanceRecord(
                            rollNumber = rollNumber,
                            group = group,
                            timestamp = timestampString
                        )
                    )
                    println("✅ ADDED to results")
                } else {
                    println("❌ REJECTED")
                }
            }

            println("=== FINAL RESULTS ===")
            println("Total matching records: ${attendanceRecords.size}")
            attendanceRecords.forEach { record ->
                println("Roll: ${record.rollNumber}, Group: ${record.group}")
            }

            attendanceRecords.sortedBy { it.rollNumber }
        } catch (e: Exception) {
            println("Error fetching attendance: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    private fun formatFirebaseTimestamp(timestamp: Timestamp): String {
        val date = timestamp.toDate()
        val formatter = SimpleDateFormat("MMMM d, yyyy 'at' h:mm:ss a zzz", Locale.getDefault())
        return formatter.format(date)
    }
}