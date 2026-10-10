package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Role
import com.example.ui.components.AcademyBrandHeader
import com.example.ui.components.PersonaSwitcherDialog
import com.example.ui.screens.DailyDuasScreen
import com.example.ui.screens.PrincipalDashboardScreen
import com.example.ui.screens.QaidaScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.TeacherDashboardScreen
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceWhite
import com.example.viewmodel.AcademyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: AcademyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentLanguage by viewModel.currentLanguage.collectAsState()
                val strings by viewModel.strings.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val activeUsers by viewModel.allActiveUsers.collectAsState()
                val statusNotification by viewModel.statusNotification.collectAsState()

                var showPersonaDialog by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("MAIN") } // "MAIN", "QAIDA", "DUAS"

                BackHandler(enabled = currentScreen != "MAIN") {
                    currentScreen = "MAIN"
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        AcademyBrandHeader(
                            strings = strings,
                            currentLanguage = currentLanguage,
                            currentUser = currentUser,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onSwitchUserClick = { showPersonaDialog = true }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Main Navigation Switcher
                        when (currentScreen) {
                            "MAIN" -> {
                                when (currentUser?.role) {
                                    Role.PRINCIPAL -> {
                                        PrincipalDashboardScreen(
                                            viewModel = viewModel,
                                            strings = strings,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Role.TEACHER -> {
                                        TeacherDashboardScreen(
                                            viewModel = viewModel,
                                            strings = strings,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Role.STUDENT, null -> {
                                        StudentDashboardScreen(
                                            viewModel = viewModel,
                                            strings = strings,
                                            onNavigateToQaida = { currentScreen = "QAIDA" },
                                            onNavigateToDuas = { currentScreen = "DUAS" },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                            "QAIDA" -> {
                                QaidaScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    onBack = { currentScreen = "MAIN" },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            "DUAS" -> {
                                DailyDuasScreen(
                                    viewModel = viewModel,
                                    strings = strings,
                                    onBack = { currentScreen = "MAIN" },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Floating Notification Feedback Banner
                        AnimatedVisibility(
                            visible = statusNotification != null,
                            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(16.dp)
                        ) {
                            statusNotification?.let { msg ->
                                Surface(
                                    color = DeepTeal,
                                    shape = RoundedCornerShape(12.dp),
                                    shadowElevation = 6.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = SurfaceWhite,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = msg,
                                            color = SurfaceWhite,
                                            fontSize = 12.sp,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { viewModel.clearNotification() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Text("✕", color = SurfaceWhite, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        // Persona Switching Modal
                        if (showPersonaDialog) {
                            PersonaSwitcherDialog(
                                strings = strings,
                                users = activeUsers,
                                currentUser = currentUser,
                                onUserSelected = { selectedUser ->
                                    viewModel.switchUser(selectedUser)
                                    currentScreen = "MAIN"
                                },
                                onDismiss = { showPersonaDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
