package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Receipt

actual object PrintingService {
    actual fun printReceipt(receipt: Receipt) {
        KMPLogger.log("DEBUG: [Printing] JS Printing requested for ${receipt.invoiceNumber}")
        // TODO: Implement JS printing logic (e.g., window.print())
    }
}
