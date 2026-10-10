package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun StudentDashboardScreen(
    viewModel: AcademyViewModel,
    strings: AppStrings,
    onNavigateToQaida: () -> Unit,
    onNavigateToDuas: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudents by viewModel.allStudentsEducational.collectAsState()
    val studentProfile = remember(allStudents, currentUser) {
        allStudents.firstOrNull { it.studentId == currentUser?.linkedId }
    }
    val myClasses by viewModel.getUpcomingClassesForCurrentUser().collectAsState(initial = emptyList())
    val myProgress by viewModel.getProgressForCurrentStudent().collectAsState(initial = emptyList())
    val announcements by viewModel.announcements.collectAsState()

    val nextLiveClass = remember(myClasses) {
        myClasses.firstOrNull { it.status == "LIVE" } ?: myClasses.firstOrNull { it.status == "UPCOMING" }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            // Student Welcome & Enrolled Track Card
            item {
                GlassmorphicCard(
                    backgroundColor = WarmIvoryContainer.copy(alpha = 0.85f),
                    borderColor = AntiqueGold.copy(alpha = 0.4f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "As-salamu alaykum,",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    color = TextDarkSecondary
                                )
                            )
                            Text(
                                text = currentUser?.displayName ?: "Student",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTeal
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CourseTrackBadge(track = studentProfile?.courseTrack ?: "Qaida")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${strings.learnerId}: ${studentProfile?.studentId ?: "STD"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextDarkSecondary
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(AntiqueGold.copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, AntiqueGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = AntiqueGoldDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    studentProfile?.let { prof ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderSubtle.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${strings.trackLevel}: ${prof.currentLevel}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = DeepTeal
                            )
                        )
                        if (prof.learningGoals.isNotBlank()) {
                            Text(
                                text = "Current Goal: ${prof.learningGoals}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextDarkSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Prominent Zoom Live Class Section
            item {
                Text(
                    text = strings.myNextClass,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                )
            }

            item {
                if (nextLiveClass != null) {
                    GlassmorphicCard(
                        backgroundColor = SurfaceWhite,
                        borderColor = if (nextLiveClass.status == "LIVE") StatusError else AntiqueGold
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nextLiveClass.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextDarkPrimary
                                )
                            )
                            StatusBadge(status = nextLiveClass.status)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = DeepTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${nextLiveClass.scheduledDateTime} • ${nextLiveClass.durationMinutes} mins",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
                            )
                        }

                        if (nextLiveClass.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lesson focus: ${nextLiveClass.notes}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextDarkSecondary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(nextLiveClass.zoomUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (nextLiveClass.status == "LIVE") StatusError else DeepTeal
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_join_zoom_class")
                        ) {
                            Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (nextLiveClass.status == "LIVE") "Join Live Class Now" else strings.joinClass,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                } else {
                    GlassmorphicCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.noUpcomingClass,
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextDarkSecondary)
                            )
                        }
                    }
                }
            }

            // Quick Access to Approved Educational Resources
            item {
                Text(
                    text = strings.educationalResources,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Qaida Tile
                    GlassmorphicCard(
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToQaida
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AntiqueGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = AntiqueGoldDark, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.qaidaLessons,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                        Text(
                            text = "Alphabet & Harakat",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    // Duas Tile
                    GlassmorphicCard(
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDuas
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(DeepTeal.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = DeepTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.dailyDuas,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                        Text(
                            text = "Authentic Supplications",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                }
            }

            // Latest Progress & Teacher Feedback
            item {
                Text(
                    text = strings.latestFeedback,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                )
            }

            if (myProgress.isNotEmpty()) {
                items(myProgress.take(3)) { progress ->
                    GlassmorphicCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = progress.chapterLesson,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            StatusBadge(status = progress.status)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Date: ${progress.date} • Instructor: ${progress.submittedBy}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = WarmIvoryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = "\"${progress.teacherFeedback}\"",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = DeepTeal
                                    )
                                )
                                if (progress.homework.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Homework: ${progress.homework}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSecondary)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    GlassmorphicCard {
                        Text(
                            text = "No evaluations recorded yet. Complete your first session with your teacher!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextDarkSecondary)
                        )
                    }
                }
            }

            // Academy Announcements
            item {
                Text(
                    text = strings.announcementsTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDarkPrimary
                    )
                )
            }

            items(announcements.take(2)) { notice ->
                GlassmorphicCard {
                    Text(
                        text = notice.titleEn,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = notice.contentEn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextDarkSecondary)
                    )
                }
            }
        }
    }
}
