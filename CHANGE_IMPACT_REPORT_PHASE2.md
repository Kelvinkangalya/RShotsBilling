# CHANGE IMPACT REPORT - PHASE 2 (Reports Restoration)

## Proposed Changes
Restoration of professional reports functionality including Charts, KPI summaries, and export features (PDF/Excel) that were degraded during the KMP stabilization.

### Files to be Modified
- `composeApp/src/commonMain/kotlin/com/example/rshotsbilling/ui/ReportsScreen.kt`: 
    - Re-add Chart sections.
    - Restore KPI Summary cards.
    - Fix alignment of Export buttons.
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/viewmodel/ReportsViewModel.kt`:
    - Ensure `profitTrend` is correctly calculated for charts.
- `shared/src/androidMain/kotlin/com/example/rshotsbilling/utils/PlatformReportUtils.kt`:
    - Ensure callbacks are properly wired to `MainActivity`.

### Files Intentionally Untouched
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/repository/SalesRepository.kt`
- `shared/src/commonMain/kotlin/com/example/rshotsbilling/repository/PurchaseRepository.kt`

### Firestore Collections Affected
- None (Read-only access).

### Repositories Affected
- None directly.

### Risks
- Chart rendering on Desktop: Vico is currently Android-focused. I will use a placeholder or alternative for Desktop to maintain parity without breaking the build.
- Export library dependency conflicts: Mitigated by keeping `poi` and `itext` in `androidMain`.
