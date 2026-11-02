package com.example.rshotsbilling.data.local.inventory

import androidx.room.TypeConverter

class InventoryTypeConverters {
    @TypeConverter
    fun fromMovementType(value: MovementType): String = value.name

    @TypeConverter
    @Suppress("EnumValuesSoftDeprecate")
    fun toMovementType(value: String): MovementType = MovementType.valueOf(value)

    @TypeConverter
    fun fromPOStatus(value: PurchaseOrderStatus): String = value.name

    @TypeConverter
    @Suppress("EnumValuesSoftDeprecate")
    fun toPOStatus(value: String): PurchaseOrderStatus = PurchaseOrderStatus.valueOf(value)
}
