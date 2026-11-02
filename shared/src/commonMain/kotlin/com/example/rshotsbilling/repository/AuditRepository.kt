package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.AuditLog
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuditRepository {
    private val db get() = FirestoreManager.db
    private val auditLogsRef get() = FirestoreManager.getAuditLogsRef()

    suspend fun logAction(action: String, entityId: String, details: String, metadata: Map<String, String> = emptyMap()) {
        val now = getCurrentTimestamp()
        val log = AuditLog(
            id = "log_$now",
            userEmail = UserSession.userEmail ?: "System",
            action = action,
            entityId = entityId,
            timestamp = now,
            metadata = metadata + ("details" to details)
        )
        println("DEBUG: [LOGIN] Attempting Firestore path: audit_logs/${log.id}")
        auditLogsRef.document(log.id).set(AuditLog.serializer(), log)
    }

    fun getAuditLogsFlow(): Flow<List<AuditLog>> {
        return auditLogsRef.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(AuditLog.serializer()) }.sortedByDescending { it.timestamp }
        }
    }
}
