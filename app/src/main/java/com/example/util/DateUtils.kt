package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US)
    private val shortDisplayDateFormat = SimpleDateFormat("MMM dd", Locale.US)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
    private val displayMonthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    fun getTodayDateString(): String {
        return dateFormat.format(Date())
    }

    fun getCurrentMonthYearString(): String {
        return monthYearFormat.format(Date())
    }

    fun formatDisplayDate(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            if (date != null) displayDateFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatShortDisplayDate(dateString: String): String {
        return try {
            val date = dateFormat.parse(dateString)
            if (date != null) shortDisplayDateFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    fun formatDisplayTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDisplayMonthYear(monthYearString: String): String {
        return try {
            val date = monthYearFormat.parse(monthYearString)
            if (date != null) displayMonthYearFormat.format(date) else monthYearString
        } catch (e: Exception) {
            monthYearString
        }
    }

    fun getMonthYearString(offsetMonths: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, offsetMonths)
        return monthYearFormat.format(calendar.time)
    }

    fun getDaysInMonth(monthYearString: String): Int {
        return try {
            val date = monthYearFormat.parse(monthYearString) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        } catch (e: Exception) {
            30
        }
    }
}
