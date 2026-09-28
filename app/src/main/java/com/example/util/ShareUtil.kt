package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream

object ShareUtil {

    fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val imagesFolder = File(context.cacheDir, "images").apply { mkdirs() }
            val file = File(imagesFolder, fileName)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Share Student QR Card directly to WhatsApp or app chooser.
     */
    fun shareStudentCard(
        context: Context,
        student: Student,
        cardBitmap: Bitmap,
        preferWhatsApp: Boolean = false,
        currencySymbol: String = "Rs. "
    ) {
        val uri = saveBitmapToCache(context, cardBitmap, "student_${student.studentCode}_card.png")
        if (uri == null) {
            Toast.makeText(context, "Failed to create shareable image", Toast.LENGTH_SHORT).show()
            return
        }

        val feeStr = String.format(java.util.Locale.US, "%,.2f", student.monthlyFee)
        val shareMessage = buildString {
            append("🎓 *Student Attendance Pass*\n")
            append("• Name: *${student.name}*\n")
            append("• Student ID: *${student.studentCode}*\n")
            append("• Monthly Fee: *${currencySymbol}${feeStr}*\n")
            if (student.parentsContactNumber.isNotBlank()) {
                append("• Parent Contact: ${student.parentsContactNumber}\n")
            }
            append("\nPlease keep this QR code ready for scanning during class attendance.")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        if (preferWhatsApp) {
            shareIntent.setPackage("com.whatsapp")
            try {
                context.startActivity(shareIntent)
                return
            } catch (e: Exception) {
                try {
                    shareIntent.setPackage("com.whatsapp.w4b")
                    context.startActivity(shareIntent)
                    return
                } catch (e2: Exception) {
                    shareIntent.setPackage(null)
                }
            }
        }

        val chooser = Intent.createChooser(shareIntent, "Share Student Pass")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Send direct WhatsApp message to parent/contact
     */
    fun sendWhatsAppMessageToNumber(
        context: Context,
        rawPhone: String,
        message: String
    ) {
        val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Send SMS notification to parent via native Android SMS app
     */
    fun sendSmsToNumber(
        context: Context,
        rawPhone: String,
        message: String
    ) {
        val cleanPhone = rawPhone.replace(Regex("[^0-9+]"), "")
        try {
            val smsUri = Uri.parse("smsto:$cleanPhone")
            val intent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                putExtra("sms_body", message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$cleanPhone")
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Could not open SMS: ${e2.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
