package com.example.rshotsbilling.utils

/**
 * Enterprise-grade Role-Based Access Control (RBAC).
 */
enum class EnterpriseRole {
    ADMIN,
    MANAGER,
    SALES,
    CONSULTANT,
    PHOTOGRAPHER,
    CYBER_AGENT
}

/**
 * Granular permissions for inventory operations.
 */
sealed class InventoryPermission {
    object ViewInventory : InventoryPermission()
    object ManageBatches : InventoryPermission()
    object PerformAdjustments : InventoryPermission()
    object ManageProcurement : InventoryPermission()
    object ViewFullAnalytics : InventoryPermission()
}

object EnterprisePermissionEngine {

    /**
     * Maps legacy or new roles to a set of granular permissions.
     */
    fun getPermissionsForRole(role: EnterpriseRole): Set<InventoryPermission> {
        return when (role) {
            EnterpriseRole.ADMIN -> setOf(
                InventoryPermission.ViewInventory,
                InventoryPermission.ManageBatches,
                InventoryPermission.PerformAdjustments,
                InventoryPermission.ManageProcurement,
                InventoryPermission.ViewFullAnalytics
            )
            EnterpriseRole.MANAGER -> setOf(
                InventoryPermission.ViewInventory,
                InventoryPermission.ManageBatches,
                InventoryPermission.PerformAdjustments,
                InventoryPermission.ManageProcurement
            )
            EnterpriseRole.SALES, EnterpriseRole.CONSULTANT -> setOf(
                InventoryPermission.ViewInventory
            )
            else -> emptySet()
        }
    }

    /**
     * Checks if a user role has a specific permission.
     */
    fun hasPermission(role: EnterpriseRole, permission: InventoryPermission): Boolean {
        return getPermissionsForRole(role).contains(permission)
    }
}
