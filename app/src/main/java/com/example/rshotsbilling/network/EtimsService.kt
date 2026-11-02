package com.example.rshotsbilling.network

import com.example.rshotsbilling.model.Sale
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class AndroidEtimsResponse(
    val success: Boolean,
    val receiptNumber: String? = null,
    val qrData: String? = null,
    val message: String? = null
)

interface AndroidEtimsService {
    @POST("etims/submit")
    suspend fun submitInvoice(@Body sale: Sale): AndroidEtimsResponse
}

class AndroidMockEtimsService : AndroidEtimsService {
    override suspend fun submitInvoice(sale: Sale): AndroidEtimsResponse {
        // Simulate network delay
        kotlinx.coroutines.delay(1000)
        return AndroidEtimsResponse(
            success = true,
            receiptNumber = "KRA-${System.currentTimeMillis()}",
            qrData = "MOCK-QR-DATA-${sale.invoiceNumber}",
            message = "Mock Submission Successful"
        )
    }
}

class AndroidKRAEtimsService(private val apiUrl: String) : AndroidEtimsService {
    override suspend fun submitInvoice(sale: Sale): AndroidEtimsResponse {
        // Placeholder for future KRA implementation
        // This will eventually use Retrofit to call the real apiUrl
        return AndroidEtimsResponse(
            success = false,
            message = "Real KRA Integration not implemented yet. Switch to Mock Mode in settings."
        )
    }
}
