package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Gender
import com.example.ui.theme.BoyPrimary
import com.example.ui.theme.GirlPrimary
import com.example.ui.theme.PrimaryRose
import com.example.ui.theme.UnisexPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddNameDialog(
    partnerName: String,
    onDismiss: () -> Unit,
    onAddName: (name: String, gender: Gender, origin: String, meaning: String, pronunciation: String, tags: List<String>) -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.GIRL) }
    var originText by remember { mutableStateOf("") }
    var meaningText by remember { mutableStateOf("") }
    var pronunciationText by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf(setOf("Family Favorite")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val commonOrigins = listOf("Celtic / Irish", "Latin", "Hebrew", "Greek", "Scandinavian", "Japanese", "Arabic", "French")
    val commonTags = listOf("Classic", "Modern", "Nature", "Vintage", "Short & Sweet", "Royal", "Family Favorite", "Spiritual")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .testTag("add_name_dialog"),
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = PrimaryRose
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Suggest a Baby Name",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // Info banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryRose.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = PrimaryRose,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "This will automatically go to your Liked list and appear #1 for $partnerName to swipe!",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name Input
                OutlinedTextField(
                    value = nameText,
                    onValueChange = {
                        nameText = it
                        errorMessage = null
                    },
                    label = { Text("Baby Name *") },
                    placeholder = { Text("e.g. Maeve, Leo, Soren") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gender Selection
                Text(
                    text = "Gender Category",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(Gender.GIRL, Gender.BOY, Gender.UNISEX).forEach { gender ->
                        val isSelected = selectedGender == gender
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedGender = gender },
                            label = { Text(gender.displayName()) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (gender) {
                                    Gender.GIRL -> GirlPrimary
                                    Gender.BOY -> BoyPrimary
                                    Gender.UNISEX -> UnisexPrimary
                                },
                                selectedLabelColor = Color.White
                            ),
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, tint = Color.White) }
                            } else null,
                            modifier = Modifier.testTag("gender_chip_${gender.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Origin
                OutlinedTextField(
                    value = originText,
                    onValueChange = { originText = it },
                    label = { Text("Origin / Cultural Heritage") },
                    placeholder = { Text("e.g. Irish, Latin, Japanese") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("origin_input_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                // Quick Origin chips
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonOrigins.forEach { origin ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.LightGray.copy(alpha = 0.25f),
                            onClick = { originText = origin },
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = origin,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Meaning
                OutlinedTextField(
                    value = meaningText,
                    onValueChange = { meaningText = it },
                    label = { Text("Meaning or Special Story") },
                    placeholder = { Text("e.g. Brave protector, light of dawn") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meaning_input_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Pronunciation
                OutlinedTextField(
                    value = pronunciationText,
                    onValueChange = { pronunciationText = it },
                    label = { Text("Pronunciation (Optional)") },
                    placeholder = { Text("e.g. MAYV, LAY-luh") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pronunciation_input_field"),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Style Tags
                Text(
                    text = "Style Tags",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonTags.forEach { tag ->
                        val isChecked = selectedTags.contains(tag)
                        FilterChip(
                            selected = isChecked,
                            onClick = {
                                selectedTags = if (isChecked) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            },
                            label = { Text(tag, fontSize = 12.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.isBlank()) {
                        errorMessage = "Please enter a name"
                        return@Button
                    }
                    onAddName(
                        nameText.trim(),
                        selectedGender,
                        originText.trim().ifBlank { "Special Origin" },
                        meaningText.trim().ifBlank { "Beloved suggestion by partner" },
                        pronunciationText.trim(),
                        selectedTags.toList()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
                modifier = Modifier.testTag("submit_add_name_button")
            ) {
                Text("Add & Like ✨", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_add_name_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
