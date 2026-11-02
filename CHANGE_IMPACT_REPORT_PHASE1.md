# CHANGE IMPACT REPORT - PHASE 1 (Customer Hub Recovery)

## Proposed Changes
Implementation of a one-time migration to build customer profiles from historical sales and receipts, ensuring the Customer Hub reflects all previous business activity.

### Files to be Modified
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/repository/CustomerRepository.kt`: Add `migrateFromSalesAndReceipts` function.
- `composeApp/src/commonMain/kotlin/com/example/rshotsbilling/App.kt`: Trigger migration in `loading_business` state (one-time check).
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/viewmodel/CustomerViewModel.kt` (NEW): Provide customer data to the UI.

### Files Intentionally Untouched
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/repository/SalesRepository.kt`: Logic already exists for automatic updates.
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/model/Customer.kt`: Model already updated.
- All other UI screens except Dashboard/App navigation.

### Firestore Collections Affected
- `companies/{id}/customers`: New documents created/updated.
- `companies/{id}/sales`: Read-only access for migration.
- `companies/{id}/receipts`: Read-only access for migration.

### Repositories Affected
- `CustomerRepository`: Primary logic for migration.
- `SalesRepository`: (Dependency)
- `ReceiptRepository`: (Dependency)

### Risks
- High volume of historical data: Mitigated by simple batch processing.
- Duplicate customers: Mitigated by using `phone` as a unique identifier during migration.
