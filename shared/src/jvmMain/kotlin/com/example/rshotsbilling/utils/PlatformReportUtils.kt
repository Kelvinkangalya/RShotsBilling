package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity
import java.util.*
import java.io.File
import java.awt.Desktop
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.*

actual object PlatformReportUtils {
    actual fun exportExcel(sales: List<Sale>, purchases: List<Purchase>, range: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val logo = BrandingManager.getCompanyLogoBytes()
            val bytes = ExcelReportExporter.exportReport(logo, sales, purchases, range)
            saveAndOpenFile(bytes, "report_${range.replace(" ", "_")}.xlsx")
        }
    }

    actual fun exportPdf(sales: List<Sale>, purchases: List<Purchase>, totalSales: Double, totalExpenses: Double, profit: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val logo = BrandingManager.getCompanyLogoBytes()
            val bytes = PdfReportExporter.generateSummaryReport(logo, sales, purchases, totalSales, totalExpenses, profit)
            if (bytes != null) saveAndOpenFile(bytes, "summary_report.pdf")
        }
    }

    actual fun exportPayslip(employee: EmployeeEntity, payroll: PayrollEntity) {
        val bytes = PdfGenerator.generatePayslip(employee, payroll)
        saveAndOpenFile(bytes, "payslip_${employee.fullName.replace(" ", "_")}.pdf")
    }

    actual fun exportP9(employee: EmployeeEntity, summary: P9AnnualSummary) {
        val bytes = P9Generator.generateP9Pdf(employee, summary)
        saveAndOpenFile(bytes, "P9_${employee.fullName.replace(" ", "_")}_${summary.year}.pdf")
    }

    actual fun exportPayrollExcel(employees: Map<String, EmployeeEntity>, records: List<PayrollEntity>, month: Int, year: Int) {
        val bytes = ExcelReportExporter.exportPayrollReport(employees, records, month, year)
        saveAndOpenFile(bytes, "Payroll_Register_${month}_${year}.xlsx")
    }

    actual fun exportStatutoryReport(title: String, employees: Map<String, EmployeeEntity>, records: List<PayrollEntity>, month: Int, year: Int) {
        val bytes = ExcelReportExporter.exportPayrollReport(employees, records, month, year)
        saveAndOpenFile(bytes, "${title.replace(" ", "_")}_${month}_${year}.xlsx")
    }

    actual fun saveCsv(content: String, fileName: String) {
        saveAndOpenFile(content.encodeToByteArray(), fileName)
    }

    actual fun exportZip(files: List<Pair<String, ByteArray>>, zipFileName: String) {
        val bos = ByteArrayOutputStream()
        val zos = ZipOutputStream(bos)
        files.forEach { (name, bytes) ->
            val entry = ZipEntry(name)
            zos.putNextEntry(entry)
            zos.write(bytes)
            zos.closeEntry()
        }
        zos.close()
        saveAndOpenFile(bos.toByteArray(), zipFileName)
    }

    actual fun exportAllPayslips(records: List<PayrollEntity>, employees: Map<String, EmployeeEntity>, month: Int, year: Int, asZip: Boolean) {
        if (asZip) {
            val files = records.mapNotNull { record ->
                val emp = employees[record.employeeId] ?: return@mapNotNull null
                val fileName = "${emp.employeeNumber}_${emp.fullName.replace(" ", "_")}_${month}_${year}.pdf"
                val bytes = PdfGenerator.generatePayslip(emp, record)
                fileName to bytes
            }
            exportZip(files, "Payroll_Payslips_${month}_${year}.zip")
        } else {
            // For Desktop, we might just open them or save to a folder. Zip is preferred for "All".
        }
    }

    actual fun exportPayrollRegisterPdf(records: List<PayrollEntity>, employees: Map<String, EmployeeEntity>, month: Int, year: Int) {
        val bytes = PdfGenerator.generatePayrollRegister(records, employees, month, year)
        saveAndOpenFile(bytes, "Payroll_Register_${month}_${year}.pdf")
    }

    private fun saveAndOpenFile(bytes: ByteArray, fileName: String) {
        val userHome = System.getProperty("user.home")
        val dir = File(userHome, "RShotsBilling/Reports")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        file.writeBytes(bytes)
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

actual fun formatAmount(value: Double, digits: Int): String {
    return String.format(Locale.getDefault(), "%.${digits}f", value)
}
