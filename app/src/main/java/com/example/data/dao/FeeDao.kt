package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FeePayment
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {
    @Query("SELECT * FROM fee_payments WHERE monthYear = :monthYear ORDER BY paidDate DESC")
    fun getPaymentsForMonth(monthYear: String): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments ORDER BY paidDate DESC")
    fun getAllPayments(): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY paidDate DESC")
    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId AND monthYear = :monthYear")
    fun getPaymentsForStudentAndMonth(studentId: Long, monthYear: String): Flow<List<FeePayment>>

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId AND monthYear = :monthYear")
    suspend fun getPaymentsForStudentAndMonthOnce(studentId: Long, monthYear: String): List<FeePayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: FeePayment): Long

    @Delete
    suspend fun deletePayment(payment: FeePayment)

    @Query("DELETE FROM fee_payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    @Query("SELECT SUM(amountPaid) FROM fee_payments WHERE monthYear = :monthYear")
    fun getTotalCollectedForMonth(monthYear: String): Flow<Double?>
}
