package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Role
import com.example.data.model.UserEntity
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*

@Composable
fun AcademyBrandHeader(
    strings: AppStrings,
    currentLanguage: AppLanguage,
    currentUser: UserEntity?,
    onToggleLanguage: () -> Unit,
    onSwitchUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Official Academy Logo and Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_academy_logo),
                        contentDescription = "Jamia Al-Bayan Global Official Logo",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, AntiqueGold, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.appTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal,
                                letterSpacing = (-0.3).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = strings.tagline,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp,
                                color = TextDarkSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Header Control Actions: Language Toggle & Persona Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Bilingual Switch Button
                    OutlinedButton(
                        onClick = onToggleLanguage,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("btn_language_toggle"),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, AntiqueGold)
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ENGLISH) "বাং" else "EN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal
                            )
                        )
                    }

                    // Persona / Profile Switch Button
                    IconButton(
                        onClick = onSwitchUserClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_switch_persona")
                            .background(DeepTeal.copy(alpha = 0.08f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = strings.switchAccount,
                            tint = DeepTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Active Persona Pill Banner
            currentUser?.let { user ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (user.role) {
                                Role.PRINCIPAL -> AntiqueGold.copy(alpha = 0.12f)
                                Role.TEACHER -> DeepTeal.copy(alpha = 0.10f)
                                Role.STUDENT -> MutedBlueGrey.copy(alpha = 0.18f)
                            }
                        )
                        .clickable { onSwitchUserClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val roleIcon = when (user.role) {
                            Role.PRINCIPAL -> Icons.Default.AdminPanelSettings
                            Role.TEACHER -> Icons.Default.MenuBook
                            Role.STUDENT -> Icons.Default.School
                        }
                        val roleColor = when (user.role) {
                            Role.PRINCIPAL -> AntiqueGoldDark
                            Role.TEACHER -> DeepTeal
                            Role.STUDENT -> DeepTealLight
                        }
                        Icon(
                            imageVector = roleIcon,
                            contentDescription = null,
                            tint = roleColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${user.displayName} • ${user.role.name}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = roleColor
                            )
                        )
                    }

                    Text(
                        text = strings.switchAccount,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = TextDarkSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SurfaceWhite.copy(alpha = 0.94f),
    borderColor: Color = BorderSubtle,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    } else {
        modifier.clip(RoundedCornerShape(18.dp))
    }

    Surface(
        modifier = cardModifier.shadow(2.dp, RoundedCornerShape(18.dp)),
        color = backgroundColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    icon: ImageVector,
    iconColor: Color = DeepTeal,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        color = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextDarkSecondary,
                        fontSize = 12.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextDarkPrimary
                )
            )
            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "PRESENT", "ACHIEVED" -> Triple(StatusSuccessContainer, StatusSuccess, status)
        "IN_PROGRESS", "PRACTICING" -> Triple(StatusInfoContainer, StatusInfo, status.replace("_", " "))
        "NOT_STARTED", "PENDING" -> Triple(AntiqueGoldContainer, AntiqueGoldDark, status.replace("_", " "))
        "ABSENT", "CANCELLED" -> Triple(StatusErrorContainer, StatusError, status)
        "LATE" -> Triple(StatusWarningContainer, StatusWarning, status)
        "LIVE" -> Triple(StatusErrorContainer, StatusError, "LIVE NOW")
        else -> Triple(WarmIvoryContainer, TextDarkSecondary, status)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = textColor
            )
        )
    }
}

@Composable
fun CourseTrackBadge(
    track: String,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeText) = when (track.lowercase()) {
        "qaida" -> AntiqueGoldContainer to AntiqueGoldDark
        "ampara" -> DeepTealContainer to DeepTeal
        "quran" -> StatusSuccessContainer to StatusSuccess
        else -> WarmIvoryContainer to TextDarkPrimary
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(0.5.dp, badgeText.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(badgeText, CircleShape)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = track,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = badgeText
                )
            )
        }
    }
}

@Composable
fun PersonaSwitcherDialog(
    strings: AppStrings,
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onUserSelected: (UserEntity) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = strings.selectRole,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal
                    )
                )
                Text(
                    text = strings.selectUserPrompt,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextDarkSecondary,
                        fontSize = 12.sp
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                users.forEach { user ->
                    val isSelected = user.id == currentUser?.id
                    val (roleColor, roleIcon) = when (user.role) {
                        Role.PRINCIPAL -> AntiqueGoldDark to Icons.Default.AdminPanelSettings
                        Role.TEACHER -> DeepTeal to Icons.Default.MenuBook
                        Role.STUDENT -> DeepTealLight to Icons.Default.School
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onUserSelected(user)
                                onDismiss()
                            }
                            .testTag("persona_${user.username}"),
                        color = if (isSelected) roleColor.copy(alpha = 0.12f) else SurfaceWhite,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) roleColor else BorderSubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(roleColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = roleIcon,
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = TextDarkPrimary
                                    )
                                )
                                Text(
                                    text = "${user.role.name} • ${user.linkedId}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextDarkSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = roleColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.close, color = DeepTeal)
            }
        },
        containerColor = WarmIvorySurface,
        shape = RoundedCornerShape(20.dp)
    )
}
