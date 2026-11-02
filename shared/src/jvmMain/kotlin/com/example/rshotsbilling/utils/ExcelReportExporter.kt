package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelReportExporter {

    fun exportReport(
        logoBytes: ByteArray?,
        sales: List<Sale>,
        purchases: List<Purchase>,
        range: String
    ): ByteArray {
        val workbook = XSSFWorkbook()

        // 1. SUMMARY SHEET
        val summarySheet = workbook.createSheet("Financial Summary")
        val titleRow = summarySheet.createRow(0)
        titleRow.createCell(0).setCellValue("${BusinessConfig.COMPANY_NAME} - BUSINESS REPORT")
        
        val summaryHeader = summarySheet.createRow(1)
        summaryHeader.createCell(0).setCellValue("Report Range")
        summaryHeader.createCell(1).setCellValue(range)

        val totalSales = sales.sumOf { it.total }
        val totalExp = purchases.sumOf { it.totalAmount }

        summarySheet.createRow(3).apply { createCell(0).setCellValue("Total Sales"); createCell(1).setCellValue(totalSales) }
        summarySheet.createRow(4).apply { createCell(0).setCellValue("Total Expenses"); createCell(1).setCellValue(totalExp) }
        summarySheet.createRow(5).apply { createCell(0).setCellValue("Net Profit"); createCell(1).setCellValue(totalSales - totalExp) }

        // 2. SALES SHEET
        val salesSheet = workbook.createSheet("Sales Details")
        salesSheet.createRow(0).createCell(0).setCellValue(BusinessConfig.COMPANY_NAME)
        if (logoBytes != null) addLogo(logoBytes, salesSheet, workbook)

        val sHeader = salesSheet.createRow(5)
        listOf("Date", "Invoice", "Customer", "Items", "Payment", "Total").forEachIndexed { i, h -> sHeader.createCell(i).setCellValue(h) }
        sales.forEachIndexed { index, sale ->
            val row = salesSheet.createRow(index + 6)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sale.timestamp)))
            row.createCell(1).setCellValue(sale.invoiceNumber)
            row.createCell(2).setCellValue(sale.customerName)
            row.createCell(3).setCellValue(sale.items.joinToString { "${it.serviceName}(x${it.quantity})" })
            row.createCell(4).setCellValue(sale.paymentMethod)
            row.createCell(5).setCellValue(sale.total)
        }

        // 3. M-PESA & 4. PURCHASES (kept original logic)
        val mpesaSheet = workbook.createSheet("M-Pesa Transactions")
        mpesaSheet.createRow(0).createCell(0).setCellValue("${BusinessConfig.COMPANY_NAME} - M-PESA LOG")
        val mHeader = mpesaSheet.createRow(1)
        listOf("Date", "Invoice", "M-Pesa Code", "Amount").forEachIndexed { i, h -> mHeader.createCell(i).setCellValue(h) }
        sales.filter { it.paymentMethod.contains("M-Pesa", ignoreCase = true) }.forEachIndexed { index, sale ->
            val row = mpesaSheet.createRow(index + 2)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sale.timestamp)))
            row.createCell(1).setCellValue(sale.invoiceNumber)
            row.createCell(2).setCellValue(sale.mpesaCode)
            row.createCell(3).setCellValue(sale.total)
        }

        val pSheet = workbook.createSheet("Purchases")
        pSheet.createRow(0).createCell(0).setCellValue("${BusinessConfig.COMPANY_NAME} - EXPENSES")
        val pHeader = pSheet.createRow(1)
        listOf("Date", "Item", "Supplier", "Qty", "Total").forEachIndexed { i, h -> pHeader.createCell(i).setCellValue(h) }
        purchases.forEachIndexed { index, p ->
            val row = pSheet.createRow(index + 2)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(p.date)))
            row.createCell(1).setCellValue(p.itemName)
            row.createCell(2).setCellValue(p.supplier)
            row.createCell(3).setCellValue(p.quantity.toDouble())
            row.createCell(4).setCellValue(p.totalAmount)
        }

        val stream = ByteArrayOutputStream()
        workbook.use { it.write(stream) }
        return stream.toByteArray()
    }

    fun exportPayrollReport(
        employees: Map<String, EmployeeEntity>,
        records: List<PayrollEntity>,
        month: Int,
        year: Int
    ): ByteArray {
        val workbook = XSSFWorkbook()
        
        // 1. PAYROLL REGISTER (Expanded)
        val registerSheet = workbook.createSheet("Payroll Register")
        registerSheet.createRow(0).createCell(0).setCellValue("${BusinessConfig.COMPANY_NAME} - PAYROLL REGISTER ($month/$year)")
        
        // Determine all unique allowance and deduction names to create columns
        val allowanceNames = records.flatMap { it.allowanceDetails.map { a -> a.name } }.distinct()
        val deductionNames = records.flatMap { it.deductionDetails.map { d -> d.name } }.distinct()

        val headerRow = registerSheet.createRow(2)
        val baseHeaders = mutableListOf("Name", "Basic Salary")
        baseHeaders.addAll(allowanceNames)
        baseHeaders.add("Gross Pay")
        baseHeaders.addAll(listOf("NSSF", "SHIF", "Housing Levy", "PAYE"))
        baseHeaders.addAll(deductionNames)
        baseHeaders.addAll(listOf("Net Pay", "Employer NSSF", "Employer SHIF", "Employer Levy", "Total Cost"))
        
        baseHeaders.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }
        
        records.forEachIndexed { index, record ->
            val emp = employees[record.employeeId]
            val row = registerSheet.createRow(index + 3)
            var col = 0
            row.createCell(col++).setCellValue(emp?.fullName ?: "Unknown")
            row.createCell(col++).setCellValue(record.basicSalary)
            
            // Dynamic Allowances
            allowanceNames.forEach { name ->
                val amt = record.allowanceDetails.find { it.name == name }?.amount ?: 0.0
                row.createCell(col++).setCellValue(amt)
            }
            
            row.createCell(col++).setCellValue(record.grossSalary)
            row.createCell(col++).setCellValue(record.nssfDeduction)
            row.createCell(col++).setCellValue(record.shifDeduction)
            row.createCell(col++).setCellValue(record.housingLevyDeduction)
            row.createCell(col++).setCellValue(record.payeAmount)
            
            // Dynamic Deductions
            deductionNames.forEach { name ->
                val amt = record.deductionDetails.find { it.name == name }?.amount ?: 0.0
                row.createCell(col++).setCellValue(amt)
            }
            
            row.createCell(col++).setCellValue(record.netPay)
            row.createCell(col++).setCellValue(record.employerNssf)
            row.createCell(col++).setCellValue(record.employerShif)
            row.createCell(col++).setCellValue(record.employerHousingLevy)
            row.createCell(col++).setCellValue(record.grossSalary + record.totalEmployerContribution)
        }

        // Keep NSSF and SHIF registers but simplify
        val nssfSheet = workbook.createSheet("NSSF Register")
        nssfSheet.createRow(0).createCell(0).setCellValue("NSSF CONTRIBUTIONS - $month/$year")
        val nhRow = nssfSheet.createRow(1)
        listOf("Name", "NSSF No", "Employee", "Employer", "Total").forEachIndexed { i, h -> nhRow.createCell(i).setCellValue(h) }
        records.forEachIndexed { index, r ->
            val emp = employees[r.employeeId]
            val row = nssfSheet.createRow(index + 2)
            row.createCell(0).setCellValue(emp?.fullName ?: "")
            row.createCell(1).setCellValue(emp?.nssfNumber ?: "")
            row.createCell(2).setCellValue(r.nssfDeduction)
            row.createCell(3).setCellValue(r.employerNssf)
            row.createCell(4).setCellValue(r.nssfDeduction + r.employerNssf)
        }

        val shifSheet = workbook.createSheet("SHIF Register")
        shifSheet.createRow(0).createCell(0).setCellValue("SHIF CONTRIBUTIONS - $month/$year")
        val shRow = shifSheet.createRow(1)
        listOf("Name", "SHIF No", "Employee", "Employer", "Total").forEachIndexed { i, h -> shRow.createCell(i).setCellValue(h) }
        records.forEachIndexed { index, r ->
            val emp = employees[r.employeeId]
            val row = shifSheet.createRow(index + 2)
            row.createCell(0).setCellValue(emp?.fullName ?: "")
            row.createCell(1).setCellValue(emp?.shifNumber ?: "")
            row.createCell(2).setCellValue(r.shifDeduction)
            row.createCell(3).setCellValue(r.employerShif)
            row.createCell(4).setCellValue(r.shifDeduction + r.employerShif)
        }

        val stream = ByteArrayOutputStream()
        workbook.use { it.write(stream) }
        return stream.toByteArray()
    }

    private fun addLogo(logoBytes: ByteArray, sheet: org.apache.poi.ss.usermodel.Sheet, workbook: Workbook) {
        try {
            val pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG)
            val helper = workbook.creationHelper
            val drawing = sheet.createDrawingPatriarch()
            val anchor = helper.createClientAnchor().apply { setCol1(0); setRow1(1); setCol2(2); setRow2(4) }
            drawing.createPicture(anchor, pictureIdx)
        } catch (_: Exception) {}
    }
}
