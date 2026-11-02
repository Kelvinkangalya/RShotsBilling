# CHANGE IMPACT REPORT - PHASE 6 (eTIMS / KRA Compliance Recovery)

## Proposed Changes
Verification of the eTIMS submission pipeline, ensuring sales marked as eTIMS are correctly queued and processed for KRA submission.

### Files to be Modified
- `composeApp/src/commonMain/kotlin/com/example/rshotsbilling/ui/ComplianceHubScreen.kt`: Verify alignment and HUB navigation.
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/viewmodel/EtimsViewModel.kt`: Verify queue processing logic.

### Files Intentionally Untouched
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/repository/EtimsRepository.kt`
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/model/EtimsInvoice.kt`

### Firestore Collections Affected
- `companies/{id}/etims_invoices` (Read/Write).

### Repositories Affected
- `EtimsRepository`

### Risks
- Sandbox connectivity: Requires stable internet and valid Sandbox endpoint access.
- Tax calculation accuracy: Re-verified against VAT 16% standard in `PdfReceiptGenerator`.
 bitumen
