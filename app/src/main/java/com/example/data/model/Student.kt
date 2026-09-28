package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentCode: String,
    val name: String,
    val contactNumber: String,
    val address: String = "",
    val parentsContactNumber: String,
    val monthlyFee: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
