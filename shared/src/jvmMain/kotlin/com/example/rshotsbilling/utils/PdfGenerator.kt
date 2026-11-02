package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generatePayslip(
        employee: EmployeeEntity,
        payroll: PayrollEntity
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        val document = Document(PageSize.A4, 36f, 36f, 54f, 54f)
        val writer = PdfWriter.getInstance(document, stream)
        document.open()

        val logoBytes = ReportPlatform.getLogoBytes()
        DocumentBrandingUtils.addHeader(document, "EMPLOYEE PAY SLIP", logoBytes)

        val subTitleFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL)
        val boldFont = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD)
        val smallFont = Font(Font.FontFamily.HELVETICA, 7f, Font.NORMAL)

        val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
            Calendar.getInstance().apply {
                set(Calendar.MONTH, payroll.month - 1)
                set(Calendar.YEAR, payroll.year)
            }.time
        ).uppercase()
        
        val periodPara = Paragraph("PAYROLL PERIOD: $monthName", subTitleFont)
        periodPara.alignment = Element.ALIGN_CENTER
        periodPara.spacingAfter = 10f
        document.add(periodPara)

        // Employee Info Table
        val detailsTable = PdfPTable(4)
        detailsTable.widthPercentage = 100f
        detailsTable.setWidths(floatArrayOf(20f, 30f, 20f, 30f))
        detailsTable.spacingAfter = 15f
        
        addDetailCell(detailsTable, "Employee No:", employee.employeeNumber, normalFont)
        addDetailCell(detailsTable, "Name:", employee.fullName, normalFont)
        addDetailCell(detailsTable, "National ID:", employee.nationalId, normalFont)
        addDetailCell(detailsTable, "KRA PIN:", employee.kraPin, normalFont)
        addDetailCell(detailsTable, "Department:", employee.department, normalFont)
        addDetailCell(detailsTable, "Position:", employee.jobTitle, normalFont)
        addDetailCell(detailsTable, "Terms:", employee.termsOfService, normalFont)
        addDetailCell(detailsTable, "Status:", employee.status, normalFont)
        addDetailCell(detailsTable, "Bank:", employee.bankName, normalFont)
        addDetailCell(detailsTable, "Account No:", employee.accountNumber, normalFont)
        
        document.add(detailsTable)

        // Earnings and Deductions
        val mainTable = PdfPTable(2)
        mainTable.widthPercentage = 100f
        mainTable.setWidths(floatArrayOf(50f, 50f))

        // Earnings
        val earningsTable = PdfPTable(2)
        earningsTable.widthPercentage = 100f
        val eHeader = PdfPCell(Phrase("EARNINGS", subTitleFont))
        eHeader.colspan = 2
        eHeader.backgroundColor = BaseColor.LIGHT_GRAY
        eHeader.setPadding(5f)
        earningsTable.addCell(eHeader)

        addTSCDataRow(earningsTable, "Basic Salary", payroll.basicSalary, normalFont)
        if (payroll.overtimeAmount > 0) addTSCDataRow(earningsTable, "Overtime", payroll.overtimeAmount, normalFont)
        if (payroll.bonusAmount > 0) addTSCDataRow(earningsTable, "Bonus", payroll.bonusAmount, normalFont)
        
        // Detailed Allowances
        payroll.allowanceDetails.forEach {
            addTSCDataRow(earningsTable, it.name, it.calculatedAmount, normalFont)
        }
        
        if (payroll.nonCashBenefits > 0) addTSCDataRow(earningsTable, "Non-Cash Benefits", payroll.nonCashBenefits, normalFont)
        
        repeat(2) { addTSCDataRow(earningsTable, "", null, normalFont) }

        val totalEarningsCell = PdfPCell(Phrase("GROSS PAY", boldFont))
        totalEarningsCell.setPadding(5f)
        earningsTable.addCell(totalEarningsCell)
        val totalEarningsVal = PdfPCell(Phrase(String.format(Locale.getDefault(), "%,.2f", payroll.grossSalary), boldFont))
        totalEarningsVal.horizontalAlignment = Element.ALIGN_RIGHT
        totalEarningsVal.setPadding(5f)
        earningsTable.addCell(totalEarningsVal)

        // Deductions
        val deductionsTable = PdfPTable(2)
        deductionsTable.widthPercentage = 100f
        val dHeader = PdfPCell(Phrase("DEDUCTIONS", subTitleFont))
        dHeader.colspan = 2
        dHeader.backgroundColor = BaseColor.LIGHT_GRAY
        dHeader.setPadding(5f)
        deductionsTable.addCell(dHeader)

        addTSCDataRow(deductionsTable, "PAYE Tax", payroll.payeAmount, normalFont)
        if (payroll.personalRelief > 0) addTSCDataRow(deductionsTable, "Personal Relief", -payroll.personalRelief, normalFont)
        if (payroll.insuranceRelief > 0) addTSCDataRow(deductionsTable, "Insurance Relief", -payroll.insuranceRelief, normalFont)
        
        addTSCDataRow(deductionsTable, "NSSF Contribution", payroll.nssfDeduction, normalFont)
        addTSCDataRow(deductionsTable, "SHIF/SHA", payroll.shifDeduction, normalFont)
        addTSCDataRow(deductionsTable, "Housing Levy", payroll.housingLevyDeduction, normalFont)
        
        if (payroll.otherPension > 0) addTSCDataRow(deductionsTable, "Pension", payroll.otherPension, normalFont)
        if (payroll.salaryAdvance > 0) addTSCDataRow(deductionsTable, "Salary Advance", payroll.salaryAdvance, normalFont)
        if (payroll.loanRepayment > 0) addTSCDataRow(deductionsTable, "Loan Repayment", payroll.loanRepayment, normalFont)
        if (payroll.otherDeductions > 0) addTSCDataRow(deductionsTable, "Other Deductions", payroll.otherDeductions, normalFont)
        
        // Detailed Deductions
        payroll.deductionDetails.forEach {
            addTSCDataRow(deductionsTable, it.name, it.calculatedAmount, normalFont)
        }

        val totalDeductions = payroll.grossSalary - payroll.netPay
        
        val totalDeductionsCell = PdfPCell(Phrase("TOTAL DEDUCTIONS", boldFont))
        totalDeductionsCell.setPadding(5f)
        deductionsTable.addCell(totalDeductionsCell)
        val totalDeductionsVal = PdfPCell(Phrase(String.format(Locale.getDefault(), "%,.2f", totalDeductions), boldFont))
        totalDeductionsVal.horizontalAlignment = Element.ALIGN_RIGHT
        totalDeductionsVal.setPadding(5f)
        deductionsTable.addCell(totalDeductionsVal)

        mainTable.addCell(PdfPCell(earningsTable).apply { border = Rectangle.NO_BORDER; paddingRight = 5f })
        mainTable.addCell(PdfPCell(deductionsTable).apply { border = Rectangle.NO_BORDER; paddingLeft = 5f })
        document.add(mainTable)
        document.add(Paragraph(" "))

        // Summary Table (Net Pay)
        val summaryTable = PdfPTable(2)
        summaryTable.widthPercentage = 100f
        summaryTable.setWidths(floatArrayOf(70f, 30f))
        
        val netTitleCell = PdfPCell(Phrase("NET PAYABLE", Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)))
        netTitleCell.backgroundColor = BaseColor.YELLOW
        netTitleCell.setPadding(10f)
        summaryTable.addCell(netTitleCell)
        
        val netValCell = PdfPCell(Phrase("${UserSession.currency} " + String.format(Locale.getDefault(), "%,.2f", payroll.netPay), Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)))
        netValCell.backgroundColor = BaseColor.YELLOW
        netValCell.horizontalAlignment = Element.ALIGN_RIGHT
        netValCell.setPadding(10f)
        summaryTable.addCell(netValCell)
        
        document.add(summaryTable)
        document.add(Paragraph(" "))

        // Employer Contributions
        val contribHeader = Paragraph("EMPLOYER CONTRIBUTIONS", boldFont)
        document.add(contribHeader)
        val contribPara = Paragraph("NSSF: ${formatAmount(payroll.employerNssf)} | SHIF: ${formatAmount(payroll.employerShif)} | Housing Levy: ${formatAmount(payroll.employerHousingLevy)}", smallFont)
        document.add(contribPara)

        val auditInfo = Paragraph("\nGenerated By: ${payroll.processedBy} | Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date(payroll.processedAt))}", smallFont)
        document.add(auditInfo)

        DocumentBrandingUtils.addFooter(writer, document)

        document.close()
        return stream.toByteArray()
    }

    fun generatePayrollRegister(
        records: List<PayrollEntity>,
        employees: Map<String, EmployeeEntity>,
        month: Int,
        year: Int
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        val document = Document(PageSize.A4.rotate(), 20f, 20f, 36f, 36f)
        val writer = PdfWriter.getInstance(document, stream)
        document.open()

        val logoBytes = ReportPlatform.getLogoBytes()
        DocumentBrandingUtils.addHeader(document, "PAYROLL REGISTER - $month/$year", logoBytes)

        val headerFont = Font(Font.FontFamily.HELVETICA, 7f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 7f, Font.NORMAL)
        val boldFont = Font(Font.FontFamily.HELVETICA, 7f, Font.BOLD)

        val table = PdfPTable(13)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(7f, 10f, 8f, 7f, 7f, 7f, 7f, 7f, 7f, 7f, 7f, 10f, 10f))
        
        val headers = listOf("Emp No", "Name", "Dept", "Basic", "Allow", "Gross", "SHIF", "NSSF", "Levy", "PAYE", "Other", "Net Pay", "Employer")
        headers.forEach {
            table.addCell(PdfPCell(Phrase(it, headerFont)).apply { 
                backgroundColor = BaseColor.LIGHT_GRAY
                horizontalAlignment = Element.ALIGN_CENTER
            })
        }

        records.forEach { record ->
            val emp = employees[record.employeeId]
            table.addCell(PdfPCell(Phrase(emp?.employeeNumber ?: "", normalFont)))
            table.addCell(PdfPCell(Phrase(emp?.fullName ?: "Unknown", normalFont)))
            table.addCell(PdfPCell(Phrase(emp?.department ?: "", normalFont)))
            table.addCell(formatCell(record.basicSalary, normalFont))
            table.addCell(formatCell(record.allowanceDetails.sumOf { it.calculatedAmount }, normalFont))
            table.addCell(formatCell(record.grossSalary, normalFont))
            table.addCell(formatCell(record.shifDeduction, normalFont))
            table.addCell(formatCell(record.nssfDeduction, normalFont))
            table.addCell(formatCell(record.housingLevyDeduction, normalFont))
            table.addCell(formatCell(record.payeAmount, normalFont))
            val other = record.grossSalary - record.netPay - record.payeAmount - record.shifDeduction - record.nssfDeduction - record.housingLevyDeduction
            table.addCell(formatCell(other, normalFont))
            table.addCell(formatCell(record.netPay, boldFont))
            table.addCell(formatCell(record.totalEmployerContribution, normalFont))
        }

        // Totals
        table.addCell(PdfPCell(Phrase("TOTALS", boldFont)).apply { colspan = 3; backgroundColor = BaseColor.LIGHT_GRAY })
        table.addCell(formatCell(records.sumOf { it.basicSalary }, boldFont))
        table.addCell(formatCell(records.sumOf { it.allowanceDetails.sumOf { a -> a.calculatedAmount } }, boldFont))
        table.addCell(formatCell(records.sumOf { it.grossSalary }, boldFont))
        table.addCell(formatCell(records.sumOf { it.shifDeduction }, boldFont))
        table.addCell(formatCell(records.sumOf { it.nssfDeduction }, boldFont))
        table.addCell(formatCell(records.sumOf { it.housingLevyDeduction }, boldFont))
        table.addCell(formatCell(records.sumOf { it.payeAmount }, boldFont))
        val totalOther = records.sumOf { it.grossSalary - it.netPay - it.payeAmount - it.shifDeduction - it.nssfDeduction - it.housingLevyDeduction }
        table.addCell(formatCell(totalOther, boldFont))
        table.addCell(formatCell(records.sumOf { it.netPay }, boldFont))
        table.addCell(formatCell(records.sumOf { it.totalEmployerContribution }, boldFont))

        document.add(table)
        
        val footer = Paragraph("\nGenerated By: ${UserSession.displayName} | Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())}", normalFont)
        document.add(footer)

        DocumentBrandingUtils.addFooter(writer, document)
        document.close()
        return stream.toByteArray()
    }

    private fun addDetailCell(table: PdfPTable, label: String, value: String, font: Font) {
        val cell1 = PdfPCell(Phrase(label, Font(font.family, font.size, Font.BOLD)))
        cell1.border = Rectangle.NO_BORDER
        table.addCell(cell1)
        val cell2 = PdfPCell(Phrase(value, font))
        cell2.border = Rectangle.NO_BORDER
        table.addCell(cell2)
    }

    private fun addTSCDataRow(table: PdfPTable, label: String, amount: Double?, font: Font) {
        val cell1 = PdfPCell(Phrase(label, font))
        cell1.border = Rectangle.BOTTOM
        cell1.setPadding(3f)
        table.addCell(cell1)
        
        val valText = if (amount != null && label.isNotEmpty()) String.format(Locale.getDefault(), "%,.2f", amount) else ""
        val cell2 = PdfPCell(Phrase(valText, font))
        cell2.border = Rectangle.BOTTOM
        cell2.horizontalAlignment = Element.ALIGN_RIGHT
        cell2.setPadding(3f)
        table.addCell(cell2)
    }

    private fun formatCell(value: Double, font: Font): PdfPCell {
        val cell = PdfPCell(Phrase(String.format(Locale.getDefault(), "%,.2f", value), font))
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        return cell
    }
}
