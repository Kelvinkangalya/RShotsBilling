package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class EtimsInvoice(
    val id: String = "",
    val saleId: String = "",
    val invoiceNumber: String = "",
    val customerName: String = "",
    val customerPin: String = "",
    val taxableAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = "Pending",
    val timestamp: Long = getCurrentTimestamp(),
    val submittedAt: Long? = null,
    val errorMessage: String? = null
)
