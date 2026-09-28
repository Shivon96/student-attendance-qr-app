package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AttendanceRecord
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.util.GoogleSheetsExporter
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("StudentQR", appName)
  }

  @Test
  fun `test qr code payload generation and parsing`() {
    val student = Student(
      id = 42,
      studentCode = "STU-8821",
      name = "Test Student",
      contactNumber = "+94771234567",
      parentsContactNumber = "+94719876543",
      monthlyFee = 2500.0
    )

    val payload = QrCodeGenerator.createPayload(student)
    assertEquals("STU:42:STU-8821", payload)

    val (parsedId, parsedCode) = QrCodeGenerator.parsePayload(payload)
    assertEquals(42L, parsedId)
    assertEquals("STU-8821", parsedCode)
  }

  @Test
  fun `test google sheets csv generation`() {
    val student = Student(
      id = 1,
      studentCode = "STU-1001",
      name = "Kamal Perera",
      contactNumber = "+94 77 123 4567",
      parentsContactNumber = "+94 71 987 6543",
      monthlyFee = 2500.0
    )
    val attendance = listOf(
      AttendanceRecord(id = 1, studentId = 1, date = "2026-09-28", status = "PRESENT")
    )
    val payments = listOf(
      FeePayment(id = 1, studentId = 1, monthYear = "2026-09", amountPaid = 2500.0)
    )

    val csv = GoogleSheetsExporter.generateBackupCsv(
      students = listOf(student),
      attendance = attendance,
      payments = payments,
      currencySymbol = "Rs. "
    )

    assertTrue(csv.contains("Kamal Perera"))
    assertTrue(csv.contains("STU-1001"))
    assertTrue(csv.contains("2500.00"))
    assertTrue(csv.contains("PRESENT"))
  }
}
