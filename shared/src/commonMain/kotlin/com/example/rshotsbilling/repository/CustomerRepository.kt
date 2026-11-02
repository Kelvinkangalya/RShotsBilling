package com.example.rshotsbilling.repository

import com.example.rshotsbilling.model.CustomerEntity
import com.example.rshotsbilling.utils.FirestoreManager
import com.example.rshotsbilling.utils.KMPLogger
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.gitlive.firebase.firestore.*

class CustomerRepository {
    private val db by lazy { FirestoreManager.db }
    private fun getCustomersRef() = FirestoreManager.getCompanyRef().collection("customers")

    suspend fun getCustomerByPhone(phone: String): CustomerEntity? {
        val normalizedPhone = com.example.rshotsbilling.utils.NormalizationUtils.normalizePhone(phone)
        if (normalizedPhone.isEmpty()) return null
        return try {
            val snapshot = getCustomersRef().where { "phone" equalTo normalizedPhone }.get()
            snapshot.documents.firstOrNull()?.data(CustomerEntity.serializer())
        } catch (e: Exception) {
            KMPLogger.log("ERROR: [CustomerRepository] getCustomerByPhone failed: ${e.message}")
            null
        }
    }

    suspend fun findExistingCustomer(phone: String, email: String, name: String): CustomerEntity? {
        val normalizedPhone = com.example.rshotsbilling.utils.NormalizationUtils.normalizePhone(phone)
        if (normalizedPhone.isNotEmpty()) {
            val byPhone = getCustomerByPhone(normalizedPhone)
            if (byPhone != null) return byPhone
        }

        val normalizedEmail = com.example.rshotsbilling.utils.NormalizationUtils.normalizeEmail(email)
        if (normalizedEmail.isNotEmpty()) {
            try {
                val snapshot = getCustomersRef().where { "email" equalTo normalizedEmail }.get()
                val byEmail = snapshot.documents.firstOrNull()?.data(CustomerEntity.serializer())
                if (byEmail != null) return byEmail
            } catch (e: Exception) {}
        }
        
        return null // Name matching is risky, skipping as per instructions unless explicitly requested as fallback
    }

    suspend fun addOrUpdateCustomer(customer: CustomerEntity) {
        val now = getCurrentTimestamp()
        val normalizedPhone = com.example.rshotsbilling.utils.NormalizationUtils.normalizePhone(customer.phone)
        val normalizedEmail = com.example.rshotsbilling.utils.NormalizationUtils.normalizeEmail(customer.email)
        val normalizedName = com.example.rshotsbilling.utils.NormalizationUtils.normalizeName(customer.name)
        
        val id = if (customer.id.isEmpty()) "cust_$now" else customer.id
        val finalCustomer = customer.copy(
            id = id, 
            name = normalizedName,
            phone = normalizedPhone,
            email = normalizedEmail,
            updatedAt = now
        )
        try {
            getCustomersRef().document(id).set(CustomerEntity.serializer(), finalCustomer)
        } catch (e: Exception) {
            KMPLogger.log("ERROR: [CustomerRepository] addOrUpdateCustomer failed: ${e.message}")
        }
    }

    fun getCustomersFlow(): Flow<List<CustomerEntity>> {
        return getCustomersRef().snapshots().map { snapshot ->
            snapshot.documents.map { it.data(CustomerEntity.serializer()) }
        }
    }

    suspend fun getCustomers(): List<CustomerEntity> {
        return try {
            val snapshot = getCustomersRef().get()
            snapshot.documents.map { it.data(CustomerEntity.serializer()) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun migrateFromHistory(sales: List<com.example.rshotsbilling.model.Sale>) {
        if (sales.isEmpty()) return
        
        val existingCustomers = getCustomers().associateBy { com.example.rshotsbilling.utils.NormalizationUtils.normalizePhone(it.phone) }
        val customersToUpdate = mutableMapOf<String, CustomerEntity>()

        sales.filter { it.customerPhone.isNotEmpty() }.sortedBy { it.timestamp }.forEach { sale ->
            val phone = com.example.rshotsbilling.utils.NormalizationUtils.normalizePhone(sale.customerPhone)
            if (phone.isEmpty()) return@forEach

            val current = customersToUpdate[phone] 
                ?: existingCustomers[phone] 
                ?: CustomerEntity(
                    name = com.example.rshotsbilling.utils.NormalizationUtils.normalizeName(sale.customerName),
                    phone = phone,
                    kraPin = sale.customerKraPin,
                    createdAt = sale.timestamp
                )

            val updated = current.copy(
                totalSpending = current.totalSpending + sale.total,
                numberOfPurchases = current.numberOfPurchases + 1,
                lastPurchaseDate = maxOf(current.lastPurchaseDate ?: 0L, sale.timestamp),
                updatedAt = getCurrentTimestamp()
            )
            customersToUpdate[phone] = updated
        }

        customersToUpdate.values.forEach { addOrUpdateCustomer(it) }
        KMPLogger.log("DEBUG: [CustomerRepository] Migration complete. ${customersToUpdate.size} customers processed.")
    }
}
