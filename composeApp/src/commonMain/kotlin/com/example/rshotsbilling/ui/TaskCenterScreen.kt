package com.example.rshotsbilling.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rshotsbilling.model.*
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.viewmodel.MessagingViewModel
import com.example.rshotsbilling.viewmodel.StaffViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCenterScreen(
    messagingViewModel: MessagingViewModel,
    staffViewModel: StaffViewModel,
    onBack: () -> Unit
) {
    val tasks by messagingViewModel.tasks.collectAsState()
    val staffList by staffViewModel.staffList.collectAsState()
    
    val activeCount by messagingViewModel.activeTasksCount.collectAsState(0)
    val overdueCount by messagingViewModel.overdueTasksCount.collectAsState(0)
    val completedToday by messagingViewModel.completedTodayCount.collectAsState(0)

    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task Center & SLA Hub", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, "New Task") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatCard(Modifier.weight(1f), "Active", activeCount.toString(), Icons.Default.Pending, Color.Yellow)
                TaskStatCard(Modifier.weight(1f), "Overdue", overdueCount.toString(), Icons.Default.Timer, Color.Red)
                TaskStatCard(Modifier.weight(1f), "Done Today", completedToday.toString(), Icons.Default.CheckCircle, Color.Green)
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    messagingViewModel.setSearchQuery(it)
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search tasks, messages...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tasks) { task ->
                    TaskCard(task, messagingViewModel)
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(staffList, messagingViewModel) { showCreateDialog = false }
    }
}

@Composable
fun TaskStatCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatusBadge(status: TaskStatus) {
    val color = when(status) {
        TaskStatus.COMPLETED -> Color.Green
        TaskStatus.IN_PROGRESS -> Color.Cyan
        TaskStatus.PENDING -> Color.Yellow
        TaskStatus.ARCHIVED -> Color.Red
        else -> Color.Gray
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = status.name,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TaskCard(task: StaffTaskUpgrade, viewModel: MessagingViewModel) {
    var showChat by remember { mutableStateOf(false) }
    val unreadCount = task.conversation.count { 
        it.senderEmail != UserSession.userEmail && !it.readBy.containsKey(UserSession.userEmail) 
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showChat = true },
        colors = CardDefaults.cardColors(containerColor = if (task.isPinned) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface),
        border = if (task.isPinned) androidx.compose.foundation.BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.5f)) else null
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task.isPinned) Icon(Icons.Default.PushPin, null, Modifier.size(12.dp), tint = Color.Cyan)
                    Text(task.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                if (unreadCount > 0) {
                    Badge(containerColor = Color.Red) { Text(unreadCount.toString(), color = Color.White) }
                }
            }
            
            Spacer(Modifier.height(4.dp))
            Text(task.description, maxLines = 2, fontSize = 12.sp, color = Color.Gray)
            
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(task.status)
                Text("Assignee: ${task.assigneeEmail.split("@")[0]}", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }

    if (showChat) {
        ChatDialog(task, viewModel, onDismiss = { showChat = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskDialog(staffList: List<StaffMember>, viewModel: MessagingViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("ALL") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("New Enterprise Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                
                var expandedStaff by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedStaff, onExpandedChange = { expandedStaff = !expandedStaff }) {
                    OutlinedTextField(value = assignee, onValueChange = {}, readOnly = true, label = { Text("Assign To") }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = expandedStaff, onDismissRequest = { expandedStaff = false }) {
                        DropdownMenuItem(text = { Text("BROADCAST (ALL)") }, onClick = { assignee = "ALL"; expandedStaff = false })
                        staffList.forEach { s ->
                            DropdownMenuItem(text = { Text(s.email) }, onClick = { assignee = s.email; expandedStaff = false })
                        }
                    }
                }

                var expandedPriority by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expandedPriority, onExpandedChange = { expandedPriority = !expandedPriority }) {
                    OutlinedTextField(value = priority.name, onValueChange = {}, readOnly = true, label = { Text("Priority") }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = expandedPriority, onDismissRequest = { expandedPriority = false }) {
                        TaskPriority.entries.forEach { p ->
                            DropdownMenuItem(text = { Text(p.name) }, onClick = { priority = p; expandedPriority = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val task = StaffTaskUpgrade(
                    creatorEmail = UserSession.userEmail ?: "ADMIN",
                    assigneeEmail = assignee,
                    title = title,
                    description = description,
                    priority = priority,
                    status = TaskStatus.PENDING,
                    createdAt = 0L
                )
                viewModel.createTask(task)
                onDismiss()
            }, enabled = title.isNotBlank()) {
                Text("CREATE TASK")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}
