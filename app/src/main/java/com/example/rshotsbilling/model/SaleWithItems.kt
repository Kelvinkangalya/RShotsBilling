package com.example.rshotsbilling.model

import androidx.room.Embedded
import androidx.room.Relation

data class SaleWithItems(
    @Embedded val sale: LocalSale,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val items: List<LocalSaleItemEntity>
)
