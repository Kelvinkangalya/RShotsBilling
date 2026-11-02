package com.example.rshotsbilling.utils

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

object FirestoreManager {
    private var _db: dev.gitlive.firebase.firestore.FirebaseFirestore? = null

    fun provideDb(): dev.gitlive.firebase.firestore.FirebaseFirestore {
        if (_db != null) return _db!!
        
        KMPLogger.log("[PERF] Firestore database provider requested START.")
        val startTime = getCurrentTimestamp()
        
        return try {
            val db = PlatformFirebase.firestore
            PlatformFirebase.configureSettings(db)
            _db = db
            KMPLogger.log("[PERF] Firestore instance successfully cached END: ${getCurrentTimestamp() - startTime} ms")
            db
        } catch (e: Exception) {
            KMPLogger.log("[PERF] Firestore provider FAILURE: ${e.message}")
            PlatformFirebase.firestore
        }
    }

    val db get() = provideDb()

    fun getCompanyId(): String {
        val id = if (UserSession.companyId.isNotEmpty()) UserSession.companyId else UserSession.businessId
        if (id.isEmpty()) {
            val isSuperVendor = UserSession.userEmail?.equals(BusinessConfig.SUPER_VENDOR_EMAIL, ignoreCase = true) == true
            if (isSuperVendor) {
                return "root"
            }
            // RETURN EMPTY instead of "unknown_company" to prevent invalid path panics
            KMPLogger.log("WARNING: [Firestore] getCompanyId() EMPTY. SessionReady: ${UserSession.isSessionReady}")
            return "" 
        }
        return id
    }

    fun getCompanyRef(): dev.gitlive.firebase.firestore.DocumentReference {
        val companyId = getCompanyId()
        if (companyId == "root") {
            return db.collection("companies").document("placeholder_root")
        }
        if (companyId.isEmpty()) {
            // Failsafe for uninitialized session
            return db.collection("companies").document("uninitialized")
        }
        return db.collection("companies").document(companyId)
    }
    
    private fun getCollection(name: String): dev.gitlive.firebase.firestore.CollectionReference {
        val companyId = getCompanyId()
        
        val ref = if (companyId == "root") {
            db.collection(name)
        } else if (companyId.isEmpty()) {
            // CRITICAL FIX: Return a dummy collection instead of /companies/unknown_company/
            // This prevents the Firestore Panic by ensuring paths are always structurally valid (at least in depth)
            db.collection("temp_uninitialized").document("placeholder").collection(name)
        } else {
            db.collection("companies").document(companyId).collection(name)
        }
        
        KMPLogger.log("DEBUG: [Firestore] getCollection($name) on companyId: '$companyId'")
        return ref
    }

    fun getSalesRef() = getCollection("sales")
    fun getProductsRef() = getCollection("products")
    fun getServicesRef() = getCollection("services")
    fun getPurchasesRef() = getCollection("purchases")
    fun getInventoryRef() = getCollection("inventory")
    fun getStaffRef() = getCollection("staff")
    fun getReportsRef() = getCollection("reports")
    fun getDevicesRef() = getCollection("devices")
    fun getSettingsRef() = getCollection("settings")
    fun getAuditLogsRef() = getCollection("audit_logs")
    fun getReceiptsRef() = getCollection("receipts")
    fun getMpesaTransactionsRef() = getCollection("mpesa_transactions")
    fun getEtimsInvoicesRef() = getCollection("etims_invoices")
    fun getTasksRef() = getCollection("staff_tasks_v2")
    fun getPasswordResetRequestsRef() = getCollection("password_reset_requests")
    fun getPayrollRecordsRef() = getCollection("payroll_records")
    fun getEmployeesRef() = getCollection("employees")
    fun getAttendanceRecordsRef() = getCollection("attendance_records")
    fun getLeaveRecordsRef() = getCollection("leave_records")
    fun getStatutoryRulesRef() = getCollection("statutory_deduction_rules")
    fun getStatutoryBracketsRef() = getCollection("statutory_brackets")

    @Deprecated("Use getCompanyRef", ReplaceWith("getCompanyRef()"))
    fun getBusinessRef() = getCompanyRef()
}
