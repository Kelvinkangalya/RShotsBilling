package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class MpesaTransaction(
    val id: String = "",
    val saleId: String = "",
    val phoneNumber: String = "",
    val amount: Double = 0.0,
    val transactionId: String? = null,
    val status: String = "PENDING", 
    val createdAt: Long = getCurrentTimestamp(),
    val updatedAt: Long = getCurrentTimestamp(),
    val errorMessage: String? = null
)
