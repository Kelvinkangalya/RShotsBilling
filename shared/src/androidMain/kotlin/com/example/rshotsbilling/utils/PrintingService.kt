package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Receipt

actual object PrintingService {
    actual fun printReceipt(receipt: Receipt) {
        KMPLogger.log("DEBUG: [Printing] Android Printing requested for ${receipt.invoiceNumber}")
        // TODO: Implement Android printing logic (e.g., using Android PrintManager)
    }
}
