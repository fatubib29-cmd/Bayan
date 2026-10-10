package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.AcademyDao
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AcademyRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AcademySecurityTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AcademyDao
    private lateinit var repository: AcademyRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.academyDao()
        repository = AcademyRepository(dao)

        runBlocking {
            AppDatabase.populateInitialData(dao)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testTeacherCannotAccessStudentPrivateGuardianData() = runBlocking {
        // Critical Security Invariant: Teachers must be blocked with SecurityException
        try {
            repository.getStudentPrivateRecordAsPrincipal(Role.TEACHER, "STD-101")
            fail("Expected SecurityException when Teacher attempts to access private guardian record")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("confidential guardian records", ignoreCase = true) == true)
        }
    }

    @Test
    fun testStudentCannotAccessPrivateGuardianData() = runBlocking {
        // Critical Security Invariant: Students must be blocked with SecurityException
        try {
            repository.getStudentPrivateRecordAsPrincipal(Role.STUDENT, "STD-101")
            fail("Expected SecurityException when Student attempts to access private guardian record")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("confidential guardian records", ignoreCase = true) == true)
        }
    }

    @Test
    fun testPrincipalCanAccessPrivateGuardianData() = runBlocking {
        // Authorized Access: Principal can view full guardian information
        val record = repository.getStudentPrivateRecordAsPrincipal(Role.PRINCIPAL, "STD-101")
        assertNotNull(record)
        assertEquals("Mohammad Karim Chowdhury", record?.guardianName)
        assertEquals("+880 1711-234567", record?.guardianPhone)
        assertEquals("House 42, Road 9, Dhanmondi, Dhaka", record?.homeAddress)
    }

    @Test
    fun testStudentCannotEnrollOtherStudents() = runBlocking {
        val educational = StudentEducationalProfile(
            studentId = "STD-999",
            displayName = "Malicious Enrollment",
            courseTrack = "Qaida",
            currentLevel = "1",
            assignedTeacherId = "TCH-001",
            enrollmentDate = "2026-10-10"
        )
        val privateRecord = StudentPrivateRecord(
            studentId = "STD-999",
            legalFullName = "Unauthorized",
            guardianName = "None",
            guardianPhone = "000",
            guardianEmail = "none@test.com",
            homeAddress = "None"
        )

        try {
            repository.enrollStudent(Role.STUDENT, educational, privateRecord, "MaliciousUser")
            fail("Expected SecurityException when Student attempts to enroll a student")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Only the Principal", ignoreCase = true) == true)
        }
    }

    @Test
    fun testStudentCannotRecordAttendanceOrProgress() = runBlocking {
        val att = AttendanceRecord(
            studentId = "STD-101",
            teacherId = "TCH-001",
            classDate = "2026-10-10",
            status = "PRESENT"
        )
        try {
            repository.recordAttendance(Role.STUDENT, att, "StudentFraud")
            fail("Expected SecurityException when Student attempts to mark attendance")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Students cannot submit attendance", ignoreCase = true) == true)
        }

        val prog = DailyProgressRecord(
            studentId = "STD-101",
            teacherId = "TCH-001",
            courseTrack = "Qaida",
            date = "2026-10-10",
            chapterLesson = "Lesson 1",
            skillsCovered = "None",
            status = "ACHIEVED",
            practiceCompleted = "Done",
            homework = "None",
            teacherFeedback = "Self-graded",
            submittedBy = "StudentFraud"
        )
        try {
            repository.submitDailyProgress(Role.STUDENT, prog, "StudentFraud")
            fail("Expected SecurityException when Student attempts to submit learning progress")
        } catch (e: SecurityException) {
            assertTrue(e.message?.contains("Only authorized faculty", ignoreCase = true) == true)
        }
    }

    @Test
    fun testTeacherCanSubmitProgressAndCreatesAuditLog() = runBlocking {
        val prog = DailyProgressRecord(
            studentId = "STD-101",
            teacherId = "TCH-001",
            courseTrack = "Qaida",
            date = "2026-10-10",
            chapterLesson = "Lesson 4: Kasrah",
            skillsCovered = "Smiling lip vowel",
            status = "ACHIEVED",
            practiceCompleted = "All exercises read",
            homework = "Lesson 5 preview",
            teacherFeedback = "Outstanding accuracy",
            submittedBy = "Ustadh Tariq Mahmud"
        )

        repository.submitDailyProgress(Role.TEACHER, prog, "Ustadh Tariq Mahmud")

        val progressList = repository.getProgressForStudent("STD-101").first()
        val found = progressList.any { it.chapterLesson == "Lesson 4: Kasrah" }
        assertTrue("Newly recorded progress must be present for student", found)

        val logs = repository.getAllAuditLogs().first()
        val auditFound = logs.any { it.action == "PROGRESS_SUBMITTED" && it.targetEntity == "STD-101" }
        assertTrue("Progress submission must generate immutable audit log", auditFound)
    }

    @Test
    fun testTeacherIsolationOnlyShowsAssignedStudents() = runBlocking {
        // Teacher TCH-001 is assigned STD-101 and STD-103
        // Teacher TCH-002 is assigned STD-102
        val studentsTch1 = repository.getStudentsForTeacher("TCH-001").first()
        val studentsTch2 = repository.getStudentsForTeacher("TCH-002").first()

        assertTrue(studentsTch1.any { it.studentId == "STD-101" })
        assertTrue(studentsTch1.any { it.studentId == "STD-103" })
        assertFalse("Teacher 1 must NOT see Teacher 2's student STD-102", studentsTch1.any { it.studentId == "STD-102" })

        assertTrue(studentsTch2.any { it.studentId == "STD-102" })
        assertFalse("Teacher 2 must NOT see Teacher 1's students", studentsTch2.any { it.studentId == "STD-101" })
    }
}
