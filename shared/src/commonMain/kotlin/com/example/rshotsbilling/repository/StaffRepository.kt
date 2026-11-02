package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.StaffMember
import com.example.rshotsbilling.model.PasswordResetRequest
import com.example.rshotsbilling.utils.FirestoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.datetime.Clock

class StaffRepository {
    private val db: FirebaseFirestore by lazy {
        println("DEBUG: StaffRepository initialized.")
        FirestoreManager.db
    }
    private fun getStaffRef() = FirestoreManager.getStaffRef()

    suspend fun addStaff(staff: StaffMember) {
        println("DEBUG: [LOGIN] Attempting Firestore path: companies/${staff.businessId}/staff/${staff.id}")
        // Save to company-scoped collection
        getStaffRef().document(staff.id).set(StaffMember.serializer(), staff)
        
        println("DEBUG: [LOGIN] Attempting Firestore path: staff/${staff.id}")
        // Save to global root collection for login lookup
        db.collection("staff").document(staff.id).set(StaffMember.serializer(), staff)
    }

    suspend fun getStaffList(): List<StaffMember> {
        val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Staff query START")
        
        val snapshot = getStaffRef().get()
        val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
        val count = snapshot.documents.size
        
        com.example.rshotsbilling.utils.KMPLogger.log("[PERF] Staff query END: $duration ms. Documents: $count")
        
        return snapshot.documents.map { it.data(StaffMember.serializer()) }
    }

    /**
     * Real-time stream of staff members for maximum reliability in the HR Panel
     */
    fun getStaffListFlow(): Flow<List<StaffMember>> {
        return getStaffRef().snapshots().map { snapshot ->
            snapshot.documents.map { it.data(StaffMember.serializer()) }
        }
    }

    suspend fun deleteStaff(id: String) {
        getStaffRef().document(id).delete()
        db.collection("staff").document(id).delete()
    }

    // Password Reset Requests
    suspend fun requestPasswordReset(email: String, businessId: String) {
        val id = "reset_${com.example.rshotsbilling.utils.getCurrentTimestamp()}"
        val request = PasswordResetRequest(
            id = id,
            email = email,
            requestedAt = Clock.System.now().toEpochMilliseconds(),
            status = "PENDING",
            businessId = businessId
        )
        FirestoreManager.getPasswordResetRequestsRef().document(id).set(PasswordResetRequest.serializer(), request)
    }

    fun getPasswordResetRequestsFlow(): Flow<List<PasswordResetRequest>> {
        return FirestoreManager.getPasswordResetRequestsRef()
            .where("status", equalTo = "PENDING")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { it.data(PasswordResetRequest.serializer()) }
            }
    }

    suspend fun updatePasswordResetRequest(request: PasswordResetRequest) {
        FirestoreManager.getPasswordResetRequestsRef().document(request.id).set(PasswordResetRequest.serializer(), request)
    }
}
