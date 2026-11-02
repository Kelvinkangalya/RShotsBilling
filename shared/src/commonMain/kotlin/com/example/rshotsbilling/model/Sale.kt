package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    var id: String = "",
    var invoiceNumber: String = "",
    var customerName: String = "",
    var customerPhone: String = "",
    var customerKraPin: String = "",
    var total: Double = 0.0,
    var paymentMethod: String = "M-Pesa",
    var mpesaCode: String = "",
    var pdfPath: String = "",
    var servedBy: String = "", 
    var status: String = "PENDING", 
    var auditReason: String = "",
    var isApproved: Boolean = false, 
    var timestamp: Long = getCurrentTimestamp(),
    var isEtims: Boolean = false,
    var etimsStatus: String = "NONE",
    var etimsReceiptNumber: String = "",
    var etimsQrData: String = "",
    var etimsSubmissionDate: Long? = null,
    var vatAmount: Double = 0.0,
    var taxableAmount: Double = 0.0,
    var taxAmount: Double = 0.0,
    var discount: Double = 0.0,
    var items: List<SaleItem> = emptyList()
)
