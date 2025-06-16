package com.humblecoders.teachersapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.humblecoders.teachersapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(authViewModel: AuthViewModel) {

    var subjectSearchText by remember { mutableStateOf("") }
    var classSearchText by remember { mutableStateOf("") }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    var showNewSubjectDialog by remember { mutableStateOf(false) }
    var showNewClassDialog by remember { mutableStateOf(false) }
    var newSubjectText by remember { mutableStateOf("") }
    var newClassText by remember { mutableStateOf("") }
    var designationDropdownExpanded by remember { mutableStateOf(false) }


    val gradientColors = listOf(
        Color(0xFF5CB8FF),
        Color(0xFF94A6FF)
    )

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
            verticalArrangement = Arrangement.spacedBy(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item { Spacer(modifier = Modifier.height(40.dp)) }

            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color(0xFF5CB8FF)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Smart Attend",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Teacher Portal",
                            fontSize = 18.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Personal Information
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Personal Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        OutlinedTextField(
                            value = authViewModel.name,
                            onValueChange = { authViewModel.name = it },
                            label = { Text("Enter your name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = designationDropdownExpanded,
                            onExpandedChange = { designationDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = authViewModel.selectedDesignation,
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Designation") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = designationDropdownExpanded,
                                onDismissRequest = { designationDropdownExpanded = false}
                            ) {
                                authViewModel.designations.forEach { designation ->
                                    DropdownMenuItem(
                                        text = { Text(designation) },
                                        onClick = {
                                            authViewModel.selectedDesignation = designation
                                            designationDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Subjects Section
            item {
                SearchableSection(
                    title = "Subjects You Teach",
                    searchText = subjectSearchText,
                    onSearchTextChange = {
                        subjectSearchText = it
                        showSubjectDropdown = it.isNotEmpty()
                    },
                    selectedItems = authViewModel.selectedSubjects,
                    availableItems = authViewModel.availableSubjects.filter {
                        it.contains(subjectSearchText, ignoreCase = true)
                    },
                    showDropdown = showSubjectDropdown,
                    onShowDropdownChange = { showSubjectDropdown = it },
                    onItemSelect = { item ->
                        authViewModel.addSubject(item)
                        subjectSearchText = ""
                        showSubjectDropdown = false
                    },
                    onItemRemove = { authViewModel.removeSubject(it) },
                    onAddNew = { showNewSubjectDialog = true },
                    placeholder = "Search subjects..."
                )
            }

            // Classes Section
            item {
                SearchableSection(
                    title = "Classes You Teach",
                    searchText = classSearchText,
                    onSearchTextChange = {
                        classSearchText = it
                        showClassDropdown = it.isNotEmpty()
                    },
                    selectedItems = authViewModel.selectedClasses,
                    availableItems = authViewModel.availableClasses.filter {
                        it.contains(classSearchText, ignoreCase = true)
                    },
                    showDropdown = showClassDropdown,
                    onShowDropdownChange = { showClassDropdown = it },
                    onItemSelect = { item ->
                        authViewModel.addClass(item)
                        classSearchText = ""
                        showClassDropdown = false
                    },
                    onItemRemove = { authViewModel.removeClass(it) },
                    onAddNew = { showNewClassDialog = true },
                    placeholder = "Search classes..."
                )
            }

            // Login Button
            item {
                Button(
                    onClick = { authViewModel.login() },
                    enabled = !authViewModel.isLoading &&
                            authViewModel.name.isNotEmpty() &&
                            authViewModel.selectedSubjects.isNotEmpty() &&
                            authViewModel.selectedClasses.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (authViewModel.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Complete Setup",
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "A Humble Solutions Product",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }

    // Dialogs
    if (showNewSubjectDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewSubjectDialog = false
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
                        authViewModel.addSubject(newSubjectText)
                        showNewSubjectDialog = false
                        newSubjectText = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewSubjectDialog = false
                        newSubjectText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNewClassDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewClassDialog = false
                newClassText = ""
            },
            title = { Text("Add New Class") },
            text = {
                OutlinedTextField(
                    value = newClassText,
                    onValueChange = { newClassText = it },
                    label = { Text("Class name") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.addClass(newClassText)
                        showNewClassDialog = false
                        newClassText = ""
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNewClassDialog = false
                        newClassText = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SearchableSection(
    title: String,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    selectedItems: List<String>,
    availableItems: List<String>,
    showDropdown: Boolean,
    onShowDropdownChange: (Boolean) -> Unit,
    onItemSelect: (String) -> Unit,
    onItemRemove: (String) -> Unit,
    onAddNew: () -> Unit,
    placeholder: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    label = { Text(placeholder) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

//                IconButton(
//                    onClick = onAddNew
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "Add",
//                        tint = Color(0xFF5CB8FF)
//                    )
//                }
            }

            if (showDropdown && availableItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(availableItems) { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = { onItemSelect(item) },
                                trailingIcon = if (selectedItems.contains(item)) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF5CB8FF)
                                        )
                                    }
                                } else null
                            )
                        }
                    }
                }
            }

            // Selected Items
            if (selectedItems.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(selectedItems) { item ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF5CB8FF).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                IconButton(
                                    onClick = { onItemRemove(item) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}