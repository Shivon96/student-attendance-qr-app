package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.example.data.model.Student
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeGenerator {

    /**
     * Generates a standard QR code payload for a student.
     * We use a clean parseable prefix: "STU:<studentId>:<studentCode>"
     * This guarantees instant parsing and backward compatibility.
     */
    fun createPayload(student: Student): String {
        return "STU:${student.id}:${student.studentCode}"
    }

    /**
     * Parse student QR code payload. Returns Pair(studentId, studentCode)
     */
    fun parsePayload(payload: String): Pair<Long?, String?> {
        val trimmed = payload.trim()
        if (trimmed.startsWith("STU:")) {
            val parts = trimmed.split(":")
            if (parts.size >= 3) {
                val id = parts[1].toLongOrNull()
                val code = parts[2]
                return Pair(id, code)
            }
        }
        // Fallback: check if the string itself is studentCode (e.g. STU-1001) or numeric ID
        val numericId = trimmed.toLongOrNull()
        return Pair(numericId, trimmed)
    }

    /**
     * Generates a clean Bitmap QR code.
     */
    fun generateQrBitmap(
        content: String,
        sizePx: Int = 512,
        foregroundColor: Int = Color.parseColor("#0F172A"),
        backgroundColor: Int = Color.WHITE
    ): Bitmap {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    /**
     * Generates a high quality Printable/Sharable Student ID Badge Card.
     */
    fun generateStudentIdCard(
        student: Student,
        currencySymbol: String = "Rs. "
    ): Bitmap {
        val qrBitmap = generateQrBitmap(createPayload(student), sizePx = 400)

        val cardWidth = 720
        val cardHeight = 1040
        val bitmap = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw card background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawColor(Color.parseColor("#F1F5F9")) // outer margin color

        // Inner card rect
        val cardRect = RectF(24f, 24f, cardWidth - 24f, cardHeight - 24f)
        canvas.drawRoundRect(cardRect, 32f, 32f, bgPaint)

        // Card border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(cardRect, 32f, 32f, borderPaint)

        // Header Background Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E3A8A") // Academic Blue
            style = Paint.Style.FILL
        }
        val headerRect = RectF(24f, 24f, cardWidth - 24f, 180f)
        canvas.drawRoundRect(headerRect, 32f, 32f, headerPaint)
        // Rectify bottom corners of header
        canvas.drawRect(24f, 120f, cardWidth - 24f, 180f, headerPaint)

        // Header Text
        val headerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("STUDENT ATTENDANCE PASS", cardWidth / 2f, 95f, headerTitlePaint)

        val headerSubPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#93C5FD")
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Official Class ID & QR Verification", cardWidth / 2f, 140f, headerSubPaint)

        // Student Name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(student.name, cardWidth / 2f, 250f, namePaint)

        // Student ID Badge
        val idBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EFF6FF")
            style = Paint.Style.FILL
        }
        val idRect = RectF(cardWidth / 2f - 140f, 275f, cardWidth / 2f + 140f, 325f)
        canvas.drawRoundRect(idRect, 16f, 16f, idBadgePaint)

        val idTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1D4ED8")
            textSize = 26f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(student.studentCode, cardWidth / 2f, 310f, idTextPaint)

        // QR Code box background
        val qrBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val qrBoxStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val qrLeft = (cardWidth - 420) / 2f
        val qrTop = 350f
        val qrRect = RectF(qrLeft, qrTop, qrLeft + 420f, qrTop + 420f)
        canvas.drawRoundRect(qrRect, 24f, 24f, qrBoxPaint)
        canvas.drawRoundRect(qrRect, 24f, 24f, qrBoxStroke)

        // Draw QR Bitmap
        canvas.drawBitmap(qrBitmap, qrLeft + 10f, qrTop + 10f, null)

        // Scan Instructions below QR
        val scanNotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Show this QR code at class entrance", cardWidth / 2f, 805f, scanNotePaint)

        // Horizontal divider
        val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 2f
        }
        canvas.drawLine(60f, 830f, cardWidth - 60f, 830f, divPaint)

        // Student Info Details (2 columns or rows)
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Row 1: Student Phone & Parent Phone
        canvas.drawText("Student Contact", 60f, 865f, labelPaint)
        canvas.drawText(student.contactNumber.ifEmpty { "N/A" }, 60f, 895f, valuePaint)

        canvas.drawText("Parent Contact", cardWidth / 2f + 20f, 865f, labelPaint)
        canvas.drawText(student.parentsContactNumber.ifEmpty { "N/A" }, cardWidth / 2f + 20f, 895f, valuePaint)

        // Row 2: Monthly Fee & Address
        canvas.drawText("Monthly Fee", 60f, 940f, labelPaint)
        val feeFormatted = "$currencySymbol${String.format(java.util.Locale.US, "%,.2f", student.monthlyFee)}"
        canvas.drawText(feeFormatted, 60f, 970f, valuePaint)

        canvas.drawText("Address", cardWidth / 2f + 20f, 940f, labelPaint)
        val shortAddr = if (student.address.length > 22) student.address.take(20) + "..." else student.address.ifEmpty { "—" }
        canvas.drawText(shortAddr, cardWidth / 2f + 20f, 970f, valuePaint)

        return bitmap
    }
}
