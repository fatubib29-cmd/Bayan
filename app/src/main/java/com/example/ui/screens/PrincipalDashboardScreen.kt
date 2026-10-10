package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.viewmodel.AcademyViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrincipalDashboardScreen(
    viewModel: AcademyViewModel,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val allStudents by viewModel.allStudentsEducational.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val allZoomClasses by viewModel.allZoomClasses.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Students, 1: Teachers, 2: Classes, 3: Audit, 4: Notices
    var showEnrollDialog by remember { mutableStateOf(false) }
    var showAppointTeacherDialog by remember { mutableStateOf(false) }
    var showScheduleClassDialog by remember { mutableStateOf(false) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var selectedStudentForPrivateView by remember { mutableStateOf<StudentEducationalProfile?>(null) }
    var privateRecordData by remember { mutableStateOf<StudentPrivateRecord?>(null) }
    var loadingPrivateRecord by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Operational Overview Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricStatCard(
                title = strings.totalStudents,
                value = allStudents.size.toString(),
                subtitle = "Active Enrollment",
                icon = Icons.Default.School,
                iconColor = DeepTeal,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = strings.activeTeachers,
                value = allTeachers.size.toString(),
                subtitle = "Faculty Staff",
                icon = Icons.Default.Person,
                iconColor = AntiqueGoldDark,
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = strings.todaysClasses,
                value = allZoomClasses.count { it.status == "LIVE" || it.scheduledDateTime.contains("Today") }.toString(),
                subtitle = "Live & Scheduled",
                icon = Icons.Default.Videocam,
                iconColor = StatusInfo,
                modifier = Modifier.weight(1f)
            )
        }

        // Section Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            contentColor = DeepTeal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(strings.studentManagement, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(strings.teacherManagement, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(strings.zoomClasses, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text(strings.auditLogTitle, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                text = { Text(strings.announcementsTitle, style = MaterialTheme.typography.labelLarge) },
                icon = { Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area based on Tab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Students Directory Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.studentManagement} (${allStudents.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = { showEnrollDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_enroll_student")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addNewStudent, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        // Strict Privacy Notice Banner
                        Surface(
                            color = AntiqueGoldContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AntiqueGoldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.principalOnlyAccessNotice,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 11.sp,
                                        color = AntiqueGoldDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(allStudents) { student ->
                                GlassmorphicCard(
                                    onClick = {
                                        selectedStudentForPrivateView = student
                                        loadingPrivateRecord = true
                                        coroutineScope.launch {
                                            privateRecordData = viewModel.getStudentPrivateRecord(student.studentId)
                                            loadingPrivateRecord = false
                                        }
                                    }
                                ) {
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
                                                text = "ID: ${student.studentId} • Assigned: ${student.assignedTeacherId}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.sp,
                                                    color = TextDarkSecondary
                                                )
                                            )
                                            Text(
                                                text = "${strings.trackLevel}: ${student.currentLevel}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = DeepTeal)
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                selectedStudentForPrivateView = student
                                                loadingPrivateRecord = true
                                                coroutineScope.launch {
                                                    privateRecordData = viewModel.getStudentPrivateRecord(student.studentId)
                                                    loadingPrivateRecord = false
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Visibility,
                                                contentDescription = "View Guardian Records",
                                                tint = AntiqueGoldDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Teacher Management Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.teacherManagement} (${allTeachers.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = { showAppointTeacherDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGoldDark),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PersonAddAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addNewTeacher, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(allTeachers) { teacher ->
                                GlassmorphicCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(DeepTeal.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = DeepTeal
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = teacher.displayName,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "ID: ${teacher.teacherId} • ${teacher.email}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontSize = 12.sp,
                                                    color = TextDarkSecondary
                                                )
                                            )
                                            Text(
                                                text = "Specialties: ${teacher.specialties}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGoldDark)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Zoom Live Classes Tab
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.zoomClasses,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = { showScheduleClassDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.scheduleClass, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(allZoomClasses) { zoomClass ->
                                GlassmorphicCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = zoomClass.title,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                StatusBadge(status = zoomClass.status)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Time: ${zoomClass.scheduledDateTime} (${zoomClass.durationMinutes} mins)",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                            )
                                            Text(
                                                text = "Teacher: ${zoomClass.teacherId} • Student: ${zoomClass.studentId}",
                                                style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSecondary)
                                            )
                                            if (zoomClass.notes.isNotBlank()) {
                                                Text(
                                                    text = "Notes: ${zoomClass.notes}",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, color = TextMuted)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // System Audit Logs
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = strings.auditLogTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(auditLogs) { log ->
                                Surface(
                                    color = SurfaceWhite,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = log.action,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepTeal
                                                )
                                            )
                                            Text(
                                                text = log.performedBy,
                                                style = MaterialTheme.typography.labelSmall.copy(color = AntiqueGoldDark)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = log.details,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp)
                                        )
                                        Text(
                                            text = "Target: ${log.targetEntity}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                4 -> {
                    // Announcements
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.announcementsTitle,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = { showAnnouncementDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publish Notice", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(announcements) { notice ->
                                GlassmorphicCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = notice.titleEn,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepTeal
                                            )
                                        )
                                        if (notice.isPinned) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Pinned",
                                                tint = AntiqueGoldDark,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notice.titleBn,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = TextDarkSecondary,
                                            fontSize = 12.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = notice.contentEn,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notice.contentBn,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 12.sp,
                                            color = TextDarkSecondary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "By ${notice.authorName}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Confidential Student Record Details (Principal Clearance Only)
    selectedStudentForPrivateView?.let { student ->
        AlertDialog(
            onDismissRequest = {
                selectedStudentForPrivateView = null
                privateRecordData = null
            },
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = AntiqueGoldDark,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = student.displayName,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "${strings.learnerId}: ${student.studentId} • Track: ${student.courseTrack}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextDarkSecondary)
                    )
                }
            },
            text = {
                if (loadingPrivateRecord) {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DeepTeal)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = AntiqueGoldContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = strings.confidentialGuardianData,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AntiqueGoldDark
                                ),
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        privateRecordData?.let { priv ->
                            Text("Legal Full Name: ${priv.legalFullName}", style = MaterialTheme.typography.bodyMedium)
                            Text("${strings.guardianName}: ${priv.guardianName}", style = MaterialTheme.typography.bodyMedium)
                            Text("${strings.guardianPhone}: ${priv.guardianPhone}", style = MaterialTheme.typography.bodyMedium)
                            Text("${strings.guardianEmail}: ${priv.guardianEmail}", style = MaterialTheme.typography.bodyMedium)
                            Text("${strings.homeAddress}: ${priv.homeAddress}", style = MaterialTheme.typography.bodyMedium)
                            if (priv.emergencyNotes.isNotBlank()) {
                                Text("Emergency Notes: ${priv.emergencyNotes}", style = MaterialTheme.typography.bodyMedium.copy(color = StatusError))
                            }
                            if (priv.adminNotes.isNotBlank()) {
                                Text("Admin Notes: ${priv.adminNotes}", style = MaterialTheme.typography.bodyMedium.copy(color = DeepTeal))
                            }
                        } ?: run {
                            Text("No private records found for this student.", style = MaterialTheme.typography.bodyMedium)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("Current Level: ${student.currentLevel}", style = MaterialTheme.typography.bodyMedium)
                        Text("Assigned Teacher: ${student.assignedTeacherId}", style = MaterialTheme.typography.bodyMedium)
                        Text("Learning Goals: ${student.learningGoals}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedStudentForPrivateView = null
                        privateRecordData = null
                    }
                ) {
                    Text(strings.close, color = DeepTeal)
                }
            },
            containerColor = WarmIvorySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal: Enroll New Student
    if (showEnrollDialog) {
        var id by remember { mutableStateOf("STD-${(104..999).random()}") }
        var name by remember { mutableStateOf("") }
        var track by remember { mutableStateOf("Qaida") }
        var level by remember { mutableStateOf("Lesson 1: Alphabets") }
        var teacherId by remember { mutableStateOf(allTeachers.firstOrNull()?.teacherId ?: "TCH-001") }
        var goals by remember { mutableStateOf("") }
        var legalName by remember { mutableStateOf("") }
        var guardianName by remember { mutableStateOf("") }
        var guardianPhone by remember { mutableStateOf("") }
        var guardianEmail by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEnrollDialog = false },
            title = {
                Text(
                    strings.addNewStudent,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = id,
                            onValueChange = { id = it },
                            label = { Text("Student ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Display Name") },
                            modifier = Modifier.fillMaxWidth().testTag("input_student_name")
                        )
                    }
                    item {
                        Text("Course Track:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Qaida", "Ampara", "Quran").forEach { trk ->
                                FilterChip(
                                    selected = track == trk,
                                    onClick = { track = trk },
                                    label = { Text(trk) }
                                )
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = level,
                            onValueChange = { level = it },
                            label = { Text("Starting Level") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = teacherId,
                            onValueChange = { teacherId = it },
                            label = { Text("Assigned Teacher ID") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = goals,
                            onValueChange = { goals = it },
                            label = { Text("Learning Goals") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Surface(
                            color = AntiqueGoldContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text(
                                "Confidential Guardian Details (Principal Clearance)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = AntiqueGoldDark),
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = legalName,
                            onValueChange = { legalName = it },
                            label = { Text("Legal Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = guardianName,
                            onValueChange = { guardianName = it },
                            label = { Text(strings.guardianName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = guardianPhone,
                            onValueChange = { guardianPhone = it },
                            label = { Text(strings.guardianPhone) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = guardianEmail,
                            onValueChange = { guardianEmail = it },
                            label = { Text(strings.guardianEmail) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text(strings.homeAddress) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.enrollNewStudent(
                                studentId = id,
                                displayName = name,
                                courseTrack = track,
                                currentLevel = level,
                                assignedTeacherId = teacherId,
                                learningGoals = goals,
                                legalFullName = if (legalName.isBlank()) name else legalName,
                                guardianName = guardianName,
                                guardianPhone = guardianPhone,
                                guardianEmail = guardianEmail,
                                homeAddress = address,
                                emergencyNotes = "Enrolled via Principal Admin Portal"
                            ) { success, _ ->
                                if (success) showEnrollDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                    modifier = Modifier.testTag("btn_confirm_enroll")
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnrollDialog = false }) {
                    Text(strings.cancel)
                }
            },
            containerColor = WarmIvorySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal: Appoint New Teacher
    if (showAppointTeacherDialog) {
        var id by remember { mutableStateOf("TCH-${(103..999).random()}") }
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var specialties by remember { mutableStateOf("Qaida, Tajweed, Makharij") }

        AlertDialog(
            onDismissRequest = { showAppointTeacherDialog = false },
            title = {
                Text(
                    strings.addNewTeacher,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Teacher ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = specialties,
                        onValueChange = { specialties = it },
                        label = { Text("Teaching Specialties") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.appointTeacher(
                                teacherId = id,
                                displayName = name,
                                email = email,
                                phone = phone,
                                specialties = specialties
                            ) { success, _ ->
                                if (success) showAppointTeacherDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AntiqueGoldDark)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppointTeacherDialog = false }) {
                    Text(strings.cancel)
                }
            },
            containerColor = WarmIvorySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal: Schedule Zoom Class
    if (showScheduleClassDialog) {
        var title by remember { mutableStateOf("Live Qaida Pronunciation Class") }
        var teacherId by remember { mutableStateOf(allTeachers.firstOrNull()?.teacherId ?: "TCH-001") }
        var studentId by remember { mutableStateOf(allStudents.firstOrNull()?.studentId ?: "STD-101") }
        var dateTime by remember { mutableStateOf("Today, 05:00 PM") }
        var duration by remember { mutableStateOf("45") }
        var url by remember { mutableStateOf("https://zoom.us/j/1234567890?pwd=JamiaAlBayanGlobal") }
        var notes by remember { mutableStateOf("Review joint letters and harakat.") }

        AlertDialog(
            onDismissRequest = { showScheduleClassDialog = false },
            title = {
                Text(
                    strings.scheduleClass,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Class Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = teacherId,
                        onValueChange = { teacherId = it },
                        label = { Text("Teacher ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Student ID (or 'ALL')") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dateTime,
                        onValueChange = { dateTime = it },
                        label = { Text(strings.scheduledTime) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Zoom Meeting URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.classNotes) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.scheduleZoomClass(
                            title = title,
                            teacherId = teacherId,
                            studentId = studentId,
                            dateTime = dateTime,
                            duration = duration.toIntOrNull() ?: 45,
                            zoomUrl = url,
                            notes = notes
                        ) { success, _ ->
                            if (success) showScheduleClassDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleClassDialog = false }) {
                    Text(strings.cancel)
                }
            },
            containerColor = WarmIvorySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Modal: Announcement
    if (showAnnouncementDialog) {
        var titleEn by remember { mutableStateOf("") }
        var titleBn by remember { mutableStateOf("") }
        var contentEn by remember { mutableStateOf("") }
        var contentBn by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAnnouncementDialog = false },
            title = {
                Text(
                    "Publish Academy Announcement",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = titleEn,
                        onValueChange = { titleEn = it },
                        label = { Text("Title (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = titleBn,
                        onValueChange = { titleBn = it },
                        label = { Text("শিরোনাম (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contentEn,
                        onValueChange = { contentEn = it },
                        label = { Text("Content (English)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contentBn,
                        onValueChange = { contentBn = it },
                        label = { Text("বিবরণ (বাংলা)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (titleEn.isNotBlank()) {
                            viewModel.postAnnouncement(titleEn, titleBn, contentEn, contentBn)
                            showAnnouncementDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepTeal)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAnnouncementDialog = false }) {
                    Text(strings.cancel)
                }
            },
            containerColor = WarmIvorySurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
