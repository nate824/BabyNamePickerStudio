package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Gender
import com.example.data.repository.MatchWithDetails
import com.example.ui.theme.BoyCardBg
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.GirlCardBg
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.GoldStar
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.UnisexCardBg
import com.example.ui.theme.UnisexPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SharedMatchesScreen(
    matches: List<MatchWithDetails>,
    partner1Name: String,
    partner2Name: String,
    onUpdateRating: (nameId: String, rating: Int) -> Unit,
    onUpdateNotes: (nameId: String, notes: String) -> Unit,
    onDeleteMatch: (nameId: String) -> Unit,
    onDeepDive: (MatchWithDetails) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var genderFilter by remember { mutableStateOf<Gender?>(null) }
    var editingNotesForMatch by remember { mutableStateOf<MatchWithDetails?>(null) }

    val filteredMatches = matches.filter { item ->
        val nameMatches = item.babyName.name.contains(searchQuery, ignoreCase = true) ||
                item.babyName.origin.contains(searchQuery, ignoreCase = true)
        val genderMatches = genderFilter == null || item.babyName.gender == genderFilter
        nameMatches && genderMatches
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("shared_matches_screen")
    ) {
        // Minimalist Dashboard Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Shared Favorites",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (matches.isEmpty()) "Start swiping to find mutual loves" else "${matches.size} mutual favorites for $partner1Name & $partner2Name 💕",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            if (matches.isNotEmpty()) {
                Surface(
                    shape = CircleShape,
                    color = PrimaryRose.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clickable {
                            val shareBody = buildString {
                                append("✨ Our Baby Name Matches ($partner1Name & $partner2Name):\n\n")
                                matches.forEachIndexed { i, m ->
                                    append("${i + 1}. ${m.babyName.name} (${m.babyName.gender.displayName()})\n")
                                    append("   Origin: ${m.babyName.origin}\n")
                                    append("   Meaning: \"${m.babyName.meaning}\"\n")
                                    if (m.match.notes.isNotBlank()) {
                                        append("   Notes: ${m.match.notes}\n")
                                    }
                                    append("\n")
                                }
                            }
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareBody)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Baby Name Favorites")
                            context.startActivity(shareIntent)
                        }
                        .testTag("share_matches_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = PrimaryRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Export",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRose
                        )
                    }
                }
            }
        }

        // Search & Filter
        if (matches.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search shared names or origins...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.Gray
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                } else null,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("matches_search_field"),
                shape = RoundedCornerShape(14.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = genderFilter == null,
                    onClick = { genderFilter = null },
                    label = { Text("All (${matches.size})") }
                )
                FilterChip(
                    selected = genderFilter == Gender.GIRL,
                    onClick = { genderFilter = Gender.GIRL },
                    label = { Text("Girls 🌸") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GirlPrimary, selectedLabelColor = Color.White)
                )
                FilterChip(
                    selected = genderFilter == Gender.BOY,
                    onClick = { genderFilter = Gender.BOY },
                    label = { Text("Boys ⚓") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BoyPrimary, selectedLabelColor = Color.White)
                )
            }
        }

        // Content
        if (matches.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryRose.copy(alpha = 0.1f),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = PrimaryRose,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Shared Loves Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When you and your partner both swipe right on the same name, it will automatically appear here on your shared dashboard.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredMatches, key = { it.babyName.id }) { item ->
                    MatchDashboardCard(
                        matchWithDetails = item,
                        onUpdateRating = { rating -> onUpdateRating(item.babyName.id, rating) },
                        onEditNotes = { editingNotesForMatch = item },
                        onDeleteMatch = { onDeleteMatch(item.babyName.id) },
                        onDeepDive = { onDeepDive(item) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    // Notes editing dialog
    editingNotesForMatch?.let { item ->
        var noteDraft by remember { mutableStateOf(item.match.notes) }
        AlertDialog(
            onDismissRequest = { editingNotesForMatch = null },
            title = { Text("Notes for ${item.babyName.name}") },
            text = {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    placeholder = { Text("Add nickname, middle name ideas, family reactions...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateNotes(item.babyName.id, noteDraft.trim())
                        editingNotesForMatch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose)
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingNotesForMatch = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MatchDashboardCard(
    matchWithDetails: MatchWithDetails,
    onUpdateRating: (Int) -> Unit,
    onEditNotes: () -> Unit,
    onDeleteMatch: () -> Unit,
    onDeepDive: () -> Unit
) {
    val babyName = matchWithDetails.babyName
    val match = matchWithDetails.match

    val cardBg = when (babyName.gender) {
        Gender.GIRL -> GirlCardBg
        Gender.BOY -> BoyCardBg
        Gender.UNISEX -> UnisexCardBg
    }

    val primaryColor = when (babyName.gender) {
        Gender.GIRL -> GirlPrimary
        Gender.BOY -> BoyPrimary
        Gender.UNISEX -> UnisexPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("match_card_${babyName.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Name, Gender & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = babyName.name,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = when (babyName.gender) {
                                Gender.GIRL -> "🌸 Girl"
                                Gender.BOY -> "⚓ Boy"
                                Gender.UNISEX -> "✨ Unisex"
                            },
                            color = primaryColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteMatch,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove match",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Origin & Pronunciation
            Text(
                text = "${babyName.origin} • ${babyName.pronunciation}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Meaning
            Text(
                text = "“${babyName.meaning}”",
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Star Rating (1 to 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    (1..5).forEach { star ->
                        val isFilled = star <= match.rating
                        Icon(
                            imageVector = if (isFilled) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Rate $star stars",
                            tint = if (isFilled) GoldStar else Color.Gray,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onUpdateRating(star) }
                        )
                    }
                }

                // Notes button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.clickable { onEditNotes() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit note",
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (match.notes.isBlank()) "+ Note" else "Edit Note",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryColor
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = primaryColor.copy(alpha = 0.12f),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onDeepDive() }
                    .testTag("deep_dive_button_${babyName.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Deep dive with Claude",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
            }

            // Display Note if present
            if (match.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "📝 ${match.notes}",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
