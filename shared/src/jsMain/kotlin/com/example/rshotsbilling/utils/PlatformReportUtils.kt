package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale
import com.example.rshotsbilling.model.Purchase

actual object PlatformReportUtils {
    actual fun exportExcel(sales: List<Sale>, purchases: List<Purchase>, range: String) {}
    actual fun exportPdf(sales: List<Sale>, purchases: List<Purchase>, totalSales: Double, totalExpenses: Double, profit: Double) {}
    actual fun exportPayslip(employee: com.example.rshotsbilling.model.payroll.EmployeeEntity, payroll: com.example.rshotsbilling.model.payroll.PayrollEntity) {}
    actual fun exportP9(employee: com.example.rshotsbilling.model.payroll.EmployeeEntity, records: List<com.example.rshotsbilling.model.payroll.PayrollEntity>, year: Int) {}
}
