package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fee_payments",
    foreignKeys = [
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["studentId", "monthYear"]),
        Index(value = ["monthYear"])
    ]
)
data class FeePayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val monthYear: String, // format yyyy-MM
    val amountPaid: Double,
    val paidDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash", // Cash, Bank Transfer, Online, Other
    val receiptNumber: String = "",
    val note: String = ""
)
