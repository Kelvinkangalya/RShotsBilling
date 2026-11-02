package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Receipt

actual object PrintingService {
    actual fun printReceipt(receipt: Receipt) {
        KMPLogger.log("DEBUG: [Printing] PC Printing requested for ${receipt.invoiceNumber}")
        // TODO: Implement Desktop printing logic (e.g., using Java Print Service)
    }
}
