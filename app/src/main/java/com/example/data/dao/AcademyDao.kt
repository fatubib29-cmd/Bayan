package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademyDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE isActive = 1")
    fun getAllActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Students Educational (Safe for authorized teachers) ---
    @Query("SELECT * FROM students_educational WHERE isArchived = 0 ORDER BY displayName ASC")
    fun getAllActiveStudentsEducational(): Flow<List<StudentEducationalProfile>>

    @Query("SELECT * FROM students_educational WHERE assignedTeacherId = :teacherId AND isArchived = 0 ORDER BY displayName ASC")
    fun getStudentsForTeacher(teacherId: String): Flow<List<StudentEducationalProfile>>

    @Query("SELECT * FROM students_educational WHERE studentId = :studentId LIMIT 1")
    fun getStudentEducationalById(studentId: String): Flow<StudentEducationalProfile?>

    @Query("SELECT * FROM students_educational WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentEducationalSync(studentId: String): StudentEducationalProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentEducational(student: StudentEducationalProfile)

    @Update
    suspend fun updateStudentEducational(student: StudentEducationalProfile)

    // --- Confidential Personal Records (Strictly PRINCIPAL ONLY) ---
    @Query("SELECT * FROM students_private_records WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentPrivateRecord(studentId: String): StudentPrivateRecord?

    @Query("SELECT * FROM students_private_records")
    suspend fun getAllPrivateRecords(): List<StudentPrivateRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentPrivateRecord(record: StudentPrivateRecord)

    // --- Teachers ---
    @Query("SELECT * FROM teachers WHERE isArchived = 0 ORDER BY displayName ASC")
    fun getAllTeachers(): Flow<List<TeacherProfile>>

    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId LIMIT 1")
    suspend fun getTeacherById(teacherId: String): TeacherProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherProfile)

    @Update
    suspend fun updateTeacher(teacher: TeacherProfile)

    // --- Attendance ---
    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY recordedAt DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY recordedAt DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord)

    // --- Daily Progress ---
    @Query("SELECT * FROM daily_progress_records WHERE studentId = :studentId ORDER BY submittedAt DESC")
    fun getProgressForStudent(studentId: String): Flow<List<DailyProgressRecord>>

    @Query("SELECT * FROM daily_progress_records ORDER BY submittedAt DESC")
    fun getAllProgress(): Flow<List<DailyProgressRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(record: DailyProgressRecord)

    // --- Zoom Classes ---
    @Query("SELECT * FROM zoom_classes WHERE studentId = :studentId OR studentId = 'ALL' ORDER BY scheduledDateTime ASC")
    fun getClassesForStudent(studentId: String): Flow<List<ZoomClassRecord>>

    @Query("SELECT * FROM zoom_classes WHERE teacherId = :teacherId ORDER BY scheduledDateTime ASC")
    fun getClassesForTeacher(teacherId: String): Flow<List<ZoomClassRecord>>

    @Query("SELECT * FROM zoom_classes ORDER BY scheduledDateTime ASC")
    fun getAllClasses(): Flow<List<ZoomClassRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(record: ZoomClassRecord)

    @Update
    suspend fun updateClass(record: ZoomClassRecord)

    // --- Messaging ---
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND recipientId = :userId2) OR (senderId = :userId2 AND recipientId = :userId1) ORDER BY timestamp ASC")
    fun getMessagesBetween(userId1: String, userId2: String): Flow<List<MessageRecord>>

    @Query("SELECT * FROM messages WHERE senderId = :userId OR recipientId = :userId ORDER BY timestamp DESC")
    fun getRecentMessagesForUser(userId: String): Flow<List<MessageRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(record: MessageRecord)

    // --- Announcements ---
    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, timestamp DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(record: AnnouncementRecord)

    // --- Qaida ---
    @Query("SELECT * FROM qaida_lessons ORDER BY lessonNumber ASC")
    fun getAllQaidaLessons(): Flow<List<QaidaLesson>>

    @Query("UPDATE qaida_lessons SET isCompleted = :completed WHERE id = :id")
    suspend fun updateQaidaCompletion(id: Long, completed: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQaidaLessons(lessons: List<QaidaLesson>)

    @Query("SELECT COUNT(*) FROM qaida_lessons")
    suspend fun getQaidaLessonCount(): Int

    // --- Daily Duas ---
    @Query("SELECT * FROM daily_duas ORDER BY category ASC, id ASC")
    fun getAllDuas(): Flow<List<DailyDua>>

    @Query("SELECT * FROM daily_duas WHERE titleEn LIKE '%' || :query || '%' OR titleBn LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR transliteration LIKE '%' || :query || '%'")
    fun searchDuas(query: String): Flow<List<DailyDua>>

    @Query("UPDATE daily_duas SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleDuaFavorite(id: Long, isFav: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuas(duas: List<DailyDua>)

    @Query("SELECT COUNT(*) FROM daily_duas")
    suspend fun getDuaCount(): Int

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)
}
