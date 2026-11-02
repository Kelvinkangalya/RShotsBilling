package com.example.rshotsbilling.model

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val stockQuantity: Double = -1.0
)
