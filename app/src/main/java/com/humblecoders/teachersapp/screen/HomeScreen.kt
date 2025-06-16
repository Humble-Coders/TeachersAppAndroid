package com.humblecoders.teachersapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.teachersapp.model.TeacherData
import com.humblecoders.teachersapp.viewmodel.AuthViewModel
import com.humblecoders.teachersapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    onNavigateToAttendance: () -> Unit
) {

    var roomSearchText by remember { mutableStateOf("") }
    var showRoomDropdown by remember { mutableStateOf(false) }
    var showEndSessionConfirm by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectText by remember { mutableStateOf("") }

    val gradientColors = listOf(
        Color(0xFF5CB8FF),
        Color(0xFF94A6FF)
    )

    val filteredRooms = if (roomSearchText.isEmpty()) {
        homeViewModel.availableRooms
    } else {
        homeViewModel.availableRooms.filter {
            it.contains(roomSearchText, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(gradientColors)
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Welcome, ${authViewModel.teacherData?.designation ?: ""} ${authViewModel.teacherData?.name ?: ""}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Manage your attendance sessions",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Classes Selection
            item {
                SessionCard(title = "Select Classes") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Choose the classes you're teaching:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        authViewModel.teacherData?.classes?.forEach { className ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = homeViewModel.selectedClasses.contains(className),
                                    onCheckedChange = {
                                        homeViewModel.toggleClassSelection(className)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF5CB8FF)
                                    )
                                )
                                Text(
                                    text = className,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            // Subject Selection
            item {
                SessionCard(title = "Select Subject") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Choose the subject:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ExposedDropdownMenuBox(
                                expanded = false,
                                onExpandedChange = { },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = homeViewModel.selectedSubject.ifEmpty { "Select Subject" },
                                    onValueChange = { },
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = false,
                                    onDismissRequest = { }
                                ) {
                                    authViewModel.teacherData?.subjects?.forEach { subject ->
                                        DropdownMenuItem(
                                            text = { Text(subject) },
                                            onClick = {
                                                homeViewModel.selectedSubject = subject
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { showAddSubjectDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Add",
                                    tint = Color(0xFF5CB8FF)
                                )
                            }
                        }
                    }
                }
            }

            // Room Selection
            item {
                SessionCard(title = "Select Room") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Choose the room:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Column {
                            OutlinedTextField(
                                value = roomSearchText,
                                onValueChange = {
                                    roomSearchText = it
                                    showRoomDropdown = it.isNotEmpty()
                                },
                                label = { Text("Search room...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            if (showRoomDropdown && filteredRooms.isNotEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 150.dp)
                                    ) {
                                        items(filteredRooms) { room ->
                                            DropdownMenuItem(
                                                text = { Text(room) },
                                                onClick = {
                                                    homeViewModel.selectedRoom = room
                                                    roomSearchText = room
                                                    showRoomDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Session Type
            item {
                SessionCard(title = "Session Type") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Select session type:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            homeViewModel.sessionTypes.forEach { (type, label) ->
                                FilterChip(
                                    selected = homeViewModel.selectedType == type,
                                    onClick = { homeViewModel.selectedType = type },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF5CB8FF),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = homeViewModel.isExtraClass,
                                onCheckedChange = { homeViewModel.isExtraClass = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF5CB8FF)
                                )
                            )
                            Text(
                                text = "Extra Class",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // Action Buttons
            item {
                if (!homeViewModel.isSessionActive) {
                    Button(
                        onClick = { homeViewModel.activateSession() },
                        enabled = homeViewModel.canActivateSession && !homeViewModel.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (homeViewModel.canActivateSession) Color.Green else Color.Gray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (homeViewModel.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Activate Session",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { homeViewModel.restartSession() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9800),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null
                                )
                                Text(
                                    text = "Restart",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Button(
                            onClick = { showEndSessionConfirm = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null
                                )
                                Text(
                                    text = "End Session",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Dialogs and Alerts
    if (homeViewModel.showAlert) {
        AlertDialog(
            onDismissRequest = { homeViewModel.dismissAlert() },
            title = {
                Text(if (homeViewModel.alertMessage.contains("ended successfully")) "Session Ended" else "Session Status")
            },
            text = { Text(homeViewModel.alertMessage) },
            confirmButton = {
                if (homeViewModel.alertMessage.contains("ended successfully")) {
                    TextButton(
                        onClick = {
                            homeViewModel.dismissAlert()
                            onNavigateToAttendance()
                        }
                    ) {
                        Text("Show Attendance")
                    }
                } else {
                    TextButton(
                        onClick = { homeViewModel.dismissAlert() }
                    ) {
                        Text("OK")
                    }
                }
            },
            dismissButton = if (homeViewModel.alertMessage.contains("ended successfully")) {
                {
                    TextButton(
                        onClick = { homeViewModel.dismissAlert() }
                    ) {
                        Text("OK")
                    }
                }
            } else null
        )
    }

    if (showEndSessionConfirm) {
        AlertDialog(
            onDismissRequest = { showEndSessionConfirm = false },
            title = { Text("End Session") },
            text = { Text("Are you sure you want to end the session?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEndSessionConfirm = false
                        homeViewModel.endSession()
                    }
                ) {
                    Text("End Session", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndSessionConfirm = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddSubjectDialog = false
                newSubjectText = ""
            },
            title = { Text("Add New Subject") },
            text = {
                OutlinedTextField(
                    value = newSubjectText,
                    onValueChange = { newSubjectText = it },
                    label = { Text("Subject name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedTeacherData = homeViewModel.addNewSubject(
                            newSubjectText,
                            authViewModel.teacherData
                        )
                        updatedTeacherData?.let { authViewModel.teacherData = it }
                        showAddSubjectDialog = false
                        newSubjectText = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddSubjectDialog = false
                        newSubjectText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SessionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                content()
            }
        }
    }
}