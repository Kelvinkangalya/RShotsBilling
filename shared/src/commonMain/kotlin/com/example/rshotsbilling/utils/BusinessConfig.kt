package com.example.rshotsbilling.utils

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Global Synchronization Tracker
 */
object SyncStatus {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    fun setSyncing(syncing: Boolean) {
        _isSyncing.value = syncing
    }
}

/**
 * User Roles for the POS system
 */
enum class UserRole {
    VENDOR,
    CEO,
    SUPER_VENDOR,
    VENDOR_ADMIN,
    ADMIN,
    MANAGER,
    SUPERVISOR,
    STAFF,
    SALES_STAFF,
    PHOTOGRAPHER,
    CYBER_AGENT,
    CONSULTANT;

    fun hierarchyLevel(): Int = when(this) {
        VENDOR, SUPER_VENDOR, VENDOR_ADMIN -> 100
        CEO -> 90
        ADMIN -> 80
        MANAGER -> 70
        SUPERVISOR -> 60
        STAFF, SALES_STAFF, PHOTOGRAPHER, CYBER_AGENT, CONSULTANT -> 50
    }

    fun isAtLeast(other: UserRole): Boolean {
        return this.hierarchyLevel() >= other.hierarchyLevel()
    }
    
    fun canManage(other: UserRole): Boolean {
        // Strict: Can only manage those below you, unless you are VENDOR/CEO
        if (this == VENDOR || this == SUPER_VENDOR || this == VENDOR_ADMIN || this == CEO) return true
        return this.hierarchyLevel() > other.hierarchyLevel()
    }
}

object UserPermissions {
    const val CAN_SELL = "CAN_SELL"
    const val CAN_BUY = "CAN_BUY"
    const val CAN_VIEW_REPORTS = "CAN_VIEW_REPORTS"
    const val CAN_APPROVE = "CAN_APPROVE"
    const val CAN_MANAGE_SYSTEM = "CAN_MANAGE_SYSTEM"
    const val CAN_MANAGE_RECEIPTS = "CAN_MANAGE_RECEIPTS"
    const val CAN_MANAGE_INVENTORY = "CAN_MANAGE_INVENTORY"
    const val CAN_VIEW_COMPLIANCE = "CAN_VIEW_COMPLIANCE"
    const val CAN_MANAGE_MPESA = "CAN_MANAGE_MPESA"
    const val CAN_MANAGE_PAYROLL = "CAN_MANAGE_PAYROLL"
    const val CAN_MANAGE_DEVICES = "CAN_MANAGE_DEVICES"
}

/**
 * Global Session to track the current user (Reactive for UI)
 */
object UserSession {
    var companyId by mutableStateOf("")
    var businessId by mutableStateOf("")
    var currentRole by mutableStateOf(UserRole.SALES_STAFF)
    var userEmail by mutableStateOf<String?>(null)
    var displayName by mutableStateOf<String?>(null)
    var permissions by mutableStateOf<List<String>>(emptyList())
    var isDeviceActivated by mutableStateOf(false)
    var isTemporaryPassword by mutableStateOf(false)
    var setupComplete by mutableStateOf(false)
    var currentDeviceSerial by mutableStateOf("")
    var licenseStatus by mutableStateOf("Trial")
    
    // Configuration Flags
    var isActivationBypassEnabled by mutableStateOf(true) // Phase 1 Bypass

    // Branding (Defaults to RShots Vendor Branding)
    var companyName by mutableStateOf("RSHOTS CO LTD")
    var companyLogo by mutableStateOf("")
    var companyMotto by mutableStateOf("Stability & Precision")
    var companyWebsite by mutableStateOf("www.rshots.co.ke")
    var companyEmail by mutableStateOf("rshotsco@gmail.com")
    var companyPhone by mutableStateOf("0792342140")
    var companyAddress by mutableStateOf("Nairobi, Kenya")
    var primaryColor by mutableStateOf(Color(0xFF19F500)) // Neon Green
    var secondaryColor by mutableStateOf(Color(0xFFA41010)) // Red
    var kraPin by mutableStateOf("..")
    var receiptHeader by mutableStateOf("")
    var receiptFooter by mutableStateOf("")
    var companySignature by mutableStateOf("")
    var companyStamp by mutableStateOf("")
    var authorizedPerson by mutableStateOf("")
    var managerName by mutableStateOf("")

    // Initialization State
    var isSessionReady by mutableStateOf(false)

    // Prefixes
    var invoicePrefix by mutableStateOf("INV-")
    var receiptPrefix by mutableStateOf("REC-")
    var quotationPrefix by mutableStateOf("QTN-")
    var purchasePrefix by mutableStateOf("PUR-")
    
    // Tax & Compliance Settings (Fetched from Company)
    var vatRate by mutableStateOf(0.16)
    var currency by mutableStateOf("KES")
    var etimsConfig by mutableStateOf<Map<String, String>>(emptyMap())
    var mpesaConfig by mutableStateOf<Map<String, String>>(emptyMap())

