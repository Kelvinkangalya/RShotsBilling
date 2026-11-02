package com.example.rshotsbilling.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.rshotsbilling.model.Message
import com.example.rshotsbilling.model.StaffTaskUpgrade
import com.example.rshotsbilling.model.TaskStatus
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.viewmodel.MessagingViewModel
import com.example.rshotsbilling.utils.KmpUtils

@Composable
fun ChatDialog(
    task: StaffTaskUpgrade,
    messagingViewModel: MessagingViewModel,
    onDismiss: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF121212) 
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Status: ${task.status}",
                            color = if (task.status == TaskStatus.COMPLETED) Color(0xFF39FF14) else Color(0xFFFFD600),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = Color.White.copy(alpha = 0.6f))
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 12.dp))

                // Task Description
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = task.description,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Update Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.status != TaskStatus.COMPLETED) {
                        Button(
                            onClick = { messagingViewModel.updateStatus(task, TaskStatus.COMPLETED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF39FF14), contentColor = Color.Black),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("MARK COMPLETED", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { messagingViewModel.updateStatus(task, TaskStatus.PENDING) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600), contentColor = Color.Black),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("RE-OPEN TASK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = false,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(task.conversation) { message ->
                        val isMe = message.senderEmail == UserSession.userEmail
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                color = if (isMe) Color(0xFF0D47A1).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 0.dp,
                                    bottomEnd = if (isMe) 0.dp else 12.dp
                                ),
                                border = if (isMe) BorderStroke(0.5.dp, Color(0xFF0D47A1).copy(alpha = 0.5f)) else null
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (!isMe) {
                                        Text(
                                            text = message.senderEmail.split("@")[0].uppercase(),
                                            fontSize = 9.sp,
                                            color = Color(0xFF39FF14),
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                    Text(text = message.content, color = Color.White, fontSize = 14.sp)
                                    Text(
                                        text = com.example.rshotsbilling.utils.formatTimestamp(message.timestamp),
                                        fontSize = 8.sp,
                                        color = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Message Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...", color = Color.Gray, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF39FF14),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedContainerColor = Color.White.copy(alpha = 0.03f)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                messagingViewModel.sendMessage(task, messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank(),
                        modifier = Modifier
                            .background(
                                color = if (messageText.isNotBlank()) Color(0xFF0D47A1) else Color.White.copy(alpha = 0.1f),
                                shape = CircleShape
                            )
                            .size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            null,
                            tint = if (messageText.isNotBlank()) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
