package com.example.rshotsbilling.utils

import com.example.rshotsbilling.model.Sale
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.itextpdf.text.pdf.draw.LineSeparator
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReceiptGenerator {

    fun generateReceipt(logoBytes: ByteArray?, sale: Sale): ByteArray? {
        try {
            val stream = ByteArrayOutputStream()
            val baseHeight = 450f
            val itemHeight = 25f
            val calculatedHeight = baseHeight + (sale.items.size * itemHeight)
            val pageSize = Rectangle(PageSize.A4.width, calculatedHeight.coerceAtLeast(PageSize.A4.height))

            val document = Document(pageSize, 32f, 32f, 40f, 40f)
            val writer = PdfWriter.getInstance(document, stream)
            writer.setPdfVersion(PdfWriter.VERSION_1_4)
            document.open()

            val canvas: PdfContentByte = writer.directContent
            drawGradientLine(canvas, 32f, pageSize.top - 40f, pageSize.width - 32f, pageSize.top - 30f)
            drawGradientWave(canvas, 32f, 30f, pageSize.width - 32f, 60f)

            if (logoBytes != null) {
                try {
                    val image = Image.getInstance(logoBytes)
                    image.scaleToFit(120f, 120f)
                    image.alignment = Element.ALIGN_CENTER
                    document.add(image)
                } catch (_: Exception) {}
            }

            val companyFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
            val boldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)

            val headerText = if (sale.isEtims) "TAX INVOICE (eTIMS)" else "CASH RECEIPT"
            val header = Paragraph(headerText, Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD, BaseColor.RED))
            header.alignment = Element.ALIGN_CENTER
            document.add(header)

            val company = Paragraph(BusinessConfig.COMPANY_NAME, companyFont)
            company.alignment = Element.ALIGN_CENTER
            document.add(company)

            val companyPin = Paragraph("PIN: ${BusinessConfig.BUSINESS_KRA_PIN}", boldFont)
            companyPin.alignment = Element.ALIGN_CENTER
            document.add(companyPin)

            val companyContacts = Paragraph("Phone: ${BusinessConfig.COMPANY_PHONE} | Email: ${BusinessConfig.COMPANY_EMAIL}", normalFont)
            companyContacts.alignment = Element.ALIGN_CENTER
            document.add(companyContacts)

            val companyTill = Paragraph("LIPA NA MPESA TILL: ${BusinessConfig.TILL_NUMBER}", boldFont)
            companyTill.alignment = Element.ALIGN_CENTER
            document.add(companyTill)

            document.add(Paragraph(" "))
            document.add(LineSeparator())

            val invoiceNumber = if (sale.invoiceNumber.isNotEmpty()) sale.invoiceNumber else "INV-${System.currentTimeMillis()}"
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(sale.timestamp))
            document.add(Paragraph("Invoice #: $invoiceNumber", normalFont))
            document.add(Paragraph("Date: $dateStr", normalFont))
            document.add(Paragraph("Served By: ${sale.servedBy}", normalFont))
            document.add(Paragraph("Status: PAID", normalFont))

            document.add(Paragraph(" "))
            document.add(LineSeparator())

            document.add(Paragraph("BILL TO:", boldFont))
            document.add(Paragraph("Customer: ${sale.customerName}", normalFont))
            if (sale.customerPhone.isNotEmpty()) document.add(Paragraph("Phone: ${sale.customerPhone}", normalFont))
            if (sale.customerKraPin.isNotEmpty()) document.add(Paragraph("PIN: ${sale.customerKraPin}", normalFont))
            document.add(Paragraph("Payment: ${sale.paymentMethod}", normalFont))

            document.add(Paragraph(" "))
            document.add(LineSeparator())

            val table = PdfPTable(4)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(4f, 1f, 2f, 2f))

            table.addCell(createCell("Description", boldFont, 5f))
            table.addCell(createCell("Qty", boldFont, 5f, Element.ALIGN_CENTER))
            table.addCell(createCell("Unit Price", boldFont, 5f, Element.ALIGN_RIGHT))
            table.addCell(createCell("Total", boldFont, 5f, Element.ALIGN_RIGHT))

            for (item in sale.items) {
                table.addCell(createCell(item.serviceName, normalFont, 5f))
                table.addCell(createCell(String.format(Locale.getDefault(), "%.2f", item.quantity), normalFont, 5f, Element.ALIGN_CENTER))
                table.addCell(createCell(String.format(Locale.getDefault(), "%.2f", item.price), normalFont, 5f, Element.ALIGN_RIGHT))
                table.addCell(createCell(String.format(Locale.getDefault(), "%.2f", item.quantity * item.price), normalFont, 5f, Element.ALIGN_RIGHT))
            }
            document.add(table)

            document.add(Paragraph(" "))
            document.add(LineSeparator())

            val total = sale.total
            val financials = PdfPTable(2)
            financials.widthPercentage = 45f
            financials.horizontalAlignment = Element.ALIGN_RIGHT
            
            val cellFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
            val cellBoldFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            val cellTotalFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD, BaseColor.BLACK)

            if (sale.isEtims) {
                val vatRate = 0.16
                val vatAmount = total * (vatRate / (1 + vatRate))
                val subTotal = total - vatAmount

                financials.addCell(createCell("Sub-Total (Excl):", cellFont, 0f, Element.ALIGN_RIGHT, Rectangle.NO_BORDER))
                financials.addCell(createCell(String.format(Locale.getDefault(), "%.2f", subTotal), cellFont, 0f, Element.ALIGN_RIGHT, Rectangle.NO_BORDER))
                
                financials.addCell(createCell("VAT (16%):", cellFont, 0f, Element.ALIGN_RIGHT, Rectangle.NO_BORDER))
                financials.addCell(createCell(String.format(Locale.getDefault(), "%.2f", vatAmount), cellFont, 0f, Element.ALIGN_RIGHT, Rectangle.NO_BORDER))
            }
            
            val borderType = if (sale.isEtims) Rectangle.TOP else Rectangle.NO_BORDER
            financials.addCell(createCell("TOTAL PAYABLE:", cellBoldFont, 5f, Element.ALIGN_RIGHT, borderType))
            financials.addCell(createCell(String.format(Locale.getDefault(), "%.2f", total), cellTotalFont, 5f, Element.ALIGN_RIGHT, borderType))
            
            document.add(financials)

            document.add(Paragraph(" "))
            document.add(LineSeparator())

            val footerNote = if (sale.isEtims) "This is a system-generated tax invoice compliant with eTIMS standards." else "Thank you for your business. This is an official receipt of payment."
            val footerMsg = Paragraph(footerNote, normalFont)
            footerMsg.alignment = Element.ALIGN_CENTER
            document.add(footerMsg)

            val footer = Paragraph("\u00a9 ${Calendar.getInstance().get(Calendar.YEAR)} ${BusinessConfig.COMPANY_NAME}", normalFont)
            footer.alignment = Element.ALIGN_CENTER
            document.add(footer)

            StampUtility.drawStamp(writer.directContent, pageSize.width - 182f, 80f)

            document.close()
            return stream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun createCell(text: String, font: Font, p: Float = 0f, align: Int = Element.ALIGN_LEFT, border: Int = Rectangle.BOX): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.setBorder(border)
        cell.horizontalAlignment = align
        cell.setPadding(p)
        return cell
    }

    private fun drawGradientLine(canvas: PdfContentByte, x1: Float, y1: Float, x2: Float, y2: Float) {
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

    private fun drawGradientWave(canvas: PdfContentByte, x1: Float, y1: Float, x2: Float, y2: Float) {
        val steps = 200
        val width = (x2 - x1) / steps
        val maxHeight = y2 - y1
        for (i in 0 until steps) {
            val ratio = i.toFloat() / steps
            val r = (0 * (1 - ratio) + 255 * ratio).toInt()
            val g = (230 * (1 - ratio) + 23 * ratio).toInt()
            val b = (118 * (1 - ratio) + 68 * ratio).toInt()
            val waveOffset = (Math.sin(ratio * Math.PI * 4) * (maxHeight / 2)).toFloat()
            val currentHeight = (maxHeight / 2) + waveOffset
            canvas.saveState()
            canvas.setRGBColorFill(r, g, b)
            canvas.rectangle(x1 + (i * width), y1, width, currentHeight + 5f)
            canvas.fill()
            canvas.restoreState()
        }
    }
}
