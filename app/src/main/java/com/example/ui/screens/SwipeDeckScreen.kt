package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BabyName
import com.example.data.model.Gender
import com.example.data.model.LengthPreference
import com.example.data.model.PopularityTier
import com.example.ui.components.SwipeCard
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.DislikeRed
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.LikeGreen
import com.example.ui.theme.PrimaryRose

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SwipeDeckScreen(
    currentQueue: List<BabyName>,
    partnerLikedNameIds: Set<String>,
    activeUserName: String,
    partnerName: String,
    genderFilter: Gender?,
    lengthFilter: LengthPreference,
    popularityFilter: PopularityTier?,
    isAlgoEnabled: Boolean,
    onSelectGenderFilter: (Gender?) -> Unit,
    onSelectLengthFilter: (LengthPreference) -> Unit,
    onSelectPopularityFilter: (PopularityTier?) -> Unit,
    onSwipeLeft: (BabyName) -> Unit,
    onSwipeRight: (BabyName) -> Unit,
    onUndo: () -> Unit,
    onOpenAddName: () -> Unit,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Filter & Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Gender Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = genderFilter == null,
                    onClick = { onSelectGenderFilter(null) },
                    label = { Text("Both", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRose,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_gender_both")
                )

                FilterChip(
                    selected = genderFilter == Gender.GIRL,
                    onClick = { onSelectGenderFilter(Gender.GIRL) },
                    label = { Text("Girls 🌸", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GirlPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_gender_girl")
                )

                FilterChip(
                    selected = genderFilter == Gender.BOY,
                    onClick = { onSelectGenderFilter(Gender.BOY) },
                    label = { Text("Boys ⚓", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BoyPrimary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("filter_gender_boy")
                )
            }

            // More Filters & Algo Badge Button
            Surface(
                shape = CircleShape,
                color = if (isAlgoEnabled) PrimaryRose.copy(alpha = 0.12f) else Color.LightGray.copy(alpha = 0.3f),
                onClick = { showFilterSheet = true },
                modifier = Modifier.testTag("filters_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAlgoEnabled) Icons.Default.AutoAwesome else Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (isAlgoEnabled) PrimaryRose else Color.DarkGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isAlgoEnabled) "AI Tuned" else "Random",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAlgoEnabled) PrimaryRose else Color.DarkGray
                    )
                }
            }
        }

        // Active Queue Count & Collaborative hint
        if (currentQueue.isNotEmpty()) {
            val partnerPickCount = currentQueue.count { partnerLikedNameIds.contains(it.id) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentQueue.size} names in deck",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (partnerPickCount > 0) {
                    Text(
                        text = "⭐ $partnerPickCount queued from $partnerName!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRose
                    )
                }
            }
        }

        // Main Deck Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (currentQueue.isEmpty()) {
                // Empty State
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .padding(24.dp)
                        .testTag("empty_deck_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You're All Caught Up!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You've reviewed every name for this filter. Ask Claude for fresh ideas, add one of your own, or wait for $partnerName's picks to come in.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onOpenAddName,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("empty_state_add_name_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Suggest a Name")
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onAskAi,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("empty_state_ask_ai_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask Claude for more names")
                        }
                    }
                }
            } else {
                // Stack of cards (Background card + Foreground interactive card)
                if (currentQueue.size > 1) {
                    val nextName = currentQueue[1]
                    Card(
                        modifier = Modifier
                            .fillMaxSize(0.92f)
                            .scale(0.94f),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (nextName.gender) {
                                Gender.GIRL -> Color(0xFFFFF1F2)
                                Gender.BOY -> Color(0xFFEFF6FF)
                                Gender.UNISEX -> Color(0xFFF5F3FF)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {}
                }

                // Foreground active swipe card
                val topName = currentQueue[0]
                val isPartnerPick = partnerLikedNameIds.contains(topName.id)

                // Keyed by name so each new card starts centered instead of inheriting
                // the previous card's off-screen drag offset.
                key(topName.id) {
                    SwipeCard(
                        babyName = topName,
                        isPartnerPick = isPartnerPick,
                        partnerName = partnerName,
                        modifier = Modifier.fillMaxSize(0.96f),
                        onSwipeLeft = { onSwipeLeft(topName) },
                        onSwipeRight = { onSwipeRight(topName) }
                    )
                }
            }
        }

        // Action Buttons Row (Touch targets >= 48dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Undo button
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .testTag("undo_button"),
                shape = CircleShape,
                color = Color.LightGray.copy(alpha = 0.25f),
                onClick = onUndo
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Undo swipe",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Pass / Dislike button (✕)
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .testTag("pass_button"),
                shape = CircleShape,
                color = DislikeRed.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(2.dp, DislikeRed.copy(alpha = 0.3f)),
                onClick = {
                    if (currentQueue.isNotEmpty()) onSwipeLeft(currentQueue[0])
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Pass",
                        tint = DislikeRed,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Add Custom Name quick action
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .testTag("add_name_quick_button"),
                shape = CircleShape,
                color = PrimaryRose.copy(alpha = 0.12f),
                onClick = onOpenAddName
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom name",
                        tint = PrimaryRose,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Like / Heart button (💖)
            Surface(
                modifier = Modifier
                    .size(68.dp)
                    .testTag("like_button"),
                shape = CircleShape,
                color = LikeGreen.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(2.dp, LikeGreen.copy(alpha = 0.4f)),
                onClick = {
                    if (currentQueue.isNotEmpty()) onSwipeRight(currentQueue[0])
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like name",
                        tint = LikeGreen,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }

    // Filter Dialog for Length & Popularity
    if (showFilterSheet) {
        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = { Text("Filter Deck & Style", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Name Length",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LengthPreference.values().forEach { pref ->
                            FilterChip(
                                selected = lengthFilter == pref,
                                onClick = { onSelectLengthFilter(pref) },
                                label = { Text(pref.label) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Popularity Trend",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = popularityFilter == null,
                            onClick = { onSelectPopularityFilter(null) },
                            label = { Text("All Tiers") }
                        )
                        PopularityTier.values().forEach { tier ->
                            FilterChip(
                                selected = popularityFilter == tier,
                                onClick = { onSelectPopularityFilter(tier) },
                                label = { Text(tier.label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFilterSheet = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose)
                ) {
                    Text("Apply")
                }
            }
        )
    }
}
