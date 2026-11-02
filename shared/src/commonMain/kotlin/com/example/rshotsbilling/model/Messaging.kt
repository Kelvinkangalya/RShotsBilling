package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

enum class TaskPriority { LOW, MEDIUM, HIGH, URGENT }

enum class TaskStatus { 
    DRAFT, 
    PENDING, 
    IN_PROGRESS, 
    PENDING_REVIEW, 
    COMPLETED, 
    REOPENED, 
    ARCHIVED 
}

@Serializable
data class Message(
    val id: String = "",
    val senderEmail: String = "",
    val content: String = "",
    val timestamp: Long = getCurrentTimestamp(),
    val attachmentUrls: List<String> = emptyList(),
    val repliedToId: String? = null,
    val readBy: Map<String, Long> = emptyMap(),
    val isRead: Boolean = false 
)

@Serializable
data class StaffTaskUpgrade(
    val id: String = "",
    val creatorEmail: String = "",
    val assigneeEmail: String = "",
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val conversation: List<Message> = emptyList(),
    val hiddenBy: List<String> = emptyList(), 
    val deadline: Long? = null,
    val isPinned: Boolean = false,
    val attachments: List<String> = emptyList(),
    val statusHistory: Map<String, Long> = emptyMap(), 
    val lastResponseBy: String? = null,
    val createdAt: Long = getCurrentTimestamp(),
    val lastUpdatedAt: Long = getCurrentTimestamp()
)

@Serializable
data class AuditLog(
    val id: String = "",
    val userEmail: String = "",
    val action: String = "", 
    val entityId: String = "",
    val timestamp: Long = getCurrentTimestamp(),
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class Branch(
    val id: String = "",
    val name: String = "",
    val location: String = ""
)
