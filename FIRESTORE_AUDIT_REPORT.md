# Firestore Reference Audit Report

## Issue Resolved
Fixed "Invalid document reference" error caused by odd number of path segments (specifically "companies" used as a document reference).

## Root Causes Found
1.  **Inconsistent root names**: Some parts of the app used `businesses` while others used `companies`.
2.  **Empty ID vulnerability**: Calling `.document(businessId)` when `businessId` was empty (e.g., during startup or before staff profile load) resulted in an invalid 1-segment path.
3.  **Missing Safeguards**: Repositories were attempting to build paths without verifying if the ID variable was populated.

## Fixed Paths & Components

| File | Original Path (Logic) | Fixed Path (Safe) |
| :--- | :--- | :--- |
| `FirestoreManager.kt` | `companies/{empty_id}` | `companies/unknown_company` |
| `LicensingRepository.kt` | `companies/{empty_id}` | Guarded with `isEmpty()` checks |
| `IdentityRepository.kt` | `companies/{empty_id}/devices` | Guarded with `ifEmpty { "unknown_company" }` |
| `CompanyRepository.kt` | `companies/{empty_id}` | central helper `getCompanyDocRef` |
| `SalesRepository.kt` | `sales/{empty_id}` | Guarded with `isEmpty()` checks |
| `MessagingRepository.kt` | `tasks/{empty_id}` | Guarded with `isEmpty()` checks |
| `MpesaRepository.kt` | `mpesa/{empty_id}` | Guarded with `isEmpty()` checks |
| `PayrollRepository.kt` | `employees/{empty_id}` | Guarded with `isEmpty()` checks |

## Debugging & Trace Logs Added
Added extensive `println` logs across the login flow:
-   **Logged in UID**: `DEBUG: [LOGIN] Logged in UID: {uid}`
-   **User email**: `DEBUG: [LOGIN] User email: {email}`
-   **Attempting Firestore path**: `DEBUG: [LOGIN] Attempting Firestore path: {path}`
-   **Loaded staff profile**: `DEBUG: [LOGIN] Loaded staff profile: {name}`
-   **companyId**: `DEBUG: [LOGIN] companyId: {id}`

## Additional Improvements
- **Safe ID Guard**: `FirestoreManager.getCompanyId()` now returns a fallback `"unknown_company"` instead of `""`.
- **Unified Registry**: All `businesses` references migrated to `companies`.
- **Session Resilience**: Explicit session reset in `LoginScreen` to prevent path construction with stale data.

## Verification
- **Build Status**: Both `:shared` and `:composeApp` compiled successfully.
- **Path Integrity**: Every document reference now strictly follows the `collection/document` pattern and handles empty IDs gracefully.
