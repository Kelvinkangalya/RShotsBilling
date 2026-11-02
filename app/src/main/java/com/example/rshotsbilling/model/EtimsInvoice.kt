package com.example.rshotsbilling.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "etims_invoices")
data class LocalEtimsInvoice(
    @PrimaryKey
    val id: String,
    val invoiceNumber: String,
    val totalAmount: Double,
    val taxAmount: Double,
    val status: String,
    val timestamp: Long,
    val responsePayload: String = ""
)
