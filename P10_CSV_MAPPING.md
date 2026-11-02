# KRA P10 CSV Mapping - RShotsBilling

## Column Structure
The CSV follows the "Simplified P10" template provided by KRA for seamless iTax import.

| Index | Field Name | Source / Calculation |
|-------|------------|----------------------|
| 1 | Employee PIN | `employee.kraPin` |
| 2 | Name of Employee | `employee.fullName` |
| 3 | Resident Status | `Resident` or `Non-Resident` |
| 4 | PWD Status | `Yes` or `No` |
| 5 | Total Cash Pay | `payroll.grossSalary` |
| 6 | Value of Car Benefit | `0.00` (Dynamic support coming soon) |
| 7 | Value of Meals | `0.00` |
| 8 | Non-Cash Benefits | `payroll.nonCashBenefits` |
| 9 | Value of Quarters | `payroll.valueOfQuarters` |
| 10 | Other Benefits | `0.00` |
| 11 | SHIF (2.75%) | `payroll.shifDeduction` |
| 12 | NSSF / Pension | `payroll.nssfDeduction` |
| 13 | PRMF | `payroll.prmfContribution` |
| 14 | Mortgage Interest | `payroll.mortgageInterest` |
| 15 | AHL (1.5%) | `payroll.housingLevyDeduction` |
| 16 | Personal Relief | `payroll.personalRelief` |
| 17 | Insurance Relief | `payroll.insuranceRelief` |
| 18 | Self-Assessed PAYE | `payroll.payeAmount` |

## Format Requirements
- **Delimiter**: Comma (`,`).
- **Numeric**: Fixed at 2 decimal places.
- **Headers**: No headers (as per KRA import macro requirements).
