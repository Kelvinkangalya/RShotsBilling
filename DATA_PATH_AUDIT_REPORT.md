# DATA PATH AUDIT REPORT — RSHOTS BILLING

## 1. Project Information
- **Firebase Project ID**: `rshots-billing-system`
- **Current Authenticated UID**: `m7ixYNrTuKgz6uj6wWmV14gZR2n1` (Super Vendor: `rauphinkelvin8@gmail.com`)
- **Current Role**: `VENDOR` (Super Vendor)
- **Current Company Context**: `companyId` is **EMPTY** (Bypassed during Super Vendor login).
- **Resulting Data Path Context**: `root` (Queries top-level collections).

## 2. Current Application Data Paths
All KMP repositories now use `FirestoreManager.getCollection(name)`, which resolves paths as follows:

| Entity | Path (Tenant Mode) | Path (Super Vendor / Root Mode) |
| :--- | :--- | :--- |
| **Sales** | `companies/{companyId}/sales` | `/sales` |
| **Products** | `companies/{companyId}/products` | `/products` |
| **Services** | `companies/{companyId}/services` | `/services` |
| **Customers** | `companies/{companyId}/customers` | `/customers` |
| **Purchases** | `companies/{companyId}/purchases` | `/purchases` |
| **Receipts** | `companies/{companyId}/receipts` | `/receipts` |
| **Staff** | `companies/{companyId}/staff` | `/staff` |
| **Payroll** | `companies/{companyId}/payroll_records` | `/payroll_records` |
| **Audit Logs** | `companies/{companyId}/audit_logs` | `/audit_logs` |

## 3. Old Application Data Paths (Recovered)
Based on `SAAS_CONVERSION_REPORT.md` and `FIRESTORE_AUDIT_REPORT.md`:

- **Pre-SaaS Phase**: Data was likely stored in root collections (`/sales`, `/products`, etc.).
- **SaaS Conversion Phase**: Data was moved to `businesses/{companyId}/{collection}`.
- **Unified Audit Phase**: Collection name `businesses` was renamed to `companies`.

## 4. Path Discrepancies & Issues Found

### A. Collection Name Mismatch
- **InventoryRepository** queries the `products` collection.
- **ServiceRepository** queries the `services` collection (but logs it as `products`).
- **Impact**: Items added via the Services screen may not appear in the Inventory ledger if they were saved to `/services` but queried from `/products`.

### B. "Root" Isolation
- When `companyId` is empty (common for Super Vendor), the app queries root collections (e.g., `/sales`).
- If the "RShots Company" data was actually migrated to a specific ID (e.g., `comp_172251...`), it will be **invisible** to the Super Vendor in their current "root" context.
- Super Vendor currently only sees the **list of companies** in the `VendorAdminDashboard`, not the data inside them.

### C. Missing Staff Profile Association
- `LoginViewModel` explicitly bypasses profile loading for the Super Vendor email.
- This prevents the Super Vendor from ever being associated with a `businessId`, even if a staff record exists for them.
- Consequently, they can never "Enter" a specific company to see its data unless they log in with a different account or the bypass is removed.

## 5. Evidence of Data Existence
- **Confirmed Log Entry**: `[2026-08-06 12:26:12.197] DEBUG: [Repository] addService SUCCESS for ser_1786008370892`.
- **Path Used**: `root/products` (actually root `/services` collection in Firestore).
- **Conclusion**: Data exists in the root `/services` collection as of August 6th, 2026.

## 6. Exact Reason for "Missing" Records
1. **Context mismatch**: The app is looking in `companies/{id}/sales` while data may still be in root `/sales` or vice versa.
2. **Naming mismatch**: Services are being looked for in `/services` while they might be in `/products`.
3. **Login Bypass**: Super Vendor is intentionally denied a `companyId`, forcing them into a `root` context which may be empty if data was previously moved to a company-scoped path.

## 7. Confirmation of Integrity
- **Firestore Structure**: No changes were made to document internal structures.
- **Business Repositories**: Logic remains unchanged; only the path resolution via `FirestoreManager` was affected.
- **Data Presence**: Logs confirm successful writes to root collections recently.

## 8. Next Steps (Audit Only)
- Verify if `rauphinkelvin8@gmail.com` should have a `businessId` assigned.
- Decide whether to unified `products` and `services` collection names.
- Verify if data in Firestore exists under `businesses/` or `companies/`.
