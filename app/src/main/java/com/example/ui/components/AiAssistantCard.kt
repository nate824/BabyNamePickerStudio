package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryRose
import com.example.ui.viewmodel.AiTask

/** Claude-powered actions: describe-to-generate, suggest-from-likes, and taste summary. */
@Composable
fun AiAssistantCard(
    aiTask: AiTask?,
    tasteSummary: String?,
    onDescribe: (String) -> Unit,
    onSuggest: () -> Unit,
    onTaste: () -> Unit,
    modifier: Modifier = Modifier
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    val idle = aiTask == null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_assistant_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryRose.copy(alpha = 0.06f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryRose, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ask Claude", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                "New names go straight into both of your decks.",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = prompt,
                onValueChange = { prompt = it.take(300) },
                placeholder = { Text("e.g. short Irish girl names with a nature feel") },
                shape = RoundedCornerShape(14.dp),
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_describe_input")
            )
            Spacer(modifier = Modifier.height(8.dp))
            AiButton(
                label = "Find names like this",
                icon = Icons.Default.Search,
                loading = aiTask == AiTask.DESCRIBE,
                enabled = idle && prompt.trim().length >= 2,
                filled = true,
                onClick = { onDescribe(prompt) },
                tag = "ai_describe_button"
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AiButton(
                    label = "More like ours",
                    icon = Icons.Default.AutoAwesome,
                    loading = aiTask == AiTask.SUGGEST,
                    enabled = idle,
                    filled = false,
                    onClick = onSuggest,
                    tag = "ai_suggest_button",
                    modifier = Modifier.weight(1f)
                )
                AiButton(
                    label = "Our taste",
                    icon = Icons.Default.Insights,
                    loading = aiTask == AiTask.TASTE,
                    enabled = idle,
                    filled = false,
                    onClick = onTaste,
                    tag = "ai_taste_button",
                    modifier = Modifier.weight(1f)
                )
            }

            if (tasteSummary != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tasteSummary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(12.dp)
                            .testTag("ai_taste_summary")
                    )
                }
            }
        }
    }
}

@Composable
private fun AiButton(
    label: String,
    icon: ImageVector,
    loading: Boolean,
    enabled: Boolean,
    filled: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = if (filled) Color.White else PrimaryRose
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thinking…", fontSize = 13.sp)
        } else {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, fontSize = 13.sp, maxLines = 1)
        }
    }
    if (filled) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryRose),
            shape = RoundedCornerShape(14.dp),
            modifier = modifier.fillMaxWidth().testTag(tag)
        ) { content() }
    } else {
        OutlinedButton(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            modifier = modifier.testTag(tag)
        ) { content() }
    }
}
