package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale

actual object PlatformReceiptUtils {
    var openPdfCallback: ((Sale) -> Unit)? = null
    var sharePdfCallback: ((Sale) -> Unit)? = null

    actual fun openReceiptPdf(sale: Sale) {
        openPdfCallback?.invoke(sale)
    }

    actual fun shareReceiptPdf(sale: Sale) {
        sharePdfCallback?.invoke(sale)
    }
}
