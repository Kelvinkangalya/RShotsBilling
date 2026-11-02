package com.example.rshotsbilling.utils

import android.util.Base64
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.text.SimpleDateFormat
import java.util.*

/**
 * M-Pesa STK Push Helper (Daraja API)
 */
object MpesaHelper {

    // ==========================================================
    // CONFIGURATION (REAL PRODUCTION CREDENTIALS)
    // ==========================================================
    private const val CONSUMER_KEY = "UC8orEMdCV5E7v0BNr7o5LUAVZIzAmoMLO4A6P0lT6Th7pnW-"
    private const val CONSUMER_SECRET = "Bdml464F6gFWS3L9rTtBGcJ1rBvQ67NwZ9pcllJtuCXBrbiM3WdGhh1S8uMHa32M"
    
    // YOUR REAL TILL NUMBER
    private const val BUSINESS_SHORT_CODE = "8323884" 
    
    // REPLACE THIS WITH YOUR REAL PRODUCTION PASSKEY FROM DARAJA PORTAL
    private const val PASSKEY = "YOUR_REAL_PRODUCTION_PASSKEY_HERE" 
    
    private const val CALLBACK_URL = "https://your-domain.com/mpesa/callback"
    private const val BASE_URL = "https://api.safaricom.co.ke/"

    // =========================
    // API INTERFACE
    // =========================
    interface DarajaApi {
        @GET("oauth/v1/generate?grant_type=client_credentials")
        fun getAccessToken(
            @Header("Authorization") auth: String
        ): Call<AccessTokenResponse>

        @POST("mpesa/stkpush/v1/processrequest")
        fun triggerStkPush(
            @Header("Authorization") auth: String,
            @Body request: StkPushRequest
        ): Call<StkPushResponse>
    }

    // =========================
    // DATA MODELS
    // =========================
    data class AccessTokenResponse(val access_token: String, val expires_in: String)

    data class StkPushRequest(
        val BusinessShortCode: String,
        val Password: String,
        val Timestamp: String,
        val TransactionType: String = "CustomerPayBillOnline",
        val Amount: Int,
        val PartyA: String, // Phone number sending money
        val PartyB: String, // Shortcode receiving money
        val PhoneNumber: String, // Phone number sending money
        val CallBackURL: String,
        val AccountReference: String,
        val TransactionDesc: String
    )

    data class StkPushResponse(
        val MerchantRequestID: String,
        val CheckoutRequestID: String,
        val ResponseCode: String,
        val ResponseDescription: String,
        val CustomerMessage: String
    )

    // =========================
    // RETROFIT SETUP
    // =========================
    private val api: DarajaApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(DarajaApi::class.java)
    }

    // =========================
    // CORE FUNCTIONS
    // =========================

    /**
     * Triggers the STK Push prompt on the customer's phone
     */
    fun performStkPush(phoneNumber: String, amount: Int, accountReference: String, onResult: (Boolean, String) -> Unit) {
        
        // 1. FORMAT PHONE (Must be 2547XXXXXXXX)
        val cleanPhone = phoneNumber.replace("+", "").replace(" ", "")
        val finalPhone = if (cleanPhone.startsWith("0")) "254" + cleanPhone.substring(1) else cleanPhone

        // 2. GENERATE AUTH
        val auth = "Basic " + Base64.encodeToString("$CONSUMER_KEY:$CONSUMER_SECRET".toByteArray(), Base64.NO_WRAP)

        // 3. GET ACCESS TOKEN
        api.getAccessToken(auth).enqueue(object : retrofit2.Callback<AccessTokenResponse> {
            override fun onResponse(call: Call<AccessTokenResponse>, response: retrofit2.Response<AccessTokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.access_token ?: return
                    sendRequest(token, finalPhone, amount, accountReference, onResult)
                } else {
                    onResult(false, "Failed to get access token: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<AccessTokenResponse>, t: Throwable) {
                onResult(false, "Network error: ${t.message}")
            }
        })
    }

    private fun sendRequest(token: String, phone: String, amount: Int, reference: String, onResult: (Boolean, String) -> Unit) {
        val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
        val password = Base64.encodeToString((BUSINESS_SHORT_CODE + PASSKEY + timestamp).toByteArray(), Base64.NO_WRAP)

        val request = StkPushRequest(
            BusinessShortCode = BUSINESS_SHORT_CODE,
            Password = password,
            Timestamp = timestamp,
            Amount = amount,
            PartyA = phone,
            PartyB = BUSINESS_SHORT_CODE,
            PhoneNumber = phone,
            CallBackURL = CALLBACK_URL,
            AccountReference = reference,
            TransactionDesc = "Payment for $reference"
        )

        api.triggerStkPush("Bearer $token", request).enqueue(object : retrofit2.Callback<StkPushResponse> {
            override fun onResponse(call: Call<StkPushResponse>, response: retrofit2.Response<StkPushResponse>) {
                if (response.isSuccessful) {
                    onResult(true, "STK Push sent successfully to $phone")
                } else {
                    onResult(false, "STK Push failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<StkPushResponse>, t: Throwable) {
                onResult(false, "Network error: ${t.message}")
            }
        })
    }
}