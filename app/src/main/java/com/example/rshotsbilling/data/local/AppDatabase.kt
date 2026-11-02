package com.example.rshotsbilling.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rshotsbilling.model.*
import com.example.rshotsbilling.model.payroll.*
import com.example.rshotsbilling.data.local.inventory.*

@Database(
    entities = [
        LocalSale::class, 
        LocalSaleItemEntity::class,
        LocalEtimsInvoice::class, 
        LocalMpesaTransaction::class,
        LocalSupplierEntity::class,
        LocalInventoryBatchEntity::class,
        LocalStockMovementEntity::class,
        LocalPurchaseOrderEntity::class,
        LocalPurchaseOrderItemEntity::class,
        LocalInventoryAuditEntity::class,
        LocalStockAdjustmentEntity::class,
        // New Inventory Entities (namespaced to avoid conflict)
        com.example.rshotsbilling.data.local.inventory.StockMovementEntity::class,
        com.example.rshotsbilling.data.local.inventory.InventoryBatchEntity::class,
        com.example.rshotsbilling.data.local.inventory.StockAdjustmentEntity::class,
        com.example.rshotsbilling.data.local.inventory.InventoryAuditEntity::class,
        com.example.rshotsbilling.data.local.inventory.PurchaseOrderEntity::class,
        com.example.rshotsbilling.data.local.inventory.PurchaseOrderItemEntity::class,
        com.example.rshotsbilling.data.local.inventory.SupplierAnalyticsEntity::class,
        EnterpriseStockAdjustmentEntity::class,
        EnterpriseInventoryAuditEntity::class,
        EnterpriseSupplierAnalyticsEntity::class
    ], 
    version = 16,
    exportSchema = false
)
@TypeConverters(Converters::class, InventoryTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
    abstract fun etimsDao(): EtimsDao
    abstract fun mpesaDao(): MpesaDao
    abstract fun inventoryDao(): InventoryDao
    
    // New Inventory DAOs
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun inventoryBatchDao(): InventoryBatchDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun inventoryAuditDao(): InventoryAuditDao
    abstract fun purchaseOrderDao(): PurchaseOrderDao
    abstract fun purchaseOrderItemDao(): PurchaseOrderItemDao
    abstract fun supplierAnalyticsDao(): SupplierAnalyticsDao
    abstract fun enterpriseInventoryDao(): EnterpriseInventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from 5 to 6
        private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `new_inventory_stock_movements` (`id` TEXT NOT NULL, `productId` TEXT NOT NULL, `quantity` REAL NOT NULL, `movementType` TEXT NOT NULL, `userId` TEXT NOT NULL, `notes` TEXT, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `new_inventory_batches` (`id` TEXT NOT NULL, `productId` TEXT NOT NULL, `supplierId` TEXT NOT NULL, `batchNumber` TEXT NOT NULL, `quantityPurchased` REAL NOT NULL, `remainingQuantity` REAL NOT NULL, `costPrice` REAL NOT NULL, `manufacturingDate` INTEGER, `expiryDate` INTEGER, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `new_inventory_stock_adjustments` (`id` TEXT NOT NULL, `productId` TEXT NOT NULL, `previousQuantity` REAL NOT NULL, `newQuantity` REAL NOT NULL, `adjustmentAmount` REAL NOT NULL, `reason` TEXT NOT NULL, `userId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `new_inventory_audits` (`id` TEXT NOT NULL, `productId` TEXT NOT NULL, `userId` TEXT NOT NULL, `action` TEXT NOT NULL, `beforeValue` TEXT NOT NULL, `afterValue` TEXT NOT NULL, `reason` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `new_inventory_purchase_orders` (`id` TEXT NOT NULL, `supplierId` TEXT NOT NULL, `orderNumber` TEXT NOT NULL, `status` TEXT NOT NULL, `totalAmount` REAL NOT NULL, `requestedBy` TEXT NOT NULL, `approvedBy` TEXT, `requestedAt` INTEGER NOT NULL, `approvedAt` INTEGER, `receivedAt` INTEGER, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `new_inventory_purchase_order_items` (`id` TEXT NOT NULL, `purchaseOrderId` TEXT NOT NULL, `productId` TEXT NOT NULL, `quantity` REAL NOT NULL, `unitCost` REAL NOT NULL, `totalCost` REAL NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`purchaseOrderId`) REFERENCES `new_inventory_purchase_orders`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                db.execSQL("CREATE TABLE IF NOT EXISTS `new_inventory_supplier_analytics` (`supplierId` TEXT NOT NULL, `totalSpent` REAL NOT NULL, `outstandingBalance` REAL NOT NULL, `lastPurchaseDate` INTEGER, PRIMARY KEY(`supplierId`))")
                
                // Indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_stock_movements_productId` ON `new_inventory_stock_movements` (`productId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_stock_movements_movementType` ON `new_inventory_stock_movements` (`movementType`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_batches_productId` ON `new_inventory_batches` (`productId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_new_inventory_batches_batchNumber` ON `new_inventory_batches` (`batchNumber`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_stock_adjustments_productId` ON `new_inventory_stock_adjustments` (`productId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_audits_productId` ON `new_inventory_audits` (`productId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_audits_createdAt` ON `new_inventory_audits` (`createdAt`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_new_inventory_purchase_orders_orderNumber` ON `new_inventory_purchase_orders` (`orderNumber`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_purchase_orders_supplierId` ON `new_inventory_purchase_orders` (`supplierId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_purchase_order_items_purchaseOrderId` ON `new_inventory_purchase_order_items` (`purchaseOrderId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_new_inventory_purchase_order_items_productId` ON `new_inventory_purchase_order_items` (`productId`)")
            }
        }

        // Migration from 7 to 8
        private val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Enterprise Stock Adjustments
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enterprise_stock_adjustments` (
                        `id` TEXT NOT NULL, 
                        `productId` TEXT NOT NULL, 
                        `previousQuantity` REAL NOT NULL, 
                        `newQuantity` REAL NOT NULL, 
                        `adjustmentAmount` REAL NOT NULL, 
                        `reason` TEXT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_enterprise_stock_adjustments_productId` ON `enterprise_stock_adjustments` (`productId`)")

                // Enterprise Inventory Audits
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enterprise_inventory_audits` (
                        `id` TEXT NOT NULL, 
                        `productId` TEXT NOT NULL, 
                        `userId` TEXT NOT NULL, 
                        `action` TEXT NOT NULL, 
                        `beforeValue` TEXT NOT NULL, 
                        `afterValue` TEXT NOT NULL, 
                        `reason` TEXT NOT NULL, 
                        `timestamp` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_enterprise_inventory_audits_productId` ON `enterprise_inventory_audits` (`productId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_enterprise_inventory_audits_timestamp` ON `enterprise_inventory_audits` (`timestamp`)")

                // Enterprise Supplier Analytics
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enterprise_supplier_analytics` (
                        `supplierId` TEXT NOT NULL, 
                        `totalSpent` REAL NOT NULL, 
                        `outstandingBalance` REAL NOT NULL, 
                        `totalPurchasesCount` INTEGER NOT NULL, 
                        `lastPurchaseDate` INTEGER, 
                        PRIMARY KEY(`supplierId`)
                    )
                """)
            }
        }

        // Migration from 8 to 9
        private val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Employees Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `employees` (
                        `id` TEXT NOT NULL, 
                        `fullName` TEXT NOT NULL, 
                        `nationalId` TEXT NOT NULL, 
                        `kraPin` TEXT NOT NULL, 
                        `nssfNumber` TEXT NOT NULL, 
                        `shifNumber` TEXT NOT NULL, 
                        `phone` TEXT NOT NULL, 
                        `email` TEXT NOT NULL, 
                        `department` TEXT NOT NULL, 
                        `position` TEXT NOT NULL, 
                        `basicSalary` REAL NOT NULL, 
                        `salaryType` TEXT NOT NULL, 
                        `bankName` TEXT NOT NULL, 
                        `bankBranch` TEXT NOT NULL, 
                        `accountNumber` TEXT NOT NULL, 
                        `employmentDate` INTEGER NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `housingLevyExempt` INTEGER NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)

                // Payroll Records Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payroll_records` (
                        `id` TEXT NOT NULL, 
                        `employeeId` TEXT NOT NULL, 
                        `month` INTEGER NOT NULL, 
                        `year` INTEGER NOT NULL, 
                        `grossSalary` REAL NOT NULL, 
                        `basicSalary` REAL NOT NULL, 
                        `overtimeAmount` REAL NOT NULL, 
                        `bonusAmount` REAL NOT NULL, 
                        `allowances` REAL NOT NULL, 
                        `nssfDeduction` REAL NOT NULL, 
                        `shifDeduction` REAL NOT NULL, 
                        `housingLevyDeduction` REAL NOT NULL, 
                        `taxablePay` REAL NOT NULL, 
                        `payeAmount` REAL NOT NULL, 
                        `personalRelief` REAL NOT NULL, 
                        `otherDeductions` REAL NOT NULL, 
                        `netPay` REAL NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `processedAt` INTEGER NOT NULL, 
                        `paidAt` INTEGER, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`employeeId`) REFERENCES `employees`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payroll_records_employeeId` ON `payroll_records` (`employeeId`)")

                // Attendance Records Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `attendance_records` (
                        `id` TEXT NOT NULL, 
                        `employeeId` TEXT NOT NULL, 
                        `date` INTEGER NOT NULL, 
                        `clockInTime` INTEGER, 
                        `clockOutTime` INTEGER, 
                        `status` TEXT NOT NULL, 
                        `hoursWorked` REAL NOT NULL, 
                        `overtimeHours` REAL NOT NULL, 
                        `notes` TEXT, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`employeeId`) REFERENCES `employees`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_attendance_records_employeeId` ON `attendance_records` (`employeeId`)")

                // Leave Records Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `leave_records` (
                        `id` TEXT NOT NULL, 
                        `employeeId` TEXT NOT NULL, 
                        `leaveType` TEXT NOT NULL, 
                        `startDate` INTEGER NOT NULL, 
                        `endDate` INTEGER NOT NULL, 
                        `totalDays` INTEGER NOT NULL, 
                        `reason` TEXT NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `approvedBy` TEXT, 
                        `createdAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`employeeId`) REFERENCES `employees`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_leave_records_employeeId` ON `leave_records` (`employeeId`)")
            }
        }

        // Migration from 9 to 10
        private val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Statutory Deduction Rules Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `statutory_deduction_rules` (
                        `id` TEXT NOT NULL, 
                        `name` TEXT NOT NULL, 
                        `type` TEXT NOT NULL, 
                        `employeeRate` REAL NOT NULL, 
                        `employerRate` REAL NOT NULL, 
                        `appliesTo` TEXT NOT NULL, 
                        `effectiveFrom` INTEGER NOT NULL, 
                        `effectiveTo` INTEGER, 
                        `isActive` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)

                // Statutory Brackets Table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `statutory_brackets` (
                        `id` TEXT NOT NULL, 
                        `ruleId` TEXT NOT NULL, 
                        `lowerLimit` REAL NOT NULL, 
                        `upperLimit` REAL NOT NULL, 
                        `rate` REAL NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`ruleId`) REFERENCES `statutory_deduction_rules`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_statutory_brackets_ruleId` ON `statutory_brackets` (`ruleId`)")

                // Update Payroll Records Table
                db.execSQL("ALTER TABLE `payroll_records` ADD COLUMN `employerNssf` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `payroll_records` ADD COLUMN `employerShif` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `payroll_records` ADD COLUMN `employerHousingLevy` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `payroll_records` ADD COLUMN `insuranceRelief` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `payroll_records` ADD COLUMN `nonCashBenefits` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `payroll_records` ADD COLUMN `valueOfQuarters` REAL NOT NULL DEFAULT 0.0")
            }
        }

        // Migration from 10 to 11
        private val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `employees` ADD COLUMN `employeeNumber` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `employees` ADD COLUMN `tscNumber` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `employees` ADD COLUMN `termsOfService` TEXT NOT NULL DEFAULT 'P&P'")
            }
        }

        // Migration from 11 to 12
        private val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Rename employeeNumber to pnNumber if we want to migrate data, 
                // but since it was just added, we can just ensure both exist or rename.
                // Room doesn't support easy column rename in older versions without table recreate,
                // so we add pnNumber and handle data if needed.
                db.execSQL("ALTER TABLE `employees` ADD COLUMN `pnNumber` TEXT NOT NULL DEFAULT ''")
                // tscNumber is already there from version 11
            }
        }

        // Migration from 12 to 13
        private val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // No table changes needed, just removing logic references to tscNumber
                // Room will ignore the extra column if we don't use it, but to be clean
                // we keep the schema in sync.
            }
        }

        // Migration from 13 to 14 (Cleanup and Reset for Consistency)
        private val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Drop and recreate employees table to ensure exact schema match
                db.execSQL("DROP TABLE IF EXISTS `employees` ")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `employees` (
                        `id` TEXT NOT NULL, 
                        `fullName` TEXT NOT NULL, 
                        `nationalId` TEXT NOT NULL, 
                        `kraPin` TEXT NOT NULL, 
                        `pnNumber` TEXT NOT NULL, 
                        `nssfNumber` TEXT NOT NULL, 
                        `shifNumber` TEXT NOT NULL, 
                        `phone` TEXT NOT NULL, 
                        `email` TEXT NOT NULL, 
                        `department` TEXT NOT NULL, 
                        `position` TEXT NOT NULL, 
                        `termsOfService` TEXT NOT NULL, 
                        `basicSalary` REAL NOT NULL, 
                        `salaryType` TEXT NOT NULL, 
                        `bankName` TEXT NOT NULL, 
                        `bankBranch` TEXT NOT NULL, 
                        `accountNumber` TEXT NOT NULL, 
                        `employmentDate` INTEGER NOT NULL, 
                        `status` TEXT NOT NULL, 
                        `housingLevyExempt` INTEGER NOT NULL, 
                        `createdAt` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rshots_billing_db"
                )
                    .addMigrations(
                        MIGRATION_5_6, MIGRATION_7_8, MIGRATION_8_9, 
                        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, 
                        MIGRATION_12_13, MIGRATION_13_14
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
