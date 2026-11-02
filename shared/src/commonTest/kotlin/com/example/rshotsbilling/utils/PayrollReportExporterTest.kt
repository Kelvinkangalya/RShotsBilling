package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PayrollReportExporterTest {

    @Test
    fun testGenerateP10CsvStructure() {
        val employee = EmployeeEntity(
            id = "emp1",
            fullName = "John Doe",
            kraPin = "A001234567Z",
            taxStatus = "Resident",
            pwdStatus = false,
            typeOfHousing = "None"
        )
        
        val record = PayrollEntity(
            employeeId = "emp1",
            basicSalary = 50000.0,
            grossSalary = 60000.0,
            shifDeduction = 1650.0,
            nssfDeduction = 1080.0,
            housingLevyDeduction = 900.0,
            taxablePay = 55000.0,
            personalRelief = 2400.0,
            payeAmount = 5000.0
        )

        val csv = PayrollReportExporter.generateP10Csv(
            employees = mapOf("emp1" to employee),
            records = listOf(record),
            withHeaders = true
        )

        val lines = csv.trim().split("\n")
        assertEquals(2, lines.size, "CSV should have 2 lines (header + 1 record)")

        val headers = lines[0].split(",")
        assertEquals(25, headers.size, "CSV should have 25 columns")
        assertEquals("PIN of Employee", headers[0])
        assertEquals("Self-Assessment PAYE", headers[24])

        val data = lines[1].split(",")
        assertEquals(25, data.size, "Data row should have 25 columns")

        // Value checks
        assertEquals("A001234567Z", data[0], "PIN")
        assertEquals("John Doe", data[1], "Name")
        assertEquals("Resident", data[2], "Resident Status")
        assertEquals("Primary Employee", data[3], "Type of Employee")
        assertEquals("No", data[4], "PWD")
        assertEquals("", data[5], "Exemption Certificate (Should be Blank)")
        
        // Cash Pay (Col 7)
        assertTrue(data[6].toDouble() > 0, "Total Cash Pay should be populated")
        
        // Benefits (Cols 8-10)
        assertEquals("0.00", data[7], "Value of Car (Should be 0.00)")
        assertEquals("0.00", data[8], "Value of Meals (Should be 0.00)")
        assertEquals("0.00", data[9], "Non-Cash Benefits (Should be 0.00)")
        
        // Housing (Cols 11-12)
        assertEquals("Benefit not given", data[10], "Type of Housing")
        assertEquals("", data[11], "Housing Benefits (Should be Blank for Benefit not given)")
        
        // Total Gross Pay (Col 14)
        assertEquals("", data[13], "Total Gross Pay (Should be BLANK for template to calculate)")

        // Statutory (Cols 15-20)
        assertTrue(data[14].toDouble() > 0, "SHA/SHIF should be populated")
        assertTrue(data[15].toDouble() > 0, "NSSF should be populated")
        assertEquals("", data[16], "Other Pension (Should be Blank if none)")
        assertEquals("0.00", data[17], "PRMF (Should be 0.00)")
        assertEquals("0.00", data[18], "Mortgage (Should be 0.00)")
        assertTrue(data[19].toDouble() > 0, "AHL should be populated")

        // Taxable Pay (Col 21)
        assertEquals("", data[20], "Taxable Pay (Should be BLANK for template to calculate)")

        // Reliefs (Cols 22-23)
        assertEquals("2400.00", data[21], "Personal Relief")
        assertEquals("0.00", data[22], "Insurance Relief (Should be 0.00)")

        // PAYE (Cols 24-25)
        assertEquals("", data[23], "PAYE Tax (Should be BLANK for template to calculate)")
        assertTrue(data[24].toDouble() > 0, "Self-Assessment PAYE (Should be equal to PAYE amount)")
    }
}
