package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleItem(
    val serviceId: String = "",
    val serviceName: String = "",
    val quantity: Double = 1.0,
    val price: Double = 0.0
)
