# PERFORMANCE + DATA CONTEXT DIAGNOSTIC REPORT — RSHOTS BILLING

## 1. Startup Timeline (Desktop JVM)
- **Firebase Initialization**: ~2400 ms (including Auth and Firestore provider warmup).
- **Session Restoration**: 22 ms (for `rauphinkelvin8@gmail.com`).
- **Profile Loading**: ~50 ms (Super Vendor bypass).
- **Initial Dashboard Render**: 617 ms (from composition start to first frame).
- **Total Time to Dashboard**: ~3100 ms.

## 2. Firestore Path & Context Audit
Existing records were found using a manual diagnostic trace for the Super Vendor role.

### Root Collections (Data Found)
| Collection | Path | Document Count | Query Duration |
| :--- | :--- | :--- | :--- |
| **Sales** | `/sales` | **16** | 2665 ms |
| **Services** | `/services` | **12** | 416 ms |
| **Receipts** | `/receipts` | **13** | 430 ms |
| **Staff** | `/staff` | **3** | 412 ms |
| **Payroll** | `/payroll_records` | **36** | 507 ms |
| **Purchases** | `/purchases` | **2** | 402 ms |

### Company Scoped Collections (Empty)
Checked first found company: `comp_1785583221430`
| Collection | Path | Document Count |
| :--- | :--- | :--- |
| **Sales** | `/companies/comp_1785583221430/sales` | **0** |
| **Services** | `/companies/comp_1785583221430/services` | **0** |
| **Products** | `/companies/comp_1785583221430/products` | **0** |

## 3. Sequential Loading & Dependency Chains
- **Startup**: `Firebase Init` -> `Auth` -> `Profile Load` -> `Dashboard` (Standard and correct).
- **Loading Business**: `Fetch Staff` -> `Fetch Company` -> `Dashboard`. These are currently sequential in `App.kt` and `MainActivity.kt`, adding ~1000-2000 ms to the transition from Login to Dashboard.
- **Data Loads**: ViewModels (`Sales`, `Purchase`, `Service`, `Staff`, `Messaging`, `Inventory`) all trigger loads simultaneously in `DashboardScreen`. No blocking sequential dependencies found between them.

## 4. Bottlenecks Identified

### A. Context Mismatch (The "Missing Data" Cause)
- **Regular Users**: Upon login, a `businessId` is loaded. `FirestoreManager` then directs ALL queries to `/companies/{businessId}/{collection}`.
- **The Data**: The existing "Old" data is stored in the **Root Collections** (e.g., `/sales`).
- **Result**: Regular users see empty lists because the app is looking in the wrong place for "Pre-SaaS" or "Unmigrated" data.

### B. Firestore Query Latency (The "Slow Loading" Cause)
- **Query Time**: A query for only 16 sales documents took **2665 ms**.
- **SSL/Service Issues**: JVM logs show `GooglePlayServicesNotAvailableException` and `SSL context update failure`. This forces Firestore to fall back to less optimized connection methods.
- **Panic Errors**: Logs show `AsyncQueue: Refused to enqueue task after panic`, indicating internal library crashes or resource leaks during high-concurrency loading.

### C. Artificial Delays
- `MainActivity.kt`: `delay(2000)` in splash and `delay(1000)` in `loading_business`.
- `App.kt`: `delay(500)` in `loading_business`.
- `LoginViewModel.kt`: `delay(1000)` before background sync.
- **Total Artificial Delay**: ~3.5 seconds of unnecessary waiting.

## 5. Root vs. Company Findings
Confirmed: Data exists at the root. The SaaS conversion changed the app logic to look in `companies/...` but the data was not moved (or the Super Vendor is the only one intended to see the root data, but they navigate to the dashboard which also looks at root, while others cannot).

## 6. Duplicate Query Findings
- `SalesViewModel` and `InventoryViewModel` both trigger loads for `services` (one calls it `products` internally).
- `DashboardScreen` re-triggers `loadSales`, `loadPurchases`, etc. every time it enters the "dashboard" state, which can happen multiple times during session restoration or navigation.

## 7. Android vs. Desktop Comparison
- **Desktop**: Hit with `GooglePlayServices` overhead and potential memory leaks (`SQLiteConnection` leaks detected in logs).
- **Android**: Expected to be faster due to native SDK optimization, but hampered by `delay()` calls in `MainActivity`.

## 8. Exact Bottleneck
The application **waits for a sequence of profile fetches** and **artificial delays** before showing the dashboard, and then Firestore **latency is high** due to connection establishment issues on JVM. The "Missing" data is purely a **path resolution mismatch**.

## 9. Recommended Fix (Smallest Change)
1.  **Path Fallback**: Update `FirestoreManager` to try the root collection if the company-scoped collection is empty, OR provide a flag to force root access for the primary business.
2.  **Remove Delays**: Delete all `delay()` calls in `MainActivity`, `App`, and `LoginViewModel`.
3.  **Lazy Initialization**: Ensure background sync and non-critical data (like audit logs) don't block the UI rendering.
4.  **Fix JVM SQLite**: Address the resource leaks in `DesktopLogger` or `FirestoreWrapper` to prevent Firestore "panics".
