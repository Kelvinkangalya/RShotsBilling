package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.Service
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.KMPLogger
import com.example.rshotsbilling.utils.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*
import kotlinx.datetime.Clock

class ServiceRepository {

    private val db by lazy {
        KMPLogger.log("DEBUG: [Repository] ServiceRepository initialized.")
        FirestoreManager.db
    }
    private fun getServicesRef() = FirestoreManager.getServicesRef()

    suspend fun addService(service: Service) {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
        val id = if (service.id.isEmpty() || service.id == "ser_0") "ser_$now" else service.id
        val newService = service.copy(id = id)
        
        try {
            val ref = getServicesRef()
            KMPLogger.log("DEBUG: [Repository] Attempting addService. ID: $id | Path: ${FirestoreManager.getCompanyId()}/products")
            ref.document(id).set(Service.serializer(), newService)
            KMPLogger.log("DEBUG: [Repository] addService SUCCESS for $id")
        } catch (e: Exception) {
            KMPLogger.log("ERROR: [Repository] addService FAILED for $id: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    fun getServicesFlow(): Flow<List<Service>> {
        val path = "companies/${FirestoreManager.getCompanyId()}/products"
        KMPLogger.log("DEBUG: [Query] Collection: $path | User: ${UserSession.userEmail}")
        return getServicesRef().snapshots().map { snapshot ->
            snapshot.documents.map { it.data(Service.serializer()) }
        }
    }

    suspend fun getServices(): List<Service> {
        val startTime = com.example.rshotsbilling.utils.getCurrentTimestamp()
        val path = if (FirestoreManager.getCompanyId() == "root") "/services" else "companies/${FirestoreManager.getCompanyId()}/products"
        KMPLogger.log("[PERF] Services query START. Path: $path")
        
        val snapshot = getServicesRef().get()
        val duration = com.example.rshotsbilling.utils.getCurrentTimestamp() - startTime
        val count = snapshot.documents.size
        
        KMPLogger.log("[PERF] Services query END: $duration ms. Documents: $count")

        return snapshot.documents.map {
            it.data(Service.serializer())
        }
    }

    suspend fun deleteService(id: String) {
        if (id.isEmpty()) return
        KMPLogger.log("DEBUG: [Repository] deleteService: $id")
        getServicesRef().document(id).delete()
    }
}
