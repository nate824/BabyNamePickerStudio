package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryRose
import com.example.ui.viewmodel.DeepDiveState

@Composable
fun DeepDiveDialog(
    state: DeepDiveState,
    onRetryWithLastName: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var lastName by remember(state.babyName.id) { mutableStateOf(state.lastName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryRose)
                Spacer(modifier = Modifier.width(8.dp))
                Text(state.babyName.name, fontFamily = FontFamily.Serif, fontWeight = FontWeight.ExtraBold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .testTag("deep_dive_dialog")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it.take(40) },
                        label = { Text("Your last name (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onRetryWithLastName(lastName) },
                        enabled = !state.loading && lastName.trim() != state.lastName
                    ) { Text("Update") }
                }
                Spacer(modifier = Modifier.height(12.dp))

                when {
                    state.loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = PrimaryRose)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Claude is researching ${state.babyName.name}…", fontSize = 13.sp, color = Color.Gray)
                    }
                    state.error != null -> Text(state.error, fontSize = 13.sp, color = PrimaryRose)
                    state.result != null -> {
                        val r = state.result
                        val fullName = if (state.lastName.isBlank()) "With a last name" else "${state.babyName.name} ${state.lastName}"
                        Section(fullName, r.lastNameFit)
                        Section("Nicknames", r.nicknames.joinToString(" · "))
                        Section("Middle name ideas", r.middleNameIdeas.joinToString(" · "))
                        Section("Famous namesakes", r.famousNamesakes.joinToString("\n") { "• $it" })
                        Section("History", r.history)
                        Section("Sibling style", r.sibling)
                    }
                }
            }
        },
        confirmButton = { OutlinedButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun Section(title: String, body: String) {
    if (body.isBlank()) return
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryRose)
    Text(
        body,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
