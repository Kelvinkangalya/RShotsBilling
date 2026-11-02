package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.InstallationProfile
import com.example.rshotsbilling.utils.FirestoreManager
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DeviceRepository {
    private val db: FirebaseFirestore by lazy { FirestoreManager.db }
    private val devicesRef get() = db.collection("DeviceRegistry")

    fun getAllDevicesFlow(): Flow<List<InstallationProfile>> {
        return devicesRef.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(InstallationProfile.serializer()) }
        }
    }

    fun getDevicesForCompanyFlow(companyId: String): Flow<List<InstallationProfile>> {
        return devicesRef.where { "businessId" equalTo companyId }.snapshots().map { snapshot ->
            snapshot.documents.map { it.data(InstallationProfile.serializer()) }
        }
    }

    suspend fun renameDevice(installationId: String, newName: String) {
        println("DEBUG: [LOGIN] Attempting Firestore path: DeviceRegistry/$installationId")
        devicesRef.document(installationId).update("deviceName" to newName)
    }

    suspend fun setDeviceStatus(installationId: String, active: Boolean) {
        println("DEBUG: [LOGIN] Attempting Firestore path: DeviceRegistry/$installationId")
        devicesRef.document(installationId).update("active" to active)
    }

    suspend fun approveDevice(installationId: String, approved: Boolean) {
        println("DEBUG: [LOGIN] Attempting Firestore path: DeviceRegistry/$installationId")
        devicesRef.document(installationId).update("approved" to approved)
    }

    suspend fun deleteDevice(installationId: String) {
        println("DEBUG: [LOGIN] Attempting Firestore path: DeviceRegistry/$installationId")
        devicesRef.document(installationId).delete()
    }
}