    fun updateFromStaff(staff: com.example.rshotsbilling.model.StaffMember) {
        companyId = staff.businessId
        businessId = staff.businessId
        displayName = staff.displayName
        currentRole = staff.toUserRole()
        permissions = staff.permissions
        isTemporaryPassword = staff.isTemporaryPassword
        setupComplete = staff.setupComplete
        
        // If it's a staff member without a company ID (rare but possible during init)
        if (businessId.isEmpty()) {
            isSessionReady = false
        }
    }

    fun updateFromCompany(company: com.example.rshotsbilling.model.Company) {
        companyId = company.id
        businessId = company.id
        companyName = company.name
        companyLogo = company.logo
        companyMotto = company.companyMotto
        companyWebsite = company.website
        companyEmail = company.email
        companyPhone = company.phone
        companyAddress = company.physicalAddress
        kraPin = company.kraPin
        currency = company.theme.currency
        receiptHeader = company.theme.receiptHeader
        receiptFooter = company.theme.receiptFooter
        companySignature = company.signatureUrl
        companyStamp = company.stampUrl
        authorizedPerson = company.authorizedPerson
        managerName = company.managerName

        invoicePrefix = company.invoicePrefix
        receiptPrefix = company.receiptPrefix
        quotationPrefix = company.quotationPrefix
        purchasePrefix = company.purchasePrefix
        
        try {
            primaryColor = Color(parseColor(company.theme.primaryColor))
            secondaryColor = Color(parseColor(company.theme.accentColor))
        } catch (e: Exception) {
            println("DEBUG: Failed to parse company colors: ${e.message}")
        }

        isSessionReady = true
    }

    private fun parseColor(colorString: String): Long {
        return try {
            if (colorString.startsWith("#")) {
                val hex = colorString.substring(1)
                val fullHex = if (hex.length == 6) "FF$hex" else hex
                fullHex.toLong(16)
            } else {
                0xFF0D47A1L // Default Blue
            }
        } catch (e: Exception) {
            0xFF0D47A1L
        }
    }

    fun hasPermission(permission: String): Boolean {
        if (permission.isEmpty()) return true
        val email = userEmail ?: return false
        // Vendor Account override: ensure the platform owner always has full access
        if (email.equals(BusinessConfig.SUPER_VENDOR_EMAIL, ignoreCase = true)) return true
        
        // Strict Assignment: Rights are ONLY granted if explicitly assigned by Admin
        return permissions.contains(permission)
    }

    fun clear() {
        companyId = ""
        businessId = ""
        userEmail = null
        displayName = null
        permissions = emptyList()
        currentRole = UserRole.SALES_STAFF
        isDeviceActivated = false
        licenseStatus = "Trial"
        isSessionReady = false
        
        // Reset Branding to Vendor Defaults
        companyName = "RSHOTS CO LTD"
        companyLogo = ""
        companyMotto = "Stability & Precision"
        companyWebsite = "www.rshots.co.ke"
        companyEmail = "rshotsco@gmail.com"
        companyPhone = "0792342140"
        companyAddress = "Nairobi, Kenya"
        primaryColor = Color(0xFF19F500)
        secondaryColor = Color(0xFFA41010)
        kraPin = ".."
    }
}

/**
 * Centralized configuration for the POS branding and business rules.
 */
object BusinessConfig {
    const val SUPER_VENDOR_EMAIL = "rauphinkelvin8@gmail.com"
    val COMPANY_NAME: String 
        get() {
            val email = UserSession.userEmail
            return if (email?.equals(SUPER_VENDOR_EMAIL, ignoreCase = true) == true || UserSession.companyId.isEmpty()) {
                "RSHOTS CO LTD"
            } else {
                UserSession.companyName.ifEmpty { "RSHOTS CO LTD" }
            }
        }
    val BUSINESS_KRA_PIN get() = UserSession.kraPin
    val COMPANY_EMAIL get() = UserSession.companyEmail.ifEmpty { "rshotsco@gmail.com" }
    val COMPANY_PHONE get() = UserSession.companyPhone.ifEmpty { "0792342140" }
    const val TILL_NUMBER = "8323884"
    
    val PrimaryColor get() = UserSession.primaryColor
    val SecondaryColor get() = UserSession.secondaryColor
    val SuccessColor = Color(0xFF00E676) // Vivid Green
    val ErrorColor = Color(0xFFFF1744)   // Electric Red
    val WarningColor = Color(0xFFFFD600) // Bright Gold

    const val LOW_STOCK_THRESHOLD = 5
    val CURRENCY get() = UserSession.currency
    val VAT_RATE get() = UserSession.vatRate

    val EXPENSE_CATEGORIES = listOf(
        "Inventory/Stock", "Rent", "Salaries", "Utilities (Water/Power)",
        "Marketing", "Transport", "Internet/Cyber", "Maintenance", "Other"
    )
}
