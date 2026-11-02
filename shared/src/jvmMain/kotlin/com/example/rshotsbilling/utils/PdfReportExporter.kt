package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Purchase
import com.example.rshotsbilling.model.Sale
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportExporter {

    fun generateSummaryReport(
        logoBytes: ByteArray?,
        sales: List<Sale>,
        purchases: List<Purchase>,
        totalSales: Double,
        totalPurchases: Double,
        profit: Double
    ): ByteArray? {
        return try {
            val stream = ByteArrayOutputStream()
            val document = Document(PageSize.A4, 36f, 36f, 54f, 54f)
            val writer = PdfWriter.getInstance(document, stream)
            document.open()

            val canvas = writer.directContent
            // Green (#00E676) to Red (#FF1744)
            drawBrandedLine(canvas, 36f, 800f, 559f, 805f)

            // LOGO
            if (logoBytes != null) {
                try {
                    val image = Image.getInstance(logoBytes)
                    image.scaleToFit(80f, 80f)
                    image.alignment = Element.ALIGN_CENTER
                    document.add(image)
                } catch (_: Exception) {}
            }

            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val normalFont = Font(Font.FontFamily.HELVETICA, 11f, Font.NORMAL)
            val boldFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)

            val title = Paragraph("${BusinessConfig.COMPANY_NAME}\nFINANCIAL SUMMARY REPORT", titleFont)
            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("Generated on: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", normalFont).apply { alignment = Element.ALIGN_CENTER })
            document.add(Paragraph(" "))
            
            drawBrandedLine(canvas, 36f, 650f, 559f, 652f)
            document.add(Paragraph(" "))

            // Summary Table
            val table = PdfPTable(2)
            table.widthPercentage = 100f
            table.addCell(Phrase("Description", boldFont))
            table.addCell(Phrase("Amount (${UserSession.currency})", boldFont))

            table.addCell("Total Sales")
            table.addCell("${UserSession.currency} $totalSales")

            table.addCell("Total Expenses")
            table.addCell("${UserSession.currency} $totalPurchases")

            table.addCell(Phrase("NET PROFIT", boldFont))
            table.addCell(Phrase("${UserSession.currency} $profit", boldFont))

            document.add(table)
            document.add(Paragraph(" "))
            document.add(Paragraph(" "))

            // SIGNATURE AREA
            val sigTable = PdfPTable(2)
            sigTable.widthPercentage = 100f
            sigTable.spacingBefore = 50f

            val cell1 = PdfPCell(Paragraph("\n\n__________________________\nPreparing Officer", normalFont))
            cell1.border = Rectangle.NO_BORDER
            cell1.horizontalAlignment = Element.ALIGN_CENTER

            val cell2 = PdfPCell(Paragraph("\n\n__________________________\nCEO / Manager", normalFont))
            cell2.border = Rectangle.NO_BORDER
            cell2.horizontalAlignment = Element.ALIGN_CENTER

            sigTable.addCell(cell1)
            sigTable.addCell(cell2)

            document.add(sigTable)

            // ADD APPROVED STAMP
            StampUtility.drawStamp(writer.directContent, document.right() - 150f, 80f)

            document.close()
            stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawBrandedLine(canvas: com.itextpdf.text.pdf.PdfContentByte, x1: Float, y1: Float, x2: Float, y2: Float) {
        val steps = 100
        val width = (x2 - x1) / steps
        val height = y2 - y1
        for (i in 0 until steps) {
            val ratio = i.toFloat() / steps
            val r = (0 * (1 - ratio) + 255 * ratio).toInt()
            val g = (230 * (1 - ratio) + 23 * ratio).toInt()
            val b = (118 * (1 - ratio) + 68 * ratio).toInt()
            canvas.saveState()
            canvas.setRGBColorFill(r, g, b)
            canvas.rectangle(x1 + (i * width), y1, width, height)
            canvas.fill()
            canvas.restoreState()
        }
    }
}
