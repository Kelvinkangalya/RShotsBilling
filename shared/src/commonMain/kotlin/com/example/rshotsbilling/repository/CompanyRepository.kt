package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.Company
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.getCurrentTimestamp
import com.example.rshotsbilling.model.AuditLog
import com.example.rshotsbilling.utils.UserSession
import com.example.rshotsbilling.utils.KMPLogger
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CompanyRepository {
    private val db: FirebaseFirestore by lazy { FirestoreManager.db }
    private val companiesRef get() = db.collection("companies")
    private val auditLogsRef get() = db.collection("system_audit_logs")
    private val codesRef get() = db.collection("activationCodes")

    private fun getCompanyDocRef(id: String) = companiesRef.document(id.ifEmpty { "unknown_id" })

    suspend fun createCompany(company: Company): String {
        val now = getCurrentTimestamp()
        val id = if (company.id.isEmpty()) "comp_${now}" else company.id
        
        // Generate a random 6-character activation code
        val activationCode = (1..6).map { ('A'..'Z').random() }.joinToString("")
        
        val finalCompany = company.copy(
            id = id, 
            createdDate = now,
            activationCode = activationCode,
            createdBy = UserSession.userEmail ?: "System"
        )
        
        getCompanyDocRef(id).set(Company.serializer(), finalCompany)
        
        // Create an activation code entry
        val codeEntry = com.example.rshotsbilling.model.ActivationCode(
            id = "code_$now",
            code = activationCode,
            type = "COMPANY",
            companyId = id,
            createdBy = UserSession.userEmail ?: "System"
        )
        codesRef.document(codeEntry.id).set(com.example.rshotsbilling.model.ActivationCode.serializer(), codeEntry)
        
        logAction("COMPANY_CREATED", id, "Company ${company.name} created. Code: $activationCode")
        return id
    }

    suspend fun registerCompany(company: Company): String {
        return createCompany(company)
    }

    data class RegistrationResult(
        val companyId: String,
        val adminEmail: String,
        val temporaryPassword: String,
        val activationCode: String
    )

    suspend fun registerNewCompany(company: Company): RegistrationResult {
        val now = getCurrentTimestamp()
        val id = "comp_${now}"
        val activationCode = (1..6).map { ('A'..'Z').random() }.joinToString("")
        val tempPassword = (1..8).map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
        
        val finalCompany = company.copy(
            id = id,
            createdDate = now,
            activationCode = activationCode,
            subscriptionStatus = "Active",
            createdBy = UserSession.userEmail ?: "Vendor"
        )
        
        println("DEBUG: [LOGIN] Attempting Firestore path: companies/$id")
        // 1. Create Company
        getCompanyDocRef(id).set(Company.serializer(), finalCompany)
        
        // 2. Create Company Admin record in Staff collection (Company-scoped)
        val admin = com.example.rshotsbilling.model.StaffMember(
            id = "admin_${now}",
            businessId = id,
            email = company.adminEmail,
            displayName = company.adminName,
            role = "ADMIN",
            permissions = listOf("CAN_MANAGE_SYSTEM", "CAN_SELL", "CAN_BUY", "CAN_VIEW_REPORTS", "CAN_MANAGE_INVENTORY", "CAN_MANAGE_DEVICES"),
            isTemporaryPassword = true,
            setupComplete = false,
            dateAdded = now
        )
        println("DEBUG: [LOGIN] Attempting Firestore path: companies/$id/staff/${admin.id}")
        db.collection("companies").document(id).collection("staff").document(admin.id).set(com.example.rshotsbilling.model.StaffMember.serializer(), admin)
        
        println("DEBUG: [LOGIN] Attempting Firestore path: staff/${admin.id}")
        // 2b. ALSO Create record in global staff collection for login lookup
        db.collection("staff").document(admin.id).set(com.example.rshotsbilling.model.StaffMember.serializer(), admin)
        
        KMPLogger.log("DEBUG: [Repository] Global Staff Record Created: staff/${admin.id}")
        
        // 3. Create Global Activation Code
        val codeEntry = com.example.rshotsbilling.model.ActivationCode(
            id = "code_$now",
            code = activationCode,
            type = "COMPANY",
            companyId = id,
            createdBy = UserSession.userEmail ?: "Vendor"
        )
        codesRef.document(codeEntry.id).set(com.example.rshotsbilling.model.ActivationCode.serializer(), codeEntry)
        
        logAction("COMPANY_REGISTERED", id, "Company ${company.name} registered by Vendor. Admin: ${company.adminEmail}")
        
        return RegistrationResult(id, company.adminEmail, tempPassword, activationCode)
    }

    suspend fun updateCompanySetup(company: Company) {
        getCompanyDocRef(company.id).set(Company.serializer(), company)
        logAction("COMPANY_SETUP_COMPLETE", company.id, "Company setup completed by Admin")
    }

    suspend fun generateStaffActivationCode(companyId: String): String {
        val now = getCurrentTimestamp()
        val code = (1..6).map { (0..9).random() }.joinToString("")
        val codeEntry = com.example.rshotsbilling.model.ActivationCode(
            id = "staff_code_$now",
            code = code,
            type = "STAFF",
            companyId = companyId,
            createdBy = UserSession.userEmail ?: "Admin"
        )
        codesRef.document(codeEntry.id).set(com.example.rshotsbilling.model.ActivationCode.serializer(), codeEntry)
        return code
    }

    suspend fun updateCompany(company: Company) {
        getCompanyDocRef(company.id).set(Company.serializer(), company)
        logAction("COMPANY_UPDATED", company.id, "Company ${company.name} updated")
    }

    suspend fun getCompany(id: String): Company? {
        if (id.isEmpty()) {
            KMPLogger.log("DEBUG: [Repository] getCompany called with empty ID")
            return null
        }
        KMPLogger.log("DEBUG: [Repository] getCompany started for $id")
        return try {
            val doc = getCompanyDocRef(id).get()
            KMPLogger.log("DEBUG: [Repository] getCompany Firestore call completed. Exists: ${doc.exists}")
            if (doc.exists) doc.data(Company.serializer()) else null
        } catch (e: Exception) {
            KMPLogger.log("DEBUG: [Repository] getCompany FAILED: ${e.message}")
            null
        }
    }

    fun getAllCompaniesFlow(): Flow<List<Company>> {
        KMPLogger.log("DEBUG: [Query] Collection: companies | User: ${UserSession.userEmail}")
        return companiesRef.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(Company.serializer()) }
        }
    }

    suspend fun deleteCompany(id: String) {
        getCompanyDocRef(id).delete()
        logAction("COMPANY_DELETED", id, "Company deleted")
    }
    
    suspend fun suspendCompany(id: String, suspended: Boolean) {
        val status = if (suspended) "Suspended" else "Active"
        getCompanyDocRef(id).update("active" to !suspended, "subscriptionStatus" to status)
        logAction(if (suspended) "COMPANY_SUSPENDED" else "COMPANY_ACTIVATED", id, "Company status changed to $status")
    }

    suspend fun logAction(action: String, entityId: String, details: String) {
        val log = AuditLog(
            id = "log_${getCurrentTimestamp()}",
            userEmail = UserSession.userEmail ?: "System",
            action = action,
            entityId = entityId,
            timestamp = getCurrentTimestamp(),
            metadata = mapOf("details" to details)
        )
        auditLogsRef.document(log.id).set(AuditLog.serializer(), log)
    }

    fun getAuditLogsFlow(companyId: String? = null): Flow<List<AuditLog>> {
        val path = if (companyId != null) "system_audit_logs where entityId=$companyId" else "system_audit_logs"
        println("DEBUG: [LOGIN] Attempting Firestore path: $path (Collection Query)")
        val query = if (companyId != null) auditLogsRef.where { "entityId" equalTo companyId } else auditLogsRef
        return query.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(AuditLog.serializer()) }
        }
    }
}
