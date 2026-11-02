package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale
import java.awt.Desktop
import java.io.File
import kotlinx.coroutines.*

actual object PlatformReceiptUtils {
    actual fun openReceiptPdf(sale: Sale) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logo = BrandingManager.getCompanyLogoBytes()
                val bytes = PdfReceiptGenerator.generateReceipt(logo, sale)
                if (bytes != null) {
                    val path = ReportPlatform.saveReport(bytes, "Receipt_${sale.invoiceNumber}.pdf", "Receipts")
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(File(path))
                    }
                }
            } catch (e: Exception) {}
        }
    }

    actual fun shareReceiptPdf(sale: Sale) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logo = BrandingManager.getCompanyLogoBytes()
                val bytes = PdfReceiptGenerator.generateReceipt(logo, sale)
                if (bytes != null) {
                    val path = ReportPlatform.saveReport(bytes, "Receipt_${sale.invoiceNumber}.pdf", "Receipts")
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(File(path).parentFile.toURI())
                    }
                }
            } catch (e: Exception) {}
        }
    }
}
