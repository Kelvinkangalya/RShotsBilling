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
        
        // Add Company Name at the top
        val titleRow = summarySheet.createRow(0)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("${BusinessConfig.COMPANY_NAME} - BUSINESS REPORT")
        
        val summaryHeader = summarySheet.createRow(1)
        summaryHeader.createCell(0).setCellValue("Report Range")
        summaryHeader.createCell(1).setCellValue(range)

        val totalSales = sales.sumOf { it.total }
        val totalExp = purchases.sumOf { it.totalAmount }

        val row1 = summarySheet.createRow(3)
        row1.createCell(0).setCellValue("Total Sales")
        row1.createCell(1).setCellValue(totalSales)

        val row2 = summarySheet.createRow(4)
        row2.createCell(0).setCellValue("Total Expenses")
        row2.createCell(1).setCellValue(totalExp)

        val row3 = summarySheet.createRow(5)
        row3.createCell(0).setCellValue("Net Profit")
        row3.createCell(1).setCellValue(totalSales - totalExp)

        // 2. SALES SHEET
        val salesSheet = workbook.createSheet("Sales Details")
        
        // Add Company Name to Sales Sheet as well
        val salesTitleRow = salesSheet.createRow(0)
        salesTitleRow.createCell(0).setCellValue(BusinessConfig.COMPANY_NAME)
        
        if (logoBytes != null) {
            addLogo(logoBytes, salesSheet, workbook)
        }

        val sHeader = salesSheet.createRow(5)
        val headers = listOf("Date", "Invoice", "Customer", "Items", "Payment", "Total")
        headers.forEachIndexed { i, h -> sHeader.createCell(i).setCellValue(h) }

        sales.forEachIndexed { index, sale ->
            val row = salesSheet.createRow(index + 6)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sale.timestamp)))
            row.createCell(1).setCellValue(sale.invoiceNumber)
            row.createCell(2).setCellValue(sale.customerName)
            row.createCell(3).setCellValue(sale.items.joinToString { "${it.serviceName}(x${it.quantity})" })
            row.createCell(4).setCellValue(sale.paymentMethod)
            row.createCell(5).setCellValue(sale.total)
        }

        // 3. M-PESA SHEET
        val mpesaSheet = workbook.createSheet("M-Pesa Transactions")
        val mpesaTitleRow = mpesaSheet.createRow(0)
        mpesaTitleRow.createCell(0).setCellValue("${BusinessConfig.COMPANY_NAME} - M-PESA LOG")
        
        val mHeader = mpesaSheet.createRow(1)
        listOf("Date", "Invoice", "M-Pesa Code", "Amount").forEachIndexed { i, h -> mHeader.createCell(i).setCellValue(h) }
        
        val mpesaSales = sales.filter { it.paymentMethod.contains("M-Pesa", ignoreCase = true) }
        mpesaSales.forEachIndexed { index, sale ->
            val row = mpesaSheet.createRow(index + 2)
            row.createCell(0).setCellValue(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sale.timestamp)))
            row.createCell(1).setCellValue(sale.invoiceNumber)
            row.createCell(2).setCellValue(sale.mpesaCode)
            row.createCell(3).setCellValue(sale.total)
        }

        // 4. PURCHASES SHEET
        val pSheet = workbook.createSheet("Purchases")
        val pTitleRow = pSheet.createRow(0)
        pTitleRow.createCell(0).setCellValue("${BusinessConfig.COMPANY_NAME} - EXPENSES")
        
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
        
        // 1. PAYROLL REGISTER
        val registerSheet = workbook.createSheet("Payroll Register")
        val titleRow = registerSheet.createRow(0)
        titleRow.createCell(0).setCellValue("${BusinessConfig.COMPANY_NAME} - PAYROLL REGISTER ($month/$year)")
        
        val headerRow = registerSheet.createRow(2)
        val headers = listOf("Name", "Basic Salary", "Allowances", "Gross Pay", "NSSF", "SHIF", "Housing Levy", "PAYE", "Net Pay")
        headers.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }
        
        records.forEachIndexed { index, record ->
            val emp = employees[record.employeeId]
            val row = registerSheet.createRow(index + 3)
            row.createCell(0).setCellValue(emp?.fullName ?: "Unknown")
            row.createCell(1).setCellValue(record.basicSalary)
            row.createCell(2).setCellValue(record.allowances + record.overtimeAmount + record.bonusAmount)
            row.createCell(3).setCellValue(record.grossSalary)
            row.createCell(4).setCellValue(record.nssfDeduction)
            row.createCell(5).setCellValue(record.shifDeduction)
            row.createCell(6).setCellValue(record.housingLevyDeduction)
            row.createCell(7).setCellValue(record.payeAmount)
            row.createCell(8).setCellValue(record.netPay)
        }

        // 2. NSSF REGISTER
        val nssfSheet = workbook.createSheet("NSSF Register")
        nssfSheet.createRow(0).createCell(0).setCellValue("NSSF CONTRIBUTIONS - $month/$year")
        val nssfHeaders = listOf("Name", "NSSF No", "Employee", "Employer", "Total")
        val nhRow = nssfSheet.createRow(1)
        nssfHeaders.forEachIndexed { i, h -> nhRow.createCell(i).setCellValue(h) }
        records.forEachIndexed { index, r ->
            val emp = employees[r.employeeId]
            val row = nssfSheet.createRow(index + 2)
            row.createCell(0).setCellValue(emp?.fullName ?: "")
            row.createCell(1).setCellValue(emp?.nssfNumber ?: "")
            row.createCell(2).setCellValue(r.nssfDeduction)
            row.createCell(3).setCellValue(r.employerNssf)
            row.createCell(4).setCellValue(r.nssfDeduction + r.employerNssf)
        }

        // 3. SHIF REGISTER
        val shifSheet = workbook.createSheet("SHIF Register")
        shifSheet.createRow(0).createCell(0).setCellValue("SHIF CONTRIBUTIONS - $month/$year")
        val shifHeaders = listOf("Name", "SHIF No", "Employee", "Employer", "Total")
        val shRow = shifSheet.createRow(1)
        shifHeaders.forEachIndexed { i, h -> shRow.createCell(i).setCellValue(h) }
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
            val anchor = helper.createClientAnchor()
            anchor.setCol1(0); anchor.setRow1(1) // Row 1-4 for logo
            anchor.setCol2(2); anchor.setRow2(4)
            drawing.createPicture(anchor, pictureIdx)
        } catch (_: Exception) {}
    }
}
