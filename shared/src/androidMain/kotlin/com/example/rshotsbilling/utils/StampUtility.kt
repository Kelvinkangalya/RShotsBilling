package com.example.rshotsbilling.utils

import com.itextpdf.text.BaseColor
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.Phrase
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.BaseFont
import com.itextpdf.text.pdf.ColumnText
import com.itextpdf.text.pdf.PdfContentByte
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StampUtility {

    /**
     * Draws a professional "APPROVED" rectangular stamp on the provided PDF canvas.
     */
    fun drawStamp(cb: PdfContentByte, x: Float, y: Float) {
        cb.saveState()
        try {
            val width = 150f
            val height = 70f // Increased height slightly to prevent distortion
            
            // Blue Outer Border
            cb.setRGBColorStroke(0, 102, 204) // Professional Blue
            cb.setLineWidth(2f)
            cb.rectangle(x, y, width, height)
            cb.stroke()
            
            // Inner Thin Border
            cb.setLineWidth(0.5f)
            cb.rectangle(x + 3f, y + 3f, width - 6f, height - 6f)
            cb.stroke()

            val baseFont = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED)
            val blueColor = BaseColor(0, 102, 204)
            val redColor = BaseColor(211, 47, 47)
            
            val dateText = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())

            // 1. Company Name (Blue) - Higher positioning
            val ctTitle = ColumnText(cb)
            val titleFont = Font(Font.FontFamily.HELVETICA, 9f, Font.BOLD, blueColor)
            ctTitle.setSimpleColumn(Phrase(BusinessConfig.COMPANY_NAME.uppercase(), titleFont), x + 5, y + 48, x + width - 5, y + height - 5, 10f, Element.ALIGN_CENTER)
            ctTitle.go()

            // 2. Status: APPROVED (Red) - Centered
            val ctStatus = ColumnText(cb)
            val statusFont = Font(Font.FontFamily.HELVETICA, 13f, Font.BOLD, redColor)
            ctStatus.setSimpleColumn(Phrase("APPROVED", statusFont), x + 5, y + 28, x + width - 5, y + 48, 14f, Element.ALIGN_CENTER)
            ctStatus.go()

            // 3. Role: MANAGER & Date (Red) - Lower
            val ctFooter = ColumnText(cb)
            val footerFont = Font(Font.FontFamily.HELVETICA, 8f, Font.BOLD, redColor)
            val signer = UserSession.authorizedPerson.ifEmpty { UserSession.managerName.ifEmpty { "MANAGER" } }
            ctFooter.setSimpleColumn(Phrase("${signer.uppercase()} | $dateText", footerFont), x + 5, y + 15, x + width - 5, y + 28, 9f, Element.ALIGN_CENTER)
            ctFooter.go()
            
            // 4. Signature Line
            cb.setRGBColorStroke(211, 47, 47)
            cb.setLineWidth(1f)
            cb.moveTo(x + 15f, y + 12f)
            cb.lineTo(x + width - 15f, y + 12f)
            cb.stroke()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        cb.restoreState()
    }
}
