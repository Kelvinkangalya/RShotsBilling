package com.example.rshotsbilling.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mpesa_transactions")
data class LocalMpesaTransaction(
    @PrimaryKey
    val id: String,
    val receiptNumber: String,
    val phoneNumber: String,
    val amount: Double,
    val status: String,
    val createdAt: Long,
    val transactionId: String? = null
)
