package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.*
import com.example.rshotsbilling.utils.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LicensingRepository {
    private val db by lazy {
        println("DEBUG: LicensingRepository initialized.")
        FirestoreManager.db
    }

    suspend fun getBusiness(businessId: String): Business? {
        if (businessId.isEmpty()) return null
        return try {
            println("DEBUG: [Licensing] Attempting Firestore path: companies/$businessId")
            val doc = db.collection("companies").document(businessId).get()
            if (doc.exists) doc.data<Business>() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLicense(businessId: String): License? {
        return try {
            val snapshot = db.collection("licenses")
                .where { "businessId" equalTo businessId }
                .get()
            snapshot.documents.firstOrNull()?.data<License>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun registerDevice(device: DeviceRegistration): Boolean {
        if (device.businessId.isEmpty() || device.deviceId.isEmpty()) return false
        return try {
            db.collection("companies")
                .document(device.businessId)
                .collection("devices")
                .document(device.deviceId)
                .set(DeviceRegistration.serializer(), device)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkDeviceApproval(businessId: String, deviceId: String): String {
        if (businessId.isEmpty() || deviceId.isEmpty()) return "Unknown"
        return try {
            val doc = db.collection("companies")
                .document(businessId)
                .collection("devices")
                .document(deviceId)
                .get()
            if (doc.exists) {
                doc.data<DeviceRegistration>().status
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    suspend fun activateBusiness(activationCode: String): Boolean {
        return try {
            val snapshot = db.collection("licenses")
                .where { "activationCode" equalTo activationCode }
                .get()
            
            val licenseDoc = snapshot.documents.firstOrNull() ?: return false
            val license = licenseDoc.data<License>()
            
            if (license.status != "Active") return false
            
            // Update business status
            if (license.businessId.isNotEmpty()) {
                db.collection("companies").document(license.businessId).update("status" to "Active")
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun getPendingDeviceRequests(businessId: String): Flow<List<DeviceRegistration>> {
        if (businessId.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return db.collection("companies")
            .document(businessId)
            .collection("devices")
            .where { "status" equalTo "Pending" }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data(DeviceRegistration.serializer()) }
            }
    }

    suspend fun approveDevice(businessId: String, deviceId: String): Boolean {
        if (businessId.isEmpty() || deviceId.isEmpty()) return false
        return try {
            db.collection("companies")
                .document(businessId)
                .collection("devices")
                .document(deviceId)
                .update("status" to "Approved")
            true
        } catch (e: Exception) {
            false
        }
    }
}
