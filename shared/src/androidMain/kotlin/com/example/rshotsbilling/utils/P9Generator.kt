package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import java.io.ByteArrayOutputStream
import java.util.*

object P9Generator {

    fun generateP9Pdf(
        employee: EmployeeEntity,
        summary: P9AnnualSummary
    ): ByteArray {
        val stream = ByteArrayOutputStream()
        val document = Document(PageSize.A4.rotate(), 36f, 36f, 54f, 54f) // Landscape for P9
        val writer = PdfWriter.getInstance(document, stream)
        document.open()

        val logoBytes = ReportPlatform.getLogoBytes()
        DocumentBrandingUtils.addHeader(document, "TAX DEDUCTION CARD (P9A) - YEAR ${summary.year}", logoBytes)

        val headerFont = Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD)
        val normalFont = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL)
        val boldFont = Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD)

        // Employee Info Table
        val infoTable = PdfPTable(2)
        infoTable.widthPercentage = 100f
        infoTable.setWidths(floatArrayOf(50f, 50f))
        infoTable.spacingAfter = 10f
        
        addInfoCell(infoTable, "Employer's Name:", BusinessConfig.COMPANY_NAME, normalFont)
        addInfoCell(infoTable, "Employee's Name:", employee.fullName, normalFont)
        addInfoCell(infoTable, "Employee's PIN:", employee.kraPin, normalFont)
        addInfoCell(infoTable, "Personal Number (PN):", employee.pnNumber, normalFont)
        
        document.add(infoTable)

        // Main P9 Table
        val table = PdfPTable(14)
        table.widthPercentage = 100f
        val widths = floatArrayOf(8f, 7f, 7f, 7f, 7f, 7f, 7f, 7f, 7f, 7f, 8f, 7f, 7f, 7f)
        table.setWidths(widths)

        val p9Headers = listOf(
            "Month", "Basic Salary\n(A)", "Benefits\nNon-Cash (B)", "Value of\nQuarters (C)", "Total Gross\nPay (D)",
            "AHL\n(E1)", "SHIF\n(E2)", "NSSF\n(E3)", "Interest\n(G)", "Actual\nContribution (K)", 
            "Tax\nCharged (L)", "Personal\nRelief (M)", "Insurance\nRelief (N)", "PAYE\nTax (O)"
        )

        p9Headers.forEach { header ->
            val cell = PdfPCell(Phrase(header, headerFont))
            cell.horizontalAlignment = Element.ALIGN_CENTER
            cell.backgroundColor = BaseColor.LIGHT_GRAY
            cell.setPadding(5f)
            table.addCell(cell)
        }

        // Data Rows
        summary.months.forEach { monthData ->
            table.addCell(PdfPCell(Phrase(monthData.monthName, normalFont)).apply { setPadding(3f) })
            
            if (monthData.exists) {
                table.addCell(formatCell(monthData.basicSalary, normalFont))
                table.addCell(formatCell(monthData.benefitsNonCash, normalFont))
                table.addCell(formatCell(monthData.valueOfQuarters, normalFont))
                table.addCell(formatCell(monthData.totalGrossPay, normalFont))
                table.addCell(formatCell(monthData.ahl, normalFont))
                table.addCell(formatCell(monthData.shif, normalFont))
                table.addCell(formatCell(monthData.nssf, normalFont))
                table.addCell(formatCell(monthData.interest, normalFont)) 
                table.addCell(formatCell(0.0, normalFont)) // Actual Contribution (K) - SHOULD BE ZERO
                table.addCell(formatCell(monthData.taxCharged, normalFont)) 
                table.addCell(formatCell(monthData.personalRelief, normalFont))
                table.addCell(formatCell(monthData.insuranceRelief, normalFont))
                table.addCell(formatCell(monthData.payeTax, normalFont))
            } else {
                repeat(13) { table.addCell(formatCell(0.0, normalFont)) }
            }
        }

        // Totals Row
        table.addCell(PdfPCell(Phrase("TOTALS", boldFont)).apply { backgroundColor = BaseColor.LIGHT_GRAY; setPadding(5f) })
        table.addCell(formatCell(summary.totalA, boldFont))
        table.addCell(formatCell(summary.totalB, boldFont))
        table.addCell(formatCell(summary.totalC, boldFont))
        table.addCell(formatCell(summary.totalD, boldFont))
        table.addCell(formatCell(summary.totalE1, boldFont))
        table.addCell(formatCell(summary.totalE2, boldFont))
        table.addCell(formatCell(summary.totalE3, boldFont))
        table.addCell(formatCell(summary.totalG, boldFont))
        table.addCell(formatCell(0.0, boldFont)) // Total (K) - ZERO
        table.addCell(formatCell(summary.totalL, boldFont))
        table.addCell(formatCell(summary.totalM, boldFont))
        table.addCell(formatCell(summary.totalN, boldFont))
        table.addCell(formatCell(summary.totalO, boldFont))

        document.add(table)
        document.add(Paragraph(" "))

        // Footer Summary
        val summaryPara = Paragraph("TOTAL CHARGEABLE PAY (COL K): KES ${String.format(Locale.getDefault(), "%,.2f", summary.totalK)} | TOTAL TAX (COL O): KES ${String.format(Locale.getDefault(), "%,.2f", summary.totalO)}", boldFont)
        document.add(summaryPara)
        
        DocumentBrandingUtils.addFooter(writer, document)

        document.close()
        return stream.toByteArray()
    }

    private fun addInfoCell(table: PdfPTable, label: String, value: String, font: Font) {
        val cell1 = PdfPCell(Phrase(label, font))
        cell1.border = Rectangle.NO_BORDER
        table.addCell(cell1)
        val cell2 = PdfPCell(Phrase(value, font))
        cell2.border = Rectangle.BOTTOM
        table.addCell(cell2)
    }

    private fun formatCell(value: Double, font: Font): PdfPCell {
        val text = if (value == 0.0) "-" else String.format(Locale.getDefault(), "%,.2f", value)
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = Element.ALIGN_RIGHT
        cell.setPadding(3f)
        return cell
    }
}
