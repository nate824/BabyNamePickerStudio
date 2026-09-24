package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlgorithmConfig
import com.example.data.model.AlgorithmExplanation
import com.example.data.model.LengthPreference
import com.example.data.model.PopularityFocus
import com.example.ui.theme.PrimaryRose

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlgorithmTuneScreen(
    currentConfig: AlgorithmConfig,
    explanation: AlgorithmExplanation,
    onSaveConfig: (AlgorithmConfig) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEnabled by remember(currentConfig) { mutableStateOf(currentConfig.isEnabled) }
    var selectedOrigins by remember(currentConfig) { mutableStateOf(currentConfig.targetOrigins) }
    var selectedStyles by remember(currentConfig) { mutableStateOf(currentConfig.targetStyles) }
    var preferredLength by remember(currentConfig) { mutableStateOf(currentConfig.preferredLength) }
    var popularityFocus by remember(currentConfig) { mutableStateOf(currentConfig.popularityFocus) }
    var customKeywords by remember(currentConfig) { mutableStateOf(currentConfig.userCustomKeywords) }

    val availableOrigins = listOf(
        "Celtic", "Latin", "Hebrew", "Greek", "Scandinavian", "Japanese", "Arabic", "Sanskrit", "French", "African"
    )

    val availableStyles = listOf(
        "Classic", "Modern", "Vintage", "Nature", "Celestial", "Royal", "Short & Sweet", "Spiritual", "Melodic", "Unique"
    )

    val quickPresetSuggestions = listOf(
        "Nature & Flora", "Short & punchy", "Ancient Greek", "Soft vowel endings", "Royal vintage", "Cosmic stars"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("algorithm_tune_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = PrimaryRose,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "AI Name Matchmaker",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Smart preference learning & custom tuning",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Main On/Off Toggle Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isEnabled) PrimaryRose.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEnabled) "Smart Algorithm: ACTIVE" else "Random Mode: ACTIVE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) PrimaryRose else Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isEnabled)
                            "Refining upcoming suggestions based on shared swipe history and your tuned criteria."
                        else
                            "Algorithm is turned off. Names appear in completely random, natural order.",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        lineHeight = 16.sp
                    )
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = { isEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryRose
                    ),
                    modifier = Modifier.testTag("algo_toggle_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Criteria Explainer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryRose,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "How the Algorithm is Prioritizing",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                explanation.primaryFactors.forEach { factor ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "• ", fontWeight = FontWeight.Bold, color = PrimaryRose)
                        Text(
                            text = factor,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryRose.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💡 ${explanation.matchRecommendationNote}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryRose,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Tune with Own Suggestions / Custom Prompt
        Text(
            text = "Tune with Your Own Suggestions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tell the algorithm specific traits, keywords, or feelings to look for:",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = customKeywords,
            onValueChange = { customKeywords = it },
            placeholder = { Text("e.g. botanical, poetic, starts with L, ancient...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("algo_custom_keywords_input"),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // Quick Preset Suggestions
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            quickPresetSuggestions.forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.LightGray.copy(alpha = 0.25f),
                    onClick = {
                        customKeywords = if (customKeywords.isBlank()) preset else "$customKeywords, $preset"
                    }
                ) {
                    Text(
                        text = "+ $preset",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Cultural Origin Weighting
        Text(
            text = "Cultural Heritage & Origin Focus",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Boost probability for specific origins:",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            availableOrigins.forEach { origin ->
                val isSelected = selectedOrigins.contains(origin)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedOrigins = if (isSelected) {
                            selectedOrigins - origin
                        } else {
                            selectedOrigins + origin
                        }
                    },
                    label = { Text(origin) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRose,
                        selectedLabelColor = Color.White
                    ),
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Aesthetic / Style Tag Preferences
        Text(
            text = "Aesthetic & Style Preferences",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Select vibes you gravitate toward:",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            availableStyles.forEach { style ->
                val isSelected = selectedStyles.contains(style)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedStyles = if (isSelected) {
                            selectedStyles - style
                        } else {
                            selectedStyles + style
                        }
                    },
                    label = { Text(style) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRose,
                        selectedLabelColor = Color.White
                    ),
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
                    } else null
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Popularity Focus
        Text(
            text = "Popularity Strategy",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PopularityFocus.values().forEach { focus ->
                FilterChip(
                    selected = popularityFocus == focus,
                    onClick = { popularityFocus = focus },
                    label = { Text(focus.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = {
                onSaveConfig(
                    AlgorithmConfig(
                        isEnabled = isEnabled,
                        targetOrigins = selectedOrigins,
                        targetStyles = selectedStyles,
                        preferredLength = preferredLength,
                        popularityFocus = popularityFocus,
                        userCustomKeywords = customKeywords.trim()
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_algo_config_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Apply Tuning & Recalibrate Deck ✨",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onResetDefaults,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reset_algo_config_button"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Reset to Natural Defaults")
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
