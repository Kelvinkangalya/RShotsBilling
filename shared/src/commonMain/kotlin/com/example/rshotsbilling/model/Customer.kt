package com.example.rshotsbilling.model

import com.example.rshotsbilling.utils.getCurrentTimestamp
import kotlinx.serialization.Serializable

@Serializable
data class CustomerEntity(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val kraPin: String = "",
    val category: String = "Retail", // Retail, Wholesale, Corporate
    val loyaltyPoints: Int = 0,
    val creditLimit: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val lastPurchaseDate: Long? = null,
    val notes: String = "",
    val birthday: Long? = null,
    val createdAt: Long = getCurrentTimestamp(),
    val updatedAt: Long = getCurrentTimestamp(),
    val totalSpending: Double = 0.0,
    val numberOfPurchases: Int = 0,
    val favouriteProducts: List<String> = emptyList()
)
