package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale

actual object PlatformReceiptUtils {
    actual fun openReceiptPdf(sale: Sale) {
        // Not applicable for web right now
    }

    actual fun shareReceiptPdf(sale: Sale) {
        // Not applicable for web
    }
}
