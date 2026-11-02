package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.InstallationProfile
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.UserSession
import kotlinx.datetime.Clock

class IdentityRepository {
    private val db by lazy {
        println("DEBUG: IdentityRepository initialized.")
        FirestoreManager.db
    }

    suspend fun registerOrUpdateDevice(profile: InstallationProfile): Boolean {
        println("DEBUG: [LOGIN] registerOrUpdateDevice started.")
        val companyId = if (UserSession.companyId.isNotEmpty()) UserSession.companyId else UserSession.businessId
        println("DEBUG: [LOGIN] companyId for sync: '$companyId'")
        if (companyId.isEmpty()) {
            println("DEBUG: [LOGIN] Skipping device sync - No companyId found.")
            return false
        }

        return try {
            val now = Clock.System.now().toEpochMilliseconds()
            val updatedProfile = profile.copy(
                lastSync = now,
                businessId = companyId,
                currentUser = UserSession.displayName ?: "",
                firebaseUid = UserSession.userEmail ?: ""
            )
            
            println("DEBUG: [Repository] Firestore Upload - Installation ID: ${updatedProfile.installationId}")
            println("DEBUG: [LOGIN] Attempting Firestore path: DeviceRegistry/${updatedProfile.installationId}")
            
            // 1. Global Registry (for Vendor)
            db.collection("DeviceRegistry")
                .document(updatedProfile.installationId)
                .set(InstallationProfile.serializer(), updatedProfile)
            
            // 2. Company Registry (for Isolation)
            if (companyId.isNotEmpty()) {
                println("DEBUG: [LOGIN] Attempting Firestore path: companies/$companyId/devices/${updatedProfile.installationId}")
                db.collection("companies")
                    .document(companyId)
                    .collection("devices")
                    .document(updatedProfile.installationId)
                    .set(InstallationProfile.serializer(), updatedProfile)
            }
            
            println("DEBUG: [Repository] Sync Success - Installation ID: ${updatedProfile.installationId}")
            true
        } catch (e: Exception) {
            println("DEBUG: [Repository] Sync Failure - ${e.message}")
            e.printStackTrace()
            false
        }
    }
}
