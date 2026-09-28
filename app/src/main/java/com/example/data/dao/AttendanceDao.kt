package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY timestamp DESC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date = :date LIMIT 1")
    suspend fun getAttendanceForStudentAndDate(studentId: Long, date: String): AttendanceRecord?

    @Query("SELECT * FROM attendance WHERE studentId = :studentId AND date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getAttendanceForStudentAndMonth(studentId: Long, monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAllAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC, timestamp DESC")
    fun getAllAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance ORDER BY date DESC, timestamp DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceRecord): Long

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceRecord)

    @Query("DELETE FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun deleteAttendanceForStudentAndDate(studentId: Long, date: String)

    @Query("SELECT COUNT(*) FROM attendance WHERE date = :date")
    fun getAttendanceCountForDate(date: String): Flow<Int>
}
