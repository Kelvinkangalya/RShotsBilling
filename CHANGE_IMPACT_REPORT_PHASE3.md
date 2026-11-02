# CHANGE IMPACT REPORT - PHASE 3 (HR & Payroll Recovery)

## Proposed Changes
Restoration of printable payslips and P9 forms in the HR & Payroll module, ensuring compliance documents are accessible on both Android and Desktop.

### Files to be Modified
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/utils/PlatformReportUtils.kt`: 
    - Add `exportPayslip` and `exportP9` definitions.
- `shared/src/androidMain/kotlin/com/example/rshotsbilling/utils/PlatformReportUtils.kt`:
    - Add Android implementations/callbacks for payslips and P9.
- `shared/src/androidMain/kotlin/com/example/rshotsbilling/utils/PdfGenerator.kt` (NEW): Copy from `app` module for payslips.
- `shared/src/androidMain/kotlin/com/example/rshotsbilling/utils/P9Generator.kt` (NEW): Copy from `app` module for P9.
- `composeApp/src/androidMain/kotlin/com/example/rshotsbilling/MainActivity.kt`:
    - Initialize new payroll export callbacks.
- `composeApp/src/commonMain/kotlin/com/example/rshotsbilling/ui/HRPayrollScreen.kt`:
    - Link UI buttons to `PlatformReportUtils` for payslips and P9.

### Files Intentionally Untouched
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/repository/PayrollRepository.kt`
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/model/payroll/*.kt`

### Firestore Collections Affected
- None (Read-only access).

### Repositories Affected
- None.

### Risks
- Complexity of P9 form layout: Mitigated by reusing the stable `P9Generator` logic from the original `app` module.
