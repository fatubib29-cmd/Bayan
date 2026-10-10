package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(
    viewModel: AcademyViewModel,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val assignedStudents by viewModel.getAssignedStudentsForCurrentTeacher().collectAsState(initial = emptyList())
    val myClasses by viewModel.getUpcomingClassesForCurrentUser().collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) } // 0: Students, 1: Daily Progress, 2: Attendance, 3: Zoom Classes, 4: Messages
    var showProgressDialogForStudent by remember { mutableStateOf<StudentEducationalProfile?>(null) }
    var showAttendanceDialogForStudent by remember { mutableStateOf<StudentEducationalProfile?>(null) }
    var selectedStudentForChat by remember { mutableStateOf<StudentEducationalProfile?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Teacher Header Overview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = strings.myAssignedStudents,
                value = assignedStudents.size.toString(),
                subtitle = "Active Learners",
                icon = Icons.Default.Groups,
                iconColor = DeepTeal,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = strings.todaysClasses,
                value = myClasses.count { it.status == "LIVE" || it.scheduledDateTime.contains("Today") }.toString(),
                subtitle = "Live Sessions",
                icon = Icons.Default.Videocam,
                iconColor = AntiqueGoldDark,
                modifier = Modifier.weight(1f)
            )
        }

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = DeepTeal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(strings.myAssignedStudents, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(strings.recordDailyProgress, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(strings.recordAttendance, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text(strings.zoomClasses, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Assigned Students List (Notice: NO Guardian details visible here!)
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "${strings.myAssignedStudents} (${assignedStudents.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        if (assignedStudents.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No students assigned to your faculty profile yet.", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(assignedStudents) { student ->
                                    GlassmorphicCard {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = student.displayName,
                                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        CourseTrackBadge(track = student.courseTrack)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "ID: ${student.studentId} • Level: ${student.currentLevel}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                                    )
                                                    Text(
                                                        text = "Goals: ${student.learningGoals}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(
                                                            fontSize = 11.sp,
                                                            color = TextDarkSecondary
                                                        )
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { showProgressDialogForStudent = student },
                                                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f).testTag("btn_eval_${student.studentId}")
                                                ) {
                                                    Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Record Progress", style = MaterialTheme.typography.labelSmall)
                                                }
                                                OutlinedButton(
                                                    onClick = { showAttendanceDialogForStudent = student },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Mark Attendance", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Daily Progress Recording Section
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = strings.recordDailyProgress,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Text(
                            text = "Select a student from your assigned roster to log lessons, practice tasks, and feedback.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextDarkSecondary)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(assignedStudents) { student ->
                                GlassmorphicCard(
                                    onClick = { showProgressDialogForStudent = student }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(student.displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            Text("Track: ${student.courseTrack} • Level: ${student.currentLevel}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp))
                                        }
                                        Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = DeepTeal, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Attendance Section
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = strings.recordAttendance,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(assignedStudents) { student ->
                                GlassmorphicCard {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(student.displayName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                            CourseTrackBadge(track = student.courseTrack)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Quick Mark Status for Today:", style = MaterialTheme.typography.labelSmall)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf(
                                                "PRESENT" to StatusSuccess,
                                                "ABSENT" to StatusError,
                                                "LATE" to StatusWarning,
                                                "EXCUSED" to AntiqueGoldDark
                                            ).forEach { (status, col) ->
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.recordAttendance(
                                                            studentId = student.studentId,
                                                            status = status,
                                                            notes = "Marked via Quick Attendance"
                                                        ) { _, _ -> }
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        status.take(4),
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = col
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Zoom Classes
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "My Scheduled Zoom Sessions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (myClasses.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No scheduled sessions found for your faculty account.", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 24.dp)
                            ) {
                                items(myClasses) { zoomClass ->
                                    GlassmorphicCard {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(zoomClass.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                                StatusBadge(status = zoomClass.status)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("Student: ${zoomClass.studentId} • ${zoomClass.scheduledDateTime}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp))
                                            if (zoomClass.notes.isNotBlank()) {
                                                Text("Notes: ${zoomClass.notes}", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, color = TextMuted))
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(zoomClass.zoomUrl))
                                                        context.startActivity(intent)
                                                    } catch (_: Exception) {}
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(strings.joinClass, style = MaterialTheme.typography.labelMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Daily Progress Submission
    showProgressDialogForStudent?.let { student ->
        var lessonChapter by remember { mutableStateOf("Lesson 4: Harakat - Kasrah") }
        var skillsCovered by remember { mutableStateOf("Correct smiling mouth articulation for Kasrah vowels") }
        var progressStatus by remember { mutableStateOf("PRACTICING") }
        var practiceCompleted by remember { mutableStateOf("Lines 1-4 practiced 3 times") }
        var homework by remember { mutableStateOf("Lines 5-8 reading practice for next class") }
        var teacherFeedback by remember { mutableStateOf("Excellent effort and noticeable clarity improvement!") }

        AlertDialog(
            onDismissRequest = { showProgressDialogForStudent = null },
            title = {
                Text(
                    "Log Progress: ${student.displayName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = lessonChapter,
                            onValueChange = { lessonChapter = it },
                            label = { Text(strings.lessonChapter) },
                            modifier = Modifier.fillMaxWidth().testTag("input_lesson_chapter")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = skillsCovered,
                            onValueChange = { skillsCovered = it },
                            label = { Text(strings.skillsCovered) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("Progress Status:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("NOT_STARTED", "IN_PROGRESS", "PRACTICING", "ACHIEVED").forEach { st ->
                                FilterChip(
                                    selected = progressStatus == st,
                                    onClick = { progressStatus = st },
                                    label = { Text(st.take(6), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = practiceCompleted,
                            onValueChange = { practiceCompleted = it },
                            label = { Text("Practice Completed") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = homework,
                            onValueChange = { homework = it },
                            label = { Text(strings.homeworkNextLesson) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = teacherFeedback,
                            onValueChange = { teacherFeedback = it },
                            label = { Text(strings.teacherFeedback) },
                            modifier = Modifier.fillMaxWidth().testTag("input_feedback")
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitProgress(
                            studentId = student.studentId,
                            courseTrack = student.courseTrack,
                            chapterLesson = lessonChapter,
                            skillsCovered = skillsCovered,
                            status = progressStatus,
                            practiceCompleted = practiceCompleted,
                            homework = homework,
                            teacherFeedback = teacherFeedback
                        ) { success, _ ->
                            if (success) showProgressDialogForStudent = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    modifier = Modifier.testTag("btn_submit_progress")
                ) {
                    Text(strings.submit)
                }
            },
            dismissButton = {
                TextButton(onClick = { showProgressDialogForStudent = null }) {
                    Text(strings.cancel)
                }
            },
            containerColor = WarmIvorySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal: Mark Attendance
    showAttendanceDialogForStudent?.let { student ->
        var attStatus by remember { mutableStateOf("PRESENT") }
        var notes by remember { mutableStateOf("On-time and attentive") }

        AlertDialog(
            onDismissRequest = { showAttendanceDialogForStudent = null },
            title = {
                Text(
                    "Mark Attendance: ${student.displayName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("PRESENT", "ABSENT", "LATE", "EXCUSED").forEach { st ->
                            FilterChip(
                                selected = attStatus == st,
                                onClick = { attStatus = st },
                                label = { Text(st) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Teacher Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordAttendance(
                            studentId = student.studentId,
                            status = attStatus,
                            notes = notes
                        ) { success, _ ->
                            if (success) showAttendanceDialogForStudent = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAttendanceDialogForStudent = null }) {
                    Text(strings.cancel)
                }
            },
            containerColor = WarmIvorySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
