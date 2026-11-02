package com.example.rshotsbilling.model

data class AndroidReceipt(
    val id: String,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String = "",
    val customerKraPin: String = "",
    val totalAmount: Double,
    val paymentMethod: String,
    val date: Long
)
