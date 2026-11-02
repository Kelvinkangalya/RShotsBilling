package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale

expect object PlatformReceiptUtils {
    fun openReceiptPdf(sale: Sale)
    fun shareReceiptPdf(sale: Sale)
}
