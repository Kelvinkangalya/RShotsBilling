package com.example.rshotsbilling.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "sale_items")
data class LocalSaleItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val saleId: String,
    val serviceId: String,
    val serviceName: String,
    val quantity: Double,
    val price: Double
)
