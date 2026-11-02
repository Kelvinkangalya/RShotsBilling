package com.example.rshotsbilling.network

import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data class EtimsResponse(
    val success: Boolean,
    val receiptNumber: String? = null,
    val qrData: String? = null,
    val message: String? = null
)

interface EtimsService {
    suspend fun submitInvoice(sale: Sale): EtimsResponse
}

class MockEtimsService : EtimsService {
    override suspend fun submitInvoice(sale: Sale): EtimsResponse {
        // Simulate network delay
        delay(1000)
        return EtimsResponse(
            success = true,
            receiptNumber = "KRA-${getCurrentTimestamp()}",
            qrData = "MOCK-QR-DATA-${sale.invoiceNumber}",
            message = "Mock Submission Successful"
        )
    }
}

class KRAEtimsService(private val apiUrl: String) : EtimsService {
    override suspend fun submitInvoice(sale: Sale): EtimsResponse {
        // Placeholder for future KRA implementation using Ktor
        return EtimsResponse(
            success = false,
            message = "Real KRA Integration not implemented yet. Switch to Mock Mode in settings."
        )
    }
}
