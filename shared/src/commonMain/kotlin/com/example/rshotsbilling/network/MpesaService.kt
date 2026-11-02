package com.example.rshotsbilling.network

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
data class MpesaResponse(
    val success: Boolean,
    val transactionId: String? = null,
    val message: String? = null,
    val isPending: Boolean = false
)

interface MpesaService {
    suspend fun initiatePayment(phone: String, amount: Double, reference: String): MpesaResponse
    suspend fun checkTransactionStatus(transactionId: String): MpesaResponse
}

class MockMpesaService : MpesaService {
    override suspend fun initiatePayment(phone: String, amount: Double, reference: String): MpesaResponse {
        // Simulate STK Push delay
        delay(2000)
        return MpesaResponse(
            success = true,
            transactionId = "MPESA-${getCurrentTimestamp()}",
            message = "STK Push Sent Successfully",
            isPending = true 
        )
    }

    override suspend fun checkTransactionStatus(transactionId: String): MpesaResponse {
        delay(1000)
        return MpesaResponse(
            success = true,
            transactionId = transactionId,
            message = "Payment Confirmed Successfully"
        )
    }
}

class DarajaMpesaService(
    private val shortcode: String,
    private val consumerKey: String,
    private val consumerSecret: String,
    private val passkey: String,
    private val environment: String
) : MpesaService {
    override suspend fun initiatePayment(phone: String, amount: Double, reference: String): MpesaResponse {
        // Placeholder for real Safaricom Daraja API logic using Ktor
        return MpesaResponse(
            success = false,
            message = "Production Mode (Daraja) not yet implemented. Use Mock Mode in settings."
        )
    }

    override suspend fun checkTransactionStatus(transactionId: String): MpesaResponse {
        return MpesaResponse(
            success = false,
            message = "Daraja Status Check not implemented."
        )
    }
}
