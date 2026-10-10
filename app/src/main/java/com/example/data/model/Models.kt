package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Role {
    PRINCIPAL,
    TEACHER,
    STUDENT
}

enum class CourseTrack(val displayName: String) {
    QAIDA("Qaida"),
    AMPARA("Ampara"),
    QURAN("Quran")
}

enum class ProgressStatus(val displayName: String) {
    NOT_STARTED("Not started"),
    IN_PROGRESS("In progress"),
    PRACTICING("Practicing"),
    ACHIEVED("Achieved")
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LATE,
    EXCUSED
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val role: Role,
    val displayName: String,
    val preferredLanguage: String = "en",
    val isActive: Boolean = true,
    val linkedId: String = "" // Points to studentId or teacherId
)

@Entity(tableName = "students_educational")
data class StudentEducationalProfile(
    @PrimaryKey val studentId: String,
    val displayName: String,
    val courseTrack: String, // "Qaida", "Ampara", "Quran"
    val currentLevel: String,
    val assignedTeacherId: String,
    val enrollmentDate: String,
    val preferredLanguage: String = "en",
    val timeZone: String = "UTC+6",
    val learningGoals: String = "",
    val isActive: Boolean = true,
    val isArchived: Boolean = false
)

// CRITICAL PRIVACY ARCHITECTURE:
// Private personal details (legal name, guardian, phone, address) are separated in a dedicated
// table strictly accessible ONLY to Principal. Neither Teachers nor Students can query or access this.
@Entity(tableName = "students_private_records")
data class StudentPrivateRecord(
    @PrimaryKey val studentId: String,
    val legalFullName: String,
    val guardianName: String,
    val guardianPhone: String,
    val guardianEmail: String,
    val homeAddress: String,
    val emergencyNotes: String = "",
    val adminNotes: String = ""
)

@Entity(tableName = "teachers")
data class TeacherProfile(
    @PrimaryKey val teacherId: String,
    val displayName: String,
    val email: String,
    val phone: String,
    val preferredLanguage: String = "en",
    val specialties: String = "Tajweed, Qaida, Hifz",
    val isActive: Boolean = true,
    val isArchived: Boolean = false
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val teacherId: String,
    val classDate: String,
    val status: String, // "PRESENT", "ABSENT", "LATE", "EXCUSED"
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_progress_records")
data class DailyProgressRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: String,
    val teacherId: String,
    val courseTrack: String,
    val date: String,
    val chapterLesson: String,
    val skillsCovered: String,
    val status: String, // "NOT_STARTED", "IN_PROGRESS", "PRACTICING", "ACHIEVED"
    val practiceCompleted: String,
    val homework: String,
    val teacherFeedback: String,
    val submittedBy: String,
    val submittedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "zoom_classes")
data class ZoomClassRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val teacherId: String,
    val studentId: String,
    val scheduledDateTime: String,
    val durationMinutes: Int = 45,
    val zoomUrl: String,
    val notes: String = "",
    val status: String = "UPCOMING" // "UPCOMING", "LIVE", "COMPLETED", "CANCELLED"
)

@Entity(tableName = "messages")
data class MessageRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val recipientId: String,
    val senderName: String,
    val senderRole: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "announcements")
data class AnnouncementRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titleEn: String,
    val titleBn: String,
    val contentEn: String,
    val contentBn: String,
    val authorName: String,
    val targetRole: String = "ALL", // "ALL", "TEACHERS", "STUDENTS"
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "qaida_lessons")
data class QaidaLesson(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val lessonNumber: Int,
    val titleEn: String,
    val titleBn: String,
    val arabicContent: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val category: String, // "Alphabets", "Joint Letters", "Harakat", "Tanween", "Sukoon", "Madd"
    val isCompleted: Boolean = false
)

@Entity(tableName = "daily_duas")
data class DailyDua(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,
    val titleEn: String,
    val titleBn: String,
    val arabicText: String,
    val transliteration: String,
    val meaningEn: String,
    val meaningBn: String,
    val reference: String,
    val isFavorite: Boolean = false
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val performedBy: String,
    val targetEntity: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
