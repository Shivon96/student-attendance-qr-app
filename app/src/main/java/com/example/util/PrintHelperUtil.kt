package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.print.PrintHelper

object PrintHelperUtil {
    fun printStudentCard(context: Context, cardBitmap: Bitmap, jobName: String = "Student_Pass") {
        try {
            val printHelper = PrintHelper(context).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT
                colorMode = PrintHelper.COLOR_MODE_COLOR
            }
            printHelper.printBitmap(jobName, cardBitmap)
        } catch (e: Exception) {
            Toast.makeText(context, "Print error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
