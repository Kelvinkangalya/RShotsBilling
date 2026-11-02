# Kenya Payroll Compliance Report - RShots Billing

## Payroll Calculations Implemented
- **PAYE**: Implemented 2024/2025 progressive tax bands (10% to 35%).
- **NSSF**: Implemented Tier I and Tier II contributions (February 2024 update).
- **SHIF (SHA)**: Implemented 2.75% of gross pay with KES 300 minimum.
- **Housing Levy**: Implemented 1.5% employee and 1.5% employer contributions.
- **Tax Deductibility**: SHIF and Housing Levy are treated as tax-deductible expenses (post-Dec 2024 rules).

## CSV Mapping (KRA P10)
- **Columns**: PIN, Name, Resident Status, PWD, Total Cash Pay, Car Benefit, Meals, Non-Cash, Housing, Other, SHIF, NSSF, PRMF, Mortgage, AHL, Personal Relief, Insurance Relief, PAYE.
- **Compatibility**: Generates a standard CSV compatible with the KRA iTax P10 import template.

## P9 Generation
- **Format**: PDF (A4 Landscape).
- **Details**: Includes all 14 standard columns required by KRA (A through O).
- **Features**: Monthly breakdown, Totals row, Employer/Employee details, and digital stamp.

## Payslip Improvements
- **Design**: Professional layout with separate sections for Earnings and Deductions.
- **Details**: Includes statutory deductions (NSSF, SHIF, Housing Levy, PAYE) individually.
- **Compliance**: Displays Employer PIN and Statutory Registration numbers.

## Reports Generated
1. **Payroll Register**: Full monthly breakdown of earnings and deductions.
2. **NSSF Register**: Specific report for NSSF remittances.
3. **SHIF Register**: Specific report for SHIF remittances.
4. **P10 CSV**: Ready-to-import file for iTax.

## Validation Rules
- **KRA PIN**: Warns if PIN is missing or improperly formatted.
- **Payroll Period**: Ensures records are processed for the correct month/year.
- **Mandatory Fields**: Validates existence of Basic Salary and Statutory IDs.

## Platform Verification
- **Android**: Verified CSV export via Callback system to `MainActivity`.
- **Desktop (JVM)**: Verified PDF generation and automatic file opening via `java.awt.Desktop`.
