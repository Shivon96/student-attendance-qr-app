package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.StudentRepository
import com.example.data.model.AttendanceRecord
import com.example.data.model.FeePayment
import com.example.data.model.Student
import com.example.ui.components.PaymentReceiptInfo
import com.example.util.DateUtils
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class FeeStatus {
    PAID,
    PARTIAL,
    UNPAID
}

data class StudentAttendanceFeeStatus(
    val student: Student,
    val isPresentToday: Boolean,
    val todayCheckInTime: String?,
    val monthlyAttendedDays: Int,
    val totalPaidThisMonth: Double,
    val feeStatus: FeeStatus
)

sealed interface ScanResultState {
    object Idle : ScanResultState
    data class Success(
        val student: Student,
        val isAlreadyMarked: Boolean,
        val checkInTime: String,
        val monthlyAttendedDays: Int,
        val totalPaidThisMonth: Double,
        val feeStatus: FeeStatus
    ) : ScanResultState
    data class Error(val message: String) : ScanResultState
}

class StudentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudentRepository
    val todayDateString = DateUtils.getTodayDateString()

    private val _selectedMonthYear = MutableStateFlow(DateUtils.getCurrentMonthYearString())
    val selectedMonthYear: StateFlow<String> = _selectedMonthYear.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterStatus = MutableStateFlow<FeeStatus?>(null)
    val filterStatus: StateFlow<FeeStatus?> = _filterStatus.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResultState>(ScanResultState.Idle)
    val scanResult: StateFlow<ScanResultState> = _scanResult.asStateFlow()

    private val _selectedStudentForQr = MutableStateFlow<Student?>(null)
    val selectedStudentForQr: StateFlow<Student?> = _selectedStudentForQr.asStateFlow()

    private val _lastPaymentReceipt = MutableStateFlow<PaymentReceiptInfo?>(null)
    val lastPaymentReceipt: StateFlow<PaymentReceiptInfo?> = _lastPaymentReceipt.asStateFlow()

    private val _currencySymbol = MutableStateFlow("Rs. ")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = StudentRepository(db.studentDao(), db.attendanceDao(), db.feeDao())
        seedDefaultDataIfEmpty()
    }

    val allStudents: StateFlow<List<Student>> = repository.allActiveStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendanceRecords: StateFlow<List<AttendanceRecord>> = repository.getAllAttendanceRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeePayments: StateFlow<List<FeePayment>> = repository.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAttendance: StateFlow<List<AttendanceRecord>> = repository.getAttendanceForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayAttendanceCount: StateFlow<Int> = repository.getTodayAttendanceCount(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentMonthPayments: StateFlow<List<FeePayment>> = _selectedMonthYear
        .combine(repository.getPaymentsForMonth(_selectedMonthYear.value)) { _, payments -> payments }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthAttendanceRecords: StateFlow<List<AttendanceRecord>> = _selectedMonthYear
        .combine(repository.getAllAttendanceForMonth(_selectedMonthYear.value)) { _, records -> records }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Base status for all students in current selected month
    private val rawStudentsWithStatus: Flow<List<StudentAttendanceFeeStatus>> = combine(
        allStudents,
        todayAttendance,
        monthAttendanceRecords,
        currentMonthPayments
    ) { students, todayAtt, monthAtt, payments ->
        val todayMap = todayAtt.associateBy { it.studentId }
        val monthCountMap = monthAtt.groupBy { it.studentId }.mapValues { it.value.size }
        val paymentSumMap = payments.groupBy { it.studentId }.mapValues { entry ->
            entry.value.sumOf { it.amountPaid }
        }

        students.map { student ->
            val isPresent = todayMap.containsKey(student.id)
            val todayTime = todayMap[student.id]?.let { DateUtils.formatDisplayTime(it.timestamp) }
            val attendedDays = monthCountMap[student.id] ?: 0
            val paidThisMonth = paymentSumMap[student.id] ?: 0.0

            val feeStatus = when {
                paidThisMonth >= student.monthlyFee -> FeeStatus.PAID
                paidThisMonth > 0 -> FeeStatus.PARTIAL
                else -> FeeStatus.UNPAID
            }

            StudentAttendanceFeeStatus(
                student = student,
                isPresentToday = isPresent,
                todayCheckInTime = todayTime,
                monthlyAttendedDays = attendedDays,
                totalPaidThisMonth = paidThisMonth,
                feeStatus = feeStatus
            )
        }
    }

    // Filtered and searched list
    val studentsWithStatus: StateFlow<List<StudentAttendanceFeeStatus>> = combine(
        rawStudentsWithStatus,
        _searchQuery,
        _filterStatus
    ) { list, query, filter ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.student.name.contains(query, ignoreCase = true) ||
                    item.student.studentCode.contains(query, ignoreCase = true) ||
                    item.student.contactNumber.contains(query, ignoreCase = true) ||
                    item.student.parentsContactNumber.contains(query, ignoreCase = true)

            val matchesFilter = filter == null || item.feeStatus == filter
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterStatus(status: FeeStatus?) {
        _filterStatus.value = status
    }

    fun setSelectedMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun setSelectedStudentForQr(student: Student?) {
        _selectedStudentForQr.value = student
    }

    fun clearScanResult() {
        _scanResult.value = ScanResultState.Idle
    }

    /**
     * Scan QR Code payload and automatically record attendance and evaluate fee status.
     */
    fun onQrScanned(rawPayload: String) {
        viewModelScope.launch {
            val (parsedId, parsedCode) = QrCodeGenerator.parsePayload(rawPayload)
            val student = withContext(Dispatchers.IO) {
                if (parsedId != null && parsedId > 0) {
                    repository.getStudentByIdOnce(parsedId)
                        ?: (parsedCode?.let { repository.getStudentByCode(it) })
                } else if (!parsedCode.isNullOrBlank()) {
                    repository.getStudentByCode(parsedCode)
                } else {
                    null
                }
            }

            if (student == null) {
                playBeep(isSuccess = false)
                _scanResult.value = ScanResultState.Error("Student QR code not found: $rawPayload")
                return@launch
            }

            // Check if already attended today
            val existingAttendance = withContext(Dispatchers.IO) {
                repository.getAttendanceForStudentAndDate(student.id, todayDateString)
            }

            val checkInTimestamp = existingAttendance?.timestamp ?: System.currentTimeMillis()
            var isAlreadyMarked = false

            if (existingAttendance == null) {
                // Record new attendance!
                withContext(Dispatchers.IO) {
                    repository.insertAttendance(
                        AttendanceRecord(
                            studentId = student.id,
                            date = todayDateString,
                            timestamp = checkInTimestamp,
                            status = "PRESENT"
                        )
                    )
                }
            } else {
                isAlreadyMarked = true
            }

            // Fetch monthly stats
            val currentMonth = _selectedMonthYear.value
            val payments = withContext(Dispatchers.IO) {
                repository.getPaymentsForStudentAndMonthOnce(student.id, currentMonth)
            }
            val totalPaidThisMonth = payments.sumOf { it.amountPaid }
            val feeStatus = when {
                totalPaidThisMonth >= student.monthlyFee -> FeeStatus.PAID
                totalPaidThisMonth > 0 -> FeeStatus.PARTIAL
                else -> FeeStatus.UNPAID
            }

            // Play feedback
            playBeep(isSuccess = true)
            vibrateDevice()

            _scanResult.value = ScanResultState.Success(
                student = student,
                isAlreadyMarked = isAlreadyMarked,
                checkInTime = DateUtils.formatDisplayTime(checkInTimestamp),
                monthlyAttendedDays = 1, // Will be reflected in overall counts
                totalPaidThisMonth = totalPaidThisMonth,
                feeStatus = feeStatus
            )
        }
    }

    /**
     * Manual 1-tap check-in for a student
     */
    fun markAttendance(studentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getAttendanceForStudentAndDate(studentId, todayDateString)
            if (existing == null) {
                repository.insertAttendance(
                    AttendanceRecord(
                        studentId = studentId,
                        date = todayDateString,
                        timestamp = System.currentTimeMillis(),
                        status = "PRESENT"
                    )
                )
            }
        }
    }

    /**
     * Remove today's attendance for a student (in case of accidental scan)
     */
    fun removeAttendanceToday(studentId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAttendanceForStudentAndDate(studentId, todayDateString)
        }
    }

    /**
     * Register a new student and optionally record their first monthly fee
     */
    fun registerStudent(
        name: String,
        contactNumber: String,
        address: String,
        parentsContactNumber: String,
        monthlyFee: Double,
        markCurrentMonthPaid: Boolean,
        onRegistered: (Student) -> Unit
    ) {
        viewModelScope.launch {
            // Generate unique student code e.g. STU-8492
            val shortId = (1000 + (Math.random() * 9000).toInt()).toString()
            val studentCode = "STU-$shortId"

            val student = Student(
                studentCode = studentCode,
                name = name.trim(),
                contactNumber = contactNumber.trim(),
                address = address.trim(),
                parentsContactNumber = parentsContactNumber.trim(),
                monthlyFee = monthlyFee
            )

            val insertedId = withContext(Dispatchers.IO) {
                val id = repository.insertStudent(student)
                if (markCurrentMonthPaid && monthlyFee > 0) {
                    repository.insertPayment(
                        FeePayment(
                            studentId = id,
                            monthYear = DateUtils.getCurrentMonthYearString(),
                            amountPaid = monthlyFee,
                            paymentMethod = "Cash",
                            note = "Initial registration payment"
                        )
                    )
                }
                id
            }

            val savedStudent = student.copy(id = insertedId)
            if (markCurrentMonthPaid && monthlyFee > 0) {
                _lastPaymentReceipt.value = PaymentReceiptInfo(
                    student = savedStudent,
                    amount = monthlyFee,
                    monthYear = DateUtils.getCurrentMonthYearString(),
                    paymentMethod = "Cash"
                )
            }
            _selectedStudentForQr.value = savedStudent
            onRegistered(savedStudent)
        }
    }

    fun clearLastPaymentReceipt() {
        _lastPaymentReceipt.value = null
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStudent(student)
        }
    }

    /**
     * Record monthly fee payment and trigger parent SMS / WhatsApp receipt prompt
     */
    fun recordFeePayment(
        studentId: Long,
        monthYear: String,
        amount: Double,
        paymentMethod: String = "Cash",
        note: String = ""
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertPayment(
                    FeePayment(
                        studentId = studentId,
                        monthYear = monthYear,
                        amountPaid = amount,
                        paymentMethod = paymentMethod,
                        note = note
                    )
                )
            }
            val student = withContext(Dispatchers.IO) {
                repository.getStudentByIdOnce(studentId)
            }
            if (student != null) {
                _lastPaymentReceipt.value = PaymentReceiptInfo(
                    student = student,
                    amount = amount,
                    monthYear = monthYear,
                    paymentMethod = paymentMethod
                )
            }
        }
    }

    fun deletePayment(payment: FeePayment) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePayment(payment)
        }
    }

    private fun playBeep(isSuccess: Boolean) {
        try {
            val toneType = if (isSuccess) ToneGenerator.TONE_PROP_BEEP else ToneGenerator.TONE_PROP_NACK
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
            toneGenerator.startTone(toneType, 200)
        } catch (_: Exception) {}
    }

    private fun vibrateDevice() {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(100)
                }
            }
        } catch (_: Exception) {}
    }

    private fun seedDefaultDataIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.getStudentByIdOnce(1)
            if (existing == null) {
                val currentMonth = DateUtils.getCurrentMonthYearString()
                val today = DateUtils.getTodayDateString()

                val s1 = Student(
                    id = 1,
                    studentCode = "STU-1001",
                    name = "Alex Johnson",
                    contactNumber = "+94 77 123 4567",
                    address = "Colombo 03, Sri Lanka",
                    parentsContactNumber = "+94 71 987 6543",
                    monthlyFee = 2500.0
                )
                val s2 = Student(
                    id = 2,
                    studentCode = "STU-1002",
                    name = "Sophia Miller",
                    contactNumber = "+94 76 234 5678",
                    address = "Kandy, Sri Lanka",
                    parentsContactNumber = "+94 72 876 5432",
                    monthlyFee = 3000.0
                )
                val s3 = Student(
                    id = 3,
                    studentCode = "STU-1003",
                    name = "David Chen",
                    contactNumber = "+94 75 345 6789",
                    address = "Galle, Sri Lanka",
                    parentsContactNumber = "+94 78 765 4321",
                    monthlyFee = 2000.0
                )

                repository.insertStudent(s1)
                repository.insertStudent(s2)
                repository.insertStudent(s3)

                // Alex paid this month
                repository.insertPayment(
                    FeePayment(
                        studentId = 1,
                        monthYear = currentMonth,
                        amountPaid = 2500.0,
                        paymentMethod = "Cash",
                        note = "Monthly tuition fee paid"
                    )
                )

                // Alex also attended today
                repository.insertAttendance(
                    AttendanceRecord(
                        studentId = 1,
                        date = today,
                        timestamp = System.currentTimeMillis() - 3600000,
                        status = "PRESENT"
                    )
                )
            }
        }
    }
}
