package com.example.rshotsbilling.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rshotsbilling.model.Company
import com.example.rshotsbilling.model.StaffMember
import com.example.rshotsbilling.utils.*
import com.example.rshotsbilling.repository.StaffRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val nextScreen: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val authService get() = PlatformAuth.service

    /**
     * Attempts to restore an existing session.
     * Returns the target screen to navigate to.
     */
    suspend fun restoreSession(): String {
        val email = authService.currentUserEmail
        val uid = authService.currentUserId
        
        if (email == null || uid == null) return "login"
        
        KMPLogger.log("[LoginViewModel] Restoring session for $email")
        UserSession.userEmail = email
        
        return try {
            val target = withTimeoutOrNull(8000) {
                processProfileLoading(email, uid)
            } ?: "login"

            // Performance & Context Diagnostics (Super Vendor Only)
            if (email.equals(BusinessConfig.SUPER_VENDOR_EMAIL, ignoreCase = true)) {
                viewModelScope.launch {
                    runDataDiagnostics()
                }
            }

            target
        } catch (e: Exception) {
            KMPLogger.log("[LoginViewModel] Session restoration error: ${e.message}")
            "login"
        }
    }

    fun login(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = LoginUiState.Error("Please fill all fields")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            KMPLogger.log("[PERF] Authentication START for $email")
            val authStart = getCurrentTimestamp()

            try {
                // 1. Firebase Authentication
                val authId = authService.signIn(email, password)
                KMPLogger.log("[PERF] Authentication END: ${getCurrentTimestamp() - authStart} ms. UID=$authId")

                // Reset session for fresh load
                UserSession.userEmail = email
                UserSession.companyId = ""
                UserSession.businessId = ""

                // 2. Profile Loading
                KMPLogger.log("[PERF] Profile Loading START")
                val profileStart = getCurrentTimestamp()
                val nextScreen = processProfileLoading(email, authId)
                KMPLogger.log("[PERF] Profile Loading END: ${getCurrentTimestamp() - profileStart} ms. Next screen: $nextScreen")
                
                _uiState.value = LoginUiState.Success(nextScreen)

                // 3. Performance & Context Diagnostics (Super Vendor Only)
                if (email.equals(BusinessConfig.SUPER_VENDOR_EMAIL, ignoreCase = true)) {
                    viewModelScope.launch {
                        runDataDiagnostics()
                    }
                }

                // 4. Background Sync
                viewModelScope.launch {
                    try {
                        delay(1000)
                        val identityViewModel = IdentityViewModel()
                        identityViewModel.syncDevice(authId)
                    } catch (e: Exception) {
                        KMPLogger.log("DEBUG: [LoginViewModel] Background sync failed: ${e.message}")
                    }
                }

            } catch (e: Exception) {
                KMPLogger.log("[PERF] Login failed: ${e.message}")
                _uiState.value = LoginUiState.Error("Auth Error: ${e.message}. Check Internet.")
            }
        }
    }

    fun requestPasswordReset(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _uiState.value = LoginUiState.Error("Please enter a valid email to request reset.")
            return
        }

        viewModelScope.launch {
            try {
                val db = FirestoreManager.db
                val staffSnapshot = db.collection("staff").where { "email" equalTo email }.get()
                
                if (staffSnapshot.documents.isNotEmpty()) {
                    val staff = staffSnapshot.documents.first().data(StaffMember.serializer())
                    val repo = StaffRepository()
                    repo.requestPasswordReset(email, staff.businessId)
                    _uiState.value = LoginUiState.Error("Reset request submitted to your administrator.") 
                } else {
                    _uiState.value = LoginUiState.Error("Account not found.")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Failed to submit request: ${e.message}")
            }
        }
    }

    private suspend fun runDataDiagnostics() {
        val db = FirestoreManager.db
        val collections = listOf("sales", "products", "services", "customers", "purchases", "receipts", "staff", "payroll_records")
        
        KMPLogger.log("[DIAGNOSTIC] Starting Root vs Company Path Audit...")
        
        // 1. Check Root Collections
        for (col in collections) {
            try {
                val start = getCurrentTimestamp()
                val snapshot = db.collection(col).get()
                val duration = getCurrentTimestamp() - start
                KMPLogger.log("[DATA_PATH] Role: VENDOR | CompanyId: root | Collection: $col | Resolved path: /$col | Documents: ${snapshot.documents.size} | Duration: $duration ms")
            } catch (e: Exception) {
                KMPLogger.log("[DATA_PATH] FAILED Root query for $col: ${e.message}")
            }
        }
        
        // 2. Check Company Scoped Collections (Try to find any companies first)
        try {
            val companiesSnapshot = db.collection("companies").get()
            KMPLogger.log("[DIAGNOSTIC] Found ${companiesSnapshot.documents.size} company documents.")
            
            for (companyDoc in companiesSnapshot.documents.take(3)) { // Limit to first 3 companies for brevity
                val cid = companyDoc.id
                for (col in collections) {
                    try {
                        val start = getCurrentTimestamp()
                        val snapshot = db.collection("companies").document(cid).collection(col).get()
                        val duration = getCurrentTimestamp() - start
                        KMPLogger.log("[DATA_PATH] Role: VENDOR | CompanyId: $cid | Collection: $col | Resolved path: /companies/$cid/$col | Documents: ${snapshot.documents.size} | Duration: $duration ms")
                    } catch (e: Exception) {
                        KMPLogger.log("[DATA_PATH] FAILED Scoped query for $cid/$col: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            KMPLogger.log("[DIAGNOSTIC] FAILED to fetch companies for diagnostic: ${e.message}")
        }
        
        KMPLogger.log("[DIAGNOSTIC] Data Path Audit Complete.")
    }

    private suspend fun processProfileLoading(email: String, authId: String): String {
        val db = FirestoreManager.db
        
        // 1. SUPER VENDOR CHECK
        if (email.equals(BusinessConfig.SUPER_VENDOR_EMAIL, ignoreCase = true)) {
            KMPLogger.log("[LoginViewModel] Super Vendor detected - Restoring Vendor Branding")
            UserSession.displayName = "SUPER ADMIN"
            UserSession.currentRole = UserRole.VENDOR
            UserSession.companyId = ""
            UserSession.businessId = ""
            UserSession.isDeviceActivated = true
            
            // LOCK VENDOR BRANDING
            UserSession.companyName = "RSHOTS CO LTD"
            UserSession.primaryColor = androidx.compose.ui.graphics.Color(0xFF19F500)
            UserSession.secondaryColor = androidx.compose.ui.graphics.Color(0xFFA41010)
            UserSession.companyMotto = "Stability & Precision"
            UserSession.companyLogo = "" // Placeholder for vendor logo if needed
            
            UserSession.isSessionReady = true

            return "vendor_dashboard"
        }

        // 2. STAFF PROFILE FETCH
        KMPLogger.log("[LoginViewModel] Fetching staff profile...")
        var staff: StaffMember? = null
        
        try {
            val doc = withTimeoutOrNull(10000) {
                db.collection("staff").document(authId).get()
            }
            if (doc != null && doc.exists) {
                staff = doc.data(StaffMember.serializer())
            }
            
            if (staff == null) {
                KMPLogger.log("[LoginViewModel] Falling back to email search")
                val emailQuery = withTimeoutOrNull(10000) {
                    db.collection("staff").where { "email" equalTo email }.get()
                }
                if (emailQuery != null && emailQuery.documents.isNotEmpty()) {
                    staff = emailQuery.documents.first().data(StaffMember.serializer())
                }
            }
        } catch (e: Exception) {
            KMPLogger.log("[LoginViewModel] Firestore error: ${e.message}")
        }

        if (staff != null) {
            KMPLogger.log("[LoginViewModel] Profile loaded for ${staff.displayName}")
            UserSession.updateFromStaff(staff)
            UserSession.isDeviceActivated = true
            
            if (staff.isTemporaryPassword) return "password_change"
            if (!staff.setupComplete && (staff.role == "ADMIN" || staff.role == "CEO")) return "company_setup"
            
            // 3. COMPANY PROFILE FETCH (Consolidated post-login flow)
            val businessId = UserSession.businessId
            if (businessId.isNotEmpty()) {
                try {
                    KMPLogger.log("[LoginViewModel] Fetching company doc: $businessId")
                    val companyDoc = withTimeoutOrNull(10000) {
                        db.collection("companies").document(businessId).get()
                    }
                    if (companyDoc != null && companyDoc.exists) {
                        val company = companyDoc.data(Company.serializer())
                        UserSession.updateFromCompany(company)
                        KMPLogger.log("[LoginViewModel] Company branding loaded: ${company.name}")
                    } else {
                        KMPLogger.log("[LoginViewModel] Company doc NOT FOUND. Marking session ready with defaults.")
                        UserSession.isSessionReady = true
                    }
                } catch (ce: Exception) {
                    KMPLogger.log("[LoginViewModel] Company fetch failed: ${ce.message}")
                    UserSession.isSessionReady = true // Failsafe
                }
            } else {
                // If no business ID, we are ready but in a limited state
                UserSession.isSessionReady = true
            }

            // Determine final destination
            return if (UserSession.isActivationBypassEnabled || UserSession.isDeviceActivated) {
                "dashboard"
            } else {
                "activation"
            }
        }

        KMPLogger.log("[LoginViewModel] No staff profile found. Defaulting to activation check.")
        UserSession.isDeviceActivated = true // Fallback
        return "dashboard"
    }
}
