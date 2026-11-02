package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class SupplierEntity(
    var id: String = "",
    var name: String = "",
    var contactPerson: String = "",
    var phone: String = "",
    var email: String = "",
    var address: String = "",
    var kraPin: String = "",
    var notes: String = "",
    var lastPurchaseDate: Long? = null,
    var totalSpent: Double = 0.0,
    var outstandingBalance: Double = 0.0,
    var timestamp: Long = getCurrentTimestamp()
)
