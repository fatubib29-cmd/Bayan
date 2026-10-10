package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AcademyRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.localization.BanglaStrings
import com.example.ui.localization.EnglishStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AcademyViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AcademyRepository(db.academyDao())

    // Language State
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    val strings: StateFlow<AppStrings> = _currentLanguage.map { lang ->
        if (lang == AppLanguage.BANGLA) BanglaStrings else EnglishStrings
    }.stateIn(viewModelScope, SharingStarted.Eagerly, EnglishStrings)

    // Current Session (Active Persona / User)
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Available Users for Quick Session / Persona Switching
    val allActiveUsers: StateFlow<List<UserEntity>> = repository.getAllActiveUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Educational Students (For Principal)
    val allStudentsEducational: StateFlow<List<StudentEducationalProfile>> = repository.getAllStudentsEducational()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Teachers
    val allTeachers: StateFlow<List<TeacherProfile>> = repository.getAllTeachers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Zoom Classes
    val allZoomClasses: StateFlow<List<ZoomClassRecord>> = repository.getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Announcements
    val announcements: StateFlow<List<AnnouncementRecord>> = repository.getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Qaida Lessons
    val qaidaLessons: StateFlow<List<QaidaLesson>> = repository.getAllQaidaLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily Duas
    private val _duaSearchQuery = MutableStateFlow("")
    val duaSearchQuery: StateFlow<String> = _duaSearchQuery.asStateFlow()

    val duasList: StateFlow<List<DailyDua>> = _duaSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.getAllDuas()
            else repository.searchDuas(query.trim())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit Logs (Principal)
    val auditLogs: StateFlow<List<AuditLog>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent Attendance & Progress
    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.getAllAttendance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProgress: StateFlow<List<DailyProgressRecord>> = repository.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Security & Action Status Message
    private val _statusNotification = MutableStateFlow<String?>(null)
    val statusNotification: StateFlow<String?> = _statusNotification.asStateFlow()

    init {
        // Prepopulate database if empty and select default Principal user
        viewModelScope.launch(Dispatchers.IO) {
            val count = db.academyDao().getQaidaLessonCount()
            if (count == 0) {
                AppDatabase.populateInitialData(db.academyDao())
            }
            // Default to Principal user initially
            val defaultUser = db.academyDao().getUserById("USR-001")
            _currentUser.value = defaultUser
        }
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
        if (user.preferredLanguage == "bn") {
            _currentLanguage.value = AppLanguage.BANGLA
        } else {
            _currentLanguage.value = AppLanguage.ENGLISH
        }
        _statusNotification.value = "Switched active profile to ${user.displayName} (${user.role.name})"
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == AppLanguage.ENGLISH) AppLanguage.BANGLA else AppLanguage.ENGLISH
    }

    fun clearNotification() {
        _statusNotification.value = null
    }

    // --- Student-specific & Teacher-specific Filtered Streams ---
    fun getAssignedStudentsForCurrentTeacher(): Flow<List<StudentEducationalProfile>> {
        val user = _currentUser.value ?: return flowOf(emptyList())
        if (user.role != Role.TEACHER) return flowOf(emptyList())
        return repository.getStudentsForTeacher(user.linkedId)
    }

    fun getUpcomingClassesForCurrentUser(): Flow<List<ZoomClassRecord>> {
        val user = _currentUser.value ?: return flowOf(emptyList())
        return when (user.role) {
            Role.STUDENT -> repository.getClassesForStudent(user.linkedId)
            Role.TEACHER -> repository.getClassesForTeacher(user.linkedId)
            Role.PRINCIPAL -> repository.getAllClasses()
        }
    }

    fun getProgressForCurrentStudent(): Flow<List<DailyProgressRecord>> {
        val user = _currentUser.value ?: return flowOf(emptyList())
        if (user.role != Role.STUDENT) return flowOf(emptyList())
        return repository.getProgressForStudent(user.linkedId)
    }

    fun getAttendanceForCurrentStudent(): Flow<List<AttendanceRecord>> {
        val user = _currentUser.value ?: return flowOf(emptyList())
        if (user.role != Role.STUDENT) return flowOf(emptyList())
        return repository.getAttendanceForStudent(user.linkedId)
    }

    // --- Strict Principal Security Check for Private Personal Records ---
    suspend fun getStudentPrivateRecord(studentId: String): StudentPrivateRecord? {
        val role = _currentUser.value?.role ?: throw SecurityException("Unauthenticated session.")
        return repository.getStudentPrivateRecordAsPrincipal(role, studentId)
    }

    // --- Principal Actions ---
    fun enrollNewStudent(
        studentId: String,
        displayName: String,
        courseTrack: String,
        currentLevel: String,
        assignedTeacherId: String,
        learningGoals: String,
        legalFullName: String,
        guardianName: String,
        guardianPhone: String,
        guardianEmail: String,
        homeAddress: String,
        emergencyNotes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val role = _currentUser.value?.role ?: Role.STUDENT
                val educational = StudentEducationalProfile(
                    studentId = studentId,
                    displayName = displayName,
                    courseTrack = courseTrack,
                    currentLevel = currentLevel,
                    assignedTeacherId = assignedTeacherId,
                    enrollmentDate = "2026-10-10",
                    learningGoals = learningGoals
                )
                val privateRecord = StudentPrivateRecord(
                    studentId = studentId,
                    legalFullName = legalFullName,
                    guardianName = guardianName,
                    guardianPhone = guardianPhone,
                    guardianEmail = guardianEmail,
                    homeAddress = homeAddress,
                    emergencyNotes = emergencyNotes
                )
                repository.enrollStudent(
                    requestingUserRole = role,
                    educationalProfile = educational,
                    privateRecord = privateRecord,
                    enrolledBy = _currentUser.value?.displayName ?: "Principal"
                )
                _statusNotification.value = "Student $displayName enrolled successfully."
                onComplete(true, "Enrolled successfully")
            } catch (e: Exception) {
                _statusNotification.value = "Enrollment error: ${e.message}"
                onComplete(false, e.localizedMessage ?: "Enrollment failed")
            }
        }
    }

    fun appointTeacher(
        teacherId: String,
        displayName: String,
        email: String,
        phone: String,
        specialties: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val role = _currentUser.value?.role ?: Role.STUDENT
                val teacher = TeacherProfile(
                    teacherId = teacherId,
                    displayName = displayName,
                    email = email,
                    phone = phone,
                    specialties = specialties
                )
                repository.addTeacher(
                    requestingUserRole = role,
                    teacher = teacher,
                    addedBy = _currentUser.value?.displayName ?: "Principal"
                )
                _statusNotification.value = "Teacher $displayName appointed successfully."
                onComplete(true, "Appointed successfully")
            } catch (e: Exception) {
                _statusNotification.value = "Appointment error: ${e.message}"
                onComplete(false, e.localizedMessage ?: "Appointment failed")
            }
        }
    }

    fun toggleStudentArchive(studentId: String, archive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val role = _currentUser.value?.role ?: Role.STUDENT
                repository.toggleStudentArchive(
                    requestingUserRole = role,
                    studentId = studentId,
                    archive = archive,
                    performedBy = _currentUser.value?.displayName ?: "Principal"
                )
                _statusNotification.value = "Student $studentId status updated."
            } catch (e: Exception) {
                _statusNotification.value = "Error: ${e.message}"
            }
        }
    }

    // --- Teacher Actions ---
    fun submitProgress(
        studentId: String,
        courseTrack: String,
        chapterLesson: String,
        skillsCovered: String,
        status: String,
        practiceCompleted: String,
        homework: String,
        teacherFeedback: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = _currentUser.value ?: throw SecurityException("Not signed in")
                val record = DailyProgressRecord(
                    studentId = studentId,
                    teacherId = user.linkedId,
                    courseTrack = courseTrack,
                    date = "2026-10-10",
                    chapterLesson = chapterLesson,
                    skillsCovered = skillsCovered,
                    status = status,
                    practiceCompleted = practiceCompleted,
                    homework = homework,
                    teacherFeedback = teacherFeedback,
                    submittedBy = user.displayName
                )
                repository.submitDailyProgress(user.role, record, user.displayName)
                _statusNotification.value = "Progress submitted for student $studentId."
                onComplete(true, "Submitted successfully")
            } catch (e: Exception) {
                _statusNotification.value = "Error: ${e.message}"
                onComplete(false, e.localizedMessage ?: "Submission failed")
            }
        }
    }

    fun recordAttendance(
        studentId: String,
        status: String,
        notes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = _currentUser.value ?: throw SecurityException("Not signed in")
                val record = AttendanceRecord(
                    studentId = studentId,
                    teacherId = user.linkedId,
                    classDate = "2026-10-10",
                    status = status,
                    notes = notes
                )
                repository.recordAttendance(user.role, record, user.displayName)
                _statusNotification.value = "Attendance marked as $status for $studentId."
                onComplete(true, "Attendance recorded")
            } catch (e: Exception) {
                _statusNotification.value = "Error: ${e.message}"
                onComplete(false, e.localizedMessage ?: "Attendance failed")
            }
        }
    }

    fun scheduleZoomClass(
        title: String,
        teacherId: String,
        studentId: String,
        dateTime: String,
        duration: Int,
        zoomUrl: String,
        notes: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = _currentUser.value ?: throw SecurityException("Not signed in")
                val zoomClass = ZoomClassRecord(
                    title = title,
                    teacherId = teacherId,
                    studentId = studentId,
                    scheduledDateTime = dateTime,
                    durationMinutes = duration,
                    zoomUrl = zoomUrl,
                    notes = notes,
                    status = "UPCOMING"
                )
                repository.scheduleZoomClass(user.role, zoomClass, user.displayName)
                _statusNotification.value = "Zoom class scheduled: $title."
                onComplete(true, "Class scheduled")
            } catch (e: Exception) {
                _statusNotification.value = "Error: ${e.message}"
                onComplete(false, e.localizedMessage ?: "Schedule failed")
            }
        }
    }

    // --- Dua & Qaida Actions ---
    fun setDuaSearchQuery(query: String) {
        _duaSearchQuery.value = query
    }

    fun toggleDuaFavorite(dua: DailyDua) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleDuaFavorite(dua.id, !dua.isFavorite)
        }
    }

    fun toggleQaidaLesson(lesson: QaidaLesson) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleQaidaCompletion(lesson.id, !lesson.isCompleted)
        }
    }

    // --- Messaging ---
    fun getMessagesBetween(recipientId: String): Flow<List<MessageRecord>> {
        val current = _currentUser.value?.id ?: return flowOf(emptyList())
        return repository.getMessagesBetween(current, recipientId)
    }

    fun sendMessage(recipientId: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value ?: return@launch
            val msg = MessageRecord(
                senderId = user.id,
                recipientId = recipientId,
                senderName = user.displayName,
                senderRole = user.role.name,
                content = content
            )
            repository.sendMessage(msg)
        }
    }

    // --- Announcements ---
    fun postAnnouncement(titleEn: String, titleBn: String, contentEn: String, contentBn: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value ?: return@launch
            val announcement = AnnouncementRecord(
                titleEn = titleEn,
                titleBn = titleBn,
                contentEn = contentEn,
                contentBn = contentBn,
                authorName = user.displayName,
                targetRole = "ALL"
            )
            repository.postAnnouncement(user.role, announcement, user.displayName)
            _statusNotification.value = "Announcement published."
        }
    }
}
