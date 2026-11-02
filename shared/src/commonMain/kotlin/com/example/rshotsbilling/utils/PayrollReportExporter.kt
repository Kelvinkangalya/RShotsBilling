package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity

/**
 * Common Payroll Export Logic
 */
object PayrollReportExporter {

    /**
     * Generates an official KRA PAYE CSV with 25 columns.
     * 
     * @param employees Map of employee ID to EmployeeEntity
     * @param records List of PayrollEntity for the period
     * @param withHeaders Whether to include the readable header row
     */
    fun generateP10Csv(
        employees: Map<String, EmployeeEntity>,
        records: List<PayrollEntity>,
        withHeaders: Boolean = false
    ): String {
        val sb = StringBuilder()
        
        if (withHeaders) {
            val headers = listOf(
                "PIN of Employee", "Name of Employee", "Resident Status", "Type of Employee", 
                "Persons With Disability", "Exemption Certificate Number", "Total Cash Pay", 
                "Value of Car", "Value of Meals", "Non-Cash Benefits", "Type of Housing", 
                "Housing Benefits", "Other Benefits", "Total Gross Pay", "SHA / SHIF", 
                "NSSF Contribution", "Other Pension Contributions", "Post-Retirement Medical Fund", 
                "Mortgage Interest", "Affordable Housing Levy", "Taxable Pay", 
                "Monthly Personal Relief", "Insurance Relief", "PAYE Tax", "Self-Assessment PAYE"
            )
            sb.append(headers.joinToString(","))
            sb.append("\n")
        }

        records.forEach { record ->
            val emp = employees[record.employeeId] ?: return@forEach
            
            // Map 25 KRA columns with Blank vs Zero vs Value logic
            val housingType = if (emp.typeOfHousing == "None" || emp.typeOfHousing.isEmpty()) "Benefit not given" else emp.typeOfHousing

            val row = listOf(
                emp.kraPin,                                 // 1. PIN of Employee
                emp.fullName,                               // 2. Name of Employee
                if (emp.taxStatus == "Resident") "Resident" else "Non-Resident", // 3. Resident Status
                "Primary Employee",                         // 4. Type of Employee (Fixed as per requirement)
                if (emp.pwdStatus) "Yes" else "No",         // 5. Persons With Disability
                if (emp.pwdStatus) emp.exemptionCertificateNumber else "", // 6. Exemption Certificate (Blank if No)
                formatAmount(record.grossSalary),           // 7. Total Cash Pay (Basic + Allowances)
                formatAmount(0.0),                          // 8. Value of Car (Zero if none)
                formatAmount(record.valueOfMeals),          // 9. Value of Meals (Zero if none)
                formatAmount(record.nonCashBenefits),       // 10. Non-Cash Benefits (Zero if none)
                housingType,                                // 11. Type of Housing
                if (housingType == "Benefit not given") "" else formatAmount(record.housingBenefit), // 12. Housing Benefits (Blank if none)
                formatAmount(record.otherBenefits),         // 13. Other Benefits (Zero if none)
                "",                                         // 14. Total Gross Pay (BLANK - Template calculates)
                formatAmount(record.shifDeduction),         // 15. SHA / SHIF
                formatAmount(record.nssfDeduction),         // 16. NSSF Contribution
                formatAmount(record.otherPension),          // 17. Other Pension (Ensured numeric 0 instead of blank)
                formatAmount(record.prmfContribution),      // 18. Post-Retirement Medical Fund (Zero if none)
                formatAmount(record.mortgageInterest),      // 19. Mortgage Interest (Zero if none)
                formatAmount(record.housingLevyDeduction),  // 20. Affordable Housing Levy
                "",                                         // 21. Taxable Pay (BLANK - Template calculates)
                formatAmount(record.personalRelief),        // 22. Monthly Personal Relief
                formatAmount(record.insuranceRelief),       // 23. Insurance Relief (Zero if none)
                "",                                         // 24. PAYE Tax (BLANK - Template calculates)
                formatAmount(record.payeAmount)             // 25. Self-Assessment PAYE (Equal to PAYE in X)
            )
            sb.append(row.map { it.replace(",", ";") }.joinToString(","))
            sb.append("\n")
        }
        
        return sb.toString()
    }
}
