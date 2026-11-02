package com.example.rshotsbilling.data.local.inventory

import androidx.room.Embedded
import androidx.room.Relation

/**
 * POJO for retrieving a Purchase Order along with its associated items.
 */
data class PurchaseOrderWithItems(
    @Embedded val purchaseOrder: PurchaseOrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseOrderId"
    )
    val items: List<PurchaseOrderItemEntity>
)
