package com.example.rshotsbilling.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.rshotsbilling.data.local.Converters

@Entity(tableName = "sales")
data class LocalSale(
    @PrimaryKey
    val id: String,
    val invoiceNumber: String,
    val customerName: String,
    val customerPhone: String,
    val customerKraPin: String,
    val total: Double,
    val taxAmount: Double = 0.0,
    val discount: Double = 0.0,
    val paymentMethod: String,
    val status: String = "PAID",
    val servedBy: String = "",
    val timestamp: Long,
    val isEtims: Boolean = false,
    val etimsStatus: String = "NONE",
    val auditReason: String = "",
    val mpesaCode: String = "",
    val pdfPath: String = "",
    val isApproved: Boolean = false,
    val etimsReceiptNumber: String = "",
    val etimsQrData: String = "",
    val etimsSubmissionDate: Long? = null,
    val vatAmount: Double = 0.0,
    val taxableAmount: Double = 0.0,
    @Ignore
    val items: List<SaleItem> = emptyList()
) {
    constructor(
        id: String,
        invoiceNumber: String,
        customerName: String,
        customerPhone: String,
        customerKraPin: String,
        total: Double,
        taxAmount: Double,
        discount: Double,
        paymentMethod: String,
        status: String,
        servedBy: String,
        timestamp: Long,
        isEtims: Boolean,
        etimsStatus: String,
        auditReason: String,
        mpesaCode: String,
        pdfPath: String,
        isApproved: Boolean,
        etimsReceiptNumber: String,
        etimsQrData: String,
        etimsSubmissionDate: Long?,
        vatAmount: Double,
        taxableAmount: Double
    ) : this(
        id,
        invoiceNumber,
        customerName,
        customerPhone,
        customerKraPin,
        total,
        taxAmount,
        discount,
        paymentMethod,
        status,
        servedBy,
        timestamp,
        isEtims,
        etimsStatus,
        auditReason,
        mpesaCode,
        pdfPath,
        isApproved,
        etimsReceiptNumber,
        etimsQrData,
        etimsSubmissionDate,
        vatAmount,
        taxableAmount,
        emptyList()
    )
}
