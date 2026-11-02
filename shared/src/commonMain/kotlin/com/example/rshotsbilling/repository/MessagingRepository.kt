package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.*
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*

class MessagingRepository {
    private val db by lazy {
        println("DEBUG: MessagingRepository initialized.")
        FirestoreManager.db
    }
    private val tasksRef get() = FirestoreManager.getTasksRef()
    private val logsRef get() = FirestoreManager.getAuditLogsRef()

    private fun now() = getCurrentTimestamp()

    suspend fun createTask(task: StaffTaskUpgrade) {
        val id = if (task.id.isEmpty()) "task_${now()}" else task.id
        val t = now()
        println("DEBUG: [LOGIN] Attempting Firestore path: staff_tasks_v2/$id")
        tasksRef.document(id).set(StaffTaskUpgrade.serializer(), task.copy(id = id, createdAt = t, lastUpdatedAt = t))
    }

    suspend fun updateTaskStatus(taskId: String, status: TaskStatus) {
        if (taskId.isEmpty()) return
        println("DEBUG: [LOGIN] Attempting Firestore path: staff_tasks_v2/$taskId")
        tasksRef.document(taskId).update("status" to status.name, "lastUpdatedAt" to now())
    }

    suspend fun addMessageToTask(taskId: String, message: Message) {
        if (taskId.isEmpty()) return
        println("DEBUG: [LOGIN] Attempting Firestore path: staff_tasks_v2/$taskId")
        val snapshot = tasksRef.document(taskId).get()
        val task = snapshot.data(StaffTaskUpgrade.serializer())
        val updatedMessages = task.conversation + message.copy(timestamp = now())
        tasksRef.document(taskId).set(StaffTaskUpgrade.serializer(), task.copy(conversation = updatedMessages, lastUpdatedAt = now()))
    }

    fun getTasksForUser(email: String): Flow<List<StaffTaskUpgrade>> {
        val startTime = getCurrentTimestamp()
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Tasks query flow START")
        return tasksRef.snapshots().map { snapshot ->
            val duration = getCurrentTimestamp() - startTime
            com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Tasks query flow received: $duration ms. Documents: ${snapshot.documents.size}")
            val allTasks = snapshot.documents.map { it.data(StaffTaskUpgrade.serializer()) }
            allTasks.filter { 
                it.creatorEmail == email || it.assigneeEmail == email || it.assigneeEmail == "ALL" 
            }.distinctBy { it.id }.sortedByDescending { it.lastUpdatedAt }
        }
    }

    suspend fun logAudit(action: String, entityId: String, userEmail: String, metadata: Map<String, String> = emptyMap()) {
        val log = AuditLog(
            id = "log_${now()}",
            userEmail = userEmail,
            action = action,
            entityId = entityId,
            timestamp = now(),
            metadata = metadata
        )
        logsRef.document(log.id).set(AuditLog.serializer(), log)
    }

    fun getAuditLogs(): Flow<List<AuditLog>> {
        return logsRef.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(AuditLog.serializer()) }.sortedByDescending { it.timestamp }
        }
    }
}
