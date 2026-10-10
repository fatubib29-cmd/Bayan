package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyDua
import com.example.ui.components.GlassmorphicCard
import com.example.ui.localization.AppStrings
import com.example.ui.theme.*
import com.example.viewmodel.AcademyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyDuasScreen(
    viewModel: AcademyViewModel,
    strings: AppStrings,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val duas by viewModel.duasList.collectAsState()
    val searchQuery by viewModel.duaSearchQuery.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(duas) {
        listOf("All") + duas.map { it.category }.distinct()
    }

    val filteredDuas = remember(duas, selectedCategory) {
        if (selectedCategory == "All") duas
        else duas.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        strings.authenticDuasTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DeepTeal)
                    )
                    Text(
                        "Verified Sahih Supplications",
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

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setDuaSearchQuery(it) },
            placeholder = { Text(strings.searchDuas, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = DeepTeal) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setDuaSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_duas_input"),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceWhite,
                unfocusedContainerColor = SurfaceWhite,
                unfocusedBorderColor = BorderSubtle,
                focusedBorderColor = AntiqueGold
            ),
            singleLine = true
        )

        // Category Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeepTeal,
                        selectedLabelColor = SurfaceWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Duas List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            items(filteredDuas) { dua ->
                GlassmorphicCard(
                    borderColor = if (dua.isFavorite) AntiqueGold.copy(alpha = 0.6f) else BorderSubtle
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = DeepTealContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = dua.category,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = DeepTeal,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dua.titleEn,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextDarkPrimary
                                )
                            )
                            Text(
                                text = dua.titleBn,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    color = TextDarkSecondary
                                )
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleDuaFavorite(dua) },
                            modifier = Modifier.testTag("bookmark_dua_${dua.id}")
                        ) {
                            Icon(
                                imageVector = if (dua.isFavorite) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Favorite",
                                tint = if (dua.isFavorite) AntiqueGoldDark else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Arabic Text Box (RTL)
                    Surface(
                        color = WarmIvorySurface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dua.arabicText,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepTeal,
                                textAlign = TextAlign.Center,
                                lineHeight = 38.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Transliteration
                    Text(
                        text = dua.transliteration,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = TextDarkSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // English Translation
                    Text(
                        text = "EN: ${dua.meaningEn}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = TextDarkPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bangla Translation
                    Text(
                        text = "বাংলা: ${dua.meaningBn}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            color = DeepTeal
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Reference
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = dua.reference,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = AntiqueGoldDark,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}
