package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QaidaLesson
import com.example.ui.components.GlassmorphicCard
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QaidaScreen(
    viewModel: AcademyViewModel,
    strings: AppStrings,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons by viewModel.qaidaLessons.collectAsState()
    var selectedLesson by remember { mutableStateOf<QaidaLesson?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        strings.qaidaLessons,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                    )
                    Text(
                        "Foundational Arabic Reading",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSecondary)
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DeepTeal)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            items(lessons) { lesson ->
                val isSelected = selectedLesson?.id == lesson.id

                GlassmorphicCard(
                    borderColor = if (lesson.isCompleted) AntiqueGold else BorderSubtle,
                    onClick = { selectedLesson = if (isSelected) null else lesson }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            if (lesson.isCompleted) AntiqueGold else DeepTeal.copy(alpha = 0.12f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${lesson.lessonNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (lesson.isCompleted) SurfaceWhite else DeepTeal
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = lesson.titleEn,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextDarkPrimary
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lesson.titleBn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextDarkSecondary
                                )
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleQaidaLesson(lesson) },
                            modifier = Modifier.testTag("toggle_qaida_${lesson.lessonNumber}")
                        ) {
                            Icon(
                                imageVector = if (lesson.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (lesson.isCompleted) "Completed" else "Mark Complete",
                                tint = if (lesson.isCompleted) AntiqueGoldDark else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arabic Content Display (Large Arabic Font, RTL formatting)
                    Surface(
                        color = WarmIvorySurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = lesson.arabicContent,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTeal,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 42.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = lesson.descriptionEn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, color = TextDarkSecondary)
                    )
                    Text(
                        text = lesson.descriptionBn,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, color = TextMuted)
                    )
                }
            }
        }
    }
}
