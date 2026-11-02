package com.example.rshotsbilling.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class LocalSupplierEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val contactPerson: String,
    val email: String,
    val phone: String,
    val physicalAddress: String,
    val category: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
