package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class Purchase(
    val id: String = "",
    val title: String = "",
    val itemName: String = "",
    val amount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val category: String = "",
    val supplier: String = "",
    val paymentMethod: String = "Cash",
    val notes: String = "",
    val recordedBy: String = "",
    val quantity: Double = 1.0, 
    val status: String = "PENDING", 
    val auditReason: String = "",
    val timestamp: Long = getCurrentTimestamp(),
    val date: Long = getCurrentTimestamp()
)
