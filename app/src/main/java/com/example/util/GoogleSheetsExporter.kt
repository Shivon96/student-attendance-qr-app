package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.FeePayment
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GoogleSheetsExporter {

    private const val AUTO_BACKUP_FILE_NAME = "StudentQR_AutoBackup_GoogleSheets.csv"

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    /**
     * Builds a comprehensive CSV spreadsheet containing Students, Attendance Logs, and Fee Payments.
     */
    fun generateBackupCsv(
        students: List<Student>,
        attendance: List<AttendanceRecord>,
        payments: List<FeePayment>,
        currencySymbol: String = "Rs. "
    ): String {
        val studentMap = students.associateBy { it.id }
        val sb = StringBuilder()

        // Section 1: Students Directory
        sb.append("=== STUDENTS DIRECTORY ===\n")
        sb.append("Student ID,Code,Name,Student Contact,Parent Contact,Address,Monthly Fee ($currencySymbol),Status,Registered Date\n")
        students.forEach { s ->
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(s.createdAt))
            sb.append("${s.id},")
            sb.append("${escapeCsv(s.studentCode)},")
            sb.append("${escapeCsv(s.name)},")
            sb.append("${escapeCsv(s.contactNumber)},")
            sb.append("${escapeCsv(s.parentsContactNumber)},")
            sb.append("${escapeCsv(s.address)},")
            sb.append("${String.format(Locale.US, "%.2f", s.monthlyFee)},")
            sb.append("${if (s.isActive) "Active" else "Inactive"},")
            sb.append("$dateStr\n")
        }

        sb.append("\n\n")

        // Section 2: Attendance Records
        sb.append("=== ATTENDANCE RECORDS ===\n")
        sb.append("Record ID,Date,Student Name,Student Code,Check-in Time,Status\n")
        attendance.forEach { a ->
            val s = studentMap[a.studentId]
            val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(a.timestamp))
            sb.append("${a.id},")
            sb.append("${a.date},")
            sb.append("${escapeCsv(s?.name ?: "Unknown")},")
            sb.append("${escapeCsv(s?.studentCode ?: "")},")
            sb.append("$timeStr,")
            sb.append("${a.status}\n")
        }

        sb.append("\n\n")

        // Section 3: Fee Payment Records
        sb.append("=== FEE PAYMENT RECORDS ===\n")
        sb.append("Payment ID,Payment Date,Student Name,Student Code,Fee Month,Amount Paid ($currencySymbol),Payment Method,Note\n")
        payments.forEach { p ->
            val s = studentMap[p.studentId]
            val payDateStr = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(p.paidDate))
            sb.append("${p.id},")
            sb.append("$payDateStr,")
            sb.append("${escapeCsv(s?.name ?: "Unknown")},")
            sb.append("${escapeCsv(s?.studentCode ?: "")},")
            sb.append("${p.monthYear},")
            sb.append("${String.format(Locale.US, "%.2f", p.amountPaid)},")
            sb.append("${escapeCsv(p.paymentMethod)},")
            sb.append("${escapeCsv(p.note)}\n")
        }

        return sb.toString()
    }

    /**
     * Automatically writes latest backup CSV file to local storage.
     */
    fun writeAutoBackupCsv(
        context: Context,
        students: List<Student>,
        attendance: List<AttendanceRecord>,
        payments: List<FeePayment>,
        currencySymbol: String = "Rs. "
    ): File? {
        return try {
            val csvContent = generateBackupCsv(students, attendance, payments, currencySymbol)
            val docsFolder = File(context.cacheDir, "docs").apply { mkdirs() }
            val file = File(docsFolder, AUTO_BACKUP_FILE_NAME)
            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }
            file
        } catch (e: Exception) {
            null
        }
    }

    fun getAutoBackupLastModified(context: Context): String? {
        val docsFolder = File(context.cacheDir, "docs")
        val file = File(docsFolder, AUTO_BACKUP_FILE_NAME)
        return if (file.exists()) {
            SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US).format(Date(file.lastModified()))
        } else {
            null
        }
    }

    /**
     * Exports and opens file in Google Drive or Google Sheets
     */
    fun exportToGoogleDriveOrSheets(
        context: Context,
        students: List<Student>,
        attendance: List<AttendanceRecord>,
        payments: List<FeePayment>,
        currencySymbol: String = "Rs. ",
        preferGoogleDrive: Boolean = false
    ) {
        try {
            val file = writeAutoBackupCsv(context, students, attendance, payments, currencySymbol)
            if (file == null) {
                Toast.makeText(context, "Could not generate backup file", Toast.LENGTH_SHORT).show()
                return
            }

            val fileUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "StudentQR Google Sheets Auto-Backup")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Student Attendance & Fee Records Backup.\nCompatible with Google Sheets & Google Drive."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (preferGoogleDrive) {
                shareIntent.setPackage("com.google.android.apps.docs")
                try {
                    context.startActivity(shareIntent)
                    return
                } catch (_: Exception) {
                    shareIntent.setPackage(null)
                }
            }

            val chooser = Intent.createChooser(shareIntent, "Open in Google Sheets / Drive")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
