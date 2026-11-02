package com.example.rshotsbilling.data.local.inventory

/**
 * Categorizes the reason for stock changes.
 */
enum class MovementType {
    PURCHASE,
    SALE,
    RETURN_IN,
    RETURN_OUT,
    DAMAGED,
    EXPIRED,
    LOST,
    ADJUSTMENT,
    TRANSFER
}

/**
 * Tracks the lifecycle of a Purchase Order.
 */
enum class PurchaseOrderStatus {
    REQUESTED,
    APPROVED,
    ORDERED,
    RECEIVED,
    CLOSED
}
