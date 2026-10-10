package com.example.data.repository

import com.example.data.dao.AcademyDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AcademyRepository(private val dao: AcademyDao) {

    // --- Active User Sessions & Role Validation ---
    fun getAllActiveUsers(): Flow<List<UserEntity>> = dao.getAllActiveUsers()

    suspend fun getUserById(userId: String): UserEntity? = dao.getUserById(userId)

    suspend fun switchOrUpdateUser(user: UserEntity) = dao.updateUser(user)

    // --- Educational Profiles ---
    fun getAllStudentsEducational(): Flow<List<StudentEducationalProfile>> =
        dao.getAllActiveStudentsEducational()

    fun getStudentsForTeacher(teacherId: String): Flow<List<StudentEducationalProfile>> =
        dao.getStudentsForTeacher(teacherId)

    fun getStudentEducationalById(studentId: String): Flow<StudentEducationalProfile?> =
        dao.getStudentEducationalById(studentId)

    // --- SECURE PRIVACY BOUNDARY: Confidential Personal Records ---
    /**
     * Strict Database / Security Rule:
     * Only the Principal is authorized to read confidential student records (guardian contact, home address).
     * Any caller with role other than PRINCIPAL will be rejected with SecurityException.
     */
    suspend fun getStudentPrivateRecordAsPrincipal(
        requestingUserRole: Role,
        studentId: String
    ): StudentPrivateRecord? {
        if (requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Confidential guardian records may only be accessed by the Principal.")
        }
        return dao.getStudentPrivateRecord(studentId)
    }

    suspend fun getAllPrivateRecordsAsPrincipal(requestingUserRole: Role): List<StudentPrivateRecord> {
        if (requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Confidential personal directory may only be accessed by the Principal.")
        }
        return dao.getAllPrivateRecords()
    }

    // --- Student Enrollment Workflow (Principal) ---
    suspend fun enrollStudent(
        requestingUserRole: Role,
        educationalProfile: StudentEducationalProfile,
        privateRecord: StudentPrivateRecord,
        enrolledBy: String
    ) {
        if (requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Only the Principal has authorization to enroll new students.")
        }
        dao.insertStudentEducational(educationalProfile)
        dao.insertStudentPrivateRecord(privateRecord)

        // Create linked student account
        val userAccount = UserEntity(
            id = "USR-${educationalProfile.studentId}",
            username = "std_${educationalProfile.studentId.lowercase()}",
            role = Role.STUDENT,
            displayName = educationalProfile.displayName,
            preferredLanguage = educationalProfile.preferredLanguage,
            isActive = true,
            linkedId = educationalProfile.studentId
        )
        dao.insertUser(userAccount)

        // Audit log entry
        dao.insertAuditLog(
            AuditLog(
                action = "STUDENT_ENROLLED",
                performedBy = enrolledBy,
                targetEntity = educationalProfile.studentId,
                details = "Enrolled student '${educationalProfile.displayName}' in track '${educationalProfile.courseTrack}', assigned to teacher '${educationalProfile.assignedTeacherId}'."
            )
        )
    }

    suspend fun updateStudentEducational(
        requestingUserRole: Role,
        profile: StudentEducationalProfile,
        updatedBy: String
    ) {
        if (requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Only the Principal can update core enrollment and teacher assignment.")
        }
        dao.updateStudentEducational(profile)
        dao.insertAuditLog(
            AuditLog(
                action = "STUDENT_PROFILE_UPDATED",
                performedBy = updatedBy,
                targetEntity = profile.studentId,
                details = "Updated student level: ${profile.currentLevel}, Track: ${profile.courseTrack}, Teacher: ${profile.assignedTeacherId}"
            )
        )
    }

    suspend fun toggleStudentArchive(
        requestingUserRole: Role,
        studentId: String,
        archive: Boolean,
        performedBy: String
    ) {
        if (requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Only the Principal can deactivate or archive student accounts.")
        }
        val current = dao.getStudentEducationalSync(studentId)
        if (current != null) {
            dao.updateStudentEducational(current.copy(isArchived = archive, isActive = !archive))
            dao.insertAuditLog(
                AuditLog(
                    action = if (archive) "STUDENT_ARCHIVED" else "STUDENT_RESTORED",
                    performedBy = performedBy,
                    targetEntity = studentId,
                    details = "Changed student status: isActive = ${!archive}, isArchived = $archive"
                )
            )
        }
    }

    // --- Teachers Management ---
    fun getAllTeachers(): Flow<List<TeacherProfile>> = dao.getAllTeachers()

    suspend fun addTeacher(
        requestingUserRole: Role,
        teacher: TeacherProfile,
        addedBy: String
    ) {
        if (requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Only the Principal can appoint new teachers.")
        }
        dao.insertTeacher(teacher)
        val userAccount = UserEntity(
            id = "USR-${teacher.teacherId}",
            username = "tch_${teacher.teacherId.lowercase()}",
            role = Role.TEACHER,
            displayName = teacher.displayName,
            preferredLanguage = teacher.preferredLanguage,
            isActive = true,
            linkedId = teacher.teacherId
        )
        dao.insertUser(userAccount)

        dao.insertAuditLog(
            AuditLog(
                action = "TEACHER_APPOINTED",
                performedBy = addedBy,
                targetEntity = teacher.teacherId,
                details = "Appointed teacher '${teacher.displayName}', specialties: '${teacher.specialties}'"
            )
        )
    }

    // --- Attendance Tracking ---
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>> =
        dao.getAttendanceForStudent(studentId)

    fun getAllAttendance(): Flow<List<AttendanceRecord>> = dao.getAllAttendance()

    suspend fun recordAttendance(
        requestingUserRole: Role,
        record: AttendanceRecord,
        recordedByName: String
    ) {
        if (requestingUserRole != Role.TEACHER && requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Students cannot submit attendance.")
        }
        dao.insertAttendance(record)
        dao.insertAuditLog(
            AuditLog(
                action = "ATTENDANCE_RECORDED",
                performedBy = recordedByName,
                targetEntity = record.studentId,
                details = "Attendance marked as ${record.status} for date ${record.classDate}"
            )
        )
    }

    // --- Daily Progress Submission ---
    fun getProgressForStudent(studentId: String): Flow<List<DailyProgressRecord>> =
        dao.getProgressForStudent(studentId)

    fun getAllProgress(): Flow<List<DailyProgressRecord>> = dao.getAllProgress()

    suspend fun submitDailyProgress(
        requestingUserRole: Role,
        record: DailyProgressRecord,
        submittedByName: String
    ) {
        if (requestingUserRole != Role.TEACHER && requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Only authorized faculty may submit learning progress.")
        }
        dao.insertProgress(record)
        dao.insertAuditLog(
            AuditLog(
                action = "PROGRESS_SUBMITTED",
                performedBy = submittedByName,
                targetEntity = record.studentId,
                details = "Submitted lesson '${record.chapterLesson}', status: '${record.status}', homework: '${record.homework}'"
            )
        )
    }

    // --- Zoom Live Classes ---
    fun getClassesForStudent(studentId: String): Flow<List<ZoomClassRecord>> =
        dao.getClassesForStudent(studentId)

    fun getClassesForTeacher(teacherId: String): Flow<List<ZoomClassRecord>> =
        dao.getClassesForTeacher(teacherId)

    fun getAllClasses(): Flow<List<ZoomClassRecord>> = dao.getAllClasses()

    suspend fun scheduleZoomClass(
        requestingUserRole: Role,
        zoomClass: ZoomClassRecord,
        scheduledByName: String
    ) {
        if (requestingUserRole != Role.PRINCIPAL && requestingUserRole != Role.TEACHER) {
            throw SecurityException("Access Denied: Students cannot schedule live classes.")
        }
        dao.insertClass(zoomClass)
        dao.insertAuditLog(
            AuditLog(
                action = "ZOOM_CLASS_SCHEDULED",
                performedBy = scheduledByName,
                targetEntity = zoomClass.studentId,
                details = "Scheduled Zoom class '${zoomClass.title}' for ${zoomClass.scheduledDateTime}"
            )
        )
    }

    // --- Messaging ---
    fun getMessagesBetween(userId1: String, userId2: String): Flow<List<MessageRecord>> =
        dao.getMessagesBetween(userId1, userId2)

    suspend fun sendMessage(record: MessageRecord) = dao.insertMessage(record)

    // --- Announcements ---
    fun getAllAnnouncements(): Flow<List<AnnouncementRecord>> = dao.getAllAnnouncements()

    suspend fun postAnnouncement(
        requestingUserRole: Role,
        announcement: AnnouncementRecord,
        postedByName: String
    ) {
        if (requestingUserRole != Role.PRINCIPAL) {
            throw SecurityException("Access Denied: Only the Principal may publish academy-wide announcements.")
        }
        dao.insertAnnouncement(announcement)
        dao.insertAuditLog(
            AuditLog(
                action = "ANNOUNCEMENT_POSTED",
                performedBy = postedByName,
                targetEntity = "ANNOUNCEMENTS",
                details = "Published announcement: '${announcement.titleEn}'"
            )
        )
    }

    // --- Qaida & Duas ---
    fun getAllQaidaLessons(): Flow<List<QaidaLesson>> = dao.getAllQaidaLessons()

    suspend fun toggleQaidaCompletion(lessonId: Long, isCompleted: Boolean) =
        dao.updateQaidaCompletion(lessonId, isCompleted)

    fun getAllDuas(): Flow<List<DailyDua>> = dao.getAllDuas()

    fun searchDuas(query: String): Flow<List<DailyDua>> = dao.searchDuas(query)

    suspend fun toggleDuaFavorite(duaId: Long, isFavorite: Boolean) =
        dao.toggleDuaFavorite(duaId, isFavorite)

    // --- Audit Logs ---
    fun getAllAuditLogs(): Flow<List<AuditLog>> = dao.getAllAuditLogs()
}
