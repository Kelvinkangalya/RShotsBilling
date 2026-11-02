package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.*
import com.example.rshotsbilling.repository.MessagingRepository
import com.example.rshotsbilling.utils.UserSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessagingViewModel : ViewModel() {
    private val repo = MessagingRepository()

    private val _tasks = MutableStateFlow<List<StaffTaskUpgrade>>(emptyList())
    
    private val _searchQuery = MutableStateFlow("")
    private val _filterStatus = MutableStateFlow<TaskStatus?>(null)
    private val _filterPriority = MutableStateFlow<TaskPriority?>(null)
    private val _filterAssignee = MutableStateFlow<String?>(null)

    val tasks: StateFlow<List<StaffTaskUpgrade>> = combine(
        _tasks, _searchQuery, _filterStatus, _filterPriority, _filterAssignee
    ) { tasksList, query, status, priority, assignee ->
        tasksList.filter { task ->
            (query.isEmpty() || task.title.contains(query, true) || task.description.contains(query, true) || task.conversation.any { it.content.contains(query, true) }) &&
            (status == null || task.status == status) &&
            (priority == null || task.priority == priority) &&
            (assignee == null || task.assigneeEmail == assignee)
        }.sortedWith(compareByDescending<StaffTaskUpgrade> { it.isPinned }.thenByDescending { it.lastUpdatedAt })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    val activeTasksCount = _tasks.map { list -> list.count { t -> t.status != TaskStatus.COMPLETED && t.status != TaskStatus.ARCHIVED } }
    val overdueTasksCount = _tasks.map { list -> list.count { t -> t.deadline != null && t.deadline < 0L && t.status != TaskStatus.COMPLETED } }
    val completedTodayCount = _tasks.map { list -> list.count { t -> t.status == TaskStatus.COMPLETED } }

    private var currentEmail: String? = null

    fun loadTasks(email: String) {
        if (currentEmail == email) return 
        currentEmail = email
        
        viewModelScope.launch {
            try {
                repo.getTasksForUser(email).collectLatest { allTasks ->
                    _tasks.value = allTasks.filter { !it.hiddenBy.contains(email) }
                }
            } catch (e: Exception) {
            }
        }
    }

    fun loadAuditLogs() {
        viewModelScope.launch {
            repo.getAuditLogs().collect {
                _auditLogs.value = it
            }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setFilterStatus(status: TaskStatus?) { _filterStatus.value = status }
    fun setFilterPriority(priority: TaskPriority?) { _filterPriority.value = priority }
    fun setFilterAssignee(email: String?) { _filterAssignee.value = email }

    fun createTask(task: StaffTaskUpgrade) {
        viewModelScope.launch {
            try {
                repo.createTask(task)
            } catch (e: Exception) {
            }
        }
    }

    fun hideTaskForUser(task: StaffTaskUpgrade, email: String) {
        viewModelScope.launch {
            try {
                val updatedHiddenBy = task.hiddenBy + email
                repo.createTask(task.copy(hiddenBy = updatedHiddenBy))
            } catch (e: Exception) {
            }
        }
    }

    fun togglePin(task: StaffTaskUpgrade) {
        viewModelScope.launch {
            repo.createTask(task.copy(isPinned = !task.isPinned))
        }
    }

    fun sendMessage(task: StaffTaskUpgrade, content: String, attachmentUrls: List<String> = emptyList(), repliedToId: String? = null) {
        viewModelScope.launch {
            try {
                val email = UserSession.userEmail ?: return@launch
                val newMessage = Message(
                    id = "msg_0",
                    senderEmail = email,
                    content = content,
                    attachmentUrls = attachmentUrls,
                    repliedToId = repliedToId,
                    readBy = mapOf(email to 0L)
                )
                repo.addMessageToTask(task.id, newMessage)
            } catch (e: Exception) {
            }
        }
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            try {
                repo.updateTaskStatus(taskId, status)
            } catch (e: Exception) {
            }
        }
    }

    // Keep original updateStatus for compatibility if needed
    fun updateStatus(task: StaffTaskUpgrade, newStatus: TaskStatus) {
        updateTaskStatus(task.id, newStatus)
        logAudit("TASK_STATUS_CHANGE", task.id, UserSession.userEmail ?: "Unknown", mapOf("old" to task.status.name, "new" to newStatus.name))
    }

    fun logAudit(action: String, entityId: String, userEmail: String, metadata: Map<String, String> = emptyMap()) {
        viewModelScope.launch {
            repo.logAudit(action, entityId, userEmail, metadata)
        }
    }

    fun markMessagesRead(task: StaffTaskUpgrade) {
        val email = UserSession.userEmail ?: return
        val hasUnread = task.conversation.any { it.senderEmail != email && !it.readBy.containsKey(email) }
        if (!hasUnread) return

        viewModelScope.launch {
            try {
                val updatedConv = task.conversation.map { msg ->
                    if (msg.senderEmail != email && !msg.readBy.containsKey(email)) {
                        msg.copy(readBy = msg.readBy + (email to 0L))
                    } else msg
                }
                repo.createTask(task.copy(conversation = updatedConv))
            } catch (e: Exception) {
            }
        }
    }

    fun reassignTask(task: StaffTaskUpgrade, newAssignee: String) {
        viewModelScope.launch {
            repo.createTask(task.copy(assigneeEmail = newAssignee))
            repo.logAudit("TASK_REASSIGN", task.id, UserSession.userEmail ?: "Unknown", mapOf("from" to task.assigneeEmail, "to" to newAssignee))
        }
    }
}
