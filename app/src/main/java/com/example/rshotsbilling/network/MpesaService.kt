package com.example.rshotsbilling.network

data class AndroidMpesaResponse(
    val success: Boolean,
    val transactionId: String? = null,
    val message: String? = null,
    val isPending: Boolean = false
)

interface AndroidMpesaService {
    suspend fun initiatePayment(phone: String, amount: Double, reference: String): AndroidMpesaResponse
    suspend fun checkTransactionStatus(transactionId: String): AndroidMpesaResponse
}

class AndroidMockMpesaService : AndroidMpesaService {
    override suspend fun initiatePayment(phone: String, amount: Double, reference: String): AndroidMpesaResponse {
        // Simulate STK Push delay
        kotlinx.coroutines.delay(2000)
        return AndroidMpesaResponse(
            success = true,
            transactionId = "MPESA-${System.currentTimeMillis()}",
            message = "STK Push Sent Successfully",
            isPending = true // Mocking that it starts as pending until "user" confirms
        )
    }

    override suspend fun checkTransactionStatus(transactionId: String): AndroidMpesaResponse {
        kotlinx.coroutines.delay(1000)
        return AndroidMpesaResponse(
            success = true,
            transactionId = transactionId,
            message = "Payment Confirmed Successfully"
        )
    }
}

class AndroidDarajaMpesaService(
    private val shortcode: String,
    private val consumerKey: String,
    private val consumerSecret: String,
    private val passkey: String,
    private val environment: String
) : AndroidMpesaService {
    override suspend fun initiatePayment(phone: String, amount: Double, reference: String): AndroidMpesaResponse {
        // Placeholder for real Safaricom Daraja API logic
        return AndroidMpesaResponse(
            success = false,
            message = "Production Mode (Daraja) not yet implemented. Use Mock Mode in settings."
        )
    }

    override suspend fun checkTransactionStatus(transactionId: String): AndroidMpesaResponse {
        return AndroidMpesaResponse(
            success = false,
            message = "Daraja Status Check not implemented."
        )
    }
}
