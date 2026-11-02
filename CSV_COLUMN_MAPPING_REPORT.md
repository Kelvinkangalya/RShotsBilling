# KRA PAYE CSV Column Mapping Report - RShotsBilling

This report documents the implementation of the official 24-column KRA PAYE CSV structure.

## Column Mapping Table

| # | KRA Column Name | Internal Source Field | Default Value | Blank/Zero Rule |
|---|-----------------|------------------------|---------------|-----------------|
| 1 | Employee PIN | `EmployeeEntity.kraPin` | - | Mandatory |
| 2 | Employee Name | `EmployeeEntity.fullName` | - | Mandatory |
| 3 | Resident Status | `EmployeeEntity.isResident` | Resident | "Resident" / "Non-Resident" |
| 4 | Type of Employee | `EmployeeEntity.employeeType` | Normal Employee | - |
| 5 | Exemption Cert No | `EmployeeEntity.exemptionCertificateNumber` | "" | Only if PWD |
| 6 | Total Cash Pay | `PayrollEntity.basicSalary` | 0.00 | - |
| 7 | Value of Car Benefit | `PayrollEntity.carBenefit` | 0.00 | - |
| 8 | Value of Meals | `PayrollEntity.valueOfMeals` | 0.00 | - |
| 9 | Other Non-Cash | `PayrollEntity.nonCashBenefits` | 0.00 | - |
| 10 | Type of Housing | `EmployeeEntity.typeOfHousing` | None | - |
| 11 | Housing Benefit | `PayrollEntity.housingBenefit` | 0.00 | - |
| 12 | Other Benefits | `PayrollEntity.otherBenefits` | 0.00 | - |
| 13 | Total Gross Pay | `PayrollEntity.grossSalary` | 0.00 | Auto-calculated |
| 14 | SHIF / SHA | `PayrollEntity.shifDeduction` | 0.00 | - |
| 15 | NSSF Contribution | `PayrollEntity.nssfDeduction` | 0.00 | - |
| 16 | Other Pension | `PayrollEntity.otherPension` | 0.00 | - |
| 17 | PRMF | `PayrollEntity.prmfContribution` | 0.00 | - |
| 18 | Mortgage Interest | `PayrollEntity.mortgageInterest` | 0.00 | - |
| 19 | Housing Levy | `PayrollEntity.housingLevyDeduction` | 0.00 | Auto-calculated |
| 20 | Taxable Pay | `PayrollEntity.taxablePay` | 0.00 | Auto-calculated |
| 21 | Personal Relief | `PayrollEntity.personalRelief` | 2400.00 | - |
| 22 | Insurance Relief | `PayrollEntity.insuranceRelief` | 0.00 | - |
| 23 | PAYE Tax | `PayrollEntity.payeAmount` | 0.00 | Auto-calculated |
| 24 | Self Assessment PAYE | `PayrollEntity.payeAmount` | 0.00 | Same as PAYE Tax |

## Validation Rules
The system validates the following before allowing export:
1. **Employee PIN**: Must not be empty.
2. **Employee Type**: Must be selected.
3. **Salary**: Basic salary must be greater than zero.
4. **Payroll Status**: Must be "PAID" or "FINALIZED".

## Export Modes
1. **Export CSV (With Headers)**: Includes a header row for manual review in Excel.
2. **Export CSV (KRA Upload)**: No header row, ready for import into the KRA iTax template.

## Verification
- [x] Android: Verified dialog options and file generation.
- [x] Desktop: Verified 24-column order in Excel.
