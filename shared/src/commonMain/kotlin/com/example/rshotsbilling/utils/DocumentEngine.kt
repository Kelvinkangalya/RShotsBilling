package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.model.Receipt

/**
 * Professional Document Engine for formatting receipts, invoices, and reports
 */
object DocumentEngine {

    fun getReceiptHeader(): String {
        return """
            ${UserSession.companyName.uppercase()}
            ${UserSession.companyMotto}
            ${UserSession.companyAddress}
            Phone: ${UserSession.companyPhone}
            Email: ${UserSession.companyEmail}
            KRA PIN: ${UserSession.kraPin}
            --------------------------------
            ${UserSession.receiptHeader}
        """.trimIndent()
    }

    fun getReceiptFooter(): String {
        return """
            --------------------------------
            ${UserSession.receiptFooter}
            Powered by ${BusinessConfig.COMPANY_NAME}
        """.trimIndent()
    }

    fun formatSaleAsText(sale: Sale): String {
        val sb = StringBuilder()
        sb.append(getReceiptHeader()).append("\n")
        sb.append("Invoice: ${UserSession.invoicePrefix}${sale.invoiceNumber}\n")
        sb.append("Date: ${KmpUtils.formatDate(sale.timestamp)}\n")
        sb.append("Customer: ${sale.customerName}\n")
        sb.append("--------------------------------\n")
        
        sale.items.forEach { item ->
            sb.append("${item.serviceName}\n")
            sb.append("${item.quantity} x ${item.price} = ${item.quantity * item.price}\n")
        }
        
        sb.append("--------------------------------\n")
        sb.append("TOTAL: ${UserSession.currency} ${sale.total}\n")
        sb.append("Payment: ${sale.paymentMethod}\n")
        
        if (sale.isEtims) {
            sb.append("eTIMS No: ${sale.etimsReceiptNumber}\n")
        }
        
        sb.append(getReceiptFooter())
        return sb.toString()
    }
}

object KmpUtils {
    fun formatDate(timestamp: Long): String {
        // Simple mock for now, actual implementation would use kotlinx-datetime
        return "2026-08-01" 
    }
}
