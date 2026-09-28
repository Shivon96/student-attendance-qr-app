package com.example.data

import com.example.data.dao.AttendanceDao
import com.example.data.dao.FeeDao
import com.example.data.dao.StudentDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.FeePayment
import com.example.data.model.Student
import kotlinx.coroutines.flow.Flow

class StudentRepository(
    private val studentDao: StudentDao,
    private val attendanceDao: AttendanceDao,
    private val feeDao: FeeDao
) {
    val allActiveStudents: Flow<List<Student>> = studentDao.getAllActiveStudents()
    val activeStudentCount: Flow<Int> = studentDao.getActiveStudentCount()

    fun getStudentById(id: Long): Flow<Student?> = studentDao.getStudentById(id)

    suspend fun getStudentByIdOnce(id: Long): Student? = studentDao.getStudentByIdOnce(id)

    suspend fun getStudentByCode(code: String): Student? = studentDao.getStudentByCode(code)

    suspend fun getStudentByIdOrCode(id: Long, code: String): Student? =
        studentDao.getStudentByIdOrCode(id, code)

    suspend fun insertStudent(student: Student): Long = studentDao.insertStudent(student)

    suspend fun updateStudent(student: Student) = studentDao.updateStudent(student)

    suspend fun deleteStudent(student: Student) = studentDao.deleteStudent(student)

    suspend fun deleteStudentById(id: Long) = studentDao.deleteStudentById(id)

    // Attendance
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDate(date)

    fun getTodayAttendanceCount(date: String): Flow<Int> =
        attendanceDao.getAttendanceCountForDate(date)

    suspend fun getAttendanceForStudentAndDate(studentId: Long, date: String): AttendanceRecord? =
        attendanceDao.getAttendanceForStudentAndDate(studentId, date)

    fun getAttendanceForStudentAndMonth(studentId: Long, monthPrefix: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForStudentAndMonth(studentId, monthPrefix)

    fun getAllAttendanceForStudent(studentId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getAllAttendanceForStudent(studentId)

    fun getAllAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAllAttendanceForMonth(monthPrefix)

    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>> =
        attendanceDao.getAllAttendanceRecords()

    suspend fun insertAttendance(attendance: AttendanceRecord): Long =
        attendanceDao.insertAttendance(attendance)

    suspend fun deleteAttendance(attendance: AttendanceRecord) =
        attendanceDao.deleteAttendance(attendance)

    suspend fun deleteAttendanceForStudentAndDate(studentId: Long, date: String) =
        attendanceDao.deleteAttendanceForStudentAndDate(studentId, date)

    // Fees
    fun getPaymentsForMonth(monthYear: String): Flow<List<FeePayment>> =
        feeDao.getPaymentsForMonth(monthYear)

    fun getAllPayments(): Flow<List<FeePayment>> =
        feeDao.getAllPayments()

    fun getPaymentsForStudent(studentId: Long): Flow<List<FeePayment>> =
        feeDao.getPaymentsForStudent(studentId)

    fun getPaymentsForStudentAndMonth(studentId: Long, monthYear: String): Flow<List<FeePayment>> =
        feeDao.getPaymentsForStudentAndMonth(studentId, monthYear)

    suspend fun getPaymentsForStudentAndMonthOnce(studentId: Long, monthYear: String): List<FeePayment> =
        feeDao.getPaymentsForStudentAndMonthOnce(studentId, monthYear)

    suspend fun insertPayment(payment: FeePayment): Long = feeDao.insertPayment(payment)

    suspend fun deletePayment(payment: FeePayment) = feeDao.deletePayment(payment)

    suspend fun deletePaymentById(id: Long) = feeDao.deletePaymentById(id)

    fun getTotalCollectedForMonth(monthYear: String): Flow<Double?> =
        feeDao.getTotalCollectedForMonth(monthYear)
}
