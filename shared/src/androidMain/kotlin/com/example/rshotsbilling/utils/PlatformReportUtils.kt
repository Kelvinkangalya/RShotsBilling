package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.model.payroll.EmployeeEntity
import com.example.rshotsbilling.model.payroll.PayrollEntity
import java.util.*

actual object PlatformReportUtils {
    var exportExcelCallback: ((List<Sale>, List<Purchase>, String) -> Unit)? = null
    var exportPdfCallback: ((List<Sale>, List<Purchase>, Double, Double, Double) -> Unit)? = null
    var exportPayslipCallback: ((EmployeeEntity, PayrollEntity) -> Unit)? = null
    var exportP9Callback: ((EmployeeEntity, P9AnnualSummary) -> Unit)? = null
    var exportPayrollExcelCallback: ((Map<String, EmployeeEntity>, List<PayrollEntity>, Int, Int) -> Unit)? = null
    var exportStatutoryReportCallback: ((String, Map<String, EmployeeEntity>, List<PayrollEntity>, Int, Int) -> Unit)? = null
    var saveCsvCallback: ((String, String) -> Unit)? = null
    var exportZipCallback: ((List<Pair<String, ByteArray>>, String) -> Unit)? = null
    var exportAllPayslipsCallback: ((List<PayrollEntity>, Map<String, EmployeeEntity>, Int, Int, Boolean) -> Unit)? = null
    var exportPayrollRegisterPdfCallback: ((List<PayrollEntity>, Map<String, EmployeeEntity>, Int, Int) -> Unit)? = null

    actual fun exportExcel(sales: List<Sale>, purchases: List<Purchase>, range: String) {
        exportExcelCallback?.invoke(sales, purchases, range)
    }

    actual fun exportPdf(sales: List<Sale>, purchases: List<Purchase>, totalSales: Double, totalExpenses: Double, profit: Double) {
        exportPdfCallback?.invoke(sales, purchases, totalSales, totalExpenses, profit)
    }

    actual fun exportPayslip(employee: EmployeeEntity, payroll: PayrollEntity) {
        exportPayslipCallback?.invoke(employee, payroll)
    }

    actual fun exportP9(employee: EmployeeEntity, summary: P9AnnualSummary) {
        exportP9Callback?.invoke(employee, summary)
    }

    actual fun exportPayrollExcel(employees: Map<String, EmployeeEntity>, records: List<PayrollEntity>, month: Int, year: Int) {
        exportPayrollExcelCallback?.invoke(employees, records, month, year)
    }

    actual fun exportStatutoryReport(title: String, employees: Map<String, EmployeeEntity>, records: List<PayrollEntity>, month: Int, year: Int) {
        exportStatutoryReportCallback?.invoke(title, employees, records, month, year)
    }

    actual fun saveCsv(content: String, fileName: String) {
        saveCsvCallback?.invoke(content, fileName)
    }

    actual fun exportZip(files: List<Pair<String, ByteArray>>, zipFileName: String) {
        exportZipCallback?.invoke(files, zipFileName)
    }

    actual fun exportAllPayslips(records: List<PayrollEntity>, employees: Map<String, EmployeeEntity>, month: Int, year: Int, asZip: Boolean) {
        exportAllPayslipsCallback?.invoke(records, employees, month, year, asZip)
    }

    actual fun exportPayrollRegisterPdf(records: List<PayrollEntity>, employees: Map<String, EmployeeEntity>, month: Int, year: Int) {
        exportPayrollRegisterPdfCallback?.invoke(records, employees, month, year)
    }
}

actual fun formatAmount(value: Double, digits: Int): String {
    return String.format(Locale.getDefault(), "%.${digits}f", value)
}
