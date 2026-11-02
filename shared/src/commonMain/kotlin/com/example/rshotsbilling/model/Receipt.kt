package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class Receipt(
    val id: String = "",
    val saleId: String = "",
    val invoiceNumber: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerKraPin: String = "",
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",
    val pdfPath: String = "",
    val date: Long = getCurrentTimestamp()
)
